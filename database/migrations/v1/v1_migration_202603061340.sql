create table poster_template
(
    id  bigint   not null auto_increment comment '主键ID',
    url varchar(1024)     null comment '模版url',
    type   tinyint  default 0  null  comment '模版类型(0邀请模板)',
    create_time     datetime default current_timestamp comment '创建时间',
    update_time     datetime default current_timestamp on update current_timestamp comment '修改时间',
        primary key (id)

)

comment '海报模板表'