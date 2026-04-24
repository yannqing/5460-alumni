create table user_favorite
(
    favorite_id  bigint auto_increment
        primary key,
    wx_id        bigint                             not null comment '用户ID（关联微信用户信息）',
    target_type  tinyint  default 1                 not null comment '1-商户',
    target_id    bigint                             not null comment '收藏目标ID（对应各业务表的id）',
    remark       varchar(100)                       null comment '收藏备注',
    created_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted   tinyint  default 0                 null comment '是否逻辑删除：0-否，1-是',
    constraint uk_user_target
        unique (wx_id, target_type, target_id)
)
    comment '用户收藏关系表';

create index idx_target
    on user_favorite (target_type, target_id);

create index idx_user
    on user_favorite (wx_id);

