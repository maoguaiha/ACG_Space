-- =====================================================
-- ACG Space 基础表结构 (RuoYi-Vue)
-- 在迁移脚本之前运行，提供 sys_user 基础表
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint(20) NOT NULL COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户账号',
  `nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `follower_count` int(11) DEFAULT 0 COMMENT '粉丝数',
  `following_count` int(11) DEFAULT 0 COMMENT '关注数',
  `status` int(11) DEFAULT 0 COMMENT '帐号状态（0正常 1停用）',
  `vip_status` int(11) DEFAULT 0 COMMENT 'VIP状态 (0=无VIP,1=VIP,2=SVIP)',
  `vip_expire_time` datetime DEFAULT NULL COMMENT 'VIP到期时间',
  `user_level` int(11) DEFAULT 1 COMMENT '用户等级 (1-100)',
  `level_experience` int(11) DEFAULT 0 COMMENT '当前经验值',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` int(11) DEFAULT 0 COMMENT '删除标志（0存在 2删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户表';

SET FOREIGN_KEY_CHECKS = 1;
