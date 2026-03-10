--校促会组织结构
alter table organize_archi_role
	add sort tinyint default 0 null comment '排序,数值越小,越靠前';