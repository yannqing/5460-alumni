alter table alumni_association_member
    add is_show_on_home tinyint default 0 not null comment '是否展示在主页（0-否，1-是）' after user_affiliation;
