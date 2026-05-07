-- 更新用户积分
UPDATE sys_user SET points = 10000 WHERE username = 'admin';
UPDATE sys_user SET points = 5000 WHERE username = 'testuser';

-- 查看更新结果
SELECT username, points FROM sys_user WHERE del_flag = 0;
