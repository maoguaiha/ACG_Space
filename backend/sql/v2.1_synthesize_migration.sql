-- ==============================================
-- ACG Space V2.0 合成系统数据库设计
-- 执行时间: 2026-05-05
-- ==============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 合成配方表 (biz_synthesize_recipe)
-- ----------------------------
DROP TABLE IF EXISTS `biz_synthesize_recipe`;
CREATE TABLE `biz_synthesize_recipe` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `name` varchar(100) NOT NULL COMMENT '配方名称',
  `description` varchar(500) DEFAULT NULL COMMENT '配方描述',
  `result_item_id` bigint(20) NOT NULL COMMENT '产物物品ID',
  `result_quantity` int(11) NOT NULL DEFAULT 1 COMMENT '产物数量',
  `cost_type` varchar(20) NOT NULL DEFAULT 'materials' COMMENT '消耗类型 (materials=材料消耗 items=指定物品消耗)',
  `cost_items` text NOT NULL COMMENT '消耗材料配置 (JSON: [{"itemId":1,"count":3},{"itemId":2,"count":5}])',
  `cost_points` int(11) DEFAULT 0 COMMENT '额外消耗积分',
  `success_rate` int(11) NOT NULL DEFAULT 100 COMMENT '成功率 (%)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 (0=禁用 1=启用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_result_item_id` (`result_item_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合成配方表';

-- ----------------------------
-- 2. 合成记录表 (biz_synthesize_record)
-- ----------------------------
DROP TABLE IF EXISTS `biz_synthesize_record`;
CREATE TABLE `biz_synthesize_record` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `recipe_id` bigint(20) NOT NULL COMMENT '配方ID',
  `recipe_name` varchar(100) NOT NULL COMMENT '配方名称 (冗余)',
  `result_item_id` bigint(20) NOT NULL COMMENT '产物物品ID',
  `result_item_name` varchar(100) NOT NULL COMMENT '产物名称 (冗余)',
  `result_quantity` int(11) NOT NULL COMMENT '产物数量',
  `cost_points` int(11) NOT NULL COMMENT '消耗积分',
  `success` tinyint(1) NOT NULL COMMENT '是否成功',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 (1=进行中 2=成功 3=失败)',
  `create_time` datetime DEFAULT NULL COMMENT '合成时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_recipe_id` (`recipe_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合成记录表';

-- ----------------------------
-- 3. 初始化示例合成配方数据
-- ----------------------------
INSERT INTO `biz_synthesize_recipe` (`id`, `name`, `description`, `result_item_id`, `result_quantity`, `cost_type`, `cost_items`, `cost_points`, `success_rate`, `status`, `create_time`) VALUES
(1, '星空碎片合成', '3个初级强化石可合成1个星空碎片', 1, 1, 'materials', '[{"itemId":5,"count":3}]', 0, 100, 1, NOW()),
(2, '暮光精华合成', '5个初级强化石可合成1个暮光精华', 3, 1, 'materials', '[{"itemId":5,"count":5}]', 0, 100, 1, NOW());

SET FOREIGN_KEY_CHECKS = 1;
