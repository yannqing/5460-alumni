package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户反馈表
 * @TableName user_feedback
 */
@TableName(value = "user_feedback")
@Data
public class UserFeedback implements Serializable {
    /**
     * 反馈ID
     */
    @TableId(value = "feedback_id", type = IdType.ASSIGN_ID)
    private Long feedbackId;

    /**
     * 用户ID
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 反馈类型：1-数据问题，2-功能建议，3-Bug反馈，4-使用问题，5-其他
     */
    @TableField(value = "feedback_type")
    private Integer feedbackType;

    /**
     * 反馈标题
     */
    @TableField(value = "feedback_title")
    private String feedbackTitle;

    /**
     * 反馈内容
     */
    @TableField(value = "feedback_content")
    private String feedbackContent;

    /**
     * 联系方式（可选）
     */
    @TableField(value = "contact_info")
    private String contactInfo;

    /**
     * 附件ID数组（JSON格式）
     */
    @TableField(value = "attachment_ids")
    private String attachmentIds;

    /**
     * 反馈状态：0-待处理，1-处理中，2-已处理，3-已关闭
     */
    @TableField(value = "feedback_status")
    private Integer feedbackStatus;

    /**
     * 处理人ID
     */
    @TableField(value = "handler_id")
    private Long handlerId;

    /**
     * 处理时间
     */
    @TableField(value = "handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理意见
     */
    @TableField(value = "handle_comment")
    private String handleComment;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
