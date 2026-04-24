CREATE TABLE merchant_alumni_association_apply
(
    id                    BIGINT                             NOT NULL COMMENT '主键 ID'
        PRIMARY KEY,
    merchant_id           BIGINT                             NOT NULL COMMENT '申请的商户 ID',
    alumni_association_id BIGINT                             NOT NULL COMMENT '目标校友会 ID',
    applicant_wx_id       BIGINT                             NULL     COMMENT '提交申请的操作人 ID',
    application_reason    VARCHAR(500)                       NULL     COMMENT '申请理由',
    status                TINYINT  DEFAULT 0                 NOT NULL COMMENT '审核状态（0-待审核, 1-已通过, 2-已拒绝, 3-已撤销）',
    reviewer_id           BIGINT                             NULL     COMMENT '审核人',
    review_time           DATETIME                           NULL     COMMENT '审核时间',
    review_comment        VARCHAR(500)                       NULL     COMMENT '审核意见',
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time           DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete             TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除：0-未删除 1-已删除'
)
    COMMENT '商户入驻校友会申请表';