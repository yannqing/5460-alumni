--创建校友总会联系人字段增加
alter table alumni_headquarters
	add contact_person varchar(50) null comment '联系人';

