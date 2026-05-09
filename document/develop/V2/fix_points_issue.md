# 积分系统问题修复

## 问题描述

### 问题 1：重复领取积分成功
- **现象**：用户多次点击领取积分按钮，每次都显示"领取成功"，积分不断增加
- **原因**：`checkExists` 方法只检查 `del_flag=0` 的记录，软删除后（`del_flag=1`）检查失效

### 问题 2：Lua 脚本执行失败
- **现象**：抽奖时报错 `class java.lang.Integer cannot be cast to class java.lang.String`
- **原因**：`luaRedisTemplate` 使用 String 序列化，但 Lua 脚本返回值可能是 Integer 或其他类型

## 修复方案

### 1. 修复 LuaScriptExecutor 返回类型

**文件**：`backend/src/main/java/com/ruoyi/project/common/utils/LuaScriptExecutor.java`

**修改**：将 `RedisScript<String>` 改为 `RedisScript<Object>`

```java
// 修改前
RedisScript<String> script = new DefaultRedisScript<>(gachaDeductStockScript, String.class);
String result = luaRedisTemplate.execute(script, keys, args.toArray());

// 修改后
RedisScript<Object> script = new DefaultRedisScript<>(gachaDeductStockScript, Object.class);
Object result = luaRedisTemplate.execute(script, keys, args.toArray());
```

### 2. 增强日志输出

**文件**：`backend/src/main/java/com/ruoyi/project/service/impl/BizUserPointsLogServiceImpl.java`

**修改**：添加详细的日志输出，便于调试

```java
log.info("========== 开始处理注册积分领取，userId: {}, bizRefId: {} ==========", userId, bizRefId);

// 检查是否已领取
boolean exists = checkExists("REGISTRATION", bizRefId);
log.info("检查领取记录，userId: {}, exists: {}", userId, exists);

if (exists) {
    log.warn("注册积分已领取过，userId: {}, 当前积分：{}", userId, currentPoints);
    return false;
}
```

### 3. 添加数据库唯一键约束

**文件**：`backend/sql/add_unique_constraint_points_log.sql`

**执行**：
```sql
ALTER TABLE biz_user_points_log 
ADD UNIQUE KEY uk_action_biz_del (action_type, biz_reference_id, del_flag);
```

### 4. 清除已领取的记录（重置新手福利）

**执行 SQL**：
```sql
UPDATE biz_user_points_log
SET del_flag = 1, update_time = NOW()
WHERE action_type = 'REGISTRATION' AND del_flag = 0;
```

**清除 Redis 积分**：
```bash
# Docker 方式
docker exec redis redis-cli KEYS "user:points:*" | ForEach-Object { 
    if ($_ -ne "") { 
        docker exec redis redis-cli DEL $_ 
    } 
}
```

## 执行步骤

### 步骤 1：应用代码修复

1. 接受 `LuaScriptExecutor.java` 的 diff
2. 接受 `BizUserPointsLogServiceImpl.java` 的 diff
3. 重启后端服务

### 步骤 2：添加数据库约束

```bash
# 进入 MySQL 容器
docker exec -it <mysql 容器名> mysql -u root -proot acg_space

# 执行唯一键约束
source /path/to/add_unique_constraint_points_log.sql
```

### 步骤 3：重置新手福利记录

```bash
# 执行 SQL 重置脚本
docker exec <mysql 容器名> mysql -u root -proot acg_space -e "
UPDATE biz_user_points_log
SET del_flag = 1, update_time = NOW()
WHERE action_type = 'REGISTRATION' AND del_flag = 0;
"

# 清除 Redis 积分
docker exec redis redis-cli KEYS "user:points:*" | ForEach-Object { 
    if ($_ -ne "") { 
        docker exec redis redis-cli DEL $_ 
    } 
}
```

### 步骤 4：测试验证

1. **清除浏览器缓存**（Ctrl+Shift+R）
2. **打开私信页面**
3. **点击 [领取积分] 按钮**
   - 第一次点击：应该显示"领取成功，已获得 2600 积分"
   - 第二次点击：应该显示"您已领取过积分，无需重复领取"
4. **检查积分是否正确显示为 2600**
5. **尝试抽奖**：应该可以正常抽奖，不再报类型转换错误

## 验证 SQL

```sql
-- 1. 查看领取记录（应该都为 del_flag=1）
SELECT * FROM biz_user_points_log 
WHERE action_type = 'REGISTRATION' 
ORDER BY create_time DESC;

-- 2. 查看是否有未删除的记录（应该为 0）
SELECT COUNT(*) FROM biz_user_points_log 
WHERE action_type = 'REGISTRATION' AND del_flag = 0;

-- 3. 查看唯一键约束
SHOW INDEX FROM biz_user_points_log WHERE Key_name = 'uk_action_biz_del';
```

## 注意事项

1. **唯一键约束可能会失败**：如果已经存在重复记录，需要先清理
2. **Redis 和数据库要同步清除**：否则会出现积分不一致
3. **软删除机制**：使用 `del_flag=1` 而不是物理删除，保留审计日志
4. **幂等性检查**：所有积分发放接口都必须有幂等性检查

## 相关文件

- [`LuaScriptExecutor.java`](file:///c:/ProgramFiles/Program/java/ACG_Space/backend/src/main/java/com/ruoyi/project/common/utils/LuaScriptExecutor.java)
- [`BizUserPointsLogServiceImpl.java`](file:///c:/ProgramFiles/Program/java/ACG_Space/backend/src/main/java/com/ruoyi/project/service/impl/BizUserPointsLogServiceImpl.java)
- [`add_unique_constraint_points_log.sql`](file:///c:/ProgramFiles/Program/java/ACG_Space/backend/sql/add_unique_constraint_points_log.sql)
- [`reset_registration_bonus.sql`](file:///c:/ProgramFiles/Program/java/ACG_Space/backend/sql/reset_registration_bonus.sql)