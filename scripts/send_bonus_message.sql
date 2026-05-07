-- ========================================
-- 补发注册积分私信脚本
-- 执行前请确保数据库名称正确
-- ========================================

-- 查看所有需要补发私信的用户（未领取过注册积分且未收到私信的用户）
SELECT u.id, u.username, u.create_time
FROM sys_user u
WHERE u.id NOT IN (
    SELECT DISTINCT to_user_id FROM biz_message WHERE from_user_id = 0
)
AND u.id NOT IN (
    SELECT DISTINCT user_id FROM biz_user_points_log WHERE action_type = 'REGISTRATION'
);

-- 为未领取且未收到私信的用户插入私信
INSERT INTO biz_message (from_user_id, to_user_id, content, is_read, create_time)
SELECT
    0 AS from_user_id,
    u.id AS to_user_id,
    CONCAT('欢迎注册ACG Space！点击领取您的新人专属礼包，获得 ', 2600, ' 积分！\n\n[领取积分]') AS content,
    0 AS is_read,
    NOW() AS create_time
FROM sys_user u
WHERE u.id NOT IN (
    SELECT DISTINCT to_user_id FROM biz_message WHERE from_user_id = 0
)
AND u.id NOT IN (
    SELECT DISTINCT user_id FROM biz_user_points_log WHERE action_type = 'REGISTRATION'
);

-- 查看插入结果
SELECT '插入私信数量:' AS info, COUNT(*) AS count
FROM biz_message
WHERE from_user_id = 0
AND create_time > NOW() - INTERVAL 1 MINUTE;
