--校促会隐私设置表
create table local_platform_privacy_setting
(
    local_platform_privacy_setting_id bigint auto_increment comment '主键ID'
        primary key,
        platform_id  bigint     not null comment '校促会ID',
        field_name              varchar(64)                        not null comment '字段名称',
    field_code              varchar(64)                        null comment '字段',
    visibility              tinyint  default 0                 not null comment '可见性: 0 不可见；1 可见',
      create_time             datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time             datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)

COMMENT '校促会隐私设置'