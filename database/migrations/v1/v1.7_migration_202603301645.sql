-- 修改校友总会表字段
alter table alumni_headquarters modify president varchar(255) null comment '总会会长姓名';

alter table alumni_headquarters modify secretary_general varchar(255) null comment '总会秘书长姓名';

alter table alumni_headquarters modify position varchar(255) null comment '总会职务';

alter table alumni_headquarters modify office_phone varchar(255) null comment '办公电话';

alter table alumni_headquarters modify phone varchar(50) null comment '移动电话';