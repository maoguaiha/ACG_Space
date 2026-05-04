-- Add dislikes column to comment tables for reaction support
-- For MySQL: column is added only if it doesn't exist (idempotent)

ALTER TABLE `biz_comment` ADD COLUMN `dislikes` INT DEFAULT 0 AFTER `likes`;

ALTER TABLE `biz_article_comment` ADD COLUMN `dislikes` INT DEFAULT 0 AFTER `likes`;

-- End of migration