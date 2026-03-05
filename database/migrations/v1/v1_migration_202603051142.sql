-- 在活动表中添加是否展示在首页字段
ALTER TABLE `activity`
    ADD COLUMN `show_on_homepage` TINYINT DEFAULT 0 COMMENT
        '是否展示在首页：0-否，1-是' AFTER `is_public`;
