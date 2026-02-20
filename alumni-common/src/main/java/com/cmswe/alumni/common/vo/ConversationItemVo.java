package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.enums.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表项VO
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Data
@Schema(description = "会话列表项")
public class ConversationItemVo {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "对方ID（用户ID或群组ID）")
    private String peerId;

    @Schema(description = "会话类型")
    private SourceType conversationType;

    @Schema(description = "对方昵称")
    private String peerNickname;

    @Schema(description = "对方头像")
    private String peerAvatar;

    @Schema(description = "最后一条消息内容")
    private String lastMessageContent;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读消息数")
    private Integer unreadCount;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "是否免打扰")
    private Boolean isMuted;

    @Schema(description = "草稿内容")
    private String draftContent;

    @Schema(description = "对方是否在线（仅用户会话）")
    private Boolean isOnline;

    // 兼容旧版本字段
    @Schema(description = "会话名称（兼容旧版本，等同于peerNickname）")
    public String getConversationName() {
        return peerNickname;
    }

    @Schema(description = "会话头像（兼容旧版本，等同于peerAvatar）")
    public String getConversationAvatar() {
        return peerAvatar;
    }

    @Schema(description = "是否置顶（兼容旧版本，等同于isPinned）")
    public Boolean getIsTop() {
        return isPinned;
    }
}
