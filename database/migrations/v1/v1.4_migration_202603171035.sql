-- 校友会加入校促会申请表增加申请人wxid
alter table alumni_association_join_apply
	add applicant_wx_id bigint null comment '申请人wx_id';

