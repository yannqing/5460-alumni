alter table alumni_association
    add certification_flag tinyint default 0 not null comment '认证标识（0-未认证，1-校友总会，2-校促会，3-校友总会）' after platform_id;