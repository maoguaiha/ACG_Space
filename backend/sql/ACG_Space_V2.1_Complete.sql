-- =====================================================
-- ACG Space 完整数据库迁移脚本
-- 版本: V2.1 (最终版)
-- 日期: 2026-05-09
-- 说明: 包含所有功能模块的完整数据库结构和初始化数据
-- 特性: 支持重复执行不报错（幂等性）
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 第一部分：基础表结构
-- =====================================================

-- 用户表 (sys_user 已存在，通过存储过程安全添加字段)
DROP PROCEDURE IF EXISTS `SafeAddSysUserColumns`;
DELIMITER //
CREATE PROCEDURE `SafeAddSysUserColumns`()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'points') THEN
        ALTER TABLE `sys_user` ADD COLUMN `points` int(11) DEFAULT 0 COMMENT '积分' AFTER `status`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'vip_level') THEN
        ALTER TABLE `sys_user` ADD COLUMN `vip_level` int(11) DEFAULT 0 COMMENT 'VIP等级' AFTER `points`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'vip_expire_time') THEN
        ALTER TABLE `sys_user` ADD COLUMN `vip_expire_time` datetime DEFAULT NULL COMMENT 'VIP过期时间' AFTER `vip_level`;
    END IF;
END //
DELIMITER ;

CALL `SafeAddSysUserColumns`();
DROP PROCEDURE IF EXISTS `SafeAddSysUserColumns`;

-- =====================================================
-- 第二部分：动漫模块
-- =====================================================

CREATE TABLE IF NOT EXISTS `biz_anime` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `bgm_id` int(11) DEFAULT NULL COMMENT 'Bangumi ID',
  `name` varchar(200) NOT NULL COMMENT '动漫名称',
  `name_cn` varchar(200) DEFAULT NULL COMMENT '中文名称',
  `type` varchar(50) DEFAULT NULL COMMENT '类型',
  `status` varchar(50) DEFAULT NULL COMMENT '状态',
  `cover` varchar(500) DEFAULT NULL COMMENT '封面图片',
  `rating` decimal(3,1) DEFAULT NULL COMMENT '评分',
  `rating_count` int(11) DEFAULT 0 COMMENT '评分人数',
  `summary` text COMMENT '简介',
  `air_date` date DEFAULT NULL COMMENT '开播日期',
  `air_weekday` int(11) DEFAULT NULL COMMENT '播出星期',
  `episodes` int(11) DEFAULT 0 COMMENT '集数',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_bgm_id` (`bgm_id`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动漫信息表';

-- =====================================================
-- 第三部分：文章模块
-- =====================================================

CREATE TABLE IF NOT EXISTS `biz_article` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` longtext COMMENT '内容',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图片',
  `type` varchar(50) DEFAULT 'article' COMMENT '类型',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态',
  `view_count` int(11) DEFAULT 0 COMMENT '浏览数',
  `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `comment_count` int(11) DEFAULT 0 COMMENT '评论数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

CREATE TABLE IF NOT EXISTS `biz_comment` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `target_type` varchar(50) NOT NULL COMMENT '目标类型',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父评论ID',
  `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- =====================================================
-- 第四部分：抽赏模块
-- =====================================================

CREATE TABLE IF NOT EXISTS `biz_item` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '物品名称',
  `description` text COMMENT '描述',
  `image` varchar(500) DEFAULT NULL COMMENT '图片',
  `rarity` varchar(10) DEFAULT 'N' COMMENT '稀有度',
  `type` varchar(50) DEFAULT 'character' COMMENT '类型',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_rarity` (`rarity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品表';

CREATE TABLE IF NOT EXISTS `biz_gacha_pool` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '奖池名称',
  `description` text COMMENT '描述',
  `banner` varchar(500) DEFAULT NULL COMMENT '横幅图片',
  `total_stock` int(11) DEFAULT 0 COMMENT '总库存',
  `remaining_stock` int(11) DEFAULT 0 COMMENT '剩余库存',
  `single_cost` int(11) DEFAULT 100 COMMENT '单抽消耗',
  `ten_cost` int(11) DEFAULT 900 COMMENT '十连消耗',
  `guarantee_count` int(11) DEFAULT 10 COMMENT '保底次数',
  `guarantee_type` varchar(10) DEFAULT 'SR' COMMENT '保底类型',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖池表';

CREATE TABLE IF NOT EXISTS `biz_gacha_pool_item` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `pool_id` bigint(20) NOT NULL COMMENT '奖池ID',
  `item_id` bigint(20) NOT NULL COMMENT '物品ID',
  `weight` int(11) DEFAULT 1 COMMENT '权重',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_pool_id` (`pool_id`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖池物品关联表';

CREATE TABLE IF NOT EXISTS `biz_gacha_record` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `pool_id` bigint(20) NOT NULL COMMENT '奖池ID',
  `pool_name` varchar(100) DEFAULT NULL COMMENT '奖池名称',
  `gacha_type` int(11) DEFAULT 1 COMMENT '抽卡类型',
  `cost_points` int(11) DEFAULT 0 COMMENT '消耗积分',
  `result_items` longtext COMMENT '结果物品',
  `is_guaranteed` tinyint(4) DEFAULT 0 COMMENT '是否保底',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pool_id` (`pool_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖记录表';

-- =====================================================
-- 第五部分：用户资产模块
-- =====================================================

CREATE TABLE IF NOT EXISTS `biz_user_asset` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `item_id` bigint(20) NOT NULL COMMENT '物品ID',
  `asset_key` varchar(100) NOT NULL COMMENT '资产唯一键',
  `quantity` int(11) DEFAULT 1 COMMENT '数量',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 (1=正常 2=已核销 3=已出售 4=已合成)',
  `is_physical` tinyint(1) DEFAULT 0 COMMENT '是否实物',
  `acquire_type` varchar(50) DEFAULT NULL COMMENT '获取方式',
  `acquire_source_id` varchar(100) DEFAULT NULL COMMENT '获取来源ID',
  `item_name` varchar(100) DEFAULT NULL COMMENT '物品名称',
  `item_image` longtext COMMENT '物品图片',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度',
  `item_type` varchar(50) DEFAULT NULL COMMENT '物品类型',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_key` (`asset_key`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资产表';

CREATE TABLE IF NOT EXISTS `biz_user_points_log` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `points` int(11) NOT NULL COMMENT '积分变动',
  `type` varchar(50) NOT NULL COMMENT '类型',
  `source_id` varchar(100) DEFAULT NULL COMMENT '来源ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `uk_user_type_source` (`user_id`, `type`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分日志表';

-- =====================================================
-- 第六部分：集市模块（已废弃，保留表结构）
-- =====================================================

CREATE TABLE IF NOT EXISTS `biz_market_item` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `price` int(11) NOT NULL COMMENT '价格',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集市商品表';

-- =====================================================
-- 第七部分：V2.1 新增功能 - 碎片、合成、兑换、充值
-- =====================================================

-- 用户碎片表
CREATE TABLE IF NOT EXISTS `biz_user_fragment` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `fragment_type` varchar(20) NOT NULL DEFAULT 'normal' COMMENT '碎片类型',
  `quantity` int(11) NOT NULL DEFAULT 0 COMMENT '碎片数量',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_fragment_type` (`user_id`, `fragment_type`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户碎片表';

-- 合成规则表
CREATE TABLE IF NOT EXISTS `biz_synthesize_rule` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `source_rarity` varchar(10) NOT NULL COMMENT '源品质',
  `source_count` int(11) NOT NULL DEFAULT 10 COMMENT '需要数量',
  `target_rarity` varchar(10) NOT NULL COMMENT '目标品质',
  `target_count` int(11) NOT NULL DEFAULT 1 COMMENT '产出数量',
  `is_physical` tinyint(1) DEFAULT 0 COMMENT '是否实物',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_source_rarity` (`source_rarity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合成规则表';

-- 兑换订单表（包含商品相关字段）
CREATE TABLE IF NOT EXISTS `biz_redeem_order` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `asset_id` bigint(20) DEFAULT NULL COMMENT '资产ID（兼容旧字段）',
  `item_id` bigint(20) DEFAULT NULL COMMENT '物品ID（兼容旧字段）',
  `item_name` varchar(100) DEFAULT NULL COMMENT '物品名称（兼容旧字段）',
  `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片（兼容旧字段）',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度（兼容旧字段）',
  `product_id` bigint(20) DEFAULT NULL COMMENT '商品ID',
  `product_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
  `product_image` longtext DEFAULT NULL COMMENT '商品图片',
  `ur_fragment_cost` int(11) DEFAULT 0 COMMENT '消耗UR碎片数量',
  `points_cost` int(11) DEFAULT 0 COMMENT '消耗积分数量',
  `receiver` varchar(50) NOT NULL COMMENT '收货人',
  `phone` varchar(20) NOT NULL COMMENT '电话',
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `district` varchar(50) DEFAULT NULL COMMENT '区',
  `address` varchar(200) NOT NULL COMMENT '详细地址',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 (0=待发货 1=已发货 2=已完成)',
  `logistics_company` varchar(50) DEFAULT NULL COMMENT '物流公司',
  `logistics_no` varchar(50) DEFAULT NULL COMMENT '物流单号',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换订单表';

-- 兑换实物商品表
CREATE TABLE IF NOT EXISTS `biz_redeem_product` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `image` longtext DEFAULT NULL COMMENT '商品图片',
  `description` text COMMENT '商品描述',
  `ur_fragment_cost` int(11) DEFAULT 0 COMMENT '所需UR碎片数量',
  `points_cost` int(11) DEFAULT 0 COMMENT '所需积分数量',
  `stock` int(11) DEFAULT 0 COMMENT '库存数量',
  `exchanged_count` int(11) DEFAULT 0 COMMENT '已兑换数量',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 (0=下架 1=上架)',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换实物商品表';

-- 充值订单表
CREATE TABLE IF NOT EXISTS `biz_recharge_order` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '充值金额',
  `points` int(11) NOT NULL COMMENT '获得积分',
  `pay_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '支付状态 (0=待支付 1=已支付)',
  `pay_type` varchar(20) DEFAULT 'mock' COMMENT '支付方式',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `trade_no` varchar(64) DEFAULT NULL COMMENT '交易号',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pay_status` (`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值订单表';

-- =====================================================
-- 第八部分：初始化数据（幂等性：重复执行不报错）
-- =====================================================

-- 初始化合成规则
INSERT INTO `biz_synthesize_rule` (`id`, `source_rarity`, `source_count`, `target_rarity`, `target_count`, `is_physical`, `status`, `create_time`, `update_time`, `del_flag`)
SELECT 1, 'R', 10, 'SR', 1, 0, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `biz_synthesize_rule` WHERE `id` = 1);

INSERT INTO `biz_synthesize_rule` (`id`, `source_rarity`, `source_count`, `target_rarity`, `target_count`, `is_physical`, `status`, `create_time`, `update_time`, `del_flag`)
SELECT 2, 'SR', 10, 'SSR', 1, 0, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `biz_synthesize_rule` WHERE `id` = 2);

INSERT INTO `biz_synthesize_rule` (`id`, `source_rarity`, `source_count`, `target_rarity`, `target_count`, `is_physical`, `status`, `create_time`, `update_time`, `del_flag`)
SELECT 3, 'SSR', 10, 'UR', 1, 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `biz_synthesize_rule` WHERE `id` = 3);

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 迁移完成
-- =====================================================
-- 
-- 表清单（共 17 张表）：
-- 1. sys_user (系统用户表 - 已存在，仅添加字段)
-- 2. biz_anime (动漫信息表)
-- 3. biz_article (文章表)
-- 4. biz_comment (评论表)
-- 5. biz_item (物品表)
-- 6. biz_gacha_pool (奖池表)
-- 7. biz_gacha_pool_item (奖池物品关联表)
-- 8. biz_gacha_record (抽奖记录表)
-- 9. biz_user_asset (用户资产表)
-- 10. biz_user_points_log (用户积分日志表)
-- 11. biz_market_item (集市商品表 - 已废弃)
-- 12. biz_user_fragment (用户碎片表)
-- 13. biz_synthesize_rule (合成规则表)
-- 14. biz_redeem_order (兑换订单表)
-- 15. biz_redeem_product (兑换实物商品表)
-- 16. biz_recharge_order (充值订单表)
--
-- 使用说明：
-- 1. 本脚本支持重复执行，不会产生错误
-- 2. 使用 MySQL 客户端执行：source /path/to/ACG_Space_V2.1_Complete.sql
-- 3. 或使用数据库管理工具（如 Navicat、DBeaver）直接运行
-- 4. 执行前请确保已创建数据库：CREATE DATABASE acg_space DEFAULT CHARACTER SET utf8mb4;
--
-- =====================================================
