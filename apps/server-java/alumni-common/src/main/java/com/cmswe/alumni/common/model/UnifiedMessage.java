package com.cmswe.alumni.common.model;

import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.enums.MessagePriority;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统一消息传输对象（企业级标准）
 *
 * <p>设计原则：
 * <ul>
 *   <li>支持多种消息类别（P2P、群聊、系统通知、组织通知）</li>
 *   <li>包含完整的消息元数据（ID、时间戳、优先级等）</li>
 *   <li>支持消息追踪和去重</li>
 *   <li>扩展性强，支持自定义数据</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 消息元数据 ====================

    /**
     * 消息唯一ID（用于幂等性和去重）
     * 格式：{category}_{timestamp}_{uuid}
     */
    private String messageId;

    /**
     * 消息类别
     */
    private MessageCategory category;

    /**
     * 消息类型（具体的业务类型，如USER_FOLLOW、COMMENT等）
     */
    private String messageType;

    /**
     * 消息优先级
     */
    @Builder.Default
    private MessagePriority priority = MessagePriority.NORMAL;

    /**
     * 消息创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 消息过期时间（可选，用于临时消息）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    // ==================== 发送方信息 ====================

    /**
     * 发送方ID
     * - 用户消息：用户ID
     * - 系统消息：0 或 -1
     * - 组织消息：组织ID
     */
    private Long fromId;

    /**
     * 发送方类型（USER、SYSTEM、ORGANIZATION）
     */
    private String fromType;

    /**
     * 发送方名称
     */
    private String fromName;

    /**
     * 发送方头像URL（可选）
     */
    private String fromAvatar;

    // ==================== 接收方信息 ====================

    /**
     * 接收方ID
     * - 单个用户：用户ID
     * - 群组：群组ID
     * - 全体用户：0 或 null
     */
    private Long toId;

    /**
     * 接收方类型（USER、GROUP、ALL）
     */
    private String toType;

    /**
     * 批量接收方ID列表（用于组织通知等场景）
     */
    private List<Long> toIds;

    // ==================== 消息内容 ====================

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容（支持富文本、Markdown等）
     */
    private String content;

    /**
     * 消息摘要（用于推送通知）
     */
    private String summary;

    /**
     * 内容类型（TEXT、HTML、MARKDOWN、JSON等）
     */
    @Builder.Default
    private String contentType = "TEXT";

    // ==================== 关联业务信息 ====================

    /**
     * 关联业务ID（如文章ID、评论ID、活动ID等）
     */
    private Long relatedId;

    /**
     * 关联业务类型（ARTICLE、COMMENT、EVENT、ORGANIZATION等）
     */
    private String relatedType;

    /**
     * 关联业务跳转链接
     */
    private String relatedUrl;

    /**
     * 关联业务扩展数据
     */
    private Map<String, Object> relatedData;

    // ==================== 扩展字段 ====================

    /**
     * 扩展数据（JSON格式，用于存储业务特定数据）
     */
    private Map<String, Object> extraData;

    /**
     * 消息标签（用于分类和筛选）
     */
    private List<String> tags;

    /**
     * 是否需要持久化到数据库
     */
    @Builder.Default
    private Boolean needPersist = true;

    /**
     * 是否需要推送到客户端
     */
    @Builder.Default
    private Boolean needPush = true;

    /**
     * 是否需要离线存储
     */
    @Builder.Default
    private Boolean needOfflineStore = true;

    // ==================== 消息追踪信息 ====================

    /**
     * 消息来源服务
     */
    private String sourceService;

    /**
     * 业务追踪ID（用于分布式追踪）
     */
    private String traceId;

    /**
     * 重试次数
     */
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private Integer maxRetryCount = 3;

    // ==================== 辅助方法 ====================

    /**
     * 是否为点对点消息
     */
    public boolean isP2PMessage() {
        return MessageCategory.P2P.equals(this.category);
    }

    /**
     * 是否为群聊消息
     */
    public boolean isGroupMessage() {
        return MessageCategory.GROUP.equals(this.category);
    }

    /**
     * 是否为系统通知
     */
    public boolean isSystemNotification() {
        return MessageCategory.SYSTEM.equals(this.category);
    }

    /**
     * 是否为组织通知
     */
    public boolean isOrganizationNotification() {
        return MessageCategory.ORGANIZATION.equals(this.category);
    }

    /**
     * 是否需要重试
     */
    public boolean needRetry() {
        return retryCount < maxRetryCount;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetry() {
        this.retryCount++;
    }

    /**
     * 是否为高优先级消息
     */
    public boolean isHighPriority() {
        return this.priority == MessagePriority.HIGH || this.priority == MessagePriority.URGENT;
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }
}
