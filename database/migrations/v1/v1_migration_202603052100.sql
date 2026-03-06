-- 添加校友会邀请功能相关表和字段

-- 1. 修改notification表，添加动作相关字段
ALTER TABLE notification
    ADD COLUMN action_type VARCHAR(50) COMMENT '动作类型：NONE-无动作, CONFIRM-需要确认, APPROVAL-需要审批, INVITATION-邀请等' AFTER extra_data,
    ADD COLUMN action_data TEXT COMMENT '动作数据（JSON格式），存储动作相关的参数' AFTER action_type,
    ADD COLUMN action_status INT DEFAULT 0 COMMENT '动作状态：0-待处理, 1-已同意, 2-已拒绝, 3-已过期' AFTER action_data;

-- 2. 创建校友会邀请记录表
CREATE TABLE IF NOT EXISTS `alumni_association_invitation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `alumni_association_id` BIGINT NOT NULL COMMENT '校友会ID',
    `inviter_id` BIGINT NOT NULL COMMENT '邀请人ID（管理员）',
    `invitee_id` BIGINT NOT NULL COMMENT '被邀请人ID',
    `role_or_id` BIGINT COMMENT '组织架构角色ID（邀请时指定的角色，可为空）',
    `notification_id` BIGINT COMMENT '通知ID（关联notification表）',
    `status` INT DEFAULT 0 NOT NULL COMMENT '邀请状态：0-待处理, 1-已同意, 2-已拒绝, 3-已过期',
    `process_time` DATETIME COMMENT '处理时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT DEFAULT 0 NOT NULL COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_alumni_association_id` (`alumni_association_id`),
    INDEX `idx_invitee_id` (`invitee_id`),
    INDEX `idx_inviter_id` (`inviter_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_notification_id` (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校友会邀请记录表';
