package com.cmswe.alumni.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知消息传输模型（用于Kafka消息传输）
 *
 * @author CMSWE
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 消息类型：USER_FOLLOW, SYSTEM_NOTICE, COMMENT, LIKE等
     */
    private String messageType;

    /**
     * 发送者用户ID
     */
    private Long fromUserId;

    /**
     * 发送者用户名
     */
    private String fromUsername;

    /**
     * 接收者用户ID
     */
    private Long toUserId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 相关业务ID（如文章ID、评论ID等）
     */
    private Long relatedId;

    /**
     * 相关业务类型
     */
    private String relatedType;

    /**
     * 消息创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 扩展数据（JSON格式）
     */
    private String extraData;
}
