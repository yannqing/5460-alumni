--校促会成员排序字段增加
alter table local_platform_member
	add sort tinyint default 0 null comment '排序,数值越小,越靠前';

