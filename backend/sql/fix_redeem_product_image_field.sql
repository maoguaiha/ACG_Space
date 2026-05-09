-- ========================================
-- 修复兑换商品表 image 字段长度不足问题
-- 日期: 2026-05-09
-- 说明: 原 image 字段为 varchar(500)，无法存储完整的 base64 图片数据
--       修改为 longtext 类型以支持大图片存储
-- ========================================

ALTER TABLE `biz_redeem_product` 
MODIFY COLUMN `image` longtext COMMENT '商品图片(base64或URL)';
