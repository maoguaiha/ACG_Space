# 上下文重点 - point

## 项目概述

ACG_Space 是一个动漫内容与数字谷子集换社区平台，采用 Java 17 + Spring Boot 3 + MyBatis-Plus 后端技术栈，Vue 3 + Element Plus + Pinia 管理端技术栈，Nuxt 3 + Tailwind CSS 用户端技术栈。核心功能包括抽赏中心、数字背包、合成工坊、实物兑换、订单管理、多主题系统、动效系统等模块。

## 规则要点与避坑

1. **SecurityConfig配置**：新增API路径必须添加到permitAll()列表，否则会返回403 Forbidden
2. **BaseEntity审计字段**：所有继承BaseEntity的实体类对应的表都必须包含审计字段（create_by, create_time, update_by, update_time, remark, del_flag）
3. **审计字段显式设置**：项目未配置全局MetaObjectHandler，所有Controller在新增/更新实体时必须显式设置createTime和updateTime字段，否则会导致数据插入异常
4. **Bean命名规范**：所有@Bean注解必须显式命名，防止BeanDefinitionOverrideException冲突
5. **幂等性保障**：所有RocketMQ消费者必须包含幂等性校验逻辑
6. **分布式锁安全**：使用Redisson锁时必须配合finally块确保释放
7. **reactive表单重置**：使用reactive对象时，应使用显式赋值重置各字段，避免使用Object.assign；使用isEditMode标志明确区分新增/编辑模式
8. **图片字段长度**：存储base64图片数据的字段应使用longtext或mediumtext类型，varchar(500)会导致数据截断
9. **全局事件监听器**：应在组件onMounted时注册，onUnmounted时移除，确保整个组件生命周期内可用
10. **雪花ID精度丢失**：后端使用雪花算法生成的19位Long类型ID，必须通过Fastjson2的WriteLongAsString配置序列化为字符串返回前端，前端提交时保持字符串类型，禁止使用Number()转换，否则会导致精度丢失（JavaScript Number安全整数范围仅16位）
11. **数据库字段约束**：新增订单相关字段时，注意将历史遗留的NOT NULL字段改为允许NULL，避免插入时报"Field doesn't have a default value"错误
12. **Nuxt中间件**：使用auth中间件保护的页面，必须确保middleware/auth.ts文件存在且正确导出，否则会报"Middleware not found"错误
13. **JWT过滤器**：JwtAuthenticationTokenFilter.shouldNotFilter() 中若公开路径带 Authorization header，不能跳过过滤链，否则SecurityUtils.getUserId()返回null
14. **MyBatis-Plus updateById**：updateById 设置所有非null字段，涉及密码/统计字段时，须先设为null防止覆盖
15. **列表延迟动画**：v-for 使用 stagger-item 动画时更新列表需确保 key 唯一且不变，否则 TransitionGroup 可能无法正确识别进入/离开元素

## 当前进度

### V2.1 功能完成情况（2026-07-28）
- ✅ 前端动效系统（页面过渡 / 滚动显现 / 卡片阶梯FadeUp / 列表FLIP / Tab滑动 / 按钮微交互）
- ✅ 多主题系统完善（星空蓝统一 / 弹窗 / 按钮 / 标签/输入框全CSS变量化）
- ✅ 图片裁剪组件 ImageCropperUploader（vue-cropper，支持多比例/圆形/v-model）
- ✅ 系统自动通知（审核通过/拒绝/删除文章/删除评论/订单创建）
- ✅ 商品详情页 / 订单详情页 / 省市县三级联动
- ✅ 0库存自动下架 / 头像列长度修复 / 评论表补列
- ✅ 点赞持久化终极修复（Fastjson2 + JWT filter + delFlag）
- ✅ 文章审核驳回原因输入（管理端两个页面）

### 测试设施修复
- ✅ test profile 5个测试类全部通过（45用例，0失败）
- ✅ InMemoryValueOperations 替代 Mockito raw-type mock

### Agent 应用（用户端 AI 助手）
- ✅ 完成详细设计方案：`document/develop/V2/4.Agent应用设计方案.md`
- ✅ 范围：用户端对话式助手 + 云端大模型 API + 纯 RAG 问答（不执行写操作）
- ✅ 待确认：①供应商 ②初始语料来源 ③是否V1.1管理端 ④番剧库全量向量化评估

## 待完成

1. **P0优先级**：测试完整兑换流程（商品查询→UR碎片扣除→订单创建→前端反馈）
2. **P1优先级**：完善订单详情页物流信息展示  
3. **P1优先级**：优化兑换商品列表分页和筛选功能
4. **P2优先级**：完善DTO参数校验注解
5. **P2优先级**：补充单元测试用例
6. **Agent 应用（新增）**：确认供应商与语料来源后，按 `4.Agent应用设计方案.md` 进入 Phase 0~7 实现
