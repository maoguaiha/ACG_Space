-- 插入测试用户
INSERT INTO sys_user (id, username, nickname, password, points, vip_status, user_level, level_experience, del_flag) 
VALUES (1, 'admin', '管理员', 'admin123', 10000, 1, 10, 0, 0);

-- 插入更多测试用户
INSERT INTO sys_user (id, username, nickname, password, points, vip_status, user_level, level_experience, del_flag) 
VALUES (2, 'testuser', '测试用户', '123456', 5000, 0, 1, 0, 0);
