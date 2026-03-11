--校友会申请表的驻会代表wxid字段增加
alter table alumni_association_application
	add zh_wx_id bigint null comment '驻会代表wxid';

