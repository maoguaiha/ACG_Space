-- Idempotent migration: create reaction tables if they don't exist
-- Safe to run multiple times

CREATE TABLE IF NOT EXISTS `biz_comment_reaction` (
  `id` BIGINT NOT NULL,
  `comment_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `reaction_type` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `biz_article_comment_reaction` (
  `id` BIGINT NOT NULL,
  `article_comment_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `reaction_type` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_comment_user` (`article_comment_id`,`user_id`),
  KEY `idx_article_comment_id` (`article_comment_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- No AUTO_INCREMENT: application supplies IDs (snowflake). If you prefer auto-increment,
-- change the `id` column accordingly.

-- End of migration
