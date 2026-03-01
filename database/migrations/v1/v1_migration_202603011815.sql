-- 申请校友会表：
-- 修改主要负责人姓名、联系方式备注，新增主要负责人社会职务、驻会代表姓名、驻会代表联系电话、驻会代表社会职务字段
alter table alumni_association_application
    modify charge_name varchar(255) not null comment '主要负责人姓名';

alter table alumni_association_application
    modify contact_info varchar(255) null comment '主要负责人联系信息（会长联系方式）';

alter table alumni_association_application
    add msocial_affiliation varchar(255) null comment '主要负责人社会职务' after contact_info;

alter table alumni_association_application
    add zh_name varchar(255) null comment '驻会代表姓名' after msocial_affiliation;

alter table alumni_association_application
    add zh_phone varchar(255) null comment '驻会代表联系电话' after zh_name;

alter table alumni_association_application
    add zhsocial_affiliation varchar(255) null comment '驻会代表社会职务' after zh_phone;