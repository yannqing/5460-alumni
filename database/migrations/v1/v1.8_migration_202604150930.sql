alter table merchant_member modify role_or_id bigint default 252733488048898048 null comment '成员架构角色id';

alter table merchant_member
	add position varchar(255) not null comment '职务';