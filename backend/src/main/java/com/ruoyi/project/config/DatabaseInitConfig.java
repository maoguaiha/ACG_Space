package com.ruoyi.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化配置 - 修改表结构以支持新功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitConfig implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        modifyImageColumn();
    }

    private void modifyImageColumn() {
        try {
            // 检查当前 image 列的类型
            String checkSql = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_item' AND COLUMN_NAME = 'image'";
            String currentType = jdbcTemplate.queryForObject(checkSql, String.class);
            log.info("当前 biz_item.image 列类型: {}", currentType);

            // 如果是 VARCHAR 类型，则修改为 LONGTEXT
            if (currentType != null && currentType.toUpperCase().contains("VARCHAR")) {
                String alterSql = "ALTER TABLE `biz_item` MODIFY COLUMN `image` LONGTEXT DEFAULT NULL COMMENT '物品图片URL'";
                jdbcTemplate.execute(alterSql);
                log.info("biz_item.image 列已修改为 LONGTEXT");
            }

            // 检查并修改 description 列
            String descCheckSql = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_item' AND COLUMN_NAME = 'description'";
            String descCurrentType = jdbcTemplate.queryForObject(descCheckSql, String.class);
            log.info("当前 biz_item.description 列类型: {}", descCurrentType);

            if (descCurrentType != null && descCurrentType.toUpperCase().contains("VARCHAR")) {
                String descAlterSql = "ALTER TABLE `biz_item` MODIFY COLUMN `description` LONGTEXT DEFAULT NULL COMMENT '物品描述'";
                jdbcTemplate.execute(descAlterSql);
                log.info("biz_item.description 列已修改为 LONGTEXT");
            }
        } catch (Exception e) {
            log.warn("修改表结构时发生异常，可能已经修改过或权限不足: {}", e.getMessage());
        }
    }
}