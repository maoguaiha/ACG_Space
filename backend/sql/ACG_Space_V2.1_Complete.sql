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
  `user_id` bigint(20) NOT NULL COMMENT '用户ID(兼容旧字段)',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
  `content` longtext COMMENT '内容',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图片',
  `author_id` bigint(20) DEFAULT NULL COMMENT '作者用户ID',
  `category` varchar(50) DEFAULT NULL COMMENT '文章分类',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签(逗号分隔)',
  `type` varchar(50) DEFAULT 'article' COMMENT '类型(兼容旧字段)',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态(0草稿 1发布 2下架 3待审核 4驳回)',
  `is_vip_only` tinyint(4) DEFAULT 0 COMMENT '是否VIP专享(0否1是)',
  `is_featured` tinyint(4) DEFAULT 0 COMMENT '是否推荐(0否1是)',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回原因',
  `view_count` int(11) DEFAULT 0 COMMENT '浏览数',
  `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `dislike_count` int(11) DEFAULT 0 COMMENT '点踩数',
  `comment_count` int(11) DEFAULT 0 COMMENT '评论数',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_author_id` (`author_id`),
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
  `item_key` varchar(100) DEFAULT NULL COMMENT '物品唯一标识 (如 item_ssr_001)',
  `name` varchar(100) NOT NULL COMMENT '物品名称',
  `description` text COMMENT '描述',
  `image` varchar(500) DEFAULT NULL COMMENT '图片',
  `rarity` varchar(10) DEFAULT 'N' COMMENT '稀有度',
  `type` varchar(50) DEFAULT 'character' COMMENT '类型',
  `total_stock` int(11) DEFAULT 0 COMMENT '总库存',
  `remaining_stock` int(11) DEFAULT 0 COMMENT '剩余库存',
  `price` int(11) DEFAULT 0 COMMENT '参考价格(积分)',
  `marketable` tinyint(4) DEFAULT 1 COMMENT '是否可上架市场(0否1是)',
  `synthesizable` tinyint(4) DEFAULT 0 COMMENT '是否可合成(0否1是)',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_rarity` (`rarity`),
  KEY `idx_item_key` (`item_key`)
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
  `action_type` varchar(50) NOT NULL COMMENT '动作类型(如COMMENT,LOGIN,SHARE,REGISTRATION)',
  `points_change` int(11) NOT NULL COMMENT '积分变动(正负值)',
  `biz_reference_id` varchar(100) DEFAULT NULL COMMENT '业务关联ID(幂等键)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `uk_user_action_ref` (`user_id`, `action_type`, `biz_reference_id`)
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

-- =====================================================
-- 第十七部分：站内消息表
-- =====================================================

CREATE TABLE IF NOT EXISTS `biz_message` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `from_user_id` bigint(20) DEFAULT NULL COMMENT '发送者ID(0=系统)',
  `to_user_id` bigint(20) DEFAULT NULL COMMENT '接收者ID',
  `content` text COMMENT '消息内容',
  `is_read` tinyint(4) DEFAULT 0 COMMENT '是否已读(0否1是)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_from_user` (`from_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- =====================================================
-- 第十八部分：番剧追番记录表
-- =====================================================

CREATE TABLE IF NOT EXISTS `biz_anime_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `anime_id` bigint(20) NOT NULL COMMENT '番剧ID',
  `create_time` datetime DEFAULT NULL COMMENT '追番时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_anime` (`user_id`, `anime_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧追番记录表';

-- ---------------------------------------------------
-- 第十九部分：评论/文章互动模块 (2026-07-10 补充)
-- ---------------------------------------------------

-- 评论反应表
CREATE TABLE IF NOT EXISTS `biz_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` int(11) DEFAULT NULL COMMENT '反应类型(1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论反应表';

-- 文章评论反应表
CREATE TABLE IF NOT EXISTS `biz_article_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_comment_id` bigint(20) NOT NULL COMMENT '文章评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` int(11) DEFAULT NULL COMMENT '反应类型(1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_user` (`article_comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论反应表';

-- 文章反应表
CREATE TABLE IF NOT EXISTS `biz_article_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` int(11) DEFAULT NULL COMMENT '反应类型(1点赞 2点踩)',
  `reason` varchar(500) DEFAULT NULL COMMENT '原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_article_user` (`article_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章反应表';

-- 文章评论表
CREATE TABLE IF NOT EXISTS `biz_article_comment` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '所属文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布用户ID',
  `content` text COMMENT '评论内容',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父评论ID(0=顶级)',
  `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复目标用户ID',
  `reply_to_nickname` varchar(100) DEFAULT NULL COMMENT '回复目标用户昵称',
  `likes` int(11) DEFAULT 0 COMMENT '点赞数',
  `dislikes` int(11) DEFAULT 0 COMMENT '点踩数',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_article` (`article_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

-- ---------------------------------------------------
-- 第二十部分：合成系统模块 (2026-07-10 补充)
-- ---------------------------------------------------

-- 合成记录表
CREATE TABLE IF NOT EXISTS `biz_synthesize_record` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `recipe_id` bigint(20) DEFAULT NULL COMMENT '配方ID',
  `recipe_name` varchar(100) DEFAULT NULL COMMENT '配方名称(冗余)',
  `result_item_id` bigint(20) DEFAULT NULL COMMENT '产物物品ID',
  `result_item_name` varchar(100) DEFAULT NULL COMMENT '产物名称(冗余)',
  `result_quantity` int(11) DEFAULT 1 COMMENT '产物数量',
  `cost_points` int(11) DEFAULT 0 COMMENT '消耗积分',
  `success` tinyint(1) DEFAULT 1 COMMENT '是否成功',
  `status` int(11) DEFAULT 2 COMMENT '状态(1=进行中 2=成功 3=失败)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合成记录表';

-- 合成配方表
CREATE TABLE IF NOT EXISTS `biz_synthesize_recipe` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '配方名称',
  `description` text COMMENT '配方描述',
  `result_item_id` bigint(20) NOT NULL COMMENT '产物物品ID',
  `result_quantity` int(11) DEFAULT 1 COMMENT '产物数量',
  `cost_type` varchar(20) DEFAULT 'materials' COMMENT '消耗类型(materials=材料消耗 items=指定物品消耗)',
  `cost_items` text COMMENT '消耗材料配置(JSON)',
  `cost_points` int(11) DEFAULT 0 COMMENT '额外消耗积分',
  `success_rate` int(11) DEFAULT 100 COMMENT '成功率(%)',
  `status` int(11) DEFAULT 1 COMMENT '状态(0=禁用 1=启用)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_result_item` (`result_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合成配方表';

-- ---------------------------------------------------
-- 第二十一部分：交易/物流/地址模块 (2026-07-10 补充)
-- ---------------------------------------------------

-- O2O核销订单表
CREATE TABLE IF NOT EXISTS `biz_delivery_order` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `order_id` varchar(64) NOT NULL COMMENT '订单号(DLV+时间戳+随机)',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `asset_id` bigint(20) DEFAULT NULL COMMENT '资产ID',
  `item_id` bigint(20) DEFAULT NULL COMMENT '物品ID',
  `item_name` varchar(100) DEFAULT NULL COMMENT '物品名称(冗余)',
  `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片(冗余)',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度(冗余)',
  `receiver` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(500) DEFAULT NULL COMMENT '详细地址',
  `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
  `express_no` varchar(100) DEFAULT NULL COMMENT '快递单号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` int(11) DEFAULT 0 COMMENT '状态(0=待发货 1=已发货 2=已完成 3=已取消)',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='O2O核销订单表';

-- RocketMQ事务日志回查表
CREATE TABLE IF NOT EXISTS `biz_transaction_log` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `transaction_id` varchar(100) DEFAULT NULL COMMENT 'RocketMQ事务消息ID',
  `topic` varchar(100) DEFAULT NULL COMMENT '消息主题',
  `tag` varchar(100) DEFAULT NULL COMMENT '消息标签',
  `status` int(11) DEFAULT 0 COMMENT '事务状态(0准备中 1提交 2回滚)',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型(TRADE_BUY,TRADE_SELL等)',
  `business_data` text COMMENT '业务数据JSON',
  `check_count` int(11) DEFAULT 0 COMMENT '回查次数',
  `last_check_time` datetime DEFAULT NULL COMMENT '最后回查时间',
  `error_message` text COMMENT '错误信息',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RocketMQ事务日志回查表';

-- 用户地址表
CREATE TABLE IF NOT EXISTS `biz_user_address` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `receiver` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `detail_address` varchar(500) DEFAULT NULL COMMENT '详细地址',
  `postal_code` varchar(20) DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(4) DEFAULT 0 COMMENT '是否默认地址(0否 1是)',
  `status` int(11) DEFAULT 1 COMMENT '状态(1=正常 0=禁用)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- 用户关注关系表
CREATE TABLE IF NOT EXISTS `biz_user_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '关注者ID',
  `follow_user_id` bigint(20) NOT NULL COMMENT '被关注者ID',
  `create_time` datetime DEFAULT NULL COMMENT '关注时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_follow` (`user_id`, `follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- 交易订单表
CREATE TABLE IF NOT EXISTS `biz_transaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `order_id` varchar(64) NOT NULL COMMENT '订单号(TXN+时间戳+随机)',
  `buyer_id` bigint(20) NOT NULL COMMENT '买家用户ID',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家用户ID',
  `asset_id` bigint(20) DEFAULT NULL COMMENT '资产ID(用户资产表)',
  `item_id` bigint(20) DEFAULT NULL COMMENT '物品ID',
  `item_name` varchar(100) DEFAULT NULL COMMENT '物品名称(冗余)',
  `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片(冗余)',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度(冗余)',
  `amount` int(11) DEFAULT 0 COMMENT '交易金额(积分)',
  `fee` int(11) DEFAULT 0 COMMENT '手续费(积分,1%)',
  `seller_amount` int(11) DEFAULT 0 COMMENT '卖家实得(积分)',
  `status` int(11) DEFAULT 0 COMMENT '状态(0=处理中 1=成功 2=失败 3=回查中)',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `rocketmq_tx_id` varchar(100) DEFAULT NULL COMMENT 'RocketMQ事务ID',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_buyer` (`buyer_id`),
  KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 迁移完成
-- =====================================================
-- 
-- 表清单（共 29 张表）：
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
-- 17. biz_message (站内消息表)
-- 18. biz_anime_follow (番剧追番记录表)
-- 19. biz_comment_reaction (评论反应表)
-- 20. biz_article_comment_reaction (文章评论反应表)
-- 21. biz_article_reaction (文章反应表)
-- 22. biz_article_comment (文章评论表)
-- 23. biz_synthesize_record (合成记录表)
-- 24. biz_synthesize_recipe (合成配方表)
-- 25. biz_delivery_order (O2O核销订单表)
-- 26. biz_transaction_log (RocketMQ事务日志回查表)
-- 27. biz_user_address (用户地址表)
-- 28. biz_user_follow (用户关注关系表)
-- 29. biz_transaction (交易订单表)
--
-- 使用说明：
-- 1. 本脚本支持重复执行，不会产生错误
-- 2. 使用 MySQL 客户端执行：source /path/to/ACG_Space_V2.1_Complete.sql
-- 3. 或使用数据库管理工具（如 Navicat、DBeaver）直接运行
-- 4. 执行前请确保已创建数据库：CREATE DATABASE acg_space DEFAULT CHARACTER SET utf8mb4;
--
-- =====================================================
