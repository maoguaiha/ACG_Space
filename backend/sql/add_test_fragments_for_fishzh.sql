-- ============================================================
-- 测试数据：为 fishzh 用户添加各类碎片到背包
-- 积分需要通过 Redis 添加（见下方说明）
-- ============================================================

-- 1. 查找 fishzh 用户ID
-- SELECT id, username, nickname FROM sys_user WHERE username = 'fishzh';

-- 2. 替换为你的实际用户ID（请从上方查询结果中获取）
SET @fishzh_user_id = 2051310244433973250;

-- ============================================================
-- 3. 添加各类碎片到用户背包
-- ============================================================

-- 添加 SR 碎片 50 个
INSERT INTO biz_user_asset (
    id, user_id, item_id, asset_key, quantity, status, 
    is_physical, acquire_type, acquire_source_id, 
    item_name, item_image, item_rarity, item_type,
    create_time, update_time, del_flag
) VALUES (
    FLOOR(UNIX_TIMESTAMP() * 1000) + 1, @fishzh_user_id, 0, 
    CONCAT(@fishzh_user_id, '_fragment_SR'), 50, 1,
    0, 'test', 'test_add_fragment_sr',
    'SR碎片', 
    'https://picsum.photos/seed/fragment_sr/200/200',
    'SR', 'fragment',
    NOW(), NOW(), 0
) ON DUPLICATE KEY UPDATE quantity = quantity + 50, update_time = NOW();

-- 添加 SSR 碎片 30 个
INSERT INTO biz_user_asset (
    id, user_id, item_id, asset_key, quantity, status, 
    is_physical, acquire_type, acquire_source_id, 
    item_name, item_image, item_rarity, item_type,
    create_time, update_time, del_flag
) VALUES (
    FLOOR(UNIX_TIMESTAMP() * 1000) + 2, @fishzh_user_id, 0, 
    CONCAT(@fishzh_user_id, '_fragment_SSR'), 30, 1,
    0, 'test', 'test_add_fragment_ssr',
    'SSR碎片', 
    'https://picsum.photos/seed/fragment_ssr/200/200',
    'SSR', 'fragment',
    NOW(), NOW(), 0
) ON DUPLICATE KEY UPDATE quantity = quantity + 30, update_time = NOW();

-- 添加 UR 碎片 10 个
INSERT INTO biz_user_asset (
    id, user_id, item_id, asset_key, quantity, status, 
    is_physical, acquire_type, acquire_source_id, 
    item_name, item_image, item_rarity, item_type,
    create_time, update_time, del_flag
) VALUES (
    FLOOR(UNIX_TIMESTAMP() * 1000) + 3, @fishzh_user_id, 0, 
    CONCAT(@fishzh_user_id, '_fragment_UR'), 10, 1,
    0, 'test', 'test_add_fragment_ur',
    'UR碎片', 
    'https://picsum.photos/seed/fragment_ur/200/200',
    'UR', 'fragment',
    NOW(), NOW(), 0
) ON DUPLICATE KEY UPDATE quantity = quantity + 10, update_time = NOW();

-- ============================================================
-- 4. 验证数据
-- ============================================================

-- 查看用户背包中的碎片
SELECT 
    id, item_name, item_rarity, quantity, status, item_type
FROM biz_user_asset 
WHERE user_id = @fishzh_user_id 
  AND item_type = 'fragment'
  AND del_flag = 0
ORDER BY item_rarity;

-- ============================================================
-- 5. 添加积分（通过 Redis CLI 执行）
-- ============================================================
-- 打开 Redis CLI 执行以下命令：
-- redis-cli
-- SET user:points:2051310244433973250 500
-- （将 2051310244433973250 替换为实际用户ID）
