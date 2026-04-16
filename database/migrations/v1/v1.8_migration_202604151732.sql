alter table merchant
	add logo varchar(1024) null comment '商家logo';

alter table merchant
	add background_image json null comment '商家背景图';
