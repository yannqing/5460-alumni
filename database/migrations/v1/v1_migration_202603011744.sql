-- 申请校友会表：
-- 新增简介字段
alter table alumni_association_application
    add association_profile text null comment '校友会简介' after application_reason;