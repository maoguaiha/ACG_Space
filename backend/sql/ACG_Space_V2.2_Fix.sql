-- =====================================================
-- ACG Space V2.2 紧急修复 - 修复表结构与实体不匹配导致的SQL错误
-- 日期: 2026-07-22
-- 说明: 
--   1. biz_market_item 缺少9个字段，导致 market_buy/list 等接口500
--   2. biz_gacha_pool_item 缺少3个字段，导致抽赏奖品管理异常
--   3. biz_gacha_pool 缺少5个字段，导致奖池完整功能异常
-- 特性: 幂等，可重复执行不报错
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS `SafeAddV22Columns`;
DELIMITER //
CREATE PROCEDURE `SafeAddV22Columns`()
BEGIN

    -- =====================================================
    -- 一、biz_market_item 补全字段
    -- 问题: 表只有10列，但实体BizMarketItem有19个字段
    -- 影响: market_buy/list/delist 全部可能因为缺失字段报错
    -- =====================================================

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'item_id') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `item_id` bigint(20) DEFAULT NULL COMMENT '物品ID' AFTER `asset_id`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'item_name') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `item_name` varchar(100) DEFAULT NULL COMMENT '物品名称' AFTER `item_id`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'item_image') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片' AFTER `item_name`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'item_rarity') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度' AFTER `item_image`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'item_type') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `item_type` varchar(50) DEFAULT NULL COMMENT '物品类型' AFTER `item_rarity`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'seller_id') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `seller_id` bigint(20) DEFAULT NULL COMMENT '卖家用户ID' AFTER `item_type`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'order_id') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `order_id` varchar(64) DEFAULT NULL COMMENT '市场订单号' AFTER `status`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'sold_time') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `sold_time` datetime DEFAULT NULL COMMENT '售出时间' AFTER `order_id`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'delist_time') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `delist_time` datetime DEFAULT NULL COMMENT '下架时间' AFTER `sold_time`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_market_item' AND COLUMN_NAME = 'remark') THEN
        ALTER TABLE `biz_market_item` ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `delist_time`;
    END IF;

    -- 旧表有 user_id 字段，新实体用 seller_id，保留 user_id 向后兼容

    -- =====================================================
    -- 二、biz_gacha_pool_item 补全字段
    -- 问题: 表缺少 rarity、is_guarantee、stock_limit 三列
    -- 影响: 奖品创建/查询时这些字段为NULL，业务逻辑异常
    -- =====================================================

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_gacha_pool_item' AND COLUMN_NAME = 'rarity') THEN
        ALTER TABLE `biz_gacha_pool_item` ADD COLUMN `rarity` varchar(10) DEFAULT 'N' COMMENT '物品稀有度' AFTER `item_id`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_gacha_pool_item' AND COLUMN_NAME = 'is_guarantee') THEN
        ALTER TABLE `biz_gacha_pool_item` ADD COLUMN `is_guarantee` tinyint(4) DEFAULT 0 COMMENT '是否保底物品(0否1是)' AFTER `weight`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_gacha_pool_item' AND COLUMN_NAME = 'stock_limit') THEN
        ALTER TABLE `biz_gacha_pool_item` ADD COLUMN `stock_limit` int(11) DEFAULT NULL COMMENT '库存上限(NULL不限)' AFTER `is_guarantee`;
    END IF;

    -- =====================================================
    -- 三、biz_gacha_pool 补全字段
    -- 问题: 表缺少 rarity、weight_config 及 BaseEntity通用字段
    -- =====================================================

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_gacha_pool' AND COLUMN_NAME = 'rarity') THEN
        ALTER TABLE `biz_gacha_pool` ADD COLUMN `rarity` varchar(10) DEFAULT NULL COMMENT '限定稀有度(SSR/SR/normal)' AFTER `banner`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_gacha_pool' AND COLUMN_NAME = 'weight_config') THEN
        ALTER TABLE `biz_gacha_pool` ADD COLUMN `weight_config` text COMMENT '权重配置(JSON格式)' AFTER `status`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_gacha_pool' AND COLUMN_NAME = 'create_by') THEN
        ALTER TABLE `biz_gacha_pool` ADD COLUMN `create_by` varchar(64) DEFAULT NULL COMMENT '创建者' AFTER `weight_config`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_gacha_pool' AND COLUMN_NAME = 'update_by') THEN
        ALTER TABLE `biz_gacha_pool` ADD COLUMN `update_by` varchar(64) DEFAULT NULL COMMENT '更新者' AFTER `create_by`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_gacha_pool' AND COLUMN_NAME = 'remark') THEN
        ALTER TABLE `biz_gacha_pool` ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `update_by`;
    END IF;

    -- =====================================================
    -- 四、sys_user 补全（如果 points 不存在，V2.1 SP可能未执行）
    -- =====================================================

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'points') THEN
        ALTER TABLE `sys_user` ADD COLUMN `points` int(11) DEFAULT 0 COMMENT '积分' AFTER `status`;
    END IF;

END //
DELIMITER ;

CALL `SafeAddV22Columns`();
DROP PROCEDURE IF EXISTS `SafeAddV22Columns`;

SET FOREIGN_KEY_CHECKS = 1;
