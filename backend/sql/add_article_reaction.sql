-- ----------------------------
-- 8. 文章点赞点踩记录表
-- ----------------------------
DROP TABLE IF EXISTS `biz_article_reaction`;
CREATE TABLE `biz_article_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` tinyint(4) NOT NULL COMMENT '反应类型 (1点赞 2点踩)',
  `reason` varchar(500) DEFAULT NULL COMMENT '点踩理由',
  
  -- 若依通用字段
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞点踩记录表';

-- 修改文章表，增加点踩数字段
ALTER TABLE `biz_article` ADD COLUMN `dislike_count` int(11) DEFAULT '0' COMMENT '点踩数' AFTER `like_count`;
