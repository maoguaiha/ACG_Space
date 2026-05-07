-- ==============================================
-- 用户VIP状态和等级迁移
-- ==============================================

-- 添加VIP相关字段到sys_user表
ALTER TABLE `sys_user`
  ADD COLUMN `vip_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'VIP状态 (0=无VIP,1=VIP,2=SVIP)' AFTER `following_count`,
  ADD COLUMN `vip_expire_time` DATETIME DEFAULT NULL COMMENT 'VIP到期时间' AFTER `vip_status`,
  ADD COLUMN `user_level` INT NOT NULL DEFAULT 1 COMMENT '用户等级 (1-100)' AFTER `vip_expire_time`,
  ADD COLUMN `level_experience` INT NOT NULL DEFAULT 0 COMMENT '当前经验值' AFTER `user_level`;

-- 更新现有用户的默认等级（根据积分计算）
-- 积分 0-100 -> 等级1
-- 积分 101-500 -> 等级2
-- 积分 501-1000 -> 等级3
-- 以此类推，每500积分升一级
UPDATE `sys_user` SET `user_level` = GREATEST(1, LEAST(100, 1 + FLOOR(`points` / 500)));
DESCRIBE `sys_user`;

-- ==============================================
-- 迁移完成
-- ==============================================
-- 检查当前表结构
DESCRIBE `biz_article_comment_reaction`;

-- 如果有 comment_id 字段但没有 article_comment_id，需要添加或重命名
-- 添加 article_comment_id 字段（如果不存在）
ALTER TABLE `biz_article_comment_reaction` DROP COLUMN `comment_id`;
-- 如果需要，可以删除旧的 comment_id 字段（确认数据迁移后再删）
-- ALTER TABLE `biz_article_comment_reaction` DROP COLUMN `comment_id`;