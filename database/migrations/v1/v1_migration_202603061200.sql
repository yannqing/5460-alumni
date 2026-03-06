-- 为 local_platform 表增加 important_events 字段（校促会重大事记，JSON格式）
ALTER TABLE local_platform
    ADD COLUMN important_events JSON NULL COMMENT '校促会重大事记' AFTER mini_program_links;

-- 为 local_platform_member 表增加 is_show 字段（是否在主页展示）
ALTER TABLE local_platform_member
    ADD COLUMN is_show TINYINT DEFAULT 0 NULL COMMENT '是否在主页展示(0否,1是)' AFTER social_duties;
