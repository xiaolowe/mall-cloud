# Mall Cloud

Java 微服务电商平台。第一版覆盖**核心交易闭环**：注册登录 → 浏览商品 → 加入购物车 → 下单扣库存 → 模拟支付 → 订单状态回写，并支持取消订单/超时取消回补库存。

所有本地依赖（JDK、Maven、MySQL、Redis、Nacos）都在项目内 `.tools/`，不安装到系统、不需要 sudo、不修改 `~/.zshrc`。

## 技术栈

| 层次 | 选型 |
| --- | --- |
| 语言/运行时 | Java 17 |
| 基础框架 | Spring Boot 3.2.5 |
| 微服务 | Spring Cloud 2023.0.1 + Spring Cloud Alibaba 2023.0.1.0 |
| 注册中心 | Nacos 2.3.2 |
| 网关 | Spring Cloud Gateway |
| 服务调用 | OpenFeign + LoadBalancer |
| 持久层 | MyBatis-Plus 3.5.7 + MySQL 8.0 |
| 缓存 | Redis 7.2 |
| 鉴权 | JWT（网关统一校验） |
| 前端 | Vue 3.5 + Vite 5 + Vue Router 4 + Pinia 2 + Element Plus |
| 本地中间件 | 项目内二进制，免安装 |
| 云端部署 | Docker Compose |

## 服务与端口

| 服务 | 端口 | 职责 | 数据库 |
| --- | --- | --- | --- |
| mall-gateway | 9000 | 路由、JWT 校验、静态演示页 | - |
| mall-user | 9001 | 注册、登录、用户信息、收货地址 | mall_user |
| mall-product | 9002 | 分类、商品、SKU、Redis 缓存 | mall_product |
| mall-inventory | 9003 | 库存查询、幂等扣减/回补 | mall_inventory |
| mall-order | 9004 | 购物车、订单、超时取消 | mall_order |
| mall-payment | 9005 | 模拟支付、回调订单 | mall_payment |

本地中间件：MySQL `3307`（3306 已被机器上的其他服务占用）、Redis `6379`、Nacos `8848`。

## 本地启动

```bash
cd /Users/xiaolowe/agent/claude/mall-cloud

# 1. 首次：下载并初始化项目内工具链（已完成的话可跳过）
./scripts/download-tools.sh
./scripts/setup-infra.sh

# 2. 启动 MySQL / Redis / Nacos
./scripts/start-infra.sh

# 3. 编译（首次会下载 Maven 依赖到 .m2repo/，并构建 Vue 前端）
./scripts/build.sh

# 4. 启动 6 个服务
./scripts/start-services.sh
```

打开 <http://127.0.0.1:9000>。内置演示账号 **demo / 123456**，也可以现场注册新账号。

### 前端开发模式

Vue 前端在 `mall-web/`，开发时用 Vite 热更新（API 代理到网关 9000）：

```bash
./scripts/dev-web.sh
```

打开 <http://127.0.0.1:5175>。改完代码后执行 `./scripts/build.sh`，产物会自动打进网关 jar，由 9000 端口提供。

> 前端使用 hash 路由（`/#/orders`），因此网关不需要额外的 SPA fallback；如果以后改成 history 模式，需要在 Nginx 或网关加一条回退到 `index.html` 的规则。

> 本机若开启了系统 HTTP 代理（如 Clash/Surge），启动脚本会给 JVM 加 `-Dhttp.nonProxyHosts=*`，避免服务间通过局域网 IP 调用时被代理拦截。3306 已被机器上的其他 MySQL 占用，所以本项目 MySQL 使用 3307。

停止：

```bash
./scripts/stop-services.sh
./scripts/stop-infra.sh
```

## 演示流程

1. 打开网关页面，输入用户名密码，点「注册新账号」。
2. 在「收货地址」新增一条默认地址。
3. 商品卡片点「加入购物车」。
4. 右侧购物车点「结算下单」，生成待支付订单。
5. 订单卡片点「模拟支付」，订单变为「已支付」，支付记录写入 `mall_payment.payment`。
6. 也可以点「取消订单」，库存会幂等回补；待支付超过 30 分钟会被定时任务自动取消。

查看日志：

```bash
tail -f .logs/mall-order.log
tail -f .logs/mall-gateway.log
```

## 目录结构

```
mall-cloud/
├── mall-common-core/      # Result、异常、JWT、常量（不依赖 Web）
├── mall-common-web/       # 全局异常处理、用户上下文拦截器
├── mall-gateway/          # 网关 + 静态演示页
├── mall-user/
├── mall-product/
├── mall-inventory/
├── mall-order/
├── mall-payment/
├── mall-web/              # Vue 3 + Vite 前端（商品、购物车、订单、地址）
├── sql/init.sql           # 5 个库、表结构与商品种子数据
├── scripts/               # 下载、初始化、启停脚本
├── deploy/                # Dockerfile、docker-compose、云部署
├── docs/architecture.md   # 架构、链路与演进说明
├── .tools/                # 项目内 JDK/Maven/MySQL/Redis/Nacos/Node（gitignore）
├── .m2repo/               # 项目内 Maven 依赖仓库（gitignore）
└── .data/                 # MySQL 数据、Redis 配置与日志（gitignore）
```

## 腾讯云部署

> 现有服务器 `115.159.212.216` 上的 80（Docker 应用）和 8080（guanshi）保持不动，本项目使用 **9000**。

1. 本地同步代码（排除工具链、构建产物和数据）：

```bash
rsync -avz --exclude '.tools' --exclude '.m2repo' --exclude 'downloads' \
  --exclude '.data' --exclude '.logs' --exclude '**/target' \
  /Users/xiaolowe/agent/claude/mall-cloud/ root@115.159.212.216:/opt/mall-cloud/
```

2. 在服务器上配置密码并启动：

```bash
cd /opt/mall-cloud/deploy
cp .env.example .env
vi .env                      # 修改 MYSQL_ROOT_PASSWORD 和 JWT_SECRET
docker compose up -d --build
docker compose ps
```

3. 腾讯云控制台放行安全组 TCP **9000**，访问 `http://115.159.212.216:9000`。

说明：

- 整套 6 服务 + MySQL + Redis + Nacos 建议 **4C8G**；2C4G 可运行但需把各服务 `-Xmx` 降到 192m。
- 只有网关暴露 9000；MySQL/Redis/Nacos 和其余服务只在 Compose 内网可见。
- 首次启动 MySQL 会自动执行 `sql/init.sql`（数据卷已存在时不会重复执行）。
- `deploy/Dockerfile` 会先用 Node 阶段构建 Vue 前端，再打进网关镜像，不需要在服务器上装 Node。
- 生产环境请把 `.env` 中的密码和 JWT secret 换成强随机值，并关闭 9000 的公网直连（前置 Nginx + HTTPS）。

## 已验证 / 待补

已验证：本地编译、6 服务启动、注册登录、商品查询、购物车、下单扣库存、模拟支付、取消回补库存；Vue 3 前端的商品/购物车/订单/地址页面已接入网关。

待补（生产化）：Nacos 配置中心、Sentinel 限流熔断、Seata/RocketMQ 分布式事务与异步消息、Elasticsearch 搜索、对象存储、后台管理端、HTTPS/域名、CI/CD。

## 设计取舍

- **每服务独立数据库**：边界清晰，后续可独立迁库。
- **网关统一鉴权**：服务只信任网关注入的 `X-User-Id`，网关会剥离客户端伪造的同名头。
- **内部接口隔离**：服务间接口统一放在 `/internal/**`，网关只路由 `/api/**`，外网不可达。
- **库存幂等**：`stock_flow(order_no, sku_id, type)` 唯一键保证重试不会重复扣减/回补。
- **下单一致性**：先扣库存，再本地事务落订单；落库失败时调用库存回补。生产级方案见 `docs/architecture.md`。
