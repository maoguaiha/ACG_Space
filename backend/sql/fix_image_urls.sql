-- =====================================================
-- 修复旧物品图片URL：placehold.co 文字块 → picsum.photos 真实照片
-- 适用：已使用 ACG_Space_init.sql 初始化过的数据库
-- 用法：在数据库管理工具或 Railway MySQL 终端中执行
-- =====================================================

SET NAMES utf8mb4;

-- biz_item 图片修复
UPDATE biz_item SET image = 'https://picsum.photos/seed/ssr-lingbo-1001/300/300' WHERE id = 1001;
UPDATE biz_item SET image = 'https://picsum.photos/seed/ssr-xinyan-1002/300/300' WHERE id = 1002;
UPDATE biz_item SET image = 'https://picsum.photos/seed/ssr-amaterasu-1003/300/300' WHERE id = 1003;
UPDATE biz_item SET image = 'https://picsum.photos/seed/sr-kotori-1004/300/300' WHERE id = 1004;
UPDATE biz_item SET image = 'https://picsum.photos/seed/sr-mei-1005/300/300' WHERE id = 1005;
UPDATE biz_item SET image = 'https://picsum.photos/seed/sr-dawnblade-1006/300/300' WHERE id = 1006;
UPDATE biz_item SET image = 'https://picsum.photos/seed/sr-stellar-1007/300/300' WHERE id = 1007;
UPDATE biz_item SET image = 'https://picsum.photos/seed/sr-sakura-1008/300/300' WHERE id = 1008;
UPDATE biz_item SET image = 'https://picsum.photos/seed/r-ironsword-1009/300/300' WHERE id = 1009;
UPDATE biz_item SET image = 'https://picsum.photos/seed/r-windbow-1010/300/300' WHERE id = 1010;
UPDATE biz_item SET image = 'https://picsum.photos/seed/r-uniform-1011/300/300' WHERE id = 1011;
UPDATE biz_item SET image = 'https://picsum.photos/seed/r-rin-1012/300/300' WHERE id = 1012;
UPDATE biz_item SET image = 'https://picsum.photos/seed/r-mithril-1013/300/300' WHERE id = 1013;
UPDATE biz_item SET image = 'https://picsum.photos/seed/n-stick-1014/300/300' WHERE id = 1014;
UPDATE biz_item SET image = 'https://picsum.photos/seed/n-potion-1015/300/300' WHERE id = 1015;
UPDATE biz_item SET image = 'https://picsum.photos/seed/n-stone-1016/300/300' WHERE id = 1016;

SELECT CONCAT('已更新 ', ROW_COUNT(), ' 条 biz_item 图片') AS result;

-- biz_gacha_pool banner 图片修复
UPDATE biz_gacha_pool SET banner = 'https://picsum.photos/seed/pool-stellar-2001/600/200' WHERE id = 2001;
UPDATE biz_gacha_pool SET banner = 'https://picsum.photos/seed/pool-normal-2002/600/200' WHERE id = 2002;

SELECT CONCAT('已更新 ', ROW_COUNT(), ' 条 biz_gacha_pool 横幅') AS result;

-- 验证
SELECT id, name, image FROM biz_item WHERE image NOT LIKE '%picsum%';
