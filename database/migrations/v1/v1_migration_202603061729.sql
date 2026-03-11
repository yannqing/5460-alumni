  CREATE TABLE `user_feedback` (
    `feedback_id` BIGINT NOT NULL COMMENT '反馈ID',
    `wx_id` BIGINT NOT NULL COMMENT '用户ID',
    `feedback_type` TINYINT NOT NULL DEFAULT 1 COMMENT
  '反馈类型：1-数据问题，2-功能建议，3-Bug反馈，4-使用问题，5-其他',
    `feedback_title` VARCHAR(200) NOT NULL COMMENT '反馈标题',
    `feedback_content` TEXT NOT NULL COMMENT '反馈内容',
    `contact_info` VARCHAR(200) NULL COMMENT '联系方式（可选）',
    `attachment_ids` VARCHAR(500) NULL COMMENT '附件ID数组（JSON格式）',
    `feedback_status` TINYINT NOT NULL DEFAULT 0 COMMENT
  '反馈状态：0-待处理，1-处理中，2-已处理，3-已关闭',
    `handler_id` BIGINT NULL COMMENT '处理人ID',
    `handle_time` DATETIME NULL COMMENT '处理时间',
    `handle_comment` TEXT NULL COMMENT '处理意见',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT
  '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE
  CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT
  '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`feedback_id`),
    INDEX `idx_wx_id` (`wx_id`),
    INDEX `idx_feedback_type` (`feedback_type`),
    INDEX `idx_feedback_status` (`feedback_status`),
    INDEX `idx_create_time` (`create_time`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';

