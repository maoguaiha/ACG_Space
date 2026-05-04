# ACG Space - 动漫分享博客平台

一款集"高质量番剧检索、动漫资讯阅读、同好交流互动"于一体的垂直类内容社区。

## 🚀 技术栈

### 后端
- **框架**: Spring Boot 3 + RuoYi-Vue
- **语言**: Java 17
- **ORM**: MyBatis-Plus
- **缓存**: Redis
- **消息队列**: RocketMQ
- **JSON**: Fastjson2

### 前端（用户端）
- **框架**: Nuxt 3 + Vue 3
- **样式**: Tailwind CSS
- **状态管理**: Pinia

### 管理端
- **框架**: Vue 3 + Element Plus
- **构建工具**: Vite

### 数据库
- **主数据库**: MySQL 8.0+

## 📁 项目结构

```
ACG_Space/
├── admin-ui/           # 管理端（Vue 3 + Element Plus）
├── backend/            # 后端服务（Spring Boot）
│   ├── sql/            # 数据库迁移脚本
│   └── src/main/java/  # Java 源码
├── front-ui/           # 用户端（Nuxt 3）
├── document/           # 项目文档
│   ├── develop/        # 开发文档
│   └── study/          # 学习笔记
└── tools/              # 工具脚本
```

## ✨ 核心功能

### 用户端
- 🔍 **动漫库**: 番剧检索、分类筛选、详情展示
- 📰 **社区**: 文章列表、搜索、热度排序
- 👤 **个人主页**: 用户资料、文章/评论/追番三tab
- 💬 **评论系统**: 楼中楼回复、点赞/点踩
- ✍️ **文章发布**: 富文本编辑器、审核流程
- 🔔 **积分系统**: 互动积分、等级头衔

### 管理端
- 🎬 **动漫管理**: 番剧 CRUD、播放线路配置
- 📝 **文章管理**: 文章发布、审核流程
- 👥 **用户管理**: 用户列表、权限控制
- 💬 **评论管理**: 评论审核、违规处理

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