CREATE TABLE invitation_record
(
    id            bigint             NOT NULL AUTO_INCREMENT COMMENT '主键ID' primary key,
    inviter_wx_id bigint             NOT NULL COMMENT '邀请人wxid（关联wx_users.wx_id）',
    invitee_wx_id bigint             NOT NULL COMMENT '被邀请人wxid（关联wx_users.wx_id）',
    is_verified  tinyint  DEFAULT 0 NOT NULL COMMENT '是否认证(0未认证,1已认证)-关联被邀请人',
    create_time   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
)
COMMENT='邀请记录表'