# 学习记录 - study

## 2026-05-03

### 1. 编译错误修复 - AjaxResult 和 javax.validation
**问题**：
- 找不到 `com.ruoyi.common.core.domain.AjaxResult` 包
- 找不到 `javax.validation.constraints` 包

**原因**：
1. 项目使用 `Result<T>` 而非 `AjaxResult`
2. Spring Boot 3.x 使用 `jakarta.validation` 而非 `javax.validation`

**修复**：
- `AdminArticleController.java` - AjaxResult → Result
- `UserArticleController.java` - AjaxResult → Result
- `ArticleCreateDTO.java` - javax.validation → jakarta.validation

**教训**：遇到不存在的类时，先查看项目中已有的类似文件（如 AuthController），找到正确的类名和包名。

### 2. 实体类字段缺失 - rejectReason
**问题**：
- `BizArticle` 实体类缺少 `rejectReason` 字段
- 导致 `AdminArticleController.java` 调用 `setRejectReason()` 编译失败

**原因**：
- 数据库 `biz_article` 表有 `reject_reason` 字段（V1.1 迁移脚本添加）
- 但实体类 `BizArticle.java` 没有同步添加对应字段

**修复**：
- 在 `BizArticle.java` 中添加 `private String rejectReason;` 字段

**教训**：
1. 每次修改数据库表结构后（尤其是迁移SQL），必须同步更新对应的 Java 实体类
2. 养成习惯：查看数据库 schema 时，同步检查实体类字段是否完整
3. 可以通过查看 SQL 迁移脚本（如 `v1.1_article_migration.sql`）确认数据库新增了哪些字段

### 3. Vue 模板中不能使用模板字符串语法
**问题**：
- Vue 模板中三元表达式使用反引号导致编译错误
- 错误信息：`Error parsing JavaScript expression: Unexpected token`

**错误代码**：
```vue
:to="comment.type === 1 ? `/anime/${comment.targetId}` : `/article/${comment.targetId}`"
```

**正确代码**：
```vue
:to="comment.type === 1 ? '/anime/' + comment.targetId : '/article/' + comment.targetId"
```

**教训**：Vue 模板中不支持 ES6 模板字符串，必须使用字符串拼接。

### 4. LocalDateTime 类型转换
**问题**：
- `UserCommentVO.createTime` 定义为 `String`，但实体类是 `LocalDateTime`
- 导致编译错误：不兼容的类型

**修复**：
- 将 `UserCommentVO.createTime` 类型改为 `LocalDateTime`

**教训**：实体类字段类型必须与 DTO/VO 保持一致，JSON 序列化会自动处理 LocalDateTime。

### 5. 实体类字段必须与数据库表结构同步
**问题**：
- `BizComment` 和 `BizArticleComment` 实体缺少 `dislikes` 字段
- 导致点踩计数无法正确更新

**原因**：
- 数据库表已有存储点踩数据的能力（通过 `biz_comment_reaction` 表）
- 但评论实体类没有对应的 `dislikes` 字段来记录汇总计数

**修复**：
- 在 `BizComment.java` 和 `BizArticleComment.java` 中添加 `private Integer dislikes;` 字段
- 在 `BizCommentReactionServiceImpl.java` 中添加对 `reactionType == 2` 的处理
- 创建 `v1.2_comment_dislikes_migration.sql` 迁移脚本

**教训**：
1. 当需要统计某种行为的次数时，除了反应表，还需要确保实体类有对应的计数字段
2. 修改实体类后，需要同步创建数据库迁移脚本
3. 迁移脚本命名规范：`v{version}_{功能描述}_migration.sql`

### 6. 点赞/点踩状态持久化 - 需要获取当前用户反应状态
**问题**：
- 用户点赞后刷新页面，点赞状态丢失（按钮恢复到未点赞状态）
- 点踩/点赞按钮刷新后可以重新操作

**原因**：
- 前端只是根据当前操作的即时反馈更新UI
- 没有在页面加载时从后端获取用户对该文章/评论的历史反应状态

**修复**：
1. 后端添加专门的接口获取用户反应状态：
   - `BizArticleController` 添加 `GET /article/{id}/reaction-status`
   - `BizCommentController` 添加 `GET /comment/{id}/reaction-status`
   - `BizArticleCommentController` 添加 `GET /article/comment/{id}/reaction-status`
2. Service 层添加 `getReactionStatus(articleId, userId)` 方法
3. 前端页面加载后调用这些接口获取状态并存储到响应式变量
4. 操作按钮时根据状态决定是新增还是删除反应

**教训**：
1. 涉及用户状态的交互功能，必须考虑持久化
2. 页面加载时需要从后端获取当前用户的完整状态
3. 不能只依赖前端即时操作的状态反馈

### 7. 缺失 import 语句 - 常见编译错误模式
**问题**：
- `找不到符号 变量 SecurityUtils`
- `找不到符号 符号: 变量 XXX`
- Controller/Service 中使用某个工具类但编译器报错

**原因**：
1. Java 项目中使用了某个类，但没有添加对应的 import 语句
2. 从其他文件复制代码时容易遗漏 import

**常见需要手动导入的类**：
- `SecurityUtils` → `import com.ruoyi.project.common.utils.SecurityUtils;`
- `Result` → `import com.ruoyi.project.common.api.Result;`

**修复**：
1. 根据错误信息确定缺少的类
2. 查找项目中其他文件是如何导入这个类的
3. 添加对应的 import 语句

**教训**：
1. 复制粘贴代码后一定要检查 import 语句
2. 新增使用某个类的方法时，先确认该类是否已导入
3. 善用 IDE 的自动导入功能

**预防方法**：
- 每次新增方法调用时，检查是否需要添加 import
- 可以创建一个常用导入的检查清单

### 8. 前端重复点击问题 - 需要添加 loading 状态
**问题**：
- 用户快速连续点击点赞按钮，点赞数量可以一直增加
- 原因：按钮没有禁用状态，API 请求是异步的但 UI 没有防抖

**修复**：
1. 添加 `reactionLoading` 状态变量
2. 按钮添加 `:disabled="reactionLoading"` 属性
3. 操作函数开始时检查 loading 状态

**教训**：
1. 所有可能产生异步操作的用户交互都需要防抖处理
2. 按钮应该在操作进行中时禁用
3. 可以在后端也做幂等性校验（后端已经有了），但前端防抖能提供更好的用户体验

### 10. Vue/TS 变量未定义就使用 - 常见运行时错误
**问题**：
- `Cannot read properties of undefined (reading 'get')`
- `ReferenceError: reactionLoading is not defined`
- 模板或函数中使用了某个响应式变量，但该变量没有被 `ref()` 或 `reactive()` 定义

**原因**：
1. 代码经过多次迭代修改，变量定义被遗漏
2. 从其他文件复制代码时只复制了使用部分，忘记复制定义部分
3. 重构时删除了变量定义但忘记更新引用

**常见遗漏的变量**：
- `reactionLoading` - 控制点赞/点踩按钮禁用状态
- `articleReaction` - 存储当前文章的反应状态（1点赞/2点踩/null无状态）
- `commentReactionStatus` - Map类型，存储评论的反应状态

**修复**：
1. 确认变量用途
2. 添加对应的响应式定义，如：
   ```typescript
   const reactionLoading = ref(false)
   const articleReaction = ref<number | null>(null)
   const commentReactionStatus = ref<Map<string, number>>(new Map())
   ```

**教训**：
1. 复制粘贴代码后一定要检查变量定义是否完整
2. 重构删除变量时，使用 IDE 的 "Find Usages" 功能检查所有引用
3. 在使用 ref 类型时，模板中可以直接用 `reactionLoading`，但 JS 中需要 `reactionLoading.value`
4. 新增变量时，同时检查模板中的使用是否匹配

**预防方法**：
- 添加新变量时，同时在变量定义处注释说明用途
- 使用有意义的变量名，如 `articleReactionStatus` 比 `status` 更清晰

### 11. VO 字段缺失导致前端无法显示数据
**问题**：
- 评论点踩后计数不增加
- 前端显示 `dislikes` 始终为 0

**原因**：
1. `ArticleCommentVO` 类缺少 `dislikes` 字段
2. `toVO` 方法没有设置 `dislikes` 值
3. 虽然后端服务正确更新了数据库的 `dislikes` 字段，但 VO 没有传递到前端

**修复**：
1. 在 `ArticleCommentVO.java` 中添加 `private Integer dislikes;` 字段
2. 在 `toVO` 方法中添加 `vo.setDislikes(comment.getDislikes());`

**教训**：
1. VO/DTO 必须包含所有需要传递到前端的字段
2. 添加实体类字段后，务必同步更新对应的 VO 类
3. 使用 Lombok 的 `@Data` 注解时，确保所有字段都被正确复制

### 12. 开发前必读
**开始开发前，先阅读 `document/study/study.md`，避免重蹈覆辙！**

### 13. 社区页分类筛选功能开发
**需求**：社区页需要添加文章分类筛选功能，用户可以按分类过滤文章列表。

**实现步骤**：
1. **后端**：
   - 在 `BizArticleMapper.java` 中添加 `selectAllCategories()` 方法，使用 `@Select` 注解执行 SQL 查询
   - 在 `IBizArticleService.java` 中添加 `getAllCategories()` 接口方法
   - 在 `BizArticleServiceImpl.java` 中实现该方法
   - 在 `BizArticleController.java` 中添加 `GET /article/categories` 接口
   - 注意添加 `import java.util.List;` 导入语句

2. **前端**：
   - 在 `useApi.ts` 中添加 `fetchArticleCategories()` API 函数
   - 在社区页 `community.vue` 中：
     - 添加 `categories` 和 `selectedCategory` 响应式变量
     - 添加 `loadCategories()` 和 `switchCategory()` 函数
     - 在模板中添加分类筛选按钮组
     - 修改 `loadPage()` 函数，传入 `category` 参数

**注意事项**：
- 分类筛选只在文章标签页显示
- 分类列表需要在页面加载时获取
- 切换分类时需要重置页码为 1
- 使用 `flex-wrap` 确保分类过多时自动换行

**文件清单**：
- `backend/src/main/java/com/ruoyi/project/mapper/BizArticleMapper.java`
- `backend/src/main/java/com/ruoyi/project/service/IBizArticleService.java`
- `backend/src/main/java/com/ruoyi/project/service/impl/BizArticleServiceImpl.java`
- `backend/src/main/java/com/ruoyi/project/controller/BizArticleController.java`
- `front-ui/composables/useApi.ts`
- `front-ui/pages/community.vue`

### 14. 社区页文章条目大小一致，图片截取
**需求**：社区页的文章条目大小一致，太大的图片进行截取显示，与后台管理文章的图片截取操作一致。

**实现方式**：
1. 给每个文章条目添加固定高度 `h-32`
2. 图片容器设置固定尺寸：桌面端 `md:w-48`，移动端 `w-full`，高度统一 `h-32`
3. 图片使用 `object-cover` 样式进行截取，类似后台的 `fit="cover"` 效果
4. 添加无封面图时的默认占位符，显示 SVG 图标
5. 内容区域使用 `flex flex-col justify-center` 垂直居中，`h-32` 确保高度一致
6. 移动端布局保持 `flex-col`，桌面端 `flex-row`

**注意事项**：
- 图片已经使用 `object-cover` 样式，无需额外修改
- 高度固定为 `h-32`，所有文章条目高度一致
- 添加无封面图时的占位符，保证布局完整性
- 移动端和桌面端布局适配

**文件修改**：
- `front-ui/pages/community.vue`

### 15. 评论区回复点赞/踩/回复功能
**需求**：评论区其他人的回复也要有点赞、踩、回复功能

**实现方式**：
1. 在回复区域添加点赞、踩、回复按钮
2. 使用 `replyReactionStatus` Map 存储回复的反应状态
3. 添加 `handleReplyReaction()` 处理回复的点赞/踩
4. 添加 `toggleReplyToReply()` 切换回复输入框显示
5. 添加 `submitReplyToReply()` 提交回复回复

**BUG修复** - 点赞踩互斥问题：
**问题**：先点踩后点赞，踩没有取消；已点赞再点踩，踩数量增加
**原因**：`performReplyReaction()` 函数只处理了当前反应类型的计数变化，没有处理之前反应类型的计数回退
**修复**：在切换反应类型时，先取消之前的计数，再应用新的计数

**文件修改**：
- `front-ui/pages/article/[id].vue`

### 16. 文章发布功能完善
**需求**：
1. 标签可以选择性添加，支持预定义标签池 + 自定义输入
2. 封面添加要跟后端管理一样，点击时有复制图像/本地选择选项，选中后要裁剪
3. 文章内容支持富文本 + Markdown + 文件上传解析

**创建组件**：

#### TagSelector.vue
**功能**：标签选择组件
- 支持预定义标签池显示和选择
- 支持用户自定义输入创建新标签
- 支持多选，标签以逗号分隔存储
- 已选标签显示为带删除按钮的标签块

**文件位置**：`front-ui/components/TagSelector.vue`

#### CoverUploader.vue
**功能**：封面图片上传裁剪组件
- 参考 admin-ui 的 CoverImageUploader.vue 实现
- 点击添加封面时显示选项菜单（复制图像/本地选择）
- 使用 Canvas 实现图片裁剪（16:9比例 640x360）
- 支持拖拽调整位置
- 支持左/右旋转
- 支持缩放（50%-200%）
- 输出 base64 格式

**文件位置**：`front-ui/components/CoverUploader.vue`

#### RichTextEditor.vue
**功能**：富文本/Markdown 编辑器
- 支持富文本和 Markdown 两种编辑模式切换
- 富文本模式功能：
  - 加粗、斜体、下划线
  - 无序列表、有序列表、引用、标题
  - 插入链接、图片（支持粘贴上传）
- Markdown 模式功能：
  - 左侧编辑区 + 右侧预览区
  - 实时渲染 Markdown 语法
  - Tab 键插入空格
- 文件解析功能：上传 .txt/.md 文件自动填充内容

**文件位置**：`front-ui/components/RichTextEditor.vue`

**修改文件**：
- `front-ui/pages/article/create.vue`

**注意事项**：
- 标签存储格式为逗号分隔的字符串（如 "新番推荐,深度解析"）
- 封面图片存储为 base64 格式
- 富文本编辑器内容存储为 HTML 格式
- Markdown 编辑器内容存储为 Markdown 原文格式

### 17. 私信功能开发
**需求**：实现用户间的私信功能

**后端实现**：
1. **数据库表** `biz_message`：
   - 字段：id, from_user_id, to_user_id, content, is_read, create_time
   - 迁移脚本：`v1.4_message_migration.sql`

2. **实体类**：`BizMessage.java`
   - 使用 `@TableId(type = IdType.ASSIGN_ID)` 雪花算法生成 ID

3. **Service 层**：
   - `IBizMessageService.java` - 接口定义
   - `BizMessageServiceImpl.java` - 实现
     - `sendMessage()` - 发送私信
     - `getConversation()` - 获取与某用户的聊天记录
     - `getConversationList()` - 获取会话列表
     - `markAsRead()` - 标记消息已读
     - `getUnreadCount()` - 获取未读消息数

4. **Controller 层**：`BizMessageController.java`
   - `POST /api/message/send` - 发送消息
   - `GET /api/message/conversation/{userId}` - 获取聊天记录
   - `GET /api/message/list` - 获取会话列表
   - `PUT /api/message/read/{userId}` - 标记已读
   - `GET /api/message/unread` - 获取未读数

**前端实现**：
1. **API 函数**（`useApi.ts`）：
   - `fetchConversationList()` - 获取会话列表
   - `fetchConversation(userId)` - 获取聊天记录
   - `sendMessage(toUserId, content)` - 发送消息
   - `markMessagesRead(userId)` - 标记已读
   - `fetchUnreadCount()` - 获取未读数

2. **页面**：
   - `/messages.vue` - 消息列表页面
   - `/message/[userId].vue` - 私信对话页面

3. **导航栏**：
   - 在 `layouts/default.vue` 添加信封图标
   - 显示未读消息数红点

**文件清单**：
- `backend/sql/v1.4_message_migration.sql`
- `backend/src/main/java/com/ruoyi/project/domain/entity/BizMessage.java`
- `backend/src/main/java/com/ruoyi/project/domain/vo/MessageVO.java`
- `backend/src/main/java/com/ruoyi/project/domain/vo/ConversationVO.java`
- `backend/src/main/java/com/ruoyi/project/domain/dto/MessageSendDTO.java`
- `backend/src/main/java/com/ruoyi/project/mapper/BizMessageMapper.java`
- `backend/src/main/java/com/ruoyi/project/service/IBizMessageService.java`
- `backend/src/main/java/com/ruoyi/project/service/impl/BizMessageServiceImpl.java`
- `backend/src/main/java/com/ruoyi/project/controller/BizMessageController.java`
- `front-ui/composables/useApi.ts`
- `front-ui/pages/messages.vue`
- `front-ui/pages/message/[userId].vue`
- `front-ui/layouts/default.vue`

### 18. 常见 NPE 空指针问题修复

#### 18.1 Java Stream Collectors.toMap 的 key 不能为 null
**问题**：`Collectors.toMap` 在 key 为 null 时抛出 NPE
**场景**：使用 `m.getFromUserId()` 作为 map 的 key 时，如果值为 null

**错误代码**：
```java
Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
    .collect(Collectors.toMap(SysUser::getId, u -> u));
```

**修复代码**：
```java
Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of() :
    userMapper.selectBatchIds(userIds).stream()
        .filter(u -> u.getId() != null)
        .collect(Collectors.toMap(SysUser::getId, u -> u));
```

#### 18.2 groupingBy 返回 null key
**问题**：`Collectors.groupingBy` 的分类函数返回 null 时，可能导致 NPE

**错误代码**：
```java
Map<Long, List<BizMessage>> grouped = messages.stream()
    .collect(Collectors.groupingBy(m -> {
        if (m.getFromUserId().equals(userId)) return m.getToUserId();
        return m.getFromUserId();
    }));
```

**修复代码**：
```java
Map<Long, List<BizMessage>> grouped = messages.stream()
    .collect(Collectors.groupingBy(m -> {
        if (m.getFromUserId() != null && m.getFromUserId().equals(userId)) return m.getToUserId();
        if (m.getToUserId() != null && m.getToUserId().equals(userId)) return m.getFromUserId();
        return null;
    }));
grouped.remove(null);
```

#### 18.3 Comparator.comparing 无法处理 null 值
**问题**：`Comparator.comparing` 在 key 为 null 时抛出 NPE

**错误代码**：
```java
allComments.sort(Comparator.comparing(UserCommentVO::getCreateTime).reversed());
```

**正确代码**：
```java
allComments.sort(Comparator.comparing(
    UserCommentVO::getCreateTime,
    Comparator.nullsLast(Comparator.reverseOrder())
));
```

**注意**：`Comparator.nullsLast(Comparator.comparing(...))` 是错误的写法！

#### 18.4 Lambda 参数名与方法参数名冲突
**问题**：Lambda 表达式的参数名与外部方法参数名相同时，编译器报错"已在方法中定义了变量"

**错误代码**：
```java
public void getUserComments(Long id, ...) {
    List<Long> animeIds = animeComments.stream()
        .map(BizComment::getAnimeId)
        .filter(id -> id != null)  // id 与方法参数冲突
        .toList();
}
```

**修复代码**：
```java
public void getUserComments(Long id, ...) {
    List<Long> animeIds = animeComments.stream()
        .map(BizComment::getAnimeId)
        .filter(animeId -> animeId != null)  // 使用不同的参数名
        .toList();
}
```

#### 18.5 缺失 import 语句
**问题**：找不到 `@Validated` 符号

**修复**：添加 import
```java
import org.springframework.validation.annotation.Validated;
```

### 19. Vue/TS 重复声明问题
**问题**：`Identifier 'showUserList' has already been declared`

**原因**：`showUserList` 函数在代码中被声明了两次

**修复**：删除重复的函数声明，保留正确的实现

**教训**：
1. 复制粘贴代码后要检查是否有重复声明
2. 使用 IDE 的 "Find" 功能搜索函数名确认唯一性
3. 重构时使用 IDE 的 "Rename" 功能避免遗漏
