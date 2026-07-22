-- =====================================================
-- ACG_Space 测试数据初始化脚本
-- 包含: 物品(biz_item)、奖池(biz_gacha_pool)、奖池物品(biz_gacha_pool_item)
-- 日期: 2026-07-10
-- =====================================================

SET NAMES utf8mb4;

-- =====================================================
-- 1. 物品数据 (biz_item)
-- 使用小ID(1001~1016)避免与Snowflake ID冲突
-- 稀有度: SSR / SR / R / N  (必须与抽奖代码 rollRarity() 返回值一致)
-- 类型: character / weapon / skin / material
-- =====================================================

-- 清理旧测试数据
DELETE FROM biz_gacha_pool_item WHERE pool_id IN (2001, 2002);
DELETE FROM biz_gacha_pool WHERE id IN (2001, 2002);
DELETE FROM biz_item WHERE id BETWEEN 1001 AND 1016;

-- ===== SSR 级物品 (3个) =====
INSERT INTO biz_item (id, item_key, name, type, rarity, image, description, total_stock, remaining_stock, price, marketable, synthesizable, status, create_time, update_time, del_flag) VALUES
(1001, 'ssr_char_001', '绫波·零式', 'character', 'SSR', 'https://placehold.co/300x300/ff6b6b/white?text=SSR-Lingbo', '来自未来的机械少女，拥有操控时间的能力。SSR限定角色。', 100, 100, 9999, 1, 0, 1, NOW(), NOW(), 0),
(1002, 'ssr_char_002', '薪炎·觉醒', 'character', 'SSR', 'https://placehold.co/300x300/ff6b6b/white?text=SSR-Xinyan', '火焰之神的化身，焚烧一切罪恶。SSR限定角色。', 100, 100, 8888, 1, 0, 1, NOW(), NOW(), 0),
(1003, 'ssr_weapon_001', '天丛云剑', 'weapon', 'SSR', 'https://placehold.co/300x300/ff6b6b/white?text=SSR-Amaterasu', '传说中的神剑，斩断命运之锁。SSR限定武器。', 50, 50, 6666, 1, 1, 1, NOW(), NOW(), 0);

-- ===== SR 级物品 (5个) =====
INSERT INTO biz_item (id, item_key, name, type, rarity, image, description, total_stock, remaining_stock, price, marketable, synthesizable, status, create_time, update_time, del_flag) VALUES
(1004, 'sr_char_001', '琴里·灼焰', 'character', 'SR', 'https://placehold.co/300x300/4ecdc4/white?text=SR-Kotori', '热血开朗的少女，擅长火焰魔法。SR角色。', 500, 500, 3000, 1, 0, 1, NOW(), NOW(), 0),
(1005, 'sr_char_002', '芽衣·雷鸣', 'character', 'SR', 'https://placehold.co/300x300/4ecdc4/white?text=SR-Mei', '优雅而强大的雷电使。SR角色。', 500, 500, 2800, 1, 0, 1, NOW(), NOW(), 0),
(1006, 'sr_weapon_001', '破晓之剑', 'weapon', 'SR', 'https://placehold.co/300x300/4ecdc4/white?text=SR-Dawnblade', '蕴含晨光之力的长剑。SR武器。', 300, 300, 2000, 1, 1, 1, NOW(), NOW(), 0),
(1007, 'sr_skin_001', '星辰战甲', 'skin', 'SR', 'https://placehold.co/300x300/4ecdc4/white?text=SR-Stellar', '镶嵌星辉的战斗装甲。SR皮肤。', 200, 200, 1500, 1, 0, 1, NOW(), NOW(), 0),
(1008, 'sr_skin_002', '樱花和服', 'skin', 'SR', 'https://placehold.co/300x300/4ecdc4/white?text=SR-Sakura', '春日樱花主题的精美和服。SR皮肤。', 200, 200, 1500, 1, 0, 1, NOW(), NOW(), 0);

-- ===== R 级物品 (5个) =====
INSERT INTO biz_item (id, item_key, name, type, rarity, image, description, total_stock, remaining_stock, price, marketable, synthesizable, status, create_time, update_time, del_flag) VALUES
(1009, 'r_weapon_001', '铁制短剑', 'weapon', 'R', 'https://placehold.co/300x300/95e1d3/white?text=R-Ironsword', '坚固的铁制短剑，新手必备。R武器。', 1000, 1000, 500, 1, 1, 1, NOW(), NOW(), 0),
(1010, 'r_weapon_002', '长弓·风息', 'weapon', 'R', 'https://placehold.co/300x300/95e1d3/white?text=R-Windbow', '轻盈的长弓，箭矢如风。R武器。', 1000, 1000, 450, 1, 1, 1, NOW(), NOW(), 0),
(1011, 'r_skin_001', '学院制服', 'skin', 'R', 'https://placehold.co/300x300/95e1d3/white?text=R-Uniform', '标准学院制服。R皮肤。', 800, 800, 300, 1, 0, 1, NOW(), NOW(), 0),
(1012, 'r_char_001', '凛·初音', 'character', 'R', 'https://placehold.co/300x300/95e1d3/white?text=R-Rin', '活泼可爱的双马尾少女。R角色。', 800, 800, 350, 1, 0, 1, NOW(), NOW(), 0),
(1013, 'r_material_001', '秘银矿石', 'material', 'R', 'https://placehold.co/300x300/95e1d3/white?text=R-Mithril', '稀有的锻造材料。R材料。', 2000, 2000, 200, 0, 1, 1, NOW(), NOW(), 0);

-- ===== N 级物品 (3个) =====
INSERT INTO biz_item (id, item_key, name, type, rarity, image, description, total_stock, remaining_stock, price, marketable, synthesizable, status, create_time, update_time, del_flag) VALUES
(1014, 'n_weapon_001', '木棍', 'weapon', 'N', 'https://placehold.co/300x300/c8d6e5/white?text=N-Stick', '一根普通的木棍。N武器。', 9999, 9999, 10, 1, 1, 1, NOW(), NOW(), 0),
(1015, 'n_material_001', '恢复药水', 'material', 'N', 'https://placehold.co/300x300/c8d6e5/white?text=N-Potion', '恢复50点生命值。N材料。', 9999, 9999, 15, 0, 0, 1, NOW(), NOW(), 0),
(1016, 'n_material_002', '强化石', 'material', 'N', 'https://placehold.co/300x300/c8d6e5/white?text=N-Stone', '用于装备强化的基础材料。N材料。', 9999, 9999, 20, 0, 1, 1, NOW(), NOW(), 0);

-- =====================================================
-- 2. 奖池数据 (biz_gacha_pool)
-- =====================================================

-- 奖池1: 限定角色UP池 (SSR保底70抽)
INSERT INTO biz_gacha_pool (id, name, description, banner, rarity, total_stock, remaining_stock, single_cost, ten_cost, guarantee_count, guarantee_type, start_time, end_time, status, weight_config, create_time, update_time, del_flag) VALUES
(2001, '【限定UP】星穹祈愿', '限定SSR角色绫波·零式、薪炎·觉醒概率UP！每70抽必出SSR！',
 'https://placehold.co/600x200/9966cc/white?text=Banner+Stellar',
 'SSR', 10000, 10000, 100, 900, 70, 'count',
 '2026-07-01 00:00:00', '2026-12-31 23:59:59', 1,
 '{"SSR":3,"SR":20,"R":200,"N":777}',
 NOW(), NOW(), 0);

-- 奖池2: 常驻祈愿池 (SR保底10抽)
INSERT INTO biz_gacha_pool (id, name, description, banner, rarity, total_stock, remaining_stock, single_cost, ten_cost, guarantee_count, guarantee_type, start_time, end_time, status, weight_config, create_time, update_time, del_flag) VALUES
(2002, '【常驻】初心祈愿', '常驻奖池，每10抽必出SR及以上物品！',
 'https://placehold.co/600x200/4a90d9/white?text=Banner+Normal',
 'normal', 50000, 50000, 80, 720, 10, 'rarity',
 '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1,
 '{"SSR":3,"SR":20,"R":200,"N":777}',
 NOW(), NOW(), 0);

-- =====================================================
-- 3. 奖池物品关联 (biz_gacha_pool_item)
-- 注意: weight 字段当前未参与抽奖算法(等概率随机)，仅做展示用
-- =====================================================

-- 奖池1: 限定UP池 - 包含全部16个物品
INSERT INTO biz_gacha_pool_item (id, pool_id, item_id, weight, create_time, update_time, del_flag) VALUES
-- SSR (UP物品，高权重)
(3001, 2001, 1001, 100, NOW(), NOW(), 0),  -- 绫波·零式 (UP)
(3002, 2001, 1002, 100, NOW(), NOW(), 0),  -- 薪炎·觉醒 (UP)
(3003, 2001, 1003, 50, NOW(), NOW(), 0),   -- 天丛云剑
-- SR
(3004, 2001, 1004, 50, NOW(), NOW(), 0),   -- 琴里·灼焰
(3005, 2001, 1005, 50, NOW(), NOW(), 0),   -- 芽衣·雷鸣
(3006, 2001, 1006, 50, NOW(), NOW(), 0),   -- 破晓之剑
(3007, 2001, 1007, 50, NOW(), NOW(), 0),   -- 星辰战甲
(3008, 2001, 1008, 50, NOW(), NOW(), 0),   -- 樱花和服
-- R
(3009, 2001, 1009, 30, NOW(), NOW(), 0),   -- 铁制短剑
(3010, 2001, 1010, 30, NOW(), NOW(), 0),   -- 长弓·风息
(3011, 2001, 1011, 30, NOW(), NOW(), 0),   -- 学院制服
(3012, 2001, 1012, 30, NOW(), NOW(), 0),   -- 凛·初音
(3013, 2001, 1013, 30, NOW(), NOW(), 0),   -- 秘银矿石
-- N
(3014, 2001, 1014, 10, NOW(), NOW(), 0),   -- 木棍
(3015, 2001, 1015, 10, NOW(), NOW(), 0),   -- 恢复药水
(3016, 2001, 1016, 10, NOW(), NOW(), 0);   -- 强化石

-- 奖池2: 常驻池 - 同样包含全部16个物品
INSERT INTO biz_gacha_pool_item (id, pool_id, item_id, weight, create_time, update_time, del_flag) VALUES
(3017, 2002, 1001, 50, NOW(), NOW(), 0),
(3018, 2002, 1002, 50, NOW(), NOW(), 0),
(3019, 2002, 1003, 50, NOW(), NOW(), 0),
(3020, 2002, 1004, 50, NOW(), NOW(), 0),
(3021, 2002, 1005, 50, NOW(), NOW(), 0),
(3022, 2002, 1006, 50, NOW(), NOW(), 0),
(3023, 2002, 1007, 50, NOW(), NOW(), 0),
(3024, 2002, 1008, 50, NOW(), NOW(), 0),
(3025, 2002, 1009, 30, NOW(), NOW(), 0),
(3026, 2002, 1010, 30, NOW(), NOW(), 0),
(3027, 2002, 1011, 30, NOW(), NOW(), 0),
(3028, 2002, 1012, 30, NOW(), NOW(), 0),
(3029, 2002, 1013, 30, NOW(), NOW(), 0),
(3030, 2002, 1014, 10, NOW(), NOW(), 0),
(3031, 2002, 1015, 10, NOW(), NOW(), 0),
(3032, 2002, 1016, 10, NOW(), NOW(), 0);

-- =====================================================
-- 验证
-- =====================================================
SELECT '=== 物品统计 ===' AS info;
SELECT rarity, type, COUNT(*) AS cnt FROM biz_item WHERE del_flag=0 GROUP BY rarity, type ORDER BY rarity, type;

SELECT '=== 奖池列表 ===' AS info;
SELECT id, name, status, single_cost, ten_cost, guarantee_count, guarantee_type, remaining_stock FROM biz_gacha_pool WHERE del_flag=0;

SELECT '=== 奖池物品关联 ===' AS info;
SELECT pool_id, COUNT(*) AS item_count FROM biz_gacha_pool_item WHERE del_flag=0 GROUP BY pool_id;
