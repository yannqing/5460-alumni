--校促会表负责人改为联系人
alter table local_platform change principal_name contact_name varchar(50) null comment '联系人姓名';

alter table local_platform change principal_position contact_position varchar(50) null comment '联系人职务';

alter table local_platform change phone contact_phone varchar(50) null comment '联系人电话';

