ALTER TABLE local_platform
    ADD COLUMN mini_program_links JSON COMMENT
        '小程序链接列表，JSON数组格式：[{id, text, url}]' AFTER phone;

