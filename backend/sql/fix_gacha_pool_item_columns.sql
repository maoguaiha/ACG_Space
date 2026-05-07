-- 修复 biz_gacha_pool_item 表缺失的字段
-- 添加 BaseEntity 中定义的字段

ALTER TABLE `biz_gacha_pool_item`
    ADD COLUMN IF NOT EXISTS `create_by` varchar(64) DEFAULT '' COMMENT '创建者' AFTER `stock_limit`,
    ADD COLUMN IF NOT EXISTS `create_time` datetime DEFAULT NULL COMMENT '创建时间' AFTER `create_by`,
    ADD COLUMN IF NOT EXISTS `update_by` varchar(64) DEFAULT '' COMMENT '更新者' AFTER `create_time`,
    ADD COLUMN IF NOT EXISTS `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `update_by`,
    ADD COLUMN IF NOT EXISTS `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `update_time`,
    ADD COLUMN IF NOT EXISTS `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)' AFTER `remark`;

-- 验证表结构
SHOW COLUMNS FROM biz_gacha_pool_item;