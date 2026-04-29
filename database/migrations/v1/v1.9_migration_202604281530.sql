create table merchant_application
(
    application_id             bigint                             not null comment '申请ID（雪花ID）'
        primary key,
    user_id                    bigint                             not null comment '申请者wxid（关联用户ID）',
    merchant_name              varchar(255)                       not null comment '商户名称',
    legal_person               varchar(100)                       null comment '法人姓名',
    phone                      varchar(20)                        null comment '法人电话号',
    unified_social_credit_code varchar(50)                        null comment '统一社会信用代码',
    city                       varchar(100)                       null comment '所在城市',
    review_status              tinyint  default 0                 not null comment '申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销 4-待发布',
    review_reason              varchar(500)                       null comment '审核原因',
    reviewer_id                bigint                             null comment '审核人ID',
    review_time                datetime                           null comment '审核时间',
    create_time                datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time                datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete                  tinyint  default 0                 not null comment '逻辑删除（0-未删除 1-已删除）'
)
    comment '商户创建申请表' collate = utf8mb4_unicode_ci;

create index idx_create_time
    on merchant_application (create_time);

create index idx_review_status
    on merchant_application (review_status);

create index idx_user_id
    on merchant_application (user_id);

