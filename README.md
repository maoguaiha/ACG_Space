# ACG Space - 动漫内容与数字谷子集换社区 V2.0

一款集"高质量番剧检索、动漫资讯阅读"与"数字谷子集换及实体周边核销"于一体的二次元社区。系统采用混合渲染（SSG + CSR）保障高收录，并基于 Redis Lua 与 RocketMQ 构建了高并发、强一致性的数字资产交易流转闭环。

## 🚀 技术栈

### 后端
- **框架**: Spring Boot 3 + RuoYi-Vue
- **语言**: Java 17
- **ORM**: MyBatis-Plus
- **缓存与并发控制**: Redis + Redisson (MultiLock 联锁支持)
- **消息队列**: RocketMQ (支持分布式事务消息)
- **限流熔断**: Resilience4j
- **JSON**: Fastjson2

### 前端（用户端）
- **框架**: Nuxt 3 (SSG + CSR 混合渲染) + Vue 3
- **样式**: Tailwind CSS
- **状态管理**: Pinia
- **动画引擎**: GSAP / @vueuse/motion (用于抽赏与合成特效)

### 管理端
- **框架**: Vue 3 + Element Plus
- **构建工具**: Vite

### 数据库
- **主数据库**: MySQL 8.0+

## 📁 项目结构

```text
ACG_Space/
├── admin-ui/           # 管理端（Vue 3 + Element Plus）
├── backend/            # 后端服务（Spring Boot）
│   ├── sql/            # 数据库迁移脚本
│   └── src/main/java/  # Java 源码
├── front-ui/           # 用户端（Nuxt 3）
├── document/           # 项目文档
│   ├── develop/        # 开发文档 (含 V1.0 及 V2.0 架构与 PRD)
│   └── study/          # 学习笔记
└── tools/              # 工具脚本
```

✨ 核心功能 (V2.0 升级版)
用户端 (前台门户)
🎲 抽赏中心 (Gacha Hub): 提供单抽/十连抽盲盒系统，底层采用 Redis Lua 脚本保障强一致性绝对不超卖。

🎒 数字背包与记忆工坊: 可视化管理持有资产；提供多碎片同步合成功能，由 Redisson MultiLock 保障并发防刷。

🏪 跳蚤市场 (集市): 玩家间二手资产流通，官方收取 1% 交易积分税以抵抗通胀，通过 RocketMQ 事务消息保障交易资金数据的最终一致性。

🎁 兑换中心: 使用 UR 碎片或积分兑换实物商品，支持收货地址填写与订单追踪。

📦 订单管理: 用户可查看兑换订单列表、物流信息与订单状态。

📦 O2O 实体核销: 玩家可将成品 SSR 数字资产发起核销申请，填写收货地址并转化为真实物流发货订单。

🔍 动漫库与文章社区: 番剧检索、社区资讯阅读及基于积分奖励机制的楼中楼评论互动。

管理端 (中后台)
🛍️ 电商与资产中台: 配置抽赏奖池与发售库存、录入盲盒与碎片图鉴、合成配方管理、兑换商品管理。

🛡️ 风控与交易监控: 监控 RocketMQ 事务回查日志、限流与熔断器大盘面板、交易记录查询。

🚚 O2O 物流调度: 处理用户提交的实体发货申请单，录入快递公司与物流追踪单号。

📋 兑换订单管理: 查看用户兑换订单、更新物流状态、管理订单流转。

🎬 基础内容管控: 番剧 CRUD、文章及评论审核管理。

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- RocketMQ 5.x（可选，用于异步积分计算）

### 后端启动

```bash
# 进入后端目录
cd backend

# 安装依赖（Maven）
mvn clean install -DskipTests

# 配置数据库连接（修改 application.yml）
# spring.datasource.url=jdbc:mysql://localhost:3306/acg_space

# 启动服务
mvn spring-boot:run
```

### 前端启动

```bash
# 进入用户端目录
cd front-ui

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

### 管理端启动

```bash
# 进入管理端目录
cd admin-ui

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

## 🔧 配置说明

### 后端配置
后端配置文件位于 `backend/src/main/resources/application.yml`，主要配置项：

- **数据库**: `spring.datasource.*`
- **Redis**: `spring.data.redis.*`
- **RocketMQ**: `rocketmq.*`（可选）

### 前端配置
前端环境变量配置在 `.env` 文件中：

```env
NUXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

## 📊 数据库迁移

数据库迁移脚本位于 `backend/sql/` 目录：

```bash
# 执行迁移（按顺序）
mysql -u root -p < backend/sql/schema.sql
mysql -u root -p < backend/sql/v1.1_reaction_migration.sql
mysql -u root -p < backend/sql/v1.2_comment_dislikes_migration.sql
mysql -u root -p < backend/sql/v1.3_article_content_longtext_migration.sql
mysql -u root -p < backend/sql/v1.4_message_migration.sql
```

## 🔐 API 安全

- 使用 JWT Token 进行身份认证
- 接口参数使用 `@Validated` 校验
- 敏感接口需要登录权限

## 📝 开发规范

### 后端
- 响应格式统一使用 `Result<T>`
- 接口参数必须使用 `@Validated` 校验
- 使用 `SecurityUtils.getUserId()` 获取当前用户
- 数据库操作使用 MyBatis-Plus

### 前端
- TypeScript 禁止使用 `any` 类型
- 响应式变量使用 `ref()` 或 `reactive()` 定义
- API 调用统一在 `useApi.ts` 中封装
- 使用 Pinia 进行状态管理

## 📄 文档

项目文档位于 `document/` 目录：

- `document/develop/` - 需求文档、技术架构设计、功能迭代计划
- `document/study/` - 学习笔记和常见问题

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License