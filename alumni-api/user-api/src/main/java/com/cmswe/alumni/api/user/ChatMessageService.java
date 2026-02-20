package com.cmswe.alumni.api.user;

import com.cmswe.alumni.common.dto.QueryChatHistoryDto;
import com.cmswe.alumni.common.dto.SendMessageDto;
import com.cmswe.alumni.common.vo.ChatMessageVo;
import com.cmswe.alumni.common.vo.ConversationItemVo;
import com.cmswe.alumni.common.vo.PageVo;

import java.util.List;

/**
 * 聊天消息服务接口
 *
 * @author CMSWE
 * @since 2025-12-09
 */
public interface ChatMessageService {

    /**
     * 发送消息
     *
     * @param wxId           发送方用户ID
     * @param sendMessageDto 发送消息DTO
     * @return 消息ID
     */
    Long sendMessage(Long wxId, SendMessageDto sendMessageDto);

    /**
     * 获取聊天历史记录
     *
     * @param wxId     当前用户ID
     * @param queryDto 查询DTO
     * @return 聊天消息列表
     */
    PageVo<ChatMessageVo> getChatHistory(Long wxId, QueryChatHistoryDto queryDto);

    /**
     * 获取会话列表
     *
     * @param wxId 当前用户ID
     * @return 会话列表
     */
    List<ConversationItemVo> getConversationList(Long wxId);

    /**
     * 标记消息为已读
     *
     * @param wxId       当前用户ID
     * @param otherWxId  对方用户ID
     * @return 标记的消息数量
     */
    Integer markMessagesAsRead(Long wxId, Long otherWxId);

    /**
     * 获取未读消息总数
     *
     * @param wxId 当前用户ID
     * @return 未读消息总数
     */
    Integer getUnreadCount(Long wxId);

    /**
     * 撤回消息
     *
     * @param wxId      当前用户ID
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean recallMessage(Long wxId, Long messageId);
}
