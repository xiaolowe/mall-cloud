# 架构说明

## 1. 总体结构

```
                    ┌────────────────────────────┐
  浏览器 ──────────▶│ mall-gateway :9000          │
                    │ 路由 + JWT 校验 + CORS       │
                    └─────────────┬──────────────┘
                                  │ Nacos 服务发现 (lb://)
        ┌──────────────┬──────────┼───────────┬──────────────┐
        ▼              ▼          ▼           ▼              ▼
   mall-user      mall-product  mall-inventory  mall-order   mall-payment
    :9001          :9002         :9003          :9004         :9005
        │              │          │              │              │
   mall_user      mall_product  mall_inventory  mall_order     mall_payment
        └──────────────┴──────────┴──────────────┴──────────────┘
                                  MySQL 8.0（每服务独立库）
                    Redis 7.2（商品缓存）   Nacos 2.3.2（注册中心）
```

服务之间只通过 HTTP/Feign 调用，不共享数据库表。

## 2. 服务职责

### mall-web（前端）
- Vue 3.5 + Vite 5 + Vue Router 4 + Pinia 2 + Element Plus。
- 页面：登录/注册、商品列表、购物车抽屉、收货地址弹窗、我的订单。
- 开发模式跑在 `:5175`，`/api` 代理到网关 `:9000`；生产构建产物由构建脚本复制进网关 jar，统一从 `:9000` 提供。
- 使用 hash 路由，避免网关为 SPA 深链接做额外 fallback。

### mall-gateway
- 唯一公网入口，路由 `/api/**` 到对应服务。
- 解析 `Authorization: Bearer <jwt>`，校验后将 `X-User-Id` / `X-User-Name` 注入下游请求。
- 剥离客户端自带的 `X-User-Id`，防止伪造身份。
- 白名单：`/api/auth/login`、`/api/auth/register`、`GET /api/products/**`、`GET /api/categories/**`。
- `/internal/**` 不在路由表中，外网无法访问。

### mall-user
- `POST /api/auth/register`、`POST /api/auth/login`，BCrypt 存密码，JJWT 签发 token。
- `GET /api/users/me`、收货地址增查。
- 内部接口 `GET /internal/users/{id}/addresses/default` 供订单服务取默认地址。

### mall-product
- 分类、商品分页、商品详情（含 SKU）。
- `GET /internal/skus?ids=1,2` 供订单/购物车服务批量取价格与标题。
- 商品详情使用 Redis 缓存；缓存不可用时通过 `CacheErrorHandler` 降级到数据库。

### mall-inventory
- 扣减：`UPDATE stock SET available = available - ? WHERE sku_id = ? AND available >= ?`，用条件更新防止超卖。
- `stock_flow` 唯一键 `(order_no, sku_id, type)` 保证同一个订单的扣减/回补幂等。
- 一次请求内多条明细在同一本地事务中，任何一条失败整体回滚。

### mall-order
- 购物车：加、查、删。
- 下单：合并明细 → 调商品服务取价 → 调用户服务取默认地址 → 调库存服务扣减 → 本地事务写 `orders` + `order_item` → 清购物车。
- 订单状态：0 待支付、1 已支付、2 已取消、3 已完成。
- 支付回调 `POST /internal/orders/{orderNo}/paid` 幂等。
- 定时任务每 60 秒扫描超过 30 分钟的待支付订单并取消、回补库存。

### mall-payment
- `POST /api/payments/{orderNo}/pay`：校验订单归属与状态 → 写支付单 → 调订单服务标记已支付 → 更新支付单。
- 同一订单已有成功支付记录时直接返回，避免重复扣款（演示环境为模拟支付）。

## 3. 核心链路

### 下单

```
Client → Gateway → OrderService
                      ├─ ProductClient   /internal/skus
                      ├─ UserClient      /internal/users/{id}/addresses/default
                      ├─ InventoryClient /internal/inventory/deduct
                      └─ DB transaction  orders + order_item (+ clear cart)
```

失败补偿：库存扣减成功但订单落库失败时，订单服务立即调用 `/internal/inventory/restore`，回补失败会记录错误日志。

### 支付

```
Client → Gateway → PaymentService
                      ├─ OrderClient GET /internal/orders/{orderNo}
                      ├─ write payment (PENDING)
                      ├─ OrderClient POST /internal/orders/{orderNo}/paid
                      └─ mark payment SUCCESS
```

### 取消 / 超时

- 用户取消：先幂等回补库存，再条件更新订单状态，避免并发重复取消。
- 超时：`OrderTimeoutTask` 用条件更新抢到订单（`status=0 → 2`）后才回补库存，保证只回补一次。

## 4. 数据归属

| 库 | 表 |
| --- | --- |
| mall_user | users, user_address |
| mall_product | category, product, product_sku |
| mall_inventory | stock, stock_flow |
| mall_order | cart_item, orders, order_item |
| mall_payment | payment |

订单中保存商品名称、价格、封面的**快照**，商品后续改价不会影响历史订单。

## 5. 安全设计

- 密码 BCrypt 加盐哈希，不返回给前端。
- JWT 由网关统一验证；服务不直接暴露公网端口（云端 Compose 只映射 9000）。
- 服务间接口统一 `/internal/**`，网关不路由。
- 客户端传入的 `X-User-Id` 在网关被移除后重新注入。
- 演示环境的 JWT secret 写在配置默认值里，生产必须通过环境变量 `JWT_SECRET` 覆盖。

## 6. 当前取舍与生产化路线

当前版本为了“本地一条命令跑通”，刻意没有引入重量级组件：

| 能力 | 当前实现 | 生产建议 |
| --- | --- | --- |
| 分布式事务 | 本地事务 + 补偿回补 | Seata AT/TCC，或 RocketMQ 事务消息 + 本地消息表 |
| 订单超时 | Spring `@Scheduled` 轮询 | RocketMQ 延迟消息 / 时间轮 |
| 限流熔断 | 无 | Sentinel + 网关限流 |
| 配置中心 | 本地 `application.yml` + 环境变量 | Nacos Config，按 namespace/group 分环境 |
| 商品搜索 | MySQL LIKE | Elasticsearch + Canal 增量同步 |
| 图片存储 | 外链占位图 | 腾讯云 COS / MinIO |
| 链路追踪 | 日志 | Micrometer Tracing + SkyWalking |
| 部署 | 本机进程 + 云端 Compose | K8s + HPA，或腾讯云 TKE |

演进顺序建议：Nacos Config → Sentinel → RocketMQ（超时取消/异步解耦）→ Seata → ES → 可观测性。
