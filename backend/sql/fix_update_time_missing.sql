-- =====================================================
-- 修复 biz_user_points_log 等表缺失 update_time 列的问题
-- 日期: 2026-07-10
-- 问题: fix_missing_columns.sql 给多张表添加了 create_by/update_by/remark,
--        但遗漏了 update_time 列。实体类继承 BaseEntity(含 updateTime 字段),
--        当 Service 层调用 setUpdateTime() 后, MyBatis-Plus 自动生成含
--        update_time 的 INSERT 语句, 导致 "Unknown column 'update_time'" 错误。
-- 修复: 为受影响的表补上 update_time 列。
-- 注意: MySQL 不支持 ADD COLUMN IF NOT EXISTS, 如果某张表已有此列会报错,
--       跳过该表对应的语句即可。建议逐条执行。
-- =====================================================

SET NAMES utf8mb4;

-- ===== 直接报错的表 =====
ALTER TABLE `biz_user_points_log`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

-- ===== 以下表同样在 fix_missing_columns.sql 中被添加了 create_by/update_by/remark
--       但可能也遗漏了 update_time, 建议一并补上以防同类错误 =====
ALTER TABLE `biz_gacha_pool`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `biz_anime`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `biz_article`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `biz_comment`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `biz_item`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `biz_gacha_pool_item`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `biz_gacha_record`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `biz_user_asset`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `biz_market_item`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
