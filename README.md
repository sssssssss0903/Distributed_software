# 商品库存与秒杀系统

## 项目简介

这是一个基于 Spring Boot + Spring Cloud Alibaba 的分布式商品库存与秒杀系统。系统采用微服务架构，将功能拆分为用户服务、商品服务、库存服务和订单服务四个核心服务。

## 技术栈

- **开发语言**: Java 11
- **核心框架**: Spring Boot 2.7.14
- **微服务框架**: Spring Cloud Alibaba 2021.0.5.0
- **ORM 框架**: MyBatis-Plus 3.5.3
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.0
- **消息队列**: RabbitMQ 3.11

- **API 文档**: Knife4j 3.0

## 项目结构

```
seckill-system/
├── docs/                           # 项目文档
│   └── 系统设计文档.md             # 系统设计文档
├── scripts/                        # 脚本文件
│   └── sql/                        # SQL脚本
│       ├── 01_init_user_db.sql     # 用户数据库初始化
│       ├── 02_init_product_db.sql  # 商品数据库初始化
│       ├── 03_init_inventory_db.sql # 库存数据库初始化
│       └── 04_init_order_db.sql    # 订单数据库初始化
├── seckill-common/                 # 公共模块
│   └── src/main/java/com/seckill/common/
│       ├── result/                 # 统一响应结果
│       ├── exception/              # 异常处理
│       └── util/                   # 工具类
├── seckill-user-service/           # 用户服务
│   └── src/main/java/com/seckill/user/
│       ├── controller/             # 控制器层
│       ├── service/                # 业务逻辑层
│       ├── mapper/                 # 数据访问层
│       ├── entity/                 # 实体类
│       ├── dto/                    # 数据传输对象
│       ├── vo/                     # 视图对象
│       └── config/                 # 配置类
├── seckill-product-service/        # 商品服务(待实现)
├── seckill-inventory-service/      # 库存服务(待实现)
├── seckill-order-service/          # 订单服务(待实现)
├── seckill-gateway/                # API网关(待实现)
└── pom.xml                         # 父POM文件
```

## 模块说明

### 1. seckill-common (公共模块)

提供统一的响应结果封装、异常处理、工具类等公共功能。

**核心类**:

- `Result`: 统一响应结果类
- `ResultCode`: 响应状态码枚举
- `BusinessException`: 业务异常类
- `GlobalExceptionHandler`: 全局异常处理器
- `JwtUtil`: JWT 工具类

### 2. seckill-user-service (用户服务)

负责用户的注册、登录、身份验证等功能。

**端口**: 8081

**API 接口**:

- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/info` - 获取用户信息
- `POST /api/user/logout` - 用户登出

**数据库**: seckill_user

### 3. seckill-product-service (商品服务)

负责商品管理、商品查询、秒杀商品管理等功能。

**状态**: 待实现

### 4. seckill-inventory-service (库存服务)

负责库存管理、库存扣减、库存回滚等功能。

**状态**: 待实现

### 5. seckill-order-service (订单服务)

负责订单创建、订单查询、订单状态管理等功能。

**状态**: 待实现

### 6. seckill-gateway (API 网关)

提供统一的 API 网关，负责路由转发、鉴权、限流等功能。

**状态**: 待实现

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+

### 数据库初始化

执行 SQL 脚本初始化数据库:

```bash
# 进入SQL脚本目录
cd scripts/sql

# 执行初始化脚本
mysql -u root -p < 01_init_user_db.sql
mysql -u root -p < 02_init_product_db.sql
mysql -u root -p < 03_init_inventory_db.sql
mysql -u root -p < 04_init_order_db.sql
```

**注意**: 请根据实际情况修改数据库连接信息。

### 启动 Redis

```bash
redis-server
```

### 启动服务

#### 方式一: 使用 Maven 命令

```bash
# 编译整个项目
mvn clean install

# 启动用户服务
cd seckill-user-service
mvn spring-boot:run
```

#### 方式二: 使用 IDE

在 IDE 中直接运行 `UserServiceApplication.java` 的 main 方法。

### 访问服务

**用户服务**:

- 服务地址: http://localhost:8081
- API 文档: http://localhost:8081/doc.html

## API 测试示例

### 1. 用户注册

```bash
curl -X POST http://localhost:8081/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138888"
  }'
```

### 2. 用户登录

```bash
curl -X POST http://localhost:8081/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

### 3. 获取用户信息

```bash
curl -X GET http://localhost:8081/api/user/info \
  -H "Authorization: Bearer {token}"
```

## 配置说明

### 用户服务配置

配置文件: `seckill-user-service/src/main/resources/application.yml`

**主要配置项**:

- 服务端口: 8081
- 数据库连接: 需要修改为实际的数据库地址和密码
- Redis 连接: 需要修改为实际的 Redis 地址

**数据库配置**:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/seckill_user?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root # 请修改为实际密码
```

**Redis 配置**:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

## 开发进度

- [x] 系统设计文档编写
- [x] 项目结构搭建
- [x] 公共模块开发
- [x] 用户服务开发
  - [x] 用户注册功能
  - [x] 用户登录功能
  - [x] JWT 认证
  - [x] 用户信息查询
- [ ] 商品服务开发
- [ ] 库存服务开发
- [ ] 订单服务开发
- [ ] API 网关开发
- [ ] 秒杀功能实现
- [ ] 性能优化与测试

## 后续开发计划

1. 完成商品服务、库存服务、订单服务的开发
2. 实现 API 网关，统一鉴权和路由
3. 实现秒杀核心功能
   - Redis 缓存优化
   - 分布式锁防止超卖
   - 消息队列异步处理
   - 接口限流与降级
4. 压力测试与性能优化
5. 监控告警配置
6. Docker 容器化部署

## 相关文档

- [系统设计文档](docs/系统设计文档.md)
- [API 接口文档](http://localhost:8081/doc.html) (启动服务后访问)
