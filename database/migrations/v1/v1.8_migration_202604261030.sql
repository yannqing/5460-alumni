-- 活动报名功能：补全审核人字段与索引
-- 表 activity_registration 已存在（见 v1_init_schema.sql），此处仅追加缺失字段与索引

ALTER TABLE activity_registration
    ADD COLUMN auditor_id BIGINT NULL COMMENT '审核人ID（wxId）' AFTER audit_reason;

-- 详情页拉"已通过报名"列表
CREATE INDEX idx_activity_status ON activity_registration (activity_id, registration_status);

-- 用户判断"自己是否已报名"
CREATE INDEX idx_user_activity ON activity_registration (user_id, activity_id);
