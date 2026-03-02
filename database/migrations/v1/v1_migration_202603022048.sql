--校友总会表增加背景图字段
alter table alumni_headquarters
	add bg_img varchar(1024) null comment '背景图';