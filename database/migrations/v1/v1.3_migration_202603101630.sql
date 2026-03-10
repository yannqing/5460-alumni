--校促会信息表增加联系人wxid字段
alter table local_platform
	add wx_id bigint null comment '联系人wxid';

