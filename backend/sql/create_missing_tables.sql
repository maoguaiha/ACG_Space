-- ============================================================
-- ACG_Space 缺失表一次性创建脚本
-- 适用：已有数据库发现缺少实体类对应表的情况
-- 日期：2026-07-10
-- ============================================================

-- 1. biz_comment_reaction (评论反应表)
CREATE TABLE IF NOT EXISTS `biz_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` int(11) DEFAULT NULL COMMENT '反应类型(1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论反应表';

-- 2. biz_article_comment_reaction (文章评论反应表)
CREATE TABLE IF NOT EXISTS `biz_article_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_comment_id` bigint(20) NOT NULL COMMENT '文章评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` int(11) DEFAULT NULL COMMENT '反应类型(1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_user` (`article_comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论反应表';

-- 3. biz_article_reaction (文章反应表)
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

-- 4. biz_article_comment (文章评论表)
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

-- 5. biz_synthesize_record (合成记录表)
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

-- 6. biz_synthesize_recipe (合成配方表)
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

-- 7. biz_delivery_order (O2O核销订单表)
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

-- 8. biz_transaction_log (RocketMQ事务日志回查表)
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

-- 9. biz_user_address (用户地址表)
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

-- 10. biz_user_follow (用户关注关系表)
CREATE TABLE IF NOT EXISTS `biz_user_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '关注者ID',
  `follow_user_id` bigint(20) NOT NULL COMMENT '被关注者ID',
  `create_time` datetime DEFAULT NULL COMMENT '关注时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_follow` (`user_id`, `follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- 11. biz_transaction (交易订单表)
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
