--校友会成员表架构id必填修改
alter table local_platform_member modify role_or_id bigint default 1996474751486992386 null comment '成员角色';
