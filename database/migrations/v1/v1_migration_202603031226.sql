--校促会信息表字段增加
alter table local_platform
	add principal_name varchar(50) null comment '负责人姓名';

alter table local_platform
	add principal_position varchar(50) null comment '负责人职务';

alter table local_platform
	add phone varchar(50) null comment '联系电话';

