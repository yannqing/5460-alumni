alter table alumni_association_member
    add user_phone varchar(255) null comment '用户的联系电话' after role_name;

alter table alumni_association_member
    add user_affiliation varchar(255) null comment '用户的社会职务' after user_phone;