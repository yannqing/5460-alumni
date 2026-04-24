create table merchant_business_category
(
    id          bigint                             not null comment '主键id（雪花id）'
        primary key,
    parent_id   bigint   default 0                 not null comment '父级id（0表示一级类目，非0表示该类目下的经营范围）',
    name        varchar(100)                       not null comment '分类名称',
    level       tinyint  default 1                 not null comment '层级：1-经营类目 2-经营范围',
    sort_order  int      default 0                 null comment '排序权重',
    status      tinyint  default 1                 not null comment '状态：0-禁用 1-启用',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '逻辑删除：0-未删除 1-已删除'
)
    comment '商户经营类目及范围表';

create index idx_parent_id
    on merchant_business_category (parent_id);






INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713001, 0, '餐饮服务', 1, 1, 1, '2026-04-24 08:21:01', '2026-04-24 08:21:01', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713002, 0, '食品零售', 1, 2, 1, '2026-04-24 08:21:02', '2026-04-24 08:21:02', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713003, 0, '日用百货', 1, 3, 1, '2026-04-24 08:21:03', '2026-04-24 08:21:03', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713004, 0, '服装鞋帽', 1, 4, 1, '2026-04-24 08:21:03', '2026-04-24 08:21:03', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713005, 0, '美妆护肤', 1, 5, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713006, 0, '数码家电', 1, 6, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713007, 0, '母婴用品', 1, 7, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713008, 0, '生活服务', 1, 8, 1, '2026-04-24 08:21:05', '2026-04-24 08:21:05', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713101, 1997925619212713001, '中餐正餐/地方菜', 2, 1, 1, '2026-04-24 08:21:01', '2026-04-24 08:21:01', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713102, 1997925619212713001, '火锅/串串香', 2, 2, 1, '2026-04-24 08:21:01', '2026-04-24 08:21:01', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713103, 1997925619212713001, '烧烤/烤肉', 2, 3, 1, '2026-04-24 08:21:02', '2026-04-24 08:21:02', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713104, 1997925619212713001, '小吃快餐/面馆', 2, 4, 1, '2026-04-24 08:21:02', '2026-04-24 08:21:02', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713105, 1997925619212713001, '甜品奶茶/咖啡', 2, 5, 1, '2026-04-24 08:21:02', '2026-04-24 08:21:02', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713199, 1997925619212713001, '其他餐饮服务', 2, 99, 1, '2026-04-24 08:21:02', '2026-04-24 08:21:02', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713201, 1997925619212713002, '生鲜果蔬/水产', 2, 1, 1, '2026-04-24 08:21:02', '2026-04-24 08:21:02', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713202, 1997925619212713002, '休闲零食/干果', 2, 2, 1, '2026-04-24 08:21:02', '2026-04-24 08:21:02', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713203, 1997925619212713002, '烟酒糖茶/粮油', 2, 3, 1, '2026-04-24 08:21:02', '2026-04-24 08:21:02', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713299, 1997925619212713002, '其他食品零售', 2, 99, 1, '2026-04-24 08:21:03', '2026-04-24 08:21:03', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713301, 1997925619212713003, '便利店/小超市', 2, 1, 1, '2026-04-24 08:21:03', '2026-04-24 08:21:03', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713302, 1997925619212713003, '办公文具/体育用品', 2, 2, 1, '2026-04-24 08:21:03', '2026-04-24 08:21:03', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713399, 1997925619212713003, '其他日用百货', 2, 99, 1, '2026-04-24 08:21:03', '2026-04-24 08:21:03', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713401, 1997925619212713004, '流行服饰/内衣', 2, 1, 1, '2026-04-24 08:21:03', '2026-04-24 08:21:03', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713402, 1997925619212713004, '运动户外/鞋包', 2, 2, 1, '2026-04-24 08:21:03', '2026-04-24 08:21:03', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713499, 1997925619212713004, '其他服装鞋帽', 2, 99, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713501, 1997925619212713005, '美容护肤/彩妆', 2, 1, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713502, 1997925619212713005, '个人洗护/美发用品', 2, 2, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713599, 1997925619212713005, '其他美妆护肤', 2, 99, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713601, 1997925619212713006, '手机数码/电脑配件', 2, 1, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713602, 1997925619212713006, '智能家居/生活家电', 2, 2, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713699, 1997925619212713006, '其他数码家电', 2, 99, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713701, 1997925619212713007, '奶粉辅食/孕婴用品', 2, 1, 1, '2026-04-24 08:21:04', '2026-04-24 08:21:04', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713702, 1997925619212713007, '童装童鞋/益智玩具', 2, 2, 1, '2026-04-24 08:21:05', '2026-04-24 08:21:05', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713799, 1997925619212713007, '其他母婴用品', 2, 99, 1, '2026-04-24 08:21:05', '2026-04-24 08:21:05', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713801, 1997925619212713008, '美容美发/美甲美睫', 2, 1, 1, '2026-04-24 08:21:05', '2026-04-24 08:21:05', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713802, 1997925619212713008, '养生足疗/按摩SPA', 2, 2, 1, '2026-04-24 08:21:05', '2026-04-24 08:21:05', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713803, 1997925619212713008, '家政搬家/家电清洗', 2, 3, 1, '2026-04-24 08:21:05', '2026-04-24 08:21:05', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713804, 1997925619212713008, '宠物美容/医疗寄养', 2, 4, 1, '2026-04-24 08:21:05', '2026-04-24 08:21:05', 0);
INSERT INTO cni_alumni.merchant_business_category (id, parent_id, name, level, sort_order, status, create_time, update_time, is_delete) VALUES (1997925619212713899, 1997925619212713008, '其他生活服务', 2, 99, 1, '2026-04-24 08:21:05', '2026-04-24 08:21:05', 0);
