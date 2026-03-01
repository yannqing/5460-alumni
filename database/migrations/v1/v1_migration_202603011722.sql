alter table alumni_headquarters alter column active_status set default 0;

alter table alumni_headquarters
   add Create_code int not null;

alter table alumni_headquarters modify Create_code int not null comment '创建码';

alter table alumni_headquarters alter column approval_status set default 0;

alter table alumni_headquarters
   add logo varchar(1024) null comment 'logo';