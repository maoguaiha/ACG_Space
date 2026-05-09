-- 修改 biz_redeem_order 表中遗留字段为允许 NULL
-- 执行时间: 2026-05-09

-- 修改 asset_id 为允许 NULL
ALTER TABLE biz_redeem_order MODIFY COLUMN asset_id bigint(20) DEFAULT NULL COMMENT '资产ID(兑换订单可为空)';

-- 修改 item_id 为允许 NULL
ALTER TABLE biz_redeem_order MODIFY COLUMN item_id bigint(20) DEFAULT NULL COMMENT '物品ID(兑换订单可为空)';

-- 修改 item_name 为允许 NULL
ALTER TABLE biz_redeem_order MODIFY COLUMN item_name varchar(100) DEFAULT NULL COMMENT '物品名称(兑换订单可为空)';
