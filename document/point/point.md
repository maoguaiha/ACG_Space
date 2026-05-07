# 上下文重点 - point

## 项目概述

ACG_Space 是一个动漫内容与数字谷子集换社区平台，采用 Java 17 + Spring Boot 3 + MyBatis-Plus 后端技术栈，Vue 3 + Element Plus + Pinia 管理端技术栈，Nuxt 3 + Tailwind CSS 用户端技术栈。核心功能包括抽赏中心、数字背包、跳蚤市场、记忆工坊等模块。

## 规则要点与避坑

1. **Bean命名规范**：所有@Bean注解必须显式命名，防止BeanDefinitionOverrideException冲突
2. **幂等性保障**：所有RocketMQ消费者必须包含幂等性校验逻辑
3. **分布式锁安全**：使用Redisson锁时必须配合finally块确保释放
4. **参数校验先行**：接口入参必须使用@Validated注解
5. **环境隔离**：密钥、密码禁止硬编码，统一使用环境变量

## 当前进度

### 代码质量保障
- ✅ Checkstyle配置完成
- ✅ SpotBugs配置完成  
- ✅ PMD配置完成
- ✅ Pre-commit Hooks配置完成
- ✅ 前端ESLint配置完成

### 自我验证机制
- ✅ 抽赏模块单元测试用例编写完成
- ✅ 合成模块单元测试用例编写完成
- ✅ 交易模块单元测试用例编写完成
- ✅ 代码审查记录文档创建完成

### 功能开发
- ⚠️ 记忆工坊合成API待完成（当前使用mock数据）

## 待完成

1. **P0优先级**：完成记忆工坊后端合成API开发，替换mock数据
2. **P1优先级**：修复RedissonClient Bean命名问题
3. **P1优先级**：修复IdempotentInterceptor使用ObjectMapper问题
4. **P2优先级**：完善DTO参数校验注解
5. **P2优先级**：更新plan.md文档记录当前进度