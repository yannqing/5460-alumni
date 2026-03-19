-- 校友会表增加申请id
alter table alumni_association
	add application_id bigint null comment '校友会申请id';

