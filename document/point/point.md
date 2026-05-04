# 上下文重点 - point

## 项目概述
ACG Space - 动漫分享博客平台
- 后端：Spring Boot 3.2.5 + Java 17 + MyBatis-Plus
- 用户端：Nuxt 3 + TailwindCSS (端口 3000)
- 管理端：Vue 3 + Element Plus + TypeScript (端口 5173)

## 技术栈要点
- JSON：Fastjson2
- 消息队列：RocketMQ（消费者必须幂等性校验）
- 接口校验：必须使用 @Validated
- 前端：TypeScript 禁止 any

## 规则要点
- 修改 5 个文件以上需要列 Plan
- 不确定是否"强相关"时先问
- 新增依赖或修改接口签名会影响其他功能，需先确认
- 错误记录放入 document/study/study.md
- point 文件不断迭代，每次回复前先阅读
- **开发前必读 document/study/study.md**

## 当前进度
- ✅ V1.1 功能迭代开发中
- ✅ 已完成 Phase 1-4：用户主页、社区页、评论回复系统
- ✅ 已完成点赞/点踩功能（含持久化、状态同步）
- ✅ 已完成点踩理由选择和取消逻辑
- ✅ 已完成评论点踩计数修复
- ✅ 已完成文章发布功能完善：
  1. TagSelector 组件（用户端 + 管理端）
  2. CoverUploader 组件
  3. RichTextEditor 组件（用户端 + 管理端）
- ✅ 已完成文章详情页修复：
  1. 内容支持 HTML 渲染（v-html）
  2. 点赞点踩图标显示修复
  3. 右上角点赞点踩 UI 与下方一致
  4. 移除内容区封面图
- ✅ 已完成管理端文章编辑功能一致化
- ✅ 已创建数据库迁移脚本：
  1. v1.2_comment_dislikes_migration.sql
  2. v1.3_article_content_longtext_migration.sql
- ✅ 已完成数据库迁移：文章审核、热度排序、用户搜索
- ✅ 已完成个人主页评论显示修复（添加 loadComments 函数和 watch）
- ✅ 已完成私信功能：
  1. biz_message 表设计
  2. BizMessage 实体类
  3. MessageController 和 MessageService
  4. 消息列表页面 /messages
  5. 私信对话页面 /message/:userId
  6. 导航栏信封图标 + 未读消息数显示
- ✅ 已完成主题适配功能：
  1. 三种主题风格（深色蓝紫、浅色、粉色）
  2. 社区页分类/排序按钮主题适配
  3. 文章详情页封面优化（虚化+渐变）
  4. 评论区样式主题适配
  5. 番剧库页面主题适配
  6. 写文章按钮主题适配
  7. 个人主页主题适配（签名可编辑）
  8. 聊天界面优化（独立滚动+返回键）
  9. 富文本编辑器主题适配
  10. 标签选择器主题适配
  11. 返回键添加（文章详情、社区、消息、个人主页）
  12. 首页轮播文字主题适配

## 待完成模块

### 1. Phase 6 - 导航与交互增强
- [ ] 导航栏"写文章"按钮功能（已部分完成）

### 2. 自动化测试
- [ ] 集成测试覆盖
- [ ] E2E 测试

### 3. 运维硬化
- [ ] 数据库迁移工具化（Flyway/Liquibase）
- [ ] 运维 Runbook 编写

### 4. 文档与发布准备
- [ ] README 更新
- [ ] 迁移记录文档

## 已完成功能清单
1. ✅ 点赞/点踩功能（文章评论 & 普通评论）
2. ✅ 用户资料/个人主页（/user/:id）
3. ✅ 社区页（/community）
4. ✅ 评论楼中楼回复
5. ✅ 点赞/点踩状态持久化
6. ✅ 点踩理由选择弹窗
7. ✅ 点踩取消逻辑
8. ✅ B站风格大拇指 UI
9. ✅ 弹窗居中（Teleport）
10. ✅ 文章发布（标签+封面+富文本编辑器）
11. ✅ 文章详情页修复
12. ✅ 管理端文章编辑功能
13. ✅ 私信功能（消息列表 + 对话页面）
14. ✅ 主题适配（深色蓝紫、浅色、粉色三种风格）
15. ✅ 全局返回键添加
16. ✅ 首页轮播文字主题适配

## 核心问题记录
1. MyBatis-Plus @TableLogic 导致删除变逻辑删除
2. VO/DTO 字段需同步更新
3. Element Plus 图标需使用全局注册后的名称
4. `Collectors.toMap` 的 key 不能为 null，需先 filter 过滤
5. `Comparator.comparing` 无法处理 null 值，需用 `Comparator.nullsLast()`
6. Lambda 表达式参数名不能与方法参数名冲突
7. Vue/TS 中函数不能重复声明
