create table points_change
(
    id              bigint   not null auto_increment comment '主键ID',
    wx_id           bigint   not null comment '用户wxid（关联wx_users.wx_id）',
    type            tinyint  not null default 0 comment '类型：0-邀请',
    original_points int      not null comment '原有积分',
    after_points    int      not null comment '变化后积分',
    create_time     datetime default current_timestamp comment '创建时间',
    update_time     datetime default current_timestamp on update current_timestamp comment '修改时间',
    primary key (id)
)
comment='积分变化表';