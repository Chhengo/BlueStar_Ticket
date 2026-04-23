# 设计文档 — 公平限量抢票系统（BlueStar_Ticket）

> MVP 原则：先跑通「注册→登录→发票→抢票→异步下单→支付确认」完整链路，再谈优化。
> 参考规范：阿里巴巴 Java 开发手册（黄山版）

---

## 1. 项目定位

| 项 | 内容 |
|----|------|
| 解决的真实问题 | 黄牛脚本秒光票，普通用户无法购票 |
| 核心差异 | 公平排队 + 原子扣库存 + 异步落单，不是简单"先到先得" |
| MVP 范围 | 单场次票务；不含退票、电子票生成、真实支付 |
| 后续扩展方向 | 多场次、分区座位、电子票 PDF、真实支付接入 |

---
BlueStar_Ticket

## 2. 模块划分（MVP）

```
fair-ticket/
├── ticket-gateway          # 入口限流（Spring MVC 拦截器）
├── ticket-user             # 用户模块（注册/登录/JWT）
├── ticket-event            # 场次模块（活动/票档管理）
├── ticket-order            # 订单模块（下单/支付确认）
├── ticket-mq               # Kafka 消费者（异步落单）
└── ticket-common           # 公共：枚举/异常/Result/常量
```

> MVP 阶段：单模块 Spring Boot 工程，包结构按上面划分；后续按需拆微服务。

---

## 3. 数据库设计（MySQL）

### 3.1 用户表 `t_user`
```sql
CREATE TABLE t_user (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username      VARCHAR(32) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码',
    phone         VARCHAR(16) COMMENT '手机号（脱敏存储）',
    real_name     VARCHAR(32) COMMENT '实名',
    status        TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0封禁',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 3.2 活动表 `t_event`
```sql
CREATE TABLE t_event (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    name          VARCHAR(128) NOT NULL COMMENT '活动名称',
    venue         VARCHAR(128) COMMENT '场馆',
    start_time    DATETIME NOT NULL COMMENT '开始时间',
    sale_start    DATETIME NOT NULL COMMENT '开售时间',
    sale_end      DATETIME NOT NULL COMMENT '停售时间',
    status        TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1发布 2结束',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';
```

### 3.3 票档表 `t_ticket_type`
```sql
CREATE TABLE t_ticket_type (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '票档ID',
    event_id      BIGINT NOT NULL COMMENT '关联活动',
    name          VARCHAR(64) NOT NULL COMMENT '票档（如A/B/C）',
    price         DECIMAL(10,2) NOT NULL COMMENT '单价（元）',
    total_stock   INT NOT NULL COMMENT '总库存',
    remain_stock  INT NOT NULL COMMENT '剩余库存（数据库兜底）',
    per_limit     TINYINT NOT NULL DEFAULT 2 COMMENT '每人限购数',
    INDEX idx_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票档表';
```

### 3.4 订单表 `t_order`
```sql
CREATE TABLE t_order (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no       VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号（雪花ID） 已随机过',
    user_id        BIGINT NOT NULL COMMENT '用户ID',
    event_id        BIGINT NOT NULL COMMENT '活动ID',
    ticket_type_id BIGINT NOT NULL COMMENT '票档ID',
    quantity       TINYINT NOT NULL COMMENT '购买数量',
    total_amount   DECIMAL(10,2) NOT NULL COMMENT '总金额',
    status         TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2超时取消 3已退款',
    expire_at      DATETIME COMMENT '支付截止时间（15分钟）',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

### 3.5 幂等表 `t_idempotent_record`
```sql
CREATE TABLE t_idempotent_record (
    idempotent_key VARCHAR(64) PRIMARY KEY COMMENT '幂等key（userId_ticketTypeId）',
    order_no       VARCHAR(32) NOT NULL COMMENT '已生成的订单号',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下单幂等记录';
```

---

## 4. Redis Key 设计

| Key | 类型 | 说明 | TTL |
|-----|------|------|-----|
| `ticket:stock:{ticketTypeId}` | String | 库存数量（原子 DECR） | 永久（随库存初始化） |
| `ticket:user:bought:{userId}:{ticketTypeId}` | String | 该用户已购数量 | 7天 |
| `ticket:rate:limit:{userId}` | String | 用户请求计数（令牌桶） | 60s 滑动 |
| `ticket:ip:limit:{ip}` | String | IP 请求计数 | 60s |
| `ticket:order:expire:{orderNo}` | String | 待支付订单（延迟取消） | 900s（15分钟）|

---

## 5. 核心接口设计

> 统一返回格式：`{"code": 200, "msg": "ok", "data": {}}`
> 错误码规范：2xx 成功，4xx 客户端错误，5xx 服务端错误，业务错误码独立枚举。

### 5.1 用户模块

```
POST /api/user/register
  Body: {username, password, phone, realName}
  Return: {userId, username}

POST /api/user/login
  Body: {username, password}
  Return: {token, expireAt}
```

### 5.2 活动模块

```
GET /api/v1/events
  Query: {page, size, status}
  Return: 分页活动列表

GET /api/v1/events/{eventId}/ticket-types
  Return: 该活动下的票档列表（含剩余库存）
```

### 5.3 抢票核心接口（高并发入口）

```
POST /api/v1/orders/grab
  Header: Authorization: Bearer {token}
  Body: {ticketTypeId, quantity}
  Return: {orderNo, status: "PENDING", expireAt}

  核心流程（同步阶段，< 50ms）:
  1. JWT 校验
  2. 限流校验（Redis 令牌桶）
  3. 重复购买校验（Redis）
  4. Redis 原子扣库存（Lua 脚本）
  5. 发 Kafka 消息（payload: userId, ticketTypeId, quantity, orderNo）
  6. 返回 orderNo（订单异步生成中）
```

### 5.4 支付确认（模拟）

```
POST /api/v1/orders/{orderNo}/pay
  Header: Authorization: Bearer {token}
  Return: {orderNo, status: "PAID"}

GET /api/v1/orders/{orderNo}
  Return: 订单详情（含状态）
```

---

## 6. Kafka 消息设计

### Topic: `ticket.order.create`
```json
{
  "userId": 1001,
  "ticketTypeId": 5,
  "quantity": 2,
  "orderNo": "2024041900001",
  "requestTime": "2024-04-19T10:00:00"
}
```

### 消费者处理流程
```
1. 幂等校验（查 t_idempotent_record，防重复消费）
2. 写订单到 MySQL（t_order，状态：待支付）
3. 写幂等记录（t_idempotent_record）
4. 设置 Redis Key: ticket:order:expire:{orderNo}（TTL 900s）
5. 超时未支付：定时扫描 or Redis keyspace notification → 取消订单 + 回补库存
```

---

## 7. 限流方案（MVP 令牌桶 Lua 脚本）

```lua
-- key: ticket:rate:limit:{userId}
-- ARGV[1]: 每分钟限制次数（如 5）
-- 返回 1 允许，0 拒绝
local count = redis.call('INCR', KEYS[1])
if count == 1 then
    redis.call('EXPIRE', KEYS[1], 60)
end
if tonumber(count) > tonumber(ARGV[1]) then
    return 0
end
return 1
```

---

## 8. 原子扣库存 Lua 脚本

```lua
-- key: ticket:stock:{ticketTypeId}
-- ARGV[1]: 购买数量
-- 返回 1 成功，0 库存不足
local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil or stock < tonumber(ARGV[1]) then
    return 0
end
redis.call('DECRBY', KEYS[1], ARGV[1])
return 1
```

---

## 9. 安全规范

| 项 | 方案 |
|----|------|
| 密码存储 | BCrypt（不存明文，不用 MD5） |
| 接口鉴权 | JWT（HS256，密钥走环境变量，不入代码） |
| 输入校验 | `@Valid` + Bean Validation 注解，Controller 层拦截 |
| SQL 注入 | MyBatis `#{}` 绑定，禁止 `${}` 拼接 |
| 敏感配置 | 密码/密钥走 `.env` / Spring Profile，`.gitignore` 排除 |
| 接口限流 | 双重：用户级 + IP 级 |

---

## 10. 进度里程碑

| 阶段 | 内容 | 完成标准 | 进度 |
|------|------|---------|------|
| P1 | 环境搭建 + DB 建表 | 服务启动，表创建成功 | 0% |
| P2 | 用户注册/登录 + JWT | Postman 登录拿到 Token | 0% |
| P3 | 活动/票档 CRUD | 接口返回正确数据 | 0% |
| P4 | Redis 库存初始化 + Lua 扣减 | 单测通过，并发 10 线程不超卖 | 0% |
| P5 | Kafka 异步落单链路 | 抢票→MQ→订单写库 全链路通 | 0% |
| P6 | 支付确认 + 超时取消 | 15 分钟未支付自动取消并回补库存 | 0% |
| P7 | 前端购票页（Vue3） | 页面可操作，展示倒计时 | 0% |
| P8 | Docker Compose 部署 | `docker compose up` 一键启动 | 0% |
| P9 | GitHub Actions CI | push 自动跑测试 | 0% |

---

## 11. 端到端测试核心链路

```
用户注册 → 登录获取 Token → 查看活动列表 → 查看票档库存
→ 发起抢票（POST /orders/grab）→ 轮询订单状态（PENDING）
→ 模拟支付（POST /orders/{orderNo}/pay）→ 订单状态变为 PAID
→ 并发场景：100 并发，库存 10，验证订单数 = 10
```

---

## 12. 后续扩展方向（不在 MVP 内）

- 虚拟公平排队（Queue 位置编号，而非纯先到先得）
- 账号信誉评分（注册时长、实名、购票历史）
- 电子票 PDF 生成
- 真实支付接入（沙箱环境）
- 退票流程
- 座位图选座
