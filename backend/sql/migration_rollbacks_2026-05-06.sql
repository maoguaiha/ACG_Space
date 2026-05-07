-- ==============================================
-- ACG Space 迁移回滚脚本（建议仅在测试库审阅后在生产采取手动回滚）
-- 生成时间: 2026-05-06
-- 说明: 提供对主要新增表与字段的回滚示例，部分操作可能导致数据丢失，请谨慎使用。
-- ==============================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 删除新增的表（会丢失数据）
DROP TABLE IF EXISTS `biz_article_reaction`;
DROP TABLE IF EXISTS `biz_market_item`;
DROP TABLE IF EXISTS `v_user_backpack`;
DROP TABLE IF EXISTS `biz_synthesize_record`;
DROP TABLE IF EXISTS `biz_synthesize_recipe`;
DROP TABLE IF EXISTS `biz_gacha_pool_item`;
DROP TABLE IF EXISTS `biz_gacha_pool`;
DROP TABLE IF EXISTS `biz_gacha_record`;
DROP TABLE IF EXISTS `biz_user_asset`;
DROP TABLE IF EXISTS `biz_item`;
DROP TABLE IF EXISTS `biz_transaction`;
DROP TABLE IF EXISTS `biz_delivery_order`;
DROP TABLE IF EXISTS `biz_user_address`;
DROP TABLE IF EXISTS `biz_user_follow`;

-- 2. 从 sys_user 中移除新增列（小心：可能会丢失数据）
ALTER TABLE `sys_user` 
  DROP COLUMN IF EXISTS `total_assets`,
  DROP COLUMN IF EXISTS `v2_points`,
  DROP COLUMN IF EXISTS `vip_status`,
  DROP COLUMN IF EXISTS `vip_expire_time`,
  DROP COLUMN IF EXISTS `user_level`,
  DROP COLUMN IF EXISTS `level_experience`,
  DROP COLUMN IF EXISTS `points`,
  DROP COLUMN IF EXISTS `bio`,
  DROP COLUMN IF EXISTS `follower_count`,
  DROP COLUMN IF EXISTS `following_count`;

-- 3. 从 biz_item 等表中移除添加的列
ALTER TABLE `biz_item` DROP COLUMN IF EXISTS `remark`;
ALTER TABLE `biz_gacha_record` DROP COLUMN IF EXISTS `create_by`;
ALTER TABLE `biz_gacha_record` DROP COLUMN IF EXISTS `update_by`;
ALTER TABLE `biz_gacha_record` DROP COLUMN IF EXISTS `remark`;
ALTER TABLE `biz_user_asset` DROP COLUMN IF EXISTS `item_name`;
ALTER TABLE `biz_user_asset` DROP COLUMN IF EXISTS `item_image`;
ALTER TABLE `biz_user_asset` DROP COLUMN IF EXISTS `item_rarity`;
ALTER TABLE `biz_user_asset` DROP COLUMN IF EXISTS `item_type`;
ALTER TABLE `biz_transaction` DROP COLUMN IF EXISTS `item_name`;
ALTER TABLE `biz_delivery_order` DROP COLUMN IF EXISTS `item_name`;
ALTER TABLE `biz_market_item` DROP COLUMN IF EXISTS `item_name`;
ALTER TABLE `biz_article` DROP COLUMN IF EXISTS `dislike_count`;

-- 4. 回滚 avatar/cover_url 类型修改（如需回退，请确保原类型信息）
-- ALTER TABLE sys_user MODIFY COLUMN avatar varchar(255) DEFAULT NULL COMMENT '头像';
-- ALTER TABLE biz_article MODIFY COLUMN cover_url varchar(500) DEFAULT NULL COMMENT '封面图片链接';

-- 5. 删除测试用户（如不需要）
DELETE FROM sys_user WHERE username IN ('admin', 'testuser');

SET FOREIGN_KEY_CHECKS = 1;

-- 注意事项:
-- - 回滚脚本提供了常见 DROP/ALTER 示例，但并不保证对所有环境适用。
-- - 对生产环境，请先备份并在恢复策略确认后手动执行。
-- - 建议对关键表使用导出（mysqldump）备份后再执行回滚操作。
