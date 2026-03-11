-- 修改用户隐私设置默认值为：不可展示，不可搜索

alter table user_privacy_setting
    alter column visibility set default 0;

alter table user_privacy_setting
    alter column searchable set default 0;
