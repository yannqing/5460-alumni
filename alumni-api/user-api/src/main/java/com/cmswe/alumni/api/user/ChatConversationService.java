package com.cmswe.alumni.api.user;

import com.cmswe.alumni.common.entity.ChatConversation;
import com.cmswe.alumni.common.enums.SourceType;
import com.cmswe.alumni.common.vo.ConversationItemVo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话服务接口
 *
 * @author CMSWE
 * @since 2025-12-10
 */
public interface ChatConversationService {

    /**
     * 获取用户会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ConversationItemVo> getConversationList(Long userId);

    /**
     * 获取或创建会话
     *
     * @param userId           用户ID
     * @param peerId           对方ID
     * @param conversationType 会话类型
     * @return 会话
     */
    ChatConversation getOrCreateConversation(Long userId, Long peerId, SourceType conversationType);

    /**
     * 更新会话最后消息
     *
     * @param userId              用户ID
     * @param peerId              对方ID
     * @param conversationType    会话类型
     * @param lastMessageId       最后消息ID
     * @param lastMessageContent  最后消息内容
     * @param lastMessageTime     最后消息时间
     * @param lastMessageFromId   最后消息发送者ID
     */
    void updateLastMessage(Long userId, Long peerId, SourceType conversationType,
                          Long lastMessageId, String lastMessageContent,
                          LocalDateTime lastMessageTime, Long lastMessageFromId);

    /**
     * 增加未读数
     *
     * @param userId           用户ID
     * @param peerId           对方ID
     * @param conversationType 会话类型
     * @param count            增加数量
     */
    void incrementUnreadCount(Long userId, Long peerId, SourceType conversationType, Integer count);

    /**
     * 清空未读数（标记消息已读）
     *
     * @param userId           用户ID
     * @param peerId           对方ID
     * @param conversationType 会话类型
     */
    void clearUnreadCount(Long userId, Long peerId, SourceType conversationType);

    /**
     * 置顶/取消置顶会话
     *
     * @param conversationId 会话ID
     * @param isPinned       是否置顶
     */
    void updatePinnedStatus(Long conversationId, Boolean isPinned);

    /**
     * 免打扰/取消免打扰
     *
     * @param conversationId 会话ID
     * @param isMuted        是否免打扰
     */
    void updateMutedStatus(Long conversationId, Boolean isMuted);

    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     */
    void deleteConversation(Long conversationId);

    /**
     * 保存草稿
     *
     * @param conversationId 会话ID
     * @param draftContent   草稿内容
     */
    void saveDraft(Long conversationId, String draftContent);

    /**
     * 获取用户总未读数
     *
     * @param userId 用户ID
     * @return 总未读数
     */
    Integer getTotalUnreadCount(Long userId);
}
