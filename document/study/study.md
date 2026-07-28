# ACG_Space 学习记录 - Study

## 错误记录

### 已修复问题

- [2026-05-07] 错误：RedissonClient Bean未显式命名 | 原因：配置类中@Bean注解缺少名称属性 | 防范：所有@Bean注解必须显式命名，如@Bean("redissonClient")
- [2026-05-07] 错误：IdempotentInterceptor使用ObjectMapper | 原因：项目要求使用Fastjson2处理JSON | 防范：统一使用Fastjson2进行JSON序列化/反序列化
- [2026-05-08] 错误：注册积分可多次点击领取且显示成功但积分未增长 | 原因：BizMessageServiceImpl.claimRegistrationBonus方法未正确处理底层awardRegistrationBonus的返回值，总是返回true | 防范：方法调用链必须正确传递返回值，尤其是幂等性检查结果
- [2026-05-08] 错误：Lua脚本执行失败"arguments must be strings or integers" | 原因：LuaScriptExecutor使用了acgRedisTemplate（Fastjson2序列化器），导致Lua脚本参数被序列化成JSON字节数组而非纯字符串 | 防范：Lua脚本执行必须使用luaRedisTemplate（String序列化器），确保参数类型正确
- [2026-05-08] 错误：点击领取积分弹出多个弹窗（3-4个） | 原因：前端setupClaimBonusHandler在onMounted中每次都添加新的事件监听器但未在组件卸载时移除，导致监听器累积 | 防范：事件监听器必须在onUnmounted中移除，或使用Vue的事件处理机制；添加防抖标志防止重复请求
- [2026-05-08] 错误：GET /api/fragment/my 返回403 Forbidden | 原因：SecurityConfig中未配置/api/fragment/**路径的permitAll()权限 | 防范：新增API路径必须在SecurityConfig中添加到permitAll()列表，否则会被Spring Security拦截
- [2026-05-08] 错误：Unknown column 'create_by' in 'field list' for biz_user_fragment | 原因：实体类BizUserFragment继承了BaseEntity（包含create_by, update_by, remark字段），但数据库表缺少这些字段 | 防范：所有继承BaseEntity的实体类对应的表都必须包含审计字段（create_by, create_time, update_by, update_time, remark, del_flag）
- [2026-05-08] 错误：背包页面显示{ "code": 200, "data": 0, "msg": "操作成功" } | 原因：前端直接使用$fetch返回值，未正确解析Result包装的响应结构 | 防范：所有API调用必须正确解析Result<T>结构，从response.data中获取实际数据
- [2026-05-08] 错误：抽奖后背包无物品 | 原因：createUserAsset方法未设置isPhysical字段，导致数据库插入失败 | 防范：实体类所有必填字段必须在保存前设置，尤其是数据库NOT NULL字段
- [2026-05-08] 错误：背包物品无边框颜色 | 原因：getRarityBorderClass函数只返回hover效果，未设置实际边框 | 防范：稀有度颜色显示必须包含border样式，确保视觉上可区分不同稀有度
- [2026-05-09] 错误：后台兑换商品新增商品后后台无记录 | 原因：BizAdminRedeemProductController的create方法未设置createTime和updateTime字段，项目中未配置全局MetaObjectHandler自动填充 | 防范：所有继承BaseEntity的实体在新增时必须显式设置createTime和updateTime字段，或配置MybatisPlusConfig中的MetaObjectHandler全局自动填充
- [2026-05-09] 错误：新增商品显示"更新成功"且列表不显示 | 原因：使用Object.assign重置reactive表单时id字段未正确重置为undefined，导致新增模式误判为编辑模式 | 防范：使用reactive对象时，应使用显式赋值重置各字段，避免使用Object.assign；使用isEditMode标志明确区分新增/编辑模式
- [2026-05-09] 错误：图片粘贴功能点击后无效 | 原因：粘贴事件监听器只在点击按钮后临时注册，用户体验差且容易失效 | 防范：全局事件监听器应在组件onMounted时注册，onUnmounted时移除，确保整个组件生命周期内可用
- [2026-05-09] 错误：商品封面图片显示不正确 | 原因：数据库biz_redeem_product表image字段为varchar(500)，无法存储完整的base64图片数据（通常超过500字符） | 防范：图片字段应使用longtext或mediumtext类型，或采用文件上传服务存储图片URL而非base64数据
- [2026-05-09] 错误：兑换商品时显示"商品不存在"但实际商品已上架 | 原因：后端雪花ID(19位Long)通过Fastjson2 WriteLongAsString返回前端为字符串，前端使用Number()转换导致精度丢失(JavaScript Number.MAX_SAFE_INTEGER仅16位) | 防范：前端提交时保持字符串类型，后端接收时使用Object类型并手动解析为Long
- [2026-05-09] 错误：兑换订单插入失败"Field 'asset_id' doesn't have a default value" | 原因：biz_redeem_order表复用了抽卡订单结构，asset_id/item_id/item_name字段为NOT NULL但兑换订单不需要这些字段 | 防范：复用表结构时需将不适用的NOT NULL字段改为允许NULL
- [2026-05-09] 错误：兑换订单插入失败"Unknown column 'product_id' in 'field list'" | 原因：数据库表缺少商品相关字段(product_id/product_name/product_image/ur_fragment_cost/points_cost) | 防范：新增业务字段时必须同步执行数据库迁移脚本
- [2026-05-09] 错误：用户主页订单链接点不开 | 原因：订单页面指定了auth中间件但middleware/auth.ts文件不存在 | 防范：使用middleware时必须确保对应文件存在且正确导出

### 待修复问题

- [2026-05-07] 错误：数据库默认密码硬编码 | 原因：application-dev.yml中配置了默认密码 | 防范：生产环境强制使用环境变量覆盖
- [2026-05-07] 错误：部分DTO缺少参数校验注解 | 原因：Controller方法参数未添加@Validated注解 | 防范：所有DTO字段必须添加校验注解
- [2026-07-27] 错误：test profile 下 RedisTemplate.opsForValue().get 抛 ClassCastException(String cannot be cast to [C) | 原因：用 Mockito 对 raw-type 的 ValueOperations 做 get/set 的 stub，触发泛型桥方法的错误强转 | 防范：测试中需要对泛型返回类型（如 ValueOperations.get）做有状态 stub 时，不要用 raw-type Mockito mock，应提供具体的有状态实现类（如 InMemoryValueOperations）替代，彻底规避泛型桥方法强转
- [2026-07-27] 错误：BaseMapper.selectOne 的 stub 报 PotentialStubbingProblem / InvalidUseOfMatchersException(NullPointerException Boolean.booleanValue) | 原因：MyBatis-Plus 的 getOne 内部调用 selectOne(wrapper, true)，第二参是 primitive boolean；用 selectOne(any(), any()) 时 any() 返回 null 无法拆箱为 boolean，且 selectOne(any()) 单参与双参调用不匹配 | 防范：对 primitive 参数必须用 anyBoolean()/anyInt() 等原始类型 matcher，且 stub 的参数量必须与真实调用完全一致

## 代码审查记录

### 审查概览

| 序号 | 模块 | 审查日期 | 审查人 | 状态 |
|------|------|----------|--------|------|
| 1 | 抽赏模块 | 2026-05-07 | 系统 | ✅ 通过 |
| 2 | 合成模块 | 2026-05-07 | 系统 | ✅ 通过 |
| 3 | 交易模块 | 2026-05-07 | 系统 | ✅ 通过 |
| 4 | 消息队列 | 2026-05-07 | 系统 | ✅ 通过 |
| 5 | 配置类 | 2026-05-07 | 系统 | ⚠️ 待修复 |

### 详细审查记录

#### 抽赏模块审查

| 检查项 | 状态 | 说明 |
|--------|------|------|
| Lua脚本原子性 | ✅ 通过 | 库存扣减与积分扣减在一个原子操作中 |
| 幂等性保障 | ✅ 通过 | 使用@Idempotent注解防止重复提交 |
| 限流熔断 | ✅ 通过 | 配置了Resilience4j限流 |
| 参数校验 | ✅ 通过 | Controller使用@Validated注解 |

#### 合成模块审查

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 分布式锁 | ✅ 通过 | 使用Redisson MultiLock |
| 锁释放机制 | ✅ 通过 | finally块确保锁释放 |
| 库存校验 | ✅ 通过 | 合成前检查材料数量 |
| 异常处理 | ✅ 通过 | 完整的try-catch-finally结构 |

#### 交易模块审查

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 事务消息 | ✅ 通过 | RocketMQ事务消息保证最终一致性 |
| 幂等性校验 | ✅ 通过 | 消费者检查订单是否已处理 |
| 交易日志 | ✅ 通过 | 记录所有交易状态变更 |
| 回查机制 | ✅ 通过 | 支持事务回查确保一致性 |

#### 配置类审查

| 检查项 | 状态 | 说明 |
|--------|------|------|
| Bean命名 | ⚠️ 待修复 | RedissonClient未显式命名 |
| JSON配置 | ⚠️ 待修复 | IdempotentInterceptor使用ObjectMapper |
| 环境隔离 | ✅ 通过 | 敏感配置使用环境变量 |

## 代码优化建议

### 性能优化

1. **缓存优化**：抽赏结果可考虑使用Redis缓存热门奖池数据
2. **批量操作**：合成记录可批量插入以提升性能
3. **异步处理**：非核心业务逻辑可异步执行

### 代码质量

1. **DTO校验增强**：增加更多字段校验注解
2. **异常信息增强**：提供更详细的错误信息
3. **日志增强**：增加关键业务节点的日志记录

### 安全性

1. **输入验证**：加强用户输入验证防止注入攻击
2. **权限校验**：确保接口权限校验完整
3. **敏感数据脱敏**：日志中敏感信息需脱敏处理

---

## 复盘回顾（2026-07-28）

### 一、点赞持久化 — 五层嵌套根因

| 层级 | 问题 | 修复 | 为何绕了 10+ 轮 |
|------|------|------|----------------|
| L1 | Fastjson2 `BrowserCompatible` 序列化时省略 null 字段，`Result.success(null)` 输出 `{"code":200}` 无 data | controller 返回 `Result.success(0)` | 每修完一层，`data:0` 的现象仍然成立，无法通过现象判断修到了哪一层 |
| L2 | `JwtAuthenticationTokenFilter.shouldNotFilter()` 对 GET 公开路径返回 true → 即使带了 Authorization header 也不解析 Token → `SecurityUtils.getUserId()=null` | shouldNotFilter 中增加 Authorization header 检测，有 Token 时不跳过 | 前 6 轮只看前端 Network，没查后端 SQL 日志。如果第 1 轮就查 `selectOneWithDeleted` 参数，会发现 `userId=null` |
| L3 | `new BizArticleReaction()` 时 `delFlag` 默认 `null`，MyBatis-Plus `@TableLogic` 自动加 `del_flag=0` 条件 → `null != 0` → 查询返回空 | `reaction.setDelFlag(0)` + `selectOneWithDeleted` 绕过过滤 | 数据库记录存在但被逻辑删除过滤，"存在又找不到"是最难排查的状态 |
| L4 | Vue 3 `ref<Map>` 对 `Map.set()` 不触发响应式更新 | 改用 `ref<Record<string,number>>` + `value[key]=val` | `reactive(Map)` → `Map.set()` 在某些场景仍不触发重渲染，只有 `ref<Record>` 语义最明确 |
| L5 | `onMounted` 中 `if (!article.value?.id) return` 在 SSR 水合阶段 `article.value` 尚未就绪，跳过 API 调用 | 移除守卫 + `watch(isLoggedIn)` 自动重载 | SSR 水合时序问题是 Nuxt 3 的经典陷阱 |

**核心教训：** 数据不撒谎，但我不看。第 1 轮就去查后端 SQL 日志的 `selectOneWithDeleted` 参数，会发现 `userId=null`，直接定位到 JWT 过滤器。绕了 6 轮才看日志。

### 二、502 健康检查 — SSR 时序陷阱

| 环节 | 细节 |
|------|------|
| **现象** | Railway 部署 front-ui 后，健康检查 `/health` 反复 502，部署失败 |
| **根因** | 首页 SSR 渲染时 `useAsyncData` 调用后端 API，但 `useApi.ts` 没有设 `timeout`。后端部署中重启的瞬间不可达 → SSR 请求无限 hanging → health check 120s 超时 → Railway 判定不健康 |
| **修复** | `useApi.ts` 加 `timeout: 8000` + `retry: 0`；`index.vue` 的 `useAsyncData` 加 `.catch()` 兜底 |
| **教训** | SSR 阶段的所有 API 调用必须设超时。Health check endpoint 本身也要避免依赖外部服务 |

### 三、Railway 部署踩坑

| 坑 | 表现 | 原因 | 对策 |
|----|------|------|------|
| 双服务独立部署 | 改了后端，部署前端，Bug 还在 | Railway 上后端和 front-ui 是两个独立项目 | 改代码后确认改了哪个模块，两个都要 Redeploy |
| git push 认证失败 | commit 成功但 push 失败 | 沙箱环境无法交互输密码 | 本地终端 `git push`，建议配 SSH Key 免密推送 |
| 健康检查路径踩坑 | `/health` 返回 502 | front-ui 的 health 检查踩到 SSR API 无限 hanging | 首页 SSR 必须超时兜底，不能依赖外部服务可用性 |

### 四、试错模式总结

**为什么点赞持久化绕了 10+ 轮？**

```
层1: Fastjson2 省略 data 字段     → 修了，data:0 还在
层2: JWT 跳过 Token             → 修了，data:0 还在
层3: delFlag = null             → 修了，data:0 还在
层4: Map.set() 不响应           → 修了，data:0 还在
层5: onMounted 守卫提前 return   → 修了，终于 work
```

**每修一层，data: 0 这个现象都仍然成立**——无法通过现象判断修到了哪一层。只有直接查 SQL 日志才能穿透表象。

**如果能重来：**

1. 第 1 轮看到 `data: 0` → 查后端 SQL 日志 → 发现 `selectOneWithDeleted userId=null`
2. 定位 JWT 过滤器 → 修复
3. 同时检查数据库 `delFlag` 值 → 发现 `null`
4. 补 `setDelFlag(0)` → 修复
5. 确认前端 ref 响应式 → 改成 `ref<Record>`
6. 确认 onMounted 时序 → 移除守卫

**一个 commit 修完所有层，而不是 10 轮试错。**
