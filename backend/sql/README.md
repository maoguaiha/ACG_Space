# SQL 文件整理说明

## 已整理的完整迁移脚本

**主文件**: `ACG_Space_Complete_Migration.sql`
- 包含所有模块的完整表结构
- 按功能模块分类组织
- 包含初始化数据
- 支持幂等执行（使用 IF NOT EXISTS）

## SQL 文件分类

### ✅ 保留文件
1. `ACG_Space_Complete_Migration.sql` - 完整迁移脚本（推荐使用）
2. `schema.sql` - 基础schema（如果存在）
3. `backup_acg_space_2026-05-06.sql` - 数据库备份

### ❌ 已废弃的临时文件（可删除）
以下文件已被完整迁移脚本替代：

#### 修复类 (fix_*.sql)
- `fix_points_sync_issue.sql` - 积分同步修复
- `fix_asset_image_column.sql` - 资产图片字段修复
- `fix_market_item_audit_columns.sql` - 集市审计字段修复
- `fix_gacha_record_result_items.sql` - 抽奖结果字段修复
- `fix_item_image_column.sql` - 物品图片字段修复
- `fix_gacha_pool_item_columns.sql` - 奖池物品字段修复
- `fix_item_name_columns.sql` - 物品名称字段修复
- `fix_all_remark_columns_safe.sql` - 备注字段修复
- `fix_all_remark_columns.sql` - 备注字段修复
- `fix_remark_column.sql` - 备注字段修复
- `fix_synthesize_remark.sql` - 合成备注字段修复
- `fix_biz_gacha_record_columns.sql` - 抽奖记录字段修复

#### 重置类 (reset_*.sql)
- `reset_user_points_bonus.sql` - 重置用户积分奖励
- `reset_registration_bonus_selective.sql` - 重置注册奖励
- `reset_registration_bonus.sql` - 重置注册奖励

#### 添加类 (add_*.sql)
- `add_unique_constraint_points_log.sql` - 积分日志唯一约束
- `add_registration_bonus_points.sql` - 注册奖励积分
- `add_article_reaction.sql` - 文章反应

#### 更新类 (update_*.sql)
- `update_user_points.sql` - 更新用户积分

#### 查询类 (query_*.sql)
- `query_user_points.sql` - 查询用户积分

#### 插入类 (insert_*.sql)
- `insert_test_user.sql` - 插入测试用户

#### 迁移类 (migration_*.sql, migrate_*.sql)
- `migration_rollbacks_2026-05-06.sql` - 迁移回滚
- `migration_full_2026-05-06.sql` - 完整迁移
- `migration_summary_2026-05-06.sql` - 迁移摘要
- `migrate_avatar.sql` - 头像迁移
- `modify_cover_url.sql` - 修改封面URL

#### 删除类 (remove_*.sql)
- `remove_old_unique_key.sql` - 删除旧唯一键

#### 回填类 (backfill_*.sql)
- `backfill_anime_status_once.sql` - 回填动漫状态

#### 版本迁移类 (v*.sql)
- `v1.1_all_in_one.sql` - V1.1迁移
- `v1.2_user_vip_level_migration.sql` - V1.2 VIP迁移
- `v2.0_digital_asset_migration.sql` - V2.0数字资产迁移
- `v2.1_synthesize_migration.sql` - V2.1合成迁移（已合并到完整脚本）
- `v2.1_synthesize_redeem_migration.sql` - V2.1合成兑换迁移（已合并到完整脚本）

## 使用建议

### 新环境部署
```bash
# 使用完整迁移脚本
mysql -u root -p acg_space < ACG_Space_Complete_Migration.sql
```

### 现有环境升级
```bash
# 完整迁移脚本支持幂等执行，可直接运行
mysql -u root -p acg_space < ACG_Space_Complete_Migration.sql

# 如果已执行过旧版本，只需执行增量修复
mysql -u root -p acg_space < ACG_Space_Sql/acgspace.sql
```

### 数据备份
```bash
# 备份当前数据库
mysqldump -u root -p acg_space > backup_$(date +%Y%m%d).sql
```

## 表结构说明

### 模块分类
1. **基础模块**: `sys_user`
2. **动漫模块**: `biz_anime`
3. **文章模块**: `biz_article`, `biz_comment`
4. **抽赏模块**: `biz_item`, `biz_gacha_pool`, `biz_gacha_pool_item`, `biz_gacha_record`
5. **资产模块**: `biz_user_asset`, `biz_user_points_log`
6. **集市模块**: `biz_market_item` (已废弃)
7. **V2.1新增**: `biz_user_fragment`, `biz_synthesize_rule`, `biz_redeem_order`, `biz_recharge_order`

### 核心功能表
- `biz_user_fragment` - 碎片系统
- `biz_synthesize_rule` - 合成规则
- `biz_redeem_order` - 实物兑换
- `biz_recharge_order` - 充值订单

## 维护建议

1. **定期备份**: 每周自动备份数据库
2. **版本控制**: 所有SQL变更记录在完整迁移脚本中
3. **测试环境**: 先在测试环境验证SQL脚本
4. **幂等性**: 使用 IF NOT EXISTS 确保脚本可重复执行
