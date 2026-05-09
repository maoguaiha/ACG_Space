# 上下文重点 - point

## 项目概述

ACG_Space 是一个动漫内容与数字谷子集换社区平台，采用 Java 17 + Spring Boot 3 + MyBatis-Plus 后端技术栈，Vue 3 + Element Plus + Pinia 管理端技术栈，Nuxt 3 + Tailwind CSS 用户端技术栈。核心功能包括抽赏中心、数字背包、合成工坊、实物兑换、订单管理等模块。

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

## 当前进度

### V2.1 功能完成情况
- ✅ 碎片系统（抽奖获得碎片，100碎片兑换10积分）
- ✅ 合成系统（10N→1SR，10SR→1SSR实物）
- ✅ 充值系统（模拟充值流程）
- ✅ 实物兑换（地址填写，订单管理）
- ✅ 后台管理（订单列表，物流更新）
- ✅ 前端页面（抽赏、背包、兑换、充值）
- ✅ SQL整理（完整迁移脚本）
- ✅ Bug修复（403错误、字段缺失、数据同步）
- ✅ 兑换商品管理（后台新增/编辑/删除商品，用户端浏览兑换）
- ✅ 订单系统（用户订单列表、物流查看、后台订单管理）
- ✅ 交易监控（查看用户兑换记录）
- ✅ 物流调度（订单与物流信息管理）

### 代码质量保障
- ✅ Checkstyle配置完成
- ✅ SpotBugs配置完成  
- ✅ PMD配置完成
- ✅ Pre-commit Hooks配置完成
- ✅ 前端ESLint配置完成

### 数据库
- ✅ 完整迁移脚本：`ACG_Space_Complete_Migration.sql`
- ✅ SQL文件整理完成，删除33个临时文件
- ✅ README文档创建完成
- ✅ 兑换订单表字段扩展：添加product_id、product_name、product_image、ur_fragment_cost、points_cost字段
- ✅ 兑换订单表遗留字段修改：asset_id、item_id、item_name改为允许NULL

## 待完成

1. **P0优先级**：测试完整兑换流程（商品查询→UR碎片扣除→订单创建→前端反馈）
2. **P1优先级**：完善订单详情页物流信息展示
3. **P1优先级**：优化兑换商品列表分页和筛选功能
4. **P2优先级**：完善DTO参数校验注解
5. **P2优先级**：补充单元测试用例
