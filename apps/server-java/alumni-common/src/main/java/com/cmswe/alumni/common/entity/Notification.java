package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知消息表
 * @TableName notification
 * @author CMSWE
 * @since 2025-12-05
 */
@TableName(value = "notification")
@Data
public class Notification implements Serializable {

    /**
     * 通知ID
     */
    @TableId(value = "notification_id", type = IdType.ASSIGN_ID)
    private Long notificationId;

    /**
     * 消息ID（用于Kafka消息去重）
     */
    @TableField(value = "message_id")
    private String messageId;

    /**
     * 消息类型：USER_FOLLOW-用户关注, SYSTEM_NOTICE-系统通知, COMMENT-评论, LIKE-点赞等
     */
    @TableField(value = "message_type")
    private String messageType;

    /**
     * 发送者用户ID（0表示系统）
     */
    @TableField(value = "from_user_id")
    private Long fromUserId;

    /**
     * 发送者用户名
     */
    @TableField(value = "from_username")
    private String fromUsername;

    /**
     * 接收者用户ID（0表示全体用户）
     */
    @TableField(value = "to_user_id")
    private Long toUserId;

    /**
     * 消息标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 消息内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 相关业务ID（如文章ID、评论ID等）
     */
    @TableField(value = "related_id")
    private Long relatedId;

    /**
     * 相关业务类型（USER-用户, ARTICLE-文章, COMMENT-评论, ASSOCIATION-校友会等）
     */
    @TableField(value = "related_type")
    private String relatedType;

    /**
     * 阅读状态：0-未读，1-已读
     */
    @TableField(value = "read_status")
    private Integer readStatus;

    /**
     * 阅读时间
     */
    @TableField(value = "read_time")
    private LocalDateTime readTime;

    /**
     * 扩展数据（JSON格式）
     */
    @TableField(value = "extra_data")
    private String extraData;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
