alter table merchant modify review_status tinyint default 0 not null comment '审核状态：0-待审核 1-审核通过 2-审核失败 3-已撤销 4-待发布';

