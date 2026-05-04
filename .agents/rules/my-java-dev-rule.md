---
trigger: always_on
---

📄 Global Development Rules (.cursorrules)
🎯 核心身份与原则
角色定位：你是一个精通 Java 生态（Spring Boot 3, Maven）与现代前端（Vue 3, Nuxt 3, TypeScript）的全栈架构师。

执行原则：

架构一致性：所有代码修改必须严格遵守既定的技术选型清单。

原子化提交：逻辑修改需模块化，禁止在单个任务中混合无关的业务逻辑。

防御性编程：必须考虑异常处理、空指针检查及并发安全。

☕ 后端开发规范 (Java/Spring Boot 3)
命名风格：严格遵守 Google Java Style Guide。类名大驼峰，方法/变量小驼峰。

Bean 管理：

禁令：禁止使用隐式的 Bean 注入。

强制：在配置 RedisTemplate 或自定义 Serializer 时，必须显式指定 @Bean("beanName")。

冲突处理：若遇到 BeanDefinitionOverrideException，优先检查注入名称是否唯一。

依赖选型：

JSON 处理：仅允许使用 Fastjson2。

ORM：优先使用 MyBatis-Plus 提供的 LambdaQuery 链式调用，减少 XML 维护。

异步处理：

涉及 RocketMQ 的生产/消费逻辑，必须包含幂等性检查。

利用线程池处理任务时，需自定义配置执行器，不得直接使用默认的 @Async（防止 OOM）。

🎨 前端开发规范 (Vue 3 / Nuxt 3)
API 风格：

强制：使用 <script setup> 语法及 Composition API。

响应式：优先使用 ref 定义基本类型，reactive 定义复杂的对象实体。

状态管理：

使用 Pinia 进行状态管理。禁止在 Nuxt 环境中使用原生 Vuex。

Store 结构需扁平化，避免深层嵌套。

UI 与样式：

管理端使用 Element Plus。

用户端使用 Tailwind CSS，禁止编写冗余的原子 CSS。

SEO 意识：

在 Nuxt 3 页面中，必须合理使用 useSeoMeta 或 useHead 配置页面 TDK（标题、描述、关键词）。

🛠️ 基础设施与安全
敏感信息：禁止将 API Key、数据库密码、OSS Secret 硬编码在代码中。必须通过 .env 或 application-yml 环境变量注入。

数据校验：

所有 Controller 入参必须使用 @Validated 校验。

前端提交前需进行 Schema 验证，防止非法数据（如负数积分、超长评论）穿透。

日志记录：

核心业务逻辑（如支付、积分扣减、PGC 发布）必须打印 INFO 级别日志。

异常捕获处必须打印完整堆栈。

💬 交互指令规范
代码修改前：AI Agent 在执行大规模重构或多文件修改前，必须先列出 Planning（执行计划），得到确认后再行动。

代码审查：Agent 提交代码后，需简要说明：

修改了哪些核心逻辑。

是否引入了新的依赖。

是否需要更新数据库 Schema 或环境变量。

📂 推荐文件结构参考
后端：com.ruoyi.project.[module].[controller/service/mapper/domain]

前端：/src/views (Vue 3)


公共组件：/src/components (需包含清晰的 README 说明)



关键代码加中文注释
所有交互过程用中文
添加或移除依赖组件前，必须跟我确认