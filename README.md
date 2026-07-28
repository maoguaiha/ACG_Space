# ACG Space - 动漫内容与数字谷子集换社区 V2.0

一款集「高质量番剧检索、动漫资讯阅读」与「数字谷子集换及实体周边核销」于一体的二次元社区。系统采用混合渲染（SSG + CSR）保障高收录，并基于 Redis Lua 与 RocketMQ 构建了高并发、强一致性的数字资产交易流转闭环。

## 🚀 技术栈

### 后端
- **框架**: Spring Boot 3 + RuoYi-Vue
- **语言**: Java 17
- **ORM**: MyBatis-Plus
- **缓存与并发控制**: Redis + Redisson (MultiLock 联锁支持)
- **消息队列**: RocketMQ（支持分布式事务消息）
- **限流熔断**: Resilience4j
- **JSON**: Fastjson2（配置 WriteLongAsString 解决雪花 ID 精度丢失）

### 前端（用户端）
- **框架**: Nuxt 3 (SSG + CSR 混合渲染 + SSR) + Vue 3
- **样式**: Tailwind CSS（三主题：浅色/深色/粉色）
- **状态管理**: Pinia
- **动效**: 页面过渡淡入上滑、IntersectionObserver 滚动显现、FLIP 列表动画
- **图片裁剪**: vue-cropper

### 管理端
- **框架**: Vue 3 + Element Plus + Pinia
- **构建工具**: Vite + TypeScript
- **代码规范**: ESLint + Prettier

### 数据库
- **主数据库**: MySQL 8.0+
- **迁移工具**: SQL 脚本（手动执行，无 Flyway）

## 📁 项目结构

```text
ACG_Space/
├── admin-ui/                # 管理端（Vue 3 + Element Plus）
│   └── src/views/
│       ├── article/         # 文章管理、审核
│       ├── anime/           # 番剧 CRUD
│       ├── comment/         # 评论审核
│       ├── gacha/           # 抽赏配置
│       ├── item/            # 商品图鉴
│       ├── delivery/        # 物流调度
│       ├── redeem-product/  # 兑换商品管理
│       ├── redeem-order/    # 兑换订单管理
│       ├── transaction/     # 交易监控
│       └── risk-control/    # 风控中心
├── backend/                 # 后端服务（Spring Boot）
│   ├── sql/                 # 数据库脚本（初始化 + 迁移 + 种子数据）
│   └── src/main/java/com/ruoyi/project/
│       ├── controller/      # REST 控制器
│       ├── service/         # 业务逻辑层
│       ├── mapper/          # MyBatis-Plus 映射
│       ├── domain/          # entity / dto / vo
│       ├── config/          # Spring 配置（Fastjson2、Security、Redisson 等）
│       ├── mq/              # RocketMQ 生产/消费
│       ├── integration/     # 外部 API 客户端（Bangumi）
│       └── common/          # 工具类、注解、拦截器
├── front-ui/                # 用户端（Nuxt 3）
│   ├── pages/               # 页面（文件路由）
│   ├── components/          # 公共组件
│   ├── composables/         # 组合式函数（useApi.ts 等）
│   ├── stores/              # Pinia 状态（user / app / anime）
│   ├── layouts/             # 布局（含三主题 CSS 变量）
│   ├── plugins/             # 插件（v-reveal、vue-cropper）
│   └── assets/css/          # 全局样式（tailwind.css）
├── document/                # 项目文档
│   ├── develop/V1/          # V1.0 需求/架构/计划
│   ├── develop/V2/          # V2.0 PRD/架构/Agent设计/计划
│   ├── study/               # 学习笔记与 Bug 记录
│   ├── skill/               # 前端架构师技能定义
│   └── point/               # 上下文重点（Source of Truth）
├── docker-compose.yml       # 本地开发环境（MySQL / Redis / RocketMQ）
├── railway.json             # Railway 部署配置
└── CLAUDE.md / AGENTS.md    # AI 协作规则
```

## ✨ 核心功能

### 用户端（前台门户）

| 功能模块 | 说明 |
|---------|------|
| 🏠 **首页** | 轮播 Banner、新番时间表（滑动切换）、今日热播推荐、滚动显现动效 |
| 📚 **番剧库** | 番剧检索与探索（筛选/分类/搜索），TransitionGroup FLIP 列表动画 |
| 📖 **文章社区** | 资讯阅读、发表文章、楼中楼评论互动、点赞/点踩、审核/删除通知 |
| 🎲 **抽赏中心** | 单抽/十连抽盲盒系统，Redis Lua 脚本保障强一致性不超卖 |
| 🎒 **数字背包** | 可视化管理持有资产（网格/列表视图），阶梯浮现动画 |
| 🔧 **记忆工坊** | 多碎片同步合成功能，Redisson MultiLock 保障并发防刷 |
| 🎁 **兑换中心** | UR 碎片/积分兑换实物商品，省市县三级联动下拉选择地址 |
| 📦 **订单管理** | 订单详情、物流追踪、系统通知（下单/发货） |
| 👤 **用户主页** | 个人信息编辑、头像裁剪上传（1:1 圆形）、文章/追番/评论/点赞历史 |
| 💬 **私信系统** | 实时消息、会话管理、系统通知（来源 "ACG Space 官方"） |

### 管理端（中后台）

| 功能模块 | 说明 |
|---------|------|
| 🎬 **番剧管理** | 番剧 CRUD、Bangumi 同步 |
| 📄 **文章管理** | 文章列表/审核（通过/驳回含原因）、状态筛选 |
| 💬 **评论审核** | 评论列表、删除及通知 |
| 🛍️ **商品图鉴** | 物品录入、兑换商品管理 |
| 🎲 **抽赏配置** | 奖池管理、概率配置 |
| 📋 **兑换订单** | 订单列表、物流更新（发货/完成） |
| 🚚 **物流调度** | 实体发货申请单管理 |
| 🛡️ **风控中心** | 熔断器/限流器状态监控 |
| 📊 **交易监控** | 兑换记录查询 |

### 后台自动通知

| 触发操作 | 通知内容 | 接收者 |
|---------|---------|--------|
| ✅ 文章审核通过 | 🎉 您的文章《xxx》已审核通过并发布！ | 文章作者 |
| ❌ 文章审核拒绝 | 您的文章《xxx》未通过审核，原因：xxx | 文章作者 |
| 🗑️ 管理员删文章 | 您的文章《xxx》已被管理员删除 | 文章作者 |
| 🗑️ 管理员删评论 | 您的评论「xxx…」已被管理员删除 | 评论作者 |
| 🛍️ 订单创建成功 | 您的兑换订单已创建成功！订单号：xxx，物品：xxx | 下单用户 |

### 多主题系统

支持**浅色主题（星空蓝）**、**深色主题（星空紫）**、**粉色主题**三种主题：
- 所有页面通过 CSS 变量（`.theme-light` / `.theme-dark` / `.theme-pink`）适配
- 核心品牌色：浅色使用 `#6366F1→#3B82F6` 星空蓝渐变，粉色使用 `#EC4899→#F472B6`
- 公共按钮/卡片/输入框全部绑定 CSS 变量，新页面必须适配三主题

### 动效系统

| 动效 | 实现方式 | 作用范围 |
|------|---------|---------|
| 页面过渡 | Nuxt `pageTransition` + CSS keyframe | 所有页面跳转 |
| 滚动显现 | `v-reveal` 指令（IntersectionObserver） | 首页/社区/番剧库/背包/兑换/用户主页 |
| 卡片阶梯 FadeUp | `stagger-item` CSS class + `index * 0.0Xs` delay | 列表/网格首次加载 |
| 列表 FLIP | `TransitionGroup` + CSS `list-move` | 社区文章/番剧库筛选切换 |
| Tab 滑动 | `Transition` `mode="out-in"` + `translateX` | 首页新番时间表 |
| 按钮微交互 | `button:active { scale(0.95) }` | 全局按钮点击 |

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- RocketMQ 5.x（可选，用于异步积分计算）

### 本地开发环境（Docker）

```bash
# 启动 MySQL / Redis / RocketMQ
docker-compose up -d

# 初始化数据库（首次需执行所有 SQL）
mysql -h127.0.0.1 -uroot -p123456 acg_space < backend/sql/ACG_Space_init.sql

# 迁移额外修复（如果是从旧库升级）
mysql -h127.0.0.1 -uroot -p123456 acg_space < backend/sql/fix_biz_comment_columns.sql
mysql -h127.0.0.1 -uroot -p123456 acg_space < backend/sql/fix_avatar_column.sql

# 导入抽赏种子数据
mysql -h127.0.0.1 -uroot -p123456 acg_space < backend/sql/seed_gacha_items.sql
mysql -h127.0.0.1 -uroot -p123456 acg_space < backend/sql/fix_image_urls.sql
```

### 后端启动

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
# 或
java -jar target/acg-space-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### 前端启动

```bash
cd front-ui
npm install
npm run dev        # 开发模式 http://localhost:3000
npm run build      # 生产构建
```

### 管理端启动

```bash
cd admin-ui
npm install
npm run dev        # 开发模式 http://localhost:5173
npm run build      # 生产构建
```

### Railway 部署

三个独立服务部署在 Railway：

| 服务 | 构建方式 | 说明 |
|------|---------|------|
| **后端** | Dockerfile | Spring Boot JAR |
| **front-ui** | Dockerfile / Nixpacks | Nuxt SSR |
| **admin-ui** | Nixpacks | SPA 静态文件 |

关键环境变量：
- `NUXT_API_INTERNAL_BASE` — front-ui SSR 阶段访问后端的内部地址
- `BACKEND_URL` — admin-ui 的后端代理地址

## 📊 数据库脚本

| 脚本 | 用途 |
|------|------|
| `ACG_Space_init.sql` | **全新库一键初始化**（建表 + 种子数据） |
| `ACG_Space_upgrade.sql` | 存量库升级（补充缺失列 + 新增表） |
| `fix_biz_comment_columns.sql` | 评论表补列（`anime_id` / `parent_id` / `reply_to_user_id` 等） |
| `fix_avatar_column.sql` | avatar 列 `varchar(500)` → `MEDIUMTEXT` |
| `seed_gacha_items.sql` | 抽赏种子数据（10 个物品 + 奖池关联 + 兑换商品） |
| `fix_image_urls.sql` | 旧图片 URL 从 `placehold.co` 迁移至 `picsum.photos` |

## 🔐 API 安全

- JWT Token 认证（`Authorization: Bearer xxx`）
- 公开 GET 路径豁免 Token 校验（`shouldNotFilter`），但**带 Authorization header 的请求仍会解析 Token**
- 接口参数使用 `@Validated` 校验
- 统一响应格式 `Result<T>`（code=200 表示成功）

## 📝 开发规范

### 后端
- **Bean 显式命名**：所有 `@Bean` 必须设置 `name`，防止 `BeanDefinitionOverrideException`
- **幂等性**：所有 RocketMQ 消费者必须包含幂等校验
- **锁安全**：Redisson 锁必须配合 `finally` 释放
- **审计字段**：所有 `BaseEntity` 子类需确保 `createTime`/`updateTime` 显式设置
- **雪花 ID**：Fastjson2 `WriteLongAsString` → 前端保持字符串类型，禁止 `Number()` 转换
- **图片字段**：base64 数据用 `MEDIUMTEXT`/`LONGTEXT`，不用 `varchar(500)`

### 前端
- **TypeScript**：禁止 `any`
- **三主题**：每个页面适配浅色/深色/粉色
- **API 封装**：统一在 `useApi.ts` 或 `useV2Api.ts`
- **状态管理**：使用 Pinia
- **动效**：列表加 `stagger-item` + `animationDelay`，筛选切换用 `TransitionGroup`

## 📄 文档索引

- `document/develop/V1/` — V1.0 需求/架构/迭代计划
- `document/develop/V2/` — V2.0 PRD/架构/Agent设计方案/开发计划
- `document/study/study.md` — Bug 记录与修复方案
- `document/point/point.md` — 当前上下文重点（Source of Truth）
- `document/skill/front-ui.md` — 前端架构师技能定义
- `CLAUDE.md` — Claude Code 项目指南
- `AGENTS.md` — AI 协作规则

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License
