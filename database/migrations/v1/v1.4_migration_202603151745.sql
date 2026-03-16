-- 创建校友会增加覆盖区域字段
alter table alumni_association_application
	add coverage_area varchar(255) null comment '覆盖区域';

