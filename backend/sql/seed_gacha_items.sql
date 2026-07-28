-- =====================================================
-- 抽赏系统新增数据 Seed（10个物品/奖池关联/兑换商品）
-- 适用: Railway / 本地开发数据库
-- 用法: 在数据库管理工具中执行本文件即可
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 新增物品 (biz_item) — 10个
-- =====================================================
INSERT INTO biz_item (id, item_key, name, type, rarity, image, description, total_stock, remaining_stock, price, marketable, synthesizable, status, create_time, update_time, del_flag) VALUES
(1017, 'ssr_char_003', '空律·白翼', 'character', 'SSR',
 'https://picsum.photos/seed/acg-rin-1017/300/300',
 '驾驭白翼之力的审判者，SSR限定角色。', 100, 100, 9999, 1, 0, 1, NOW(), NOW(), 0),

(1018, 'ssr_skin_001', '幽蝶·星夜', 'skin', 'SSR',
 'https://picsum.photos/seed/acg-skin-1018/300/300',
 '星夜中翩翩起舞的幽蝶之裙，SSR限定皮肤。', 30, 30, 8888, 1, 0, 1, NOW(), NOW(), 0),

(1019, 'sr_char_003', '菲奥娜·冰华', 'character', 'SR',
 'https://picsum.photos/seed/acg-fiona-1019/300/300',
 '冰雪国度的冷艳公主，SR角色。', 500, 500, 2500, 1, 0, 1, NOW(), NOW(), 0),

(1020, 'sr_weapon_002', '影月双刃', 'weapon', 'SR',
 'https://picsum.photos/seed/acg-moon-1020/300/300',
 '沾染月华的双子匕首，SR武器。', 300, 300, 2200, 1, 1, 1, NOW(), NOW(), 0),

(1021, 'sr_skin_003', '水色浴衣', 'skin', 'SR',
 'https://picsum.photos/seed/acg-yukata-1021/300/300',
 '夏日祭典的清爽水色浴衣，SR皮肤。', 200, 200, 1400, 1, 0, 1, NOW(), NOW(), 0),

(1022, 'r_weapon_003', '冰晶法杖', 'weapon', 'R',
 'https://picsum.photos/seed/acg-staff-1022/300/300',
 '凝结寒冰之力的水晶法杖，R武器。', 1000, 1000, 480, 1, 1, 1, NOW(), NOW(), 0),

(1023, 'r_weapon_004', '焰形太刀', 'weapon', 'R',
 'https://picsum.photos/seed/acg-katana-1023/300/300',
 '缠绕着微弱火焰的太刀，R武器。', 1000, 1000, 500, 1, 1, 1, NOW(), NOW(), 0),

(1024, 'r_material_002', '精灵粉尘', 'material', 'R',
 'https://picsum.photos/seed/acg-dust-1024/300/300',
 '蕴含精灵魔力的微光粉尘，R材料。', 2000, 2000, 180, 0, 1, 1, NOW(), NOW(), 0),

(1025, 'n_weapon_002', '训练木刀', 'weapon', 'N',
 'https://picsum.photos/seed/acg-wood-1025/300/300',
 '道场训练用的木刀，N武器。', 9999, 9999, 12, 1, 1, 1, NOW(), NOW(), 0),

(1026, 'n_material_003', '魔力碎片', 'material', 'N',
 'https://picsum.photos/seed/acg-shard-1026/300/300',
 '破碎的魔力结晶碎片，N材料。', 9999, 9999, 18, 0, 1, 1, NOW(), NOW(), 0);


-- =====================================================
-- 2. 奖池物品关联 (biz_gacha_pool_item) — 10条
--    将新物品分配到已有的限定UP池(2001)和常驻池(2002)
-- =====================================================

-- 限定UP池 (2001)
INSERT INTO biz_gacha_pool_item (id, pool_id, item_id, rarity, weight, is_guarantee, stock_limit, create_time, update_time, del_flag) VALUES
(3033, 2001, 1017, 'SSR', 80, 1, NULL, NOW(), NOW(), 0),  -- 空律·白翼 (UP)
(3034, 2001, 1018, 'SSR', 60, 0, 30, NOW(), NOW(), 0),   -- 幽蝶·星夜 (UP)
(3035, 2001, 1019, 'SR', 180, 0, NULL, NOW(), NOW(), 0),  -- 菲奥娜·冰华
(3036, 2001, 1020, 'SR', 150, 0, NULL, NOW(), NOW(), 0),  -- 影月双刃
(3037, 2001, 1021, 'SR', 150, 0, NULL, NOW(), NOW(), 0),  -- 水色浴衣
(3038, 2001, 1024, 'R', 300, 0, NULL, NOW(), NOW(), 0);   -- 精灵粉尘

-- 常驻池 (2002)
INSERT INTO biz_gacha_pool_item (id, pool_id, item_id, rarity, weight, is_guarantee, stock_limit, create_time, update_time, del_flag) VALUES
(3039, 2002, 1022, 'R', 250, 0, NULL, NOW(), NOW(), 0),   -- 冰晶法杖
(3040, 2002, 1023, 'R', 250, 0, NULL, NOW(), NOW(), 0),   -- 焰形太刀
(3041, 2002, 1025, 'N', 500, 0, NULL, NOW(), NOW(), 0),   -- 训练木刀
(3042, 2002, 1026, 'N', 500, 0, NULL, NOW(), NOW(), 0);   -- 魔力碎片


-- =====================================================
-- 3. 兑换商品 (biz_redeem_product) — 10个
-- =====================================================
INSERT INTO biz_redeem_product (id, name, image, description, ur_fragment_cost, points_cost, stock, exchanged_count, status, sort_order, create_time, update_time, del_flag) VALUES
(4001, '绫波·零式 手办',
 'https://picsum.photos/seed/prod-figure-4001/400/400',
 '限定版绫波·零式1/7比例手办，高度还原角色造型。', 5, 5000, 50, 0, 1, 1, NOW(), NOW(), 0),

(4002, '薪炎·觉醒 立牌',
 'https://picsum.photos/seed/prod-standee-4002/400/400',
 '薪炎·觉醒主题亚克力立牌，双面印刷。', 3, 3000, 100, 0, 1, 2, NOW(), NOW(), 0),

(4003, '星穹祈愿 鼠标垫',
 'https://picsum.photos/seed/prod-mousepad-4003/400/400',
 '超大尺寸星穹祈愿主题鼠标垫，80x30cm。', 2, 2000, 200, 0, 1, 3, NOW(), NOW(), 0),

(4004, 'ACG Space 徽章套装',
 'https://picsum.photos/seed/prod-badge-4004/400/400',
 '全套ACG Space角色徽章套装，共6枚。', 1, 1500, 300, 0, 1, 4, NOW(), NOW(), 0),

(4005, '樱花和服 挂画',
 'https://picsum.photos/seed/prod-poster-4005/400/400',
 '樱花和服主题布艺挂画，60x90cm。', 2, 2500, 100, 0, 1, 5, NOW(), NOW(), 0),

(4006, '影月双刃 钥匙扣',
 'https://picsum.photos/seed/prod-keychain-4006/400/400',
 '影月双刃造型金属钥匙扣，做工精致。', 1, 800, 500, 0, 1, 6, NOW(), NOW(), 0),

(4007, '星辰战甲 T恤',
 'https://picsum.photos/seed/prod-tee-4007/400/400',
 '星辰战甲主题印花T恤，纯棉面料。', 2, 1800, 200, 0, 1, 7, NOW(), NOW(), 0),

(4008, '水色浴衣 杯垫套装',
 'https://picsum.photos/seed/prod-coaster-4008/400/400',
 '水色浴衣主题陶瓷杯垫套装，4枚入。', 1, 600, 500, 0, 1, 8, NOW(), NOW(), 0),

(4009, '空律·白翼 挂件',
 'https://picsum.photos/seed/prod-charm-4009/400/400',
 '空律·白翼Q版亚克力挂件，可挂在包上。', 1, 1000, 300, 0, 1, 9, NOW(), NOW(), 0),

(4010, '菲奥娜·冰华 明信片集',
 'https://picsum.photos/seed/prod-postcard-4010/400/400',
 '菲奥娜·冰华主题艺术明信片套装，共12张。', 1, 500, 1000, 0, 1, 10, NOW(), NOW(), 0);

SET FOREIGN_KEY_CHECKS = 1;

SELECT '=== seed done ===' AS info;
SELECT COUNT(*) AS item_count FROM biz_item;
SELECT COUNT(*) AS pool_item_count FROM biz_gacha_pool_item;
SELECT COUNT(*) AS redeem_product_count FROM biz_redeem_product;
