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

### V3.0 功能完成情况（2026-07-28）
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
- ✅ 完成详细设计方案 V1.1：`document/develop/V2/4.Agent应用设计方案.md`
- ✅ 形态：**纯 Python agent 服务嵌入项目**，Java 守门面（鉴权+持久化+SSE代理），技术栈"加 Python"不"改 Java"
- ✅ 能力：RAG 问答 + 一个**只读 Bangumi 查询工具（function calling）**，不执行写操作
- ✅ 语料：平台规则文档 + 手写 FAQ + 本地番剧快照（70 条，随平台增长重导出）
- ✅ 番剧推荐：从 **Bangumi 实时拉取**（镜像 `bgmapi.anibt.net`，仅 GET+User-Agent 无密钥，与后端 `bangumi.api.*` 一致），与平台数据解耦，支撑未来扩充
- ✅ 已锁定：用户选语料方案②（规则+FAQ+番剧快照）+ 番剧推荐走 Bangumi
- ✅ 最终决策：①供应商=LongCat(chat)+通义(embedding)（双供应商；LongCat 无对外 embedding，故 embed 另配通义）②前端入口=A(Nuxt→Java门面→Python) ③管理端延后 ④番剧快照=脚本一次性+可重跑
- ✅ Phase 0 已落地：python-agent 服务骨架（FastAPI + config/schemas/main 桩 + Dockerfile + .env.example）+ docker-compose 接入 + .gitignore 排除 .env
- ✅ Phase 1 已落地：corpus/rules/PRD_V2.md（复制 PRD）+ corpus/faq.md（10 组高频问答）+ scripts/export_anime.py（只读导出，含 `--selftest` 字段映射自测通过）；anime.json 待 MySQL 可达时生成（沙箱 docker 守护进程未起，13306 不可达）
- ✅ Phase 2 已落地：app/rag.py（按 `## ` 切片 + 余弦检索，anime.json 缺失容错）+ app/llm.py（LongCat 流式 chat / 通义 embed）+ app/prompts.py（角色+安全边界+来源署名）；main.py /chat 接入「检索→拼提示→流式」。离线自测全过（rag 检索命中 + TestClient SSE 全链路）；live LLM 需 API Key。
- ✅ Phase 3 已落地：app/tools/bangumi.py（search/detail/calendar 只读工具，仅 GET+UA、TTL 缓存，按真实镜像结构解析）+ app/tools/registry.py（3 个 function-calling schema）；main.py /chat 接入工具循环（首轮带 tools→执行→回填→流式）。离线自测全过 + 真实 Bangumi 冒烟通过。

## 待完成

1. **P0优先级**：测试完整兑换流程（商品查询→UR碎片扣除→订单创建→前端反馈）
2. **P1优先级**：完善订单详情页物流信息展示  
3. **P1优先级**：优化兑换商品列表分页和筛选功能
4. **P2优先级**：完善DTO参数校验注解
5. **P2优先级**：补充单元测试用例
6. **Agent 应用（新增）**：设计已锁定（Python agent 嵌入 + Bangumi 只读工具）。Phase 0~3 已落地（骨架 + 语料/导出脚本 + RAG 流式 + Bangumi 工具）；下一步 **Phase 4**（Java 门面：AgentController SSE 代理 + 会话/消息持久化 + Resilience4j 限流）
