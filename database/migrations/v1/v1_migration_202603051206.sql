--local_platform_member校处会成员关系表字段增加
alter table local_platform_member
	add contact_information varchar(255) null comment '联系方式';

alter table local_platform_member
	add social_duties varchar(255) null comment '社会职务';

