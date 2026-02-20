package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmswe.alumni.api.user.ChatMessageService;
import com.cmswe.alumni.common.dto.QueryChatHistoryDto;
import com.cmswe.alumni.common.dto.SendMessageDto;
import com.cmswe.alumni.common.entity.ChatMessage;
import com.cmswe.alumni.common.entity.Notification;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.enums.MessageFormat;
import com.cmswe.alumni.common.enums.MessageStatus;
import com.cmswe.alumni.common.enums.MessageType;
import com.cmswe.alumni.common.enums.SourceType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.model.ChatMessageContent;
import com.cmswe.alumni.common.vo.ChatMessageVo;
import com.cmswe.alumni.common.vo.ConversationItemVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.service.user.mapper.ChatMessageMapper;
import com.cmswe.alumni.service.user.mapper.NotificationMapper;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import com.cmswe.alumni.service.user.service.message.UnifiedMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天消息服务实现类
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private WxUserInfoMapper wxUserInfoMapper;

    @Resource
    private WxUserMapper wxUserMapper;

    @Resource
    private UnifiedMessageService unifiedMessageService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private com.cmswe.alumni.api.user.ChatConversationService chatConversationService;

    @Resource
    private NotificationMapper notificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long wxId, SendMessageDto sendMessageDto) {
        try {
            // 1. 获取发送方用户信息
            WxUser fromUser = wxUserMapper.selectById(wxId);
            if (fromUser == null) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "发送方用户不存在");
            }

            // 2. 检查接收方是否存在
            WxUser toUser = wxUserMapper.selectById(sendMessageDto.getToId());
            if (toUser == null) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "接收方用户不存在");
            }

            WxUserInfo fromUserInfo = wxUserInfoMapper.selectOne(new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, wxId));
            if (fromUserInfo == null) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "用户信息不完善，请完善个人信息");
            }

            // 3. 构建ChatMessage实体
            ChatMessage chatMessage = buildChatMessage(wxId, fromUserInfo, sendMessageDto);

            // 4. 保存到数据库（立即入库，状态为"发送中"）
            chatMessageMapper.insert(chatMessage);

            log.info("[ChatMessageService] 消息已保存到数据库 - KfMsgId: {}, From: {}, To: {}, DBId: {}",
                    chatMessage.getKfMsgId(), wxId, sendMessageDto.getToId(), chatMessage.getMessageId());

            // 5. 更新会话表（双方都需要更新）
            updateConversationAfterSendMessage(chatMessage);

            // 6. 发送消息到Kafka（异步处理推送、Redis缓存等）
            sendMessageToKafka(chatMessage, fromUserInfo);

            return chatMessage.getMessageId();

        } catch (Exception e) {
            log.error("[ChatMessageService] 发送消息失败 - From: {}, To: {}, Error: {}",
                    wxId, sendMessageDto.getToId(), e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "发送消息失败：" + e.getMessage());
        }
    }

    @Override
    public PageVo<ChatMessageVo> getChatHistory(Long wxId, QueryChatHistoryDto queryDto) {
        try {
            // 计算分页参数
            int offset = (queryDto.getPageNum() - 1) * queryDto.getPageSize();
            int limit = queryDto.getPageSize();

            // 查询聊天记录
            List<ChatMessage> messages = chatMessageMapper.selectChatHistory(
                    wxId,
                    queryDto.getOtherUserId(),
                    offset,
                    limit
            );

            // 查询总数
            LambdaQueryWrapper<ChatMessage> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.and(wrapper -> wrapper
                    .eq(ChatMessage::getFromId, wxId)
                    .eq(ChatMessage::getToId, queryDto.getOtherUserId())
                    .or()
                    .eq(ChatMessage::getFromId, queryDto.getOtherUserId())
                    .eq(ChatMessage::getToId, wxId)
            );
            countWrapper.eq(ChatMessage::getSourceType, SourceType.USER);
            long total = chatMessageMapper.selectCount(countWrapper);

            // 转换为VO
            List<ChatMessageVo> voList = messages.stream()
                    .map(msg -> convertToVo(msg, wxId))
                    .collect(Collectors.toList());

            return new PageVo<>(voList, total, (long) queryDto.getPageNum(), (long) queryDto.getPageSize());

        } catch (Exception e) {
            log.error("[ChatMessageService] 获取聊天历史失败 - WxId: {}, OtherWxId: {}, Error: {}",
                    wxId, queryDto.getOtherUserId(), e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "获取聊天历史失败");
        }
    }

    @Override
    public List<ConversationItemVo> getConversationList(Long wxId) {
        try {
            // 1. 从会话表查询聊天会话（性能优秀）
            List<ConversationItemVo> conversations = chatConversationService.getConversationList(wxId);

            // 2. 补充在线状态
            for (ConversationItemVo conversation : conversations) {
                if (conversation.getConversationType() == SourceType.USER) {
                    conversation.setIsOnline(redisCache.isUserOnline(Long.valueOf(conversation.getPeerId())));
                }
            }

            // 3. 查询通知消息并构建通知会话
            ConversationItemVo notificationConversation = buildNotificationConversation(wxId);
            if (notificationConversation != null) {
                // 将通知会话添加到列表开头
                conversations.add(0, notificationConversation);
            }

            return conversations;

        } catch (Exception e) {
            log.error("[ChatMessageService] 获取会话列表失败 - WxId: {}, Error: {}",
                    wxId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "获取会话列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer markMessagesAsRead(Long wxId, Long otherWxId) {
        try {
            // 1. 标记消息为已读
            Integer count = chatMessageMapper.batchMarkAsRead(wxId, otherWxId);

            // 2. 同步清空会话表的未读数
            chatConversationService.clearUnreadCount(wxId, otherWxId, SourceType.USER);

            log.info("[ChatMessageService] 标记消息已读 - WxId: {}, OtherWxId: {}, Count: {}",
                    wxId, otherWxId, count);

            return count;

        } catch (Exception e) {
            log.error("[ChatMessageService] 标记消息已读失败 - WxId: {}, OtherWxId: {}, Error: {}",
                    wxId, otherWxId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "标记消息已读失败");
        }
    }

    @Override
    public Integer getUnreadCount(Long wxId) {
        try {
            // 从会话表获取总未读数（性能更优）
            return chatConversationService.getTotalUnreadCount(wxId);
        } catch (Exception e) {
            log.error("[ChatMessageService] 获取未读消息数失败 - WxId: {}, Error: {}",
                    wxId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "获取未读消息数失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recallMessage(Long wxId, Long messageId) {
        try {
            // 查询消息
            ChatMessage message = chatMessageMapper.selectById(messageId);
            if (message == null) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "消息不存在");
            }

            // 验证是否是消息发送者
            if (!message.getFromId().equals(wxId)) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "只能撤回自己发送的消息");
            }

            // 检查消息发送时间（通常只允许撤回2分钟内的消息）
            LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
            if (message.getCreateTime().isBefore(twoMinutesAgo)) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "只能撤回2分钟内的消息");
            }

            // 更新消息状态为已撤回
            message.setStatus(MessageStatus.RECALLED);
            message.setUpdateTime(LocalDateTime.now());
            chatMessageMapper.updateById(message);

            log.info("[ChatMessageService] 消息已撤回 - MessageId: {}, WxId: {}", messageId, wxId);

            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ChatMessageService] 撤回消息失败 - MessageId: {}, WxId: {}, Error: {}",
                    messageId, wxId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "撤回消息失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建通知会话
     *
     * @param wxId 用户ID
     * @return 通知会话VO，如果没有通知则返回null
     */
    private ConversationItemVo buildNotificationConversation(Long wxId) {
        try {
            // 1. 查询最新的一条通知
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Notification::getToUserId, wxId)
                    .orderByDesc(Notification::getCreatedTime)
                    .last("LIMIT 1");

            Notification latestNotification = notificationMapper.selectOne(queryWrapper);

            // 如果没有通知，返回null
            if (latestNotification == null) {
                return null;
            }

            // 2. 查询未读通知数量
            int unreadCount = notificationMapper.countUnreadByUserId(wxId);

            // 3. 构建通知会话VO
            ConversationItemVo notificationVo = new ConversationItemVo();
            notificationVo.setConversationId("notification_" + wxId); // 固定的会话ID
            notificationVo.setPeerId("0"); // 系统通知的 peerId 为 0
            notificationVo.setConversationType(SourceType.NOTIFICATION);
            notificationVo.setPeerNickname("系统通知");
            notificationVo.setPeerAvatar(""); // 可以设置一个默认的通知图标URL

            // 设置最后一条消息的内容和时间
            notificationVo.setLastMessageContent(latestNotification.getContent() != null
                    ? latestNotification.getContent()
                    : latestNotification.getTitle());
            notificationVo.setLastMessageTime(latestNotification.getCreatedTime());

            // 设置未读数
            notificationVo.setUnreadCount(unreadCount);

            // 通知会话默认不置顶、不免打扰
            notificationVo.setIsPinned(false);
            notificationVo.setIsMuted(false);
            notificationVo.setDraftContent(null);
            notificationVo.setIsOnline(null); // 通知不需要在线状态

            log.debug("[ChatMessageService] 构建通知会话成功 - WxId: {}, UnreadCount: {}", wxId, unreadCount);

            return notificationVo;

        } catch (Exception e) {
            log.error("[ChatMessageService] 构建通知会话失败 - WxId: {}, Error: {}",
                    wxId, e.getMessage(), e);
            // 构建通知会话失败不影响整体，返回null
            return null;
        }
    }

    /**
     * 构建ChatMessage实体
     */
    private ChatMessage buildChatMessage(Long wxId, WxUserInfo fromUser, SendMessageDto dto) {
        ChatMessage chatMessage = new ChatMessage();

        // 生成消息唯一ID
        String messageId = generateMessageId(wxId, dto.getToId());
        chatMessage.setKfMsgId(messageId);

        // 基本信息
        chatMessage.setFromId(wxId);
        chatMessage.setToId(dto.getToId());

        // 消息格式
        MessageFormat messageFormat = parseMessageFormat(dto.getMessageFormat());
        chatMessage.setMessageFormat(messageFormat);

        // 消息类型
        MessageType messageType = parseMessageType(dto.getMessageType());
        chatMessage.setMessageType(messageType);

        // 构建消息内容
        ChatMessageContent content = new ChatMessageContent();
        content.setFormUserId(String.valueOf(wxId));
        content.setFormUserName(fromUser.getNickname());
        content.setFormUserPortrait(fromUser.getAvatarUrl());
        content.setType(dto.getMessageFormat() != null ? dto.getMessageFormat() : "text");
        content.setContent(dto.getContent());
        content.setExt(dto.getExt() != null ? dto.getExt() : "");

        chatMessage.setChatMessageContent(content);

        // 其他字段
        chatMessage.setIsShowTime(true);
        chatMessage.setStatus(MessageStatus.SENDING); // 发送中

        // 消息源类型
        SourceType sourceType = parseSourceType(dto.getSourceType());
        chatMessage.setSourceType(sourceType);

        chatMessage.setCreateTime(LocalDateTime.now());
        chatMessage.setUpdateTime(LocalDateTime.now());

        return chatMessage;
    }

    /**
     * 生成消息唯一ID
     */
    private String generateMessageId(Long fromId, Long toId) {
        return "MSG_" + fromId + "_" + toId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 解析消息格式
     */
    private MessageFormat parseMessageFormat(String format) {
        if (StringUtils.isBlank(format)) {
            return MessageFormat.TEXT;
        }
        MessageFormat messageFormat = MessageFormat.fromValue(format);
        return messageFormat != null ? messageFormat : MessageFormat.TEXT;
    }

    /**
     * 解析消息类型
     */
    private MessageType parseMessageType(String type) {
        if (StringUtils.isBlank(type)) {
            return MessageType.MESSAGE;
        }
        MessageType messageType = MessageType.valueOfAll(type);
        return messageType != null ? messageType : MessageType.MESSAGE;
    }

    /**
     * 解析消息源类型
     */
    private SourceType parseSourceType(String sourceType) {
        if (StringUtils.isBlank(sourceType)) {
            return SourceType.USER;
        }
        SourceType type = SourceType.fromValue(sourceType);
        return type != null ? type : SourceType.USER;
    }

    /**
     * 发送消息到Kafka
     */
    private void sendMessageToKafka(ChatMessage chatMessage, WxUserInfo fromUser) {
        try {
            // 构建完整的 UnifiedMessage，传递 kfMsgId
            com.cmswe.alumni.common.model.UnifiedMessage message = com.cmswe.alumni.common.model.UnifiedMessage.builder()
                    .messageId(chatMessage.getKfMsgId())  // 传递 kfMsgId 作为消息唯一ID
                    .category(com.cmswe.alumni.common.enums.MessageCategory.P2P)
                    .messageType("CHAT")
                    .fromId(chatMessage.getFromId())
                    .fromType("USER")
                    .fromName(fromUser.getNickname())
                    .fromAvatar(fromUser.getAvatarUrl())
                    .toId(chatMessage.getToId())
                    .toType("USER")
                    .content(chatMessage.getChatMessageContent().getContent())
                    .contentType(chatMessage.getMessageFormat().getValue())
                    .createTime(chatMessage.getCreateTime())
                    .needPersist(true)
                    .needPush(true)
                    .needOfflineStore(true)
                    .build();

            boolean success = unifiedMessageService.sendMessage(message);

            if (success) {
                log.info("[ChatMessageService] 消息已发送到Kafka - KfMsgId: {}", chatMessage.getKfMsgId());
            } else {
                log.error("[ChatMessageService] 消息发送到Kafka失败 - KfMsgId: {}", chatMessage.getKfMsgId());
            }

        } catch (Exception e) {
            // Kafka发送失败不影响消息入库，只记录日志
            log.error("[ChatMessageService] 发送消息到Kafka异常 - KfMsgId: {}, Error: {}",
                    chatMessage.getKfMsgId(), e.getMessage(), e);
        }
    }

    /**
     * 转换为VO
     */
    private ChatMessageVo convertToVo(ChatMessage message, Long currentUserId) {
        ChatMessageVo vo = new ChatMessageVo();
        BeanUtils.copyProperties(message, vo);
        vo.setMessageId(String.valueOf(message.getMessageId()));
        vo.setFromId(String.valueOf(message.getFromId()));
        vo.setToId(String.valueOf(message.getToId()));

        // 判断是否是当前用户发送的消息
        boolean isMine = message.getFromId().equals(currentUserId);
        vo.setIsMine(isMine);

        // 处理撤回消息：显示"您/对方撤回了一条消息"
        if (message.getStatus() == MessageStatus.RECALLED) {
            ChatMessageContent recalledContent = new ChatMessageContent();
            recalledContent.setContent(isMine ? "您撤回了一条消息" : "对方撤回了一条消息");
            recalledContent.setType("text");
            vo.setMsgContent(recalledContent);
        } else {
            vo.setMsgContent(message.getChatMessageContent());
        }

        return vo;
    }

    /**
     * 发送消息后更新会话表
     */
    private void updateConversationAfterSendMessage(ChatMessage chatMessage) {
        try {
            String messageContent = chatMessage.getChatMessageContent() != null
                    ? chatMessage.getChatMessageContent().getContent()
                    : "";

            // 限制内容长度，避免太长
            if (messageContent.length() > 100) {
                messageContent = messageContent.substring(0, 100) + "...";
            }

            // 1. 更新发送方的会话
            chatConversationService.updateLastMessage(
                    chatMessage.getFromId(),
                    chatMessage.getToId(),
                    chatMessage.getSourceType(),
                    chatMessage.getMessageId(),
                    messageContent,
                    chatMessage.getCreateTime(),
                    chatMessage.getFromId()
            );

            // 2. 更新接收方的会话
            chatConversationService.updateLastMessage(
                    chatMessage.getToId(),
                    chatMessage.getFromId(),
                    chatMessage.getSourceType(),
                    chatMessage.getMessageId(),
                    messageContent,
                    chatMessage.getCreateTime(),
                    chatMessage.getFromId()
            );

            // 3. 增加接收方的未读数
            chatConversationService.incrementUnreadCount(
                    chatMessage.getToId(),
                    chatMessage.getFromId(),
                    chatMessage.getSourceType(),
                    1
            );

            log.debug("[ChatMessageService] 会话表已更新 - From: {}, To: {}",
                    chatMessage.getFromId(), chatMessage.getToId());

        } catch (Exception e) {
            // 会话表更新失败不影响消息发送，只记录日志
            log.error("[ChatMessageService] 更新会话表失败 - MessageId: {}, Error: {}",
                    chatMessage.getMessageId(), e.getMessage(), e);
        }
    }
}
