package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知列表响应VO
 *
 * @author CMSWE
 * @since 2025-01-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "通知列表响应")
public class NotificationVo implements Serializable {

    /**
     * 通知ID
     */
    @Schema(description = "通知ID")
    private String notificationId;

    /**
     * 消息类型
     */
    @Schema(description = "消息类型：USER_FOLLOW-用户关注, SYSTEM_NOTICE-系统通知, COMMENT-评论, LIKE-点赞等")
    private String messageType;

    /**
     * 发送者用户ID（0表示系统）
     */
    @Schema(description = "发送者用户ID（0表示系统）")
    private String fromUserId;

    /**
     * 发送者用户名
     */
    @Schema(description = "发送者用户名")
    private String fromUsername;

    /**
     * 消息标题
     */
    @Schema(description = "消息标题")
    private String title;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 相关业务ID
     */
    @Schema(description = "相关业务ID（如文章ID、评论ID等）")
    private String relatedId;

    /**
     * 相关业务类型
     */
    @Schema(description = "相关业务类型（USER-用户, ARTICLE-文章, COMMENT-评论, ASSOCIATION-校友会等）")
    private String relatedType;

    /**
     * 阅读状态：0-未读，1-已读
     */
    @Schema(description = "阅读状态：0-未读，1-已读")
    private Integer readStatus;

    /**
     * 阅读时间
     */
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    /**
     * 扩展数据（JSON格式）
     */
    @Schema(description = "扩展数据（JSON格式）")
    private String extraData;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
