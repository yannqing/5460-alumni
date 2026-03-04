--校友会申请加入校促会记录表创建
create table alumni_association_join_apply
(
    id    bigint                             not null comment '校友会申请加入校促会住主键id'
        primary key,
    alumni_association_id   bigint                             not null comment '校友会ID',
    platform_id       bigint                             not null comment '校促会ID',
    status           tinyint  default 0         null comment '审核状态(0待审核,1已通过,2已拒绝)',
    create_time            datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time            datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete              tinyint  default 0                 not null comment '逻辑删除：0-未删除 1-已删除'
)

  comment '校友会申请加入校促会申请表';