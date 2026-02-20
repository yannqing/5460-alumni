package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息 Mapper 接口
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 查询两个用户之间的聊天记录
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @param offset  偏移量
     * @param limit   查询数量
     * @return 聊天消息列表
     */
    List<ChatMessage> selectChatHistory(@Param("userId1") Long userId1,
                                        @Param("userId2") Long userId2,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    /**
     * 查询用户未读消息数量
     *
     * @param toId 接收方用户ID
     * @return 未读消息数量
     */
    Integer countUnreadMessages(@Param("toId") Long toId);

    /**
     * 查询用户与某人的未读消息数量
     *
     * @param toId   接收方用户ID
     * @param fromId 发送方用户ID
     * @return 未读消息数量
     */
    Integer countUnreadMessagesByFromId(@Param("toId") Long toId, @Param("fromId") Long fromId);

    /**
     * 批量标记消息为已读
     *
     * @param toId   接收方用户ID
     * @param fromId 发送方用户ID
     * @return 更新的记录数
     */
    Integer batchMarkAsRead(@Param("toId") Long toId, @Param("fromId") Long fromId);

    /**
     * 查询群组消息
     *
     * @param groupId 群组ID
     * @param offset  偏移量
     * @param limit   查询数量
     * @return 群组消息列表
     */
    List<ChatMessage> selectGroupMessages(@Param("groupId") Long groupId,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);

    /**
     * 查询用户最近的会话列表
     *
     * @param userId 用户ID
     * @param limit  查询数量
     * @return 最近会话的用户ID列表
     */
    List<Long> selectRecentConversations(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据 Kafka 消息 ID 查询消息
     *
     * @param kfMsgId Kafka 消息 ID
     * @return 聊天消息
     */
    ChatMessage selectByKfMsgId(@Param("kfMsgId") String kfMsgId);

    /**
     * 根据 Kafka 消息 ID 更新消息状态为已送达
     *
     * @param kfMsgId Kafka 消息 ID
     * @param status  消息状态
     * @return 更新的记录数
     */
    Integer updateStatusByKfMsgId(@Param("kfMsgId") String kfMsgId, @Param("status") Integer status);
}