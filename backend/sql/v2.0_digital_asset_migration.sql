-- ==============================================
-- ACG Space V2.0 数据库迁移脚本
-- 数字资产系统 & O2O核销系统
-- 执行时间: 2026-05-05
-- ==============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 物品/商品表 (biz_item)
-- ----------------------------
DROP TABLE IF EXISTS `biz_item`;
CREATE TABLE `biz_item` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `item_key` varchar(100) NOT NULL COMMENT '物品唯一标识 (如 item_ssr_001)',
  `name` varchar(100) NOT NULL COMMENT '物品名称',
  `type` varchar(20) NOT NULL COMMENT '物品类型 (character/ weapon/skin/material)',
  `rarity` varchar(10) NOT NULL COMMENT '稀有度 (SSR/SR/R/N)',
  `image` varchar(500) DEFAULT NULL COMMENT '物品图片URL',
  `description` varchar(500) DEFAULT NULL COMMENT '物品描述',
  `total_stock` int(11) NOT NULL DEFAULT 0 COMMENT '总库存',
  `remaining_stock` int(11) NOT NULL DEFAULT 0 COMMENT '剩余库存',
  `price` int(11) NOT NULL DEFAULT 0 COMMENT '参考价格(积分)',
  `marketable` tinyint(1) DEFAULT 1 COMMENT '是否可上架市场 (0否 1是)',
  `synthesizable` tinyint(1) DEFAULT 0 COMMENT '是否可合成 (0否 1是)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_key` (`item_key`),
  KEY `idx_rarity` (`rarity`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品/商品表';

-- ----------------------------
-- 2. 用户资产表 (biz_user_asset)
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_asset`;
CREATE TABLE `biz_user_asset` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `item_id` bigint(20) NOT NULL COMMENT '物品ID',
  `asset_key` varchar(100) NOT NULL COMMENT '资产唯一标识 (用户+物品组合)',
  `quantity` int(11) NOT NULL DEFAULT 1 COMMENT '持有数量',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 (1=正常 2=锁定 3=已使用 4=已合成)',
  `acquire_type` varchar(20) NOT NULL COMMENT '获取方式 (gacha/market/synthesize/gift)',
  `acquire_source_id` varchar(64) DEFAULT NULL COMMENT '获取来源ID (抽赏记录ID/订单ID等)',
  `is_certified` tinyint(1) DEFAULT 0 COMMENT '是否已认证 (O2O核销需要)',
  `certified_time` datetime DEFAULT NULL COMMENT '认证时间',
  `create_time` datetime DEFAULT NULL COMMENT '获取时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_key` (`asset_key`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资产表';

-- ----------------------------
-- 3. 抽赏奖池表 (biz_gacha_pool)
-- ----------------------------
DROP TABLE IF EXISTS `biz_gacha_pool`;
CREATE TABLE `biz_gacha_pool` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `name` varchar(100) NOT NULL COMMENT '奖池名称',
  `description` varchar(500) DEFAULT NULL COMMENT '奖池描述',
  `banner` varchar(500) DEFAULT NULL COMMENT '奖池Banner图片URL',
  `rarity` varchar(10) NOT NULL DEFAULT 'SSR' COMMENT '限定稀有度 (SSR/SR/normal)',
  `total_stock` int(11) NOT NULL DEFAULT 0 COMMENT '奖池总库存',
  `remaining_stock` int(11) NOT NULL DEFAULT 0 COMMENT '奖池剩余库存',
  `single_cost` int(11) NOT NULL DEFAULT 280 COMMENT '单抽价格(积分)',
  `ten_cost` int(11) NOT NULL DEFAULT 2600 COMMENT '十连价格(积分)',
  `guarantee_count` int(11) NOT NULL DEFAULT 10 COMMENT '保底次数 (多少抽必出SSR)',
  `guarantee_type` varchar(20) NOT NULL DEFAULT ' rarity' COMMENT '保底类型 (rarity=稀有度保底 count=次数保底)',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 (0=未开始 1=进行中 2=已结束)',
  `weight_config` text COMMENT '权重配置 (JSON格式: [{"itemId":1,"rarity":"SSR","weight":1}])',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽赏奖池表';

-- ----------------------------
-- 4. 抽赏奖池物品配置表 (biz_gacha_pool_item)
-- ----------------------------
DROP TABLE IF EXISTS `biz_gacha_pool_item`;
CREATE TABLE `biz_gacha_pool_item` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `pool_id` bigint(20) NOT NULL COMMENT '奖池ID',
  `item_id` bigint(20) NOT NULL COMMENT '物品ID',
  `rarity` varchar(10) NOT NULL COMMENT '物品稀有度',
  `weight` int(11) NOT NULL DEFAULT 1 COMMENT '权重',
  `is_guarantee` tinyint(1) DEFAULT 0 COMMENT '是否保底物品 (0否 1是)',
  `stock_limit` int(11) DEFAULT NULL COMMENT '库存上限 (NULL表示不限)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_pool_id` (`pool_id`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽赏奖池物品配置表';

-- ----------------------------
-- 5. 抽赏记录表 (biz_gacha_record)
-- ----------------------------
DROP TABLE IF EXISTS `biz_gacha_record`;
CREATE TABLE `biz_gacha_record` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `pool_id` bigint(20) NOT NULL COMMENT '奖池ID',
  `pool_name` varchar(100) NOT NULL COMMENT '奖池名称 (冗余)',
  `gacha_type` tinyint(4) NOT NULL COMMENT '抽赏类型 (1=单抽 10=十连)',
  `cost_points` int(11) NOT NULL COMMENT '消耗积分',
  `result_items` text NOT NULL COMMENT '抽赏结果 (JSON数组: [{"itemId":1,"itemName":"xxx","rarity":"SSR"}])',
  `is_guaranteed` tinyint(1) DEFAULT 0 COMMENT '是否触发保底',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '关联交易ID',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 (1=成功 2=失败 3=退款)',
  `create_time` datetime DEFAULT NULL COMMENT '抽赏时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pool_id` (`pool_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽赏记录表';

-- ----------------------------
-- 6. 交易订单表 (biz_transaction)
-- ----------------------------
DROP TABLE IF EXISTS `biz_transaction`;
CREATE TABLE `biz_transaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `order_id` varchar(64) NOT NULL COMMENT '订单号 (TXN+时间戳+随机)',
  `buyer_id` bigint(20) NOT NULL COMMENT '买家用户ID',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家用户ID',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID (用户资产表)',
  `item_id` bigint(20) NOT NULL COMMENT '物品ID',
  `item_name` varchar(100) NOT NULL COMMENT '物品名称 (冗余)',
  `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片 (冗余)',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度 (冗余)',
  `amount` int(11) NOT NULL COMMENT '交易金额(积分)',
  `fee` int(11) NOT NULL DEFAULT 0 COMMENT '手续费(积分, 1%)',
  `seller_amount` int(11) NOT NULL COMMENT '卖家实得(积分)',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 (0=处理中 1=成功 2=失败 3=回查中)',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `rocketmq_tx_id` varchar(64) DEFAULT NULL COMMENT 'RocketMQ事务ID',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- ----------------------------
-- 7. O2O核销订单表 (biz_delivery_order)
-- ----------------------------
DROP TABLE IF EXISTS `biz_delivery_order`;
CREATE TABLE `biz_delivery_order` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `order_id` varchar(64) NOT NULL COMMENT '订单号 (DLV+时间戳+随机)',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `item_id` bigint(20) NOT NULL COMMENT '物品ID',
  `item_name` varchar(100) NOT NULL COMMENT '物品名称 (冗余)',
  `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片 (冗余)',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度 (冗余)',
  `receiver` varchar(50) NOT NULL COMMENT '收货人姓名',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `address` varchar(255) NOT NULL COMMENT '详细地址',
  `express_company` varchar(50) DEFAULT NULL COMMENT '快递公司',
  `express_no` varchar(64) DEFAULT NULL COMMENT '快递单号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 (0=待发货 1=已发货 2=已完成 3=已取消)',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='O2O核销订单表';

-- ----------------------------
-- 8. 用户地址表 (biz_user_address)
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_address`;
CREATE TABLE `biz_user_address` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `receiver` varchar(50) NOT NULL COMMENT '收货人姓名',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `postal_code` varchar(10) DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认地址 (0否 1是)',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 (1=正常 0=禁用)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- ----------------------------
-- 9. 用户背包视图 (方便查询)
-- ----------------------------
DROP VIEW IF EXISTS `v_user_backpack`;
CREATE VIEW `v_user_backpack` AS
SELECT
  a.id,
  a.user_id,
  a.item_id,
  i.name AS item_name,
  i.type AS item_type,
  i.rarity AS item_rarity,
  i.image AS item_image,
  i.description AS item_description,
  a.quantity,
  a.status,
  a.acquire_type,
  a.acquire_source_id,
  a.create_time,
  CASE
    WHEN a.status = 1 THEN '正常'
    WHEN a.status = 2 THEN '锁定'
    WHEN a.status = 3 THEN '已使用'
    WHEN a.status = 4 THEN '已合成'
    ELSE '未知'
  END AS status_name
FROM `biz_user_asset` a
LEFT JOIN `biz_item` i ON a.item_id = i.id
WHERE a.del_flag = 0;

-- ----------------------------
-- 6.5 市场挂单表 (biz_market_item)
-- ----------------------------
DROP TABLE IF EXISTS `biz_market_item`;
CREATE TABLE `biz_market_item` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `asset_id` bigint(20) NOT NULL COMMENT '用户资产ID',
  `item_id` bigint(20) NOT NULL COMMENT '物品ID',
  `item_name` varchar(100) NOT NULL COMMENT '物品名称',
  `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度',
  `item_type` varchar(20) DEFAULT NULL COMMENT '物品类型',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家用户ID',
  `price` int(11) NOT NULL COMMENT '挂单价格(积分)',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 (0=待售 1=已售 2=已下架)',
  `order_id` varchar(64) DEFAULT NULL COMMENT '市场订单号',
  `sold_time` datetime DEFAULT NULL COMMENT '售出时间',
  `delist_time` datetime DEFAULT NULL COMMENT '下架时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场挂单表';

-- ----------------------------
-- 10. 初始化示例数据
-- ----------------------------

-- 插入示例物品
INSERT INTO `biz_item` (`id`, `item_key`, `name`, `type`, `rarity`, `image`, `description`, `total_stock`, `remaining_stock`, `price`, `marketable`, `synthesizable`, `create_time`) VALUES
(1, 'char_ssr_001', '星空彼岸', 'character', 'SSR', 'https://picsum.photos/seed/item1/200/200', '限定角色，可用于合成', 1000, 847, 15000, 1, 1, NOW()),
(2, 'char_ssr_002', '月下独酌', 'character', 'SSR', 'https://picsum.photos/seed/item2/200/200', '限定角色，可用于合成', 1000, 892, 18000, 1, 1, NOW()),
(3, 'wpn_sr_001', '暮光之刃', 'weapon', 'SR', 'https://picsum.photos/seed/item3/200/200', 'SR级武器', 5000, 3421, 3200, 1, 0, NOW()),
(4, 'char_sr_001', '梦境使者', 'character', 'SR', 'https://picsum.photos/seed/item4/200/200', 'SR级角色', 8000, 5612, 4500, 1, 1, NOW()),
(5, 'mat_r_001', '初级强化石', 'material', 'R', 'https://picsum.photos/seed/item5/200/200', '强化材料x10', 100000, 45678, 100, 0, 0, NOW());

-- 插入示例奖池
INSERT INTO `biz_gacha_pool` (`id`, `name`, `description`, `banner`, `rarity`, `total_stock`, `remaining_stock`, `single_cost`, `ten_cost`, `guarantee_count`, `guarantee_type`, `start_time`, `end_time`, `status`, `weight_config`, `create_time`) VALUES
(1, '限定幻想·SSR精选', '限定角色登场，精选SSR概率提升！', 'https://picsum.photos/seed/gacha1/640/320', 'SSR', 50000, 8847, 280, 2600, 10, 'rarity', '2026-04-01 00:00:00', '2026-05-31 23:59:59', 1, '[{"itemId":1,"rarity":"SSR","weight":1},{"itemId":2,"rarity":"SSR","weight":1},{"itemId":3,"rarity":"SR","weight":15},{"itemId":4,"rarity":"SR","weight":20},{"itemId":5,"rarity":"R","weight":63}]', NOW()),
(2, '节日庆典·萌系精选', '节日限定角色服装复刻', 'https://picsum.photos/seed/gacha2/640/320', 'SR', 100000, 23456, 180, 1700, 10, 'rarity', '2026-04-15 00:00:00', '2026-05-15 23:59:59', 1, '[{"itemId":4,"rarity":"SR","weight":30},{"itemId":5,"rarity":"R","weight":70}]', NOW());

-- ----------------------------
-- 11. 为 sys_user 添加 V2.0 相关字段
-- ----------------------------
ALTER TABLE `sys_user`
  ADD COLUMN `total_assets` int(11) DEFAULT 0 COMMENT '总资产(积分)' AFTER `following_count`,
  ADD COLUMN `v2_points` int(11) DEFAULT 0 COMMENT 'V2积分(可用于市场交易)' AFTER `total_assets`;

-- ==============================================
-- 迁移完成
-- ==============================================

-- ----------------------------
-- 修复已存在表的缺失字段 (如果表已存在)
-- ----------------------------
-- 为 biz_transaction 表添加缺失字段 (如果不存在)
ALTER TABLE `biz_transaction`
  ADD COLUMN `create_by` varchar(64) DEFAULT '' COMMENT '创建者' AFTER `complete_time`,
  ADD COLUMN `update_by` varchar(64) DEFAULT '' COMMENT '更新者' AFTER `create_time`,
  ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `update_time`,
  ADD COLUMN `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)' AFTER `remark`;

-- 为 biz_delivery_order 表添加缺失字段
ALTER TABLE `biz_delivery_order`
  ADD COLUMN `create_by` varchar(64) DEFAULT '' COMMENT '创建者' AFTER `complete_time`,
  ADD COLUMN `update_by` varchar(64) DEFAULT '' COMMENT '更新者' AFTER `create_time`,
  ADD COLUMN `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)' AFTER `update_time`;

-- 为 biz_gacha_record 表添加缺失列
ALTER TABLE `biz_gacha_record`
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)' AFTER `update_time`;

-- 为 biz_gacha_pool_item 表添加缺失列
ALTER TABLE `biz_gacha_pool_item`
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)' AFTER `update_time`;

-- 创建 biz_market_item 表 (如果不存在)
CREATE TABLE IF NOT EXISTS `biz_market_item` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `asset_id` bigint(20) NOT NULL COMMENT '用户资产ID',
  `item_id` bigint(20) NOT NULL COMMENT '物品ID',
  `item_name` varchar(100) NOT NULL COMMENT '物品名称',
  `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度',
  `item_type` varchar(20) DEFAULT NULL COMMENT '物品类型',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家用户ID',
  `price` int(11) NOT NULL COMMENT '挂单价格(积分)',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 (0=待售 1=已售 2=已下架)',
  `order_id` varchar(64) DEFAULT NULL COMMENT '市场订单号',
  `sold_time` datetime DEFAULT NULL COMMENT '售出时间',
  `delist_time` datetime DEFAULT NULL COMMENT '下架时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场挂单表';


SET FOREIGN_KEY_CHECKS = 1;
