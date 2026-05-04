-- 一次性回填 biz_anime.status
-- 规则：
-- 2 未开播：publish_year > 当前年份
-- 1 已完结：publish_year < 当前年份 且 total_episodes > 0
-- 0 连载中：其余情况（含 publish_year = 当前年份 或无年份）
--
-- 执行前建议先备份：
-- CREATE TABLE biz_anime_bak_20260501 AS SELECT * FROM biz_anime;

START TRANSACTION;

UPDATE biz_anime
SET
  status = CASE
    WHEN publish_year IS NOT NULL AND publish_year > YEAR(CURDATE()) THEN 2
    WHEN publish_year IS NOT NULL AND publish_year < YEAR(CURDATE()) AND IFNULL(total_episodes, 0) > 0 THEN 1
    ELSE 0
  END,
  update_by = 'sql_backfill_status',
  update_time = NOW(),
  remark = CONCAT(
    IFNULL(remark, ''),
    CASE WHEN IFNULL(remark, '') = '' THEN '' ELSE ' | ' END,
    '一次性状态回填: ',
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
  )
WHERE del_flag = 0;

COMMIT;

-- 回填后检查
SELECT status, COUNT(*) AS cnt
FROM biz_anime
WHERE del_flag = 0
GROUP BY status
ORDER BY status;
