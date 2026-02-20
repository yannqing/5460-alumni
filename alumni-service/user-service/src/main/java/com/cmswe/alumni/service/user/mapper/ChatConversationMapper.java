package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.ChatConversation;
import com.cmswe.alumni.common.enums.SourceType;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话 Mapper 接口
 *
 * @author CMSWE
 * @since 2025-12-10
 */
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    /**
     * 查询用户的会话列表
     *
     * @param userId 用户ID
     * @param limit  查询数量
     * @return 会话列表
     */
    List<ChatConversation> selectUserConversations(@Param("userId") Long userId,
                                                   @Param("limit") Integer limit);

    /**
     * 查询或创建会话
     *
     * @param userId           用户ID
     * @param peerId           对方ID
     * @param conversationType 会话类型
     * @return 会话
     */
    ChatConversation selectOrCreateConversation(@Param("userId") Long userId,
                                                @Param("peerId") Long peerId,
                                                @Param("conversationType") SourceType conversationType);

    /**
     * 更新会话最后消息信息
     *
     * @param userId              用户ID
     * @param peerId              对方ID
     * @param conversationType    会话类型
     * @param lastMessageId       最后消息ID
     * @param lastMessageContent  最后消息内容摘要
     * @param lastMessageTime     最后消息时间
     * @param lastMessageFromId   最后消息发送者ID
     * @return 更新的记录数
     */
    int updateLastMessage(@Param("userId") Long userId,
                         @Param("peerId") Long peerId,
                         @Param("conversationType") SourceType conversationType,
                         @Param("lastMessageId") Long lastMessageId,
                         @Param("lastMessageContent") String lastMessageContent,
                         @Param("lastMessageTime") LocalDateTime lastMessageTime,
                         @Param("lastMessageFromId") Long lastMessageFromId);

    /**
     * 增加未读数
     *
     * @param userId           用户ID
     * @param peerId           对方ID
     * @param conversationType 会话类型
     * @param count            增加的数量
     * @return 更新的记录数
     */
    int incrementUnreadCount(@Param("userId") Long userId,
                            @Param("peerId") Long peerId,
                            @Param("conversationType") SourceType conversationType,
                            @Param("count") Integer count);

    /**
     * 清空未读数
     *
     * @param userId           用户ID
     * @param peerId           对方ID
     * @param conversationType 会话类型
     * @return 更新的记录数
     */
    int clearUnreadCount(@Param("userId") Long userId,
                        @Param("peerId") Long peerId,
                        @Param("conversationType") SourceType conversationType);

    /**
     * 更新置顶状态
     *
     * @param conversationId 会话ID
     * @param isPinned       是否置顶
     * @return 更新的记录数
     */
    int updatePinnedStatus(@Param("conversationId") Long conversationId,
                          @Param("isPinned") Boolean isPinned);

    /**
     * 更新免打扰状态
     *
     * @param conversationId 会话ID
     * @param isMuted        是否免打扰
     * @return 更新的记录数
     */
    int updateMutedStatus(@Param("conversationId") Long conversationId,
                         @Param("isMuted") Boolean isMuted);

    /**
     * 删除会话（软删除）
     *
     * @param conversationId 会话ID
     * @return 更新的记录数
     */
    int deleteConversation(@Param("conversationId") Long conversationId);

    /**
     * 保存草稿
     *
     * @param conversationId 会话ID
     * @param draftContent   草稿内容
     * @return 更新的记录数
     */
    int saveDraft(@Param("conversationId") Long conversationId,
                 @Param("draftContent") String draftContent);

    /**
     * 查询用户总未读数
     *
     * @param userId 用户ID
     * @return 总未读数
     */
    Integer countTotalUnread(@Param("userId") Long userId);
}
