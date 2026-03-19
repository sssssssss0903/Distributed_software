# Docker 容器化部署指南

## 架构说明

- **MySQL**: 数据库，端口 3306
- **Redis**: 缓存，端口 6379
- **Backend-1/2**: 用户服务双实例，端口 8081/8082（负载均衡）
- **Product-Service**: 商品服务，端口 8083（Redis 缓存）
- **Nginx**: 反向代理与负载均衡，端口 80

## 启动

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f
```

## 负载均衡算法切换

编辑 `nginx/conf.d/default.conf`，在 `upstream backend_user` 中：

- **轮询**（默认）：不添加额外指令
- **最少连接**：添加 `least_conn;`
- **IP 哈希**：添加 `ip_hash;`

## JMeter 压测

### 1. 负载均衡验证

```bash
jmeter -n -t jmeter/load-balance-test.jmx -l result-loadbalance.jtl
```

观察 `result-loadbalance.jtl` 的响应时间；查看 backend-1、backend-2 日志，确认请求数大致相等。

### 2. 静态文件压测

```bash
jmeter -n -t jmeter/static-file-test.jmx -l result-static.jtl
```

### 3. 动态 API 压测

```bash
jmeter -n -t jmeter/dynamic-api-test.jmx -l result-dynamic.jtl
```

### 查看结果

```bash
jmeter -g result-loadbalance.jtl -o report/
```

## 动静分离

- 静态：`/static/*` → Nginx 直接返回
- 动态：`/api/*` → 转发到后端

## Redis 缓存防护

商品详情接口实现了：

- **缓存穿透**：空值缓存（1 分钟 TTL）
- **缓存击穿**：互斥锁
- **缓存雪崩**：随机 TTL（240–360 秒）

---

## MySQL 读写分离

### 启动读写分离环境

```bash
docker-compose -f docker-compose-rw.yml up -d --build
```

包含：mysql-master(3306)、mysql-slave(3307)、主从复制、Redis、后端服务、Nginx。

### 验证读写分离

- **读操作**（走从库）：`GET /api/product/list`、`GET /api/product/{id}`
- **写操作**（走主库）：`POST /api/product` 新增商品

商品服务使用 `@DS("slave")` 和 `@DS("master")` 注解实现读写分离。

---

## Elasticsearch 商品搜索（可选）

### 启动含 ES 的环境

```bash
docker-compose -f docker-compose.yml -f docker-compose-es.yml up -d
```

### 使用方式

1. 首次同步数据：`POST /api/product/sync-es`
2. 搜索商品：`GET /api/product/search?keyword=手机`
