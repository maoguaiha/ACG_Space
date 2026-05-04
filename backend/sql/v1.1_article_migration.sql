-- Idempotent migration for article status and review fields
-- Safe to run multiple times (MySQL 8+ supports ADD COLUMN IF NOT EXISTS)

ALTER TABLE `biz_article`
  ADD COLUMN IF NOT EXISTS `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0=草稿,1=已发布,2=下架,3=待审核,4=驳回',
  ADD COLUMN IF NOT EXISTS `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因';

-- End of migration
