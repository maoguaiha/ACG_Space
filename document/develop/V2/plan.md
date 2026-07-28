# V3.0 开发计划

## 项目背景
ACG Space V3.0 版本，在 V2.0 数字谷子引擎基础上，新增多主题动效系统、图片裁剪组件、系统通知、商品/订单详情页等体验优化

## 技术栈
- 后端：Java 17 + Spring Boot 3 + MyBatis-Plus + Fastjson2
- 用户端：Nuxt 3 + Vue 3 + TailwindCSS + TypeScript
- 管理端：Vue 3 + Element Plus + TypeScript
- 消息队列：RocketMQ
- 分布式锁：Redisson

## V2.0→V3.0 功能模块

### 1. 用户端页面 (✅ 已完成)
| 页面 | 路径 | 状态 | 说明 |
|------|------|------|------|
| 抽赏中心 | /pages/gacha/index.vue | ✅ 已完成 | |
| 数字背包 | /pages/assets/index.vue | ✅ 已完成 | |
| 跳蚤市场 | /pages/market/index.vue | ✅ 已完成 | |
| 记忆工坊 | /pages/workshop/index.vue | ✅ 已完成 | 后端合成API已实现 |
| 地址管理 | /pages/address/index.vue | ✅ 已完成 | |
| 兑换中心 | /components/gacha/Redeem.vue | ✅ 已完成 | UR碎片/积分兑换实物商品 |
| 订单列表 | /pages/orders/index.vue | ✅ 已完成 | 查看兑换订单与物流信息 |
| 用户主页 | /pages/user/[id].vue | ✅ 已完成 | 新增订单入口 |

### 2. 管理端页面 (✅ 已完成)
| 页面 | 路径 | 状态 |
|------|------|------|
| 商品图鉴 | /views/item/index.vue | ✅ 已完成 |
| 抽赏配置 | /views/gacha/index.vue | ✅ 已完成 |
| 交易监控 | /views/transaction/index.vue | ✅ 已完成 |
| 物流调度 | /views/delivery/index.vue | ✅ 已完成 |
| 风控中心 | /views/risk-control/index.vue | ✅ 已完成 |
| 兑换商品管理 | /views/redeem-product/index.vue | ✅ 已完成 |
| 兑换订单管理 | /views/redeem-order/index.vue | ✅ 已完成 |

### 3. 数据库设计 (✅ 已完成)
- [x] 物品/商品表 (biz_item)
- [x] 用户资产表 (biz_user_asset)
- [x] 抽赏奖池表 (biz_gacha_pool)
- [x] 抽赏记录表 (biz_gacha_record)
- [x] 交易订单表 (biz_transaction)
- [x] O2O 核销订单表 (biz_delivery_order)
- [x] 用户地址表 (biz_user_address)
- [x] 事务日志回查表 (biz_transaction_log) - ✅ 已添加
- [x] 兑换商品表 (biz_redeem_product) - ✅ 已添加
- [x] 兑换订单表 (biz_redeem_order) - ✅ 已添加并扩展字段
- [x] 用户碎片表 (biz_user_fragment) - ✅ 已添加

### 4. 后端 API (✅ 已完成)
- [x] 物品管理 API (ItemController)
- [x] 抽赏 API (GachaController)
- [x] 交易 API (TransactionController)
- [x] 物流 API (DeliveryController)
- [x] 地址管理 API (AddressController)
- [x] 积分 API (PointsController) - ✅ 新增
- [x] 合成 API (SynthesizeController) - ✅ 新增
- [x] 碎片 API (FragmentController) - ✅ 新增
- [x] 兑换商品 API (BizRedeemProductController / BizAdminRedeemProductController) - ✅ 新增
- [x] 兑换订单 API (BizRedeemController) - ✅ 新增
- [x] 交易监控 API (BizTransactionLogController) - ✅ 新增

### 5. 前端 API 服务层 (✅ 已完成)
- [x] useV2Api.ts - V2.0 API 统一封装

### 6. 用户端页面 API 对接 (✅ 已完成)
- [x] 抽赏中心 (gacha) - 已对接
- [x] 数字背包 (assets) - 已对接
- [x] 跳蚤市场 (market) - 已对接
- [x] 地址管理 (address) - 已对接
- [x] 记忆工坊 (workshop) - 已对接后端合成API

### 7. 核心业务逻辑 (✅ 已完成)
- [x] RocketMQ 事务消息框架搭建
- [x] Redis Lua脚本防超卖 - ✅ 已实现
- [x] Redisson MultiLock多资产锁定 - ✅ 已实现
- [x] 抽赏保底机制完善 - ✅ 已实现
- [x] 资产合成逻辑完善 - ✅ 已实现
- [x] 积分系统实现 - ✅ 已实现
- [x] RocketMQ事务回查机制 - ✅ 已实现
- [x] Resilience4j限流熔断机制 - ✅ 已实现

### 8. 代码质量保障 (✅ 已完成)
- [x] Checkstyle代码风格检查 - ✅ 已配置
- [x] SpotBugs静态代码分析 - ✅ 已配置
- [x] PMD代码质量检查 - ✅ 已配置
- [x] Pre-commit Hooks - ✅ 已配置
- [x] 前端ESLint配置 - ✅ 已配置

### 9. 单元测试 (✅ 已完成)
- [x] 抽赏模块单元测试 - ✅ 已完成
- [x] 合成模块单元测试 - ✅ 已完成
- [x] 交易模块单元测试 - ✅ 已完成

---

## 待实现功能清单 (优先级排序)

### P0 - 高优先级（核心业务壁垒）
| 序号 | 功能 | 描述 | 关联模块 | 状态 |
|------|------|------|----------|------|
| 1 | Redis Lua防超卖 | 抽赏时原子化校验库存、扣减积分 | 抽赏中心 | ✅ 已完成 |
| 2 | Redisson MultiLock | 合成时同时锁定多个碎片资源 | 记忆工坊 | ✅ 已完成 |
| 3 | RocketMQ事务回查 | 市场交易最终一致性保障 | 跳蚤市场 | ✅ 已完成 |
| 4 | 积分系统 | 评论发奖、点赞发奖、签到 | 积分模块 | ✅ 已完成 |

### P1 - 中优先级（业务完整性）
| 序号 | 功能 | 描述 | 关联模块 | 状态 |
|------|------|------|----------|------|
| 5 | 抽赏保底机制完善 | 确保保底逻辑正确触发 | 抽赏中心 | ✅ 已完成 |
| 6 | 用户认证集成 | 替换硬编码用户ID | 全局 | ✅ 已完成 |
| 7 | 幂等性校验 | 防重复提交机制 | 全局 | ✅ 已完成 |
| 8 | 事务日志表 | 创建biz_transaction_log | 交易模块 | ✅ 已完成 |

### P2 - 低优先级（优化与体验）
| 序号 | 功能 | 描述 | 关联模块 | 状态 |
|------|------|------|----------|------|
| 9 | 限流熔断 | 接口限流保护 | 全局 | ✅ 已完成 |
| 10 | 管理端风控中心 | 监控MQ事务回查日志 | 管理端 | ✅ 已完成 |
| 11 | 代码质量保障 | Linter、Pre-commit Hooks | 全局 | ✅ 已完成 |
| 12 | 单元测试 | 核心模块测试用例 | 全局 | ✅ 已完成 |

---

## 已实现功能汇总

### 1. Redis Lua脚本防超卖
- 创建了 `lua/gacha_deduct_stock.lua` - 原子化扣减库存和积分
- 创建了 `lua/gacha_get_stock.lua` - 获取当前库存
- 创建了 `LuaScriptExecutor.java` - Lua脚本执行工具类
- 修改了 `BizGachaController.java` - 使用Lua脚本进行库存扣减

### 2. Redisson MultiLock
- 修改了 `BizSynthesizeServiceImpl.java` - 使用MultiLock同时锁定多个碎片资源

### 3. RocketMQ事务回查机制
- 创建了 `BizTransactionLog.java` - 事务日志实体
- 创建了 `BizTransactionLogMapper.java` - 事务日志Mapper
- 创建了 `IBizTransactionLogService.java` - 事务日志服务接口
- 创建了 `BizTransactionLogServiceImpl.java` - 事务日志服务实现
- 创建了 `TransactionLocalListener.java` - 本地事务监听器
- 修改了 `TransactionProducer.java` - 记录事务日志

### 4. 积分系统
- 创建了 `BizUserPointsLogServiceImpl.java` - 积分服务实现（含评论、点赞、签到）
- 创建了 `BizPointsController.java` - 积分API控制器

### 5. Resilience4j限流熔断
- 在 `pom.xml` 中添加了 Resilience4j 相关依赖
- 在 `application.yml` 中配置了熔断器和限流器规则
- 创建了 `@RateLimiterAndCircuitBreaker` 注解
- 创建了 `RateLimiterAndCircuitBreakerAspect` 切面
- 在关键接口上应用注解：抽赏、市场购买、合成
- 创建了 `AdminRiskControlController.java` - 风控中心API

### 6. 管理端风控中心
- 创建了 `admin-ui/src/api/risk-control.ts` - API调用层
- 创建了 `admin-ui/src/views/risk-control/index.vue` - 风控中心页面
- 更新了路由配置，添加了风控中心入口

### 7. 代码质量保障
- 在 `pom.xml` 中添加了 Checkstyle、SpotBugs、PMD 插件配置
- 创建了 `checkstyle.xml` - 阿里巴巴Java开发手册规则配置
- 创建了 `.git/hooks/pre-commit` - Git预提交钩子脚本
- 创建了 `.pre-commit-config.yaml` - pre-commit配置文件
- 更新了 `admin-ui/package.json` - 添加ESLint和Prettier依赖
- 创建了 `admin-ui/.eslintrc.js` - ESLint配置
- 创建了 `admin-ui/.prettierrc.js` - Prettier配置

### 8. 单元测试
- 创建了 `BizGachaPoolServiceTest.java` - 抽赏奖池服务测试
- 创建了 `BizSynthesizeServiceTest.java` - 合成服务测试
- 创建了 `BizTransactionServiceTest.java` - 交易服务测试

---

## 开发顺序与时间规划

### Phase 1: 核心技术壁垒实现 (✅ 已完成)
1. **Day 1**: Redis Lua脚本防超卖实现 ✅
2. **Day 2**: Redisson MultiLock实现 ✅
3. **Day 3**: RocketMQ事务回查机制 ✅

### Phase 2: 积分系统实现 (✅ 已完成)
4. **Day 4**: 积分系统核心功能 ✅
5. **Day 5**: 签到功能与积分流水 ✅

### Phase 3: 业务完善与测试 (✅ 已完成)
6. **Day 6**: 抽赏保底与用户认证 ✅
7. **Day 7**: 全局优化与测试（限流熔断、风控中心）✅

### Phase 4: 代码质量保障 (✅ 已完成)
8. **Day 8**: 代码检查工具配置 ✅
9. **Day 9**: 单元测试编写 ✅

---

## 注意事项
1. Bean 命名禁止冲突
2. RocketMQ 消费者必须幂等
3. Redisson 锁必须 finally 释放
4. 接口必须 @Validated 校验
5. 密钥禁止硬编码
6. 前端路径修改后需全局检查引用
7. UI 需适配三种主题
8. 代码提交前必须通过Pre-commit检查

---

## 当前进度
- **阶段**: V2.0 核心完成，V3.0 体验优化迭代中
- **状态**: 后端、前端构建已通过；数据库迁移脚本已生成
- **最后更新**: 2026-07-28

## V3.0 新增功能

### 前端动效系统 (2026-07-28)
- ✅ 页面过渡动画（pageTransition + CSS fade+slideUp+blur）
- ✅ 滚动显现指令 `v-reveal`（IntersectionObserver，支持延迟）
- ✅ 卡片阶梯 FadeUp（`.stagger-item` + `index * 0.0Xs` delay）
- ✅ 列表 FLIP 动画（TransitionGroup 包裹社区/番剧库/背包/用户主页）
- ✅ Tab 切换滑动（新番时间表 Transition mode="out-in"）
- ✅ 全局按钮微交互（`button:active { scale(0.95) }`）
- ✅ 动效覆盖页面：首页/番剧库/社区/背包/兑换/用户主页/我的追番

### 多主题系统完善 (2026-07-27)
- ✅ 浅色主题「星空蓝」系全面统一
- ✅ 筛选标签/按钮选中态 CSS 变量化
- ✅ 文章详情页 hero 遮罩绑定变量
- ✅ 番剧详情页 meta 标签/返回按钮主题化
- ✅ 点踩理由弹窗主题化
- ✅ 发表评论/提交审核/分享按钮星空蓝
- ✅ Markdown 编辑器/预设标签/输入框全部 CSS 变量

### 图片裁剪组件 (2026-07-28)
- ✅ ImageCropperUploader 通用组件（vue-cropper）
- ✅ 支持点击/粘贴/拖拽三种触发方式
- ✅ 动态裁剪比例（aspectRatio prop）
- ✅ 圆形裁剪框（isRound prop，头像模式）
- ✅ v-model 双向绑定 + @crop-success emit

### 系统通知 (2026-07-28)
- ✅ IBizMessageService.sendSystemNotification()
- ✅ 审核通过文章 → 通知作者
- ✅ 审核拒绝文章 → 通知作者（含原因）
- ✅ 管理员删除文章 → 通知作者
- ✅ 管理员删除评论 → 通知评论作者
- ✅ 兑换订单创建成功 → 通知用户

### 商品/订单详情页 (2026-07-28)
- ✅ /redeem-product/[id] 商品详情页
- ✅ /redeem-order/[id] 订单详情页
- ✅ 省市县三级联动下拉（GitHub 官方行政区划数据）
- ✅ 0 库存自动下架

### Bug 修复 (2026-07-27~28)
- ✅ 点赞持久化（Fastjson2 BrowserCompatible + JWT filter + delFlag 三层根因修复）
- ✅ avatar 列 varchar(500) → MEDIUMTEXT
- ✅ updateProfile 防止覆盖密码等字段
- ✅ biz_comment 表缺失列迁移
- ✅ 文章审核驳回管理端按钮缺 @click 事件
- ✅ 消息详情页返回键 z-index 被导航遮挡
- ✅ 首页 footer 被时间表面板 absolute 遮挡

## 完整功能总结

### 后端核心功能
✅ **Redis Lua 脚本防超卖**：抽赏时原子化扣减库存和积分
✅ **Redisson MultiLock**：合成时同时锁定多个碎片资源
✅ **RocketMQ 事务回查**：市场交易最终一致性保障
✅ **积分系统**：评论发奖、点赞发奖、签到功能
✅ **限流熔断**：基于 Resilience4j 的限流和熔断器保护
✅ **代码质量保障**：Checkstyle、SpotBugs、PMD、Pre-commit Hooks
✅ **单元测试**：抽赏、合成、交易模块测试用例

### 管理端
✅ **风控中心**：实时监控熔断器和限流器状态
✅ 抽赏配置、商品图鉴、交易监控、物流调度
✅ ESLint和Prettier代码检查

### 用户端
✅ 抽赏中心、数字背包、跳蚤市场、记忆工坊、地址管理
✅ 所有页面均支持浅色、深色、粉色三种主题

---

## 负责人
- 后端开发: AI Assistant
- 测试验证: AI Assistant