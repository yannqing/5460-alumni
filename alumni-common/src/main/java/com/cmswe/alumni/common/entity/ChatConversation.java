package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cmswe.alumni.common.enums.SourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 * 用于管理用户的聊天会话列表（单聊、群聊、系统消息）
 * 与 ChatMessage、ChatGroup 保持命名一致性
 *
 * @author CMSWE
 * @since 2025-12-10
 */
@EqualsAndHashCode(callSuper = false)
@Data
@TableName(value = "chat_conversation", autoResultMap = true)
public class ChatConversation {

    /**
     * 会话ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long conversationId;

    /**
     * 会话所属用户ID
     */
    @TableField("wx_id")
    private Long wxId;

    /**
     * 对方ID（单聊为用户ID，群聊为群ID）
     */
    @TableField("peer_id")
    private Long peerId;

    /**
     * 会话类型：user-单聊, group-群聊, system-系统消息
     */
    @TableField("conversation_type")
    private SourceType conversationType;

    /**
     * 最后一条消息ID
     */
    @TableField("last_message_id")
    private Long lastMessageId;

    /**
     * 最后消息内容摘要（用于列表展示）
     */
    @TableField("last_message_content")
    private String lastMessageContent;

    /**
     * 最后消息时间
     */
    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    /**
     * 最后消息发送者ID
     */
    @TableField("last_message_from_id")
    private Long lastMessageFromId;

    /**
     * 未读消息数
     */
    @TableField("unread_count")
    private Integer unreadCount;

    /**
     * 是否置顶（0-否，1-是）
     */
    @TableField("is_pinned")
    private Boolean isPinned;

    /**
     * 是否免打扰（0-否，1-是）
     */
    @TableField("is_muted")
    private Boolean isMuted;

    /**
     * 用户是否删除会话（0-否，1-是）
     */
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 是否隐藏（0-否，1-是）
     */
    @TableField("is_hidden")
    private Boolean isHidden;

    /**
     * 草稿内容
     */
    @TableField("draft_content")
    private String draftContent;

    /**
     * @我的消息数量
     */
    @TableField("mention_count")
    private Integer mentionCount;

    /**
     * 扩展信息（JSON格式）
     */
    @TableField("ext_info")
    private String extInfo;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
