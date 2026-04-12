# Homework 5 - 事务与一致性

## 场景描述

在商品秒杀系统中，订单服务（seckill-order-service，端口8085）和库存服务（seckill-inventory-service，端口8084）是两个独立的微服务，分别拥有各自的数据库（seckill_order 和 seckill_inventory）。

---

## 一、基于Redis实现库存预扣减（防超卖、限购）

### 1.1 Redis Lua脚本原子扣减

在 `OrderServiceImpl.deductStockFromRedis()` 中，使用 Lua 脚本保证库存检查和扣减的原子性：

```lua
local stock = redis.call('GET', KEYS[1])
if (not stock) then return -1 end
stock = tonumber(stock)
local q = tonumber(ARGV[1])
local limit = tonumber(ARGV[2])
-- 限购检查：单次购买数量不能超过限购上限
if (q > limit) then return -2 end
-- 库存检查：库存不足则拒绝
if (stock < q) then return -1 end
-- 原子扣减
return redis.call('DECRBY', KEYS[1], q)
```

**防超卖机制**：
- Redis `DECRBY` 是原子操作，配合 Lua 脚本中的库存检查，确保并发场景下不会出现超卖
- 库存 key 格式：`seckill:stock:{productId}`
- 返回值 `-1` 表示库存不足，`-2` 表示超过限购，`>=0` 表示扣减后的剩余库存

### 1.2 限购控制

**单用户限购**：通过 Redis `SETNX` 实现，key 格式为 `seckill:order:user:{userId}:{productId}`，有效期24小时，确保同一用户对同一商品只能秒杀一次。

**单次限购**：Lua 脚本中检查购买数量 `q > limit`（默认 `MAX_PURCHASE_LIMIT = 1`），超过限购数量直接拒绝。

### 1.3 补偿回滚

当消息发送失败或下游处理异常时，通过 `rollbackRedis()` 方法恢复 Redis 库存并删除用户限购标记。

---

## 二、基于消息的一致性保障

采用 **Kafka 消息队列** 实现最终一致性（Saga 模式）。

### 2.1 下单 + 库存扣减一致性

#### 流程

```
用户请求 → [Redis预扣减] → [发送Kafka消息] → [异步创建订单] → [异步扣减DB库存]
                                                    ↓ 失败
                                              [Kafka回滚消息] → [恢复Redis库存] + [恢复DB库存] + [取消订单]
```

#### 消息Topic

| Topic | 说明 | 生产者 | 消费者 |
|-------|------|--------|--------|
| `seckill.order.create` | 秒杀下单请求 | OrderServiceImpl | OrderCreateConsumer |
| `seckill.order.created` | 订单创建成功 | OrderCreateConsumer | InventoryDeductConsumer |
| `seckill.order.rollback` | 回滚消息 | 任意失败环节 | OrderRollbackRedisConsumer + OrderCreateConsumer(回滚) + InventoryDeductConsumer(回滚) |

#### 详细步骤

1. **Redis 预扣减**：OrderServiceImpl 通过 Lua 脚本原子扣减 Redis 库存
2. **发送下单消息**：成功后发送 `seckill.order.create` 消息到 Kafka
3. **异步创建订单**：OrderCreateConsumer 消费消息，在订单数据库中插入订单（状态=0待支付）
4. **异步扣减库存**：订单创建成功后发送 `seckill.order.created`，InventoryDeductConsumer 扣减数据库库存（available_stock → locked_stock）
5. **失败回滚**：任何环节失败，发送 `seckill.order.rollback` 消息，触发 Redis 库存恢复 + DB库存回滚 + 订单状态置为已取消

#### 幂等性保障

- OrderCreateConsumer：先查询订单是否存在，防止重复创建
- 数据库唯一键约束 `uk_user_product_seckill` 兜底防重

### 2.2 订单支付 + 订单状态更新一致性

#### 流程

```
支付请求 → [发送Kafka支付消息] → [异步处理支付] → [更新订单状态为已支付] → [确认库存扣减]
                                       ↓ 失败
                                 [订单状态→支付失败] → [Kafka回滚消息] → [恢复DB锁定库存] + [恢复Redis库存]
```

#### 消息Topic

| Topic | 说明 | 生产者 | 消费者 |
|-------|------|--------|--------|
| `seckill.order.pay` | 支付请求 | OrderServiceImpl | OrderPayConsumer |
| `seckill.order.paid` | 支付成功确认 | OrderPayConsumer | InventoryConfirmConsumer |
| `seckill.order.rollback` | 支付失败回滚 | OrderPayConsumer | InventoryDeductConsumer(回滚) + OrderRollbackRedisConsumer |

#### 详细步骤

1. **发起支付**：用户调用 `POST /api/order/pay/{orderId}`，校验订单状态后发送 `seckill.order.pay` 消息
2. **异步处理支付**：OrderPayConsumer 消费消息，调用支付网关处理支付
3. **支付成功**：使用乐观锁（`WHERE status = 0`）更新订单状态为已支付（status=1），设置支付时间，发送 `seckill.order.paid` 消息
4. **确认库存**：InventoryConfirmConsumer 消费 `seckill.order.paid` 消息，将 locked_stock 转为已售出（locked_stock 减少）
5. **支付失败**：订单状态设为支付失败（status=3），发送 `seckill.order.rollback` 回滚库存

#### 超时自动取消

OrderTimeoutScheduler 定时任务每60秒扫描一次，将超过30分钟未支付的订单自动取消（status=4），并发送回滚消息恢复库存。

---

## 三、库存状态流转

```
                    下单成功                    支付成功
  available_stock ──────────→ locked_stock ──────────→ 已售出(减少locked_stock)
        ↑                         |
        |     支付失败/超时取消     |
        └─────────────────────────┘
              rollbackDeduct
```

### 数据库库存操作

| 操作 | SQL | 场景 |
|------|-----|------|
| `deduct` | available - qty, locked + qty | 下单成功，锁定库存 |
| `confirmDeduct` | locked - qty | 支付成功，确认售出 |
| `rollbackDeduct` | available + qty, locked - qty | 支付失败/超时，释放库存 |

---

## 四、订单状态流转

```
  0(待支付) ──支付成功──→ 1(已支付)
      |
      ├──创建失败──→ 2(已取消/回滚)
      ├──支付失败──→ 3(支付失败)
      └──超时取消──→ 4(超时取消)
```

---

## 五、关键代码文件

| 文件 | 说明 |
|------|------|
| `OrderServiceImpl.java` | Redis预扣减(Lua脚本)、限购检查、支付发起 |
| `OrderCreateConsumer.java` | 异步创建订单、创建失败回滚 |
| `OrderPayConsumer.java` | 异步支付处理、支付状态更新 |
| `OrderRollbackRedisConsumer.java` | Redis库存补偿 |
| `OrderTimeoutScheduler.java` | 超时未支付订单自动取消 |
| `InventoryDeductConsumer.java` | DB库存扣减(锁定)、库存回滚 |
| `InventoryConfirmConsumer.java` | 支付成功后确认库存扣减 |
| `InventoryMapper.java` | deduct/confirmDeduct/rollbackDeduct SQL |

---

## 六、API接口

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `/api/order/seckill` | 秒杀下单（Redis预扣减 + Kafka异步） |
| POST | `/api/order/pay/{orderId}?userId=xx` | 订单支付（Kafka消息一致性） |
| GET | `/api/order/{orderId}` | 查询订单详情 |
| GET | `/api/order/user/{userId}` | 查询用户订单列表 |
