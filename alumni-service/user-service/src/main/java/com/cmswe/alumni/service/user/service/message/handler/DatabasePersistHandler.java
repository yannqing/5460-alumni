package com.cmswe.alumni.service.user.service.message.handler;

import com.cmswe.alumni.common.entity.ChatMessage;
import com.cmswe.alumni.common.entity.Notification;
import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.enums.MessageFormat;
import com.cmswe.alumni.common.enums.MessageStatus;
import com.cmswe.alumni.common.enums.MessageType;
import com.cmswe.alumni.common.enums.SourceType;
import com.cmswe.alumni.common.model.ChatMessageContent;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.handler.AbstractMessageHandler;
import com.cmswe.alumni.service.user.mapper.ChatMessageMapper;
import com.cmswe.alumni.service.user.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库持久化处理器（责任链模式）
 *
 * <p>负责将消息保存到数据库
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Component
public class DatabasePersistHandler extends AbstractMessageHandler<UnifiedMessage> {

    private final NotificationService notificationService;
    private final ChatMessageMapper chatMessageMapper;
    private final ObjectMapper objectMapper;

    public DatabasePersistHandler(
            NotificationService notificationService,
            ChatMessageMapper chatMessageMapper,
            ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.chatMessageMapper = chatMessageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getHandlerName() {
        return "DatabasePersistHandler";
    }

    @Override
    protected boolean doHandle(UnifiedMessage message) {
        // 检查是否需要持久化
        if (!message.getNeedPersist()) {
            log.debug("[DatabasePersistHandler] 消息不需要持久化 - MessageId: {}", message.getMessageId());
            return true;
        }

        try {
            // 根据消息类别选择不同的持久化策略
            switch (message.getCategory()) {
                case P2P -> persistP2PMessage(message);
                case GROUP -> persistGroupMessage(message);
                case SYSTEM, ORGANIZATION, BUSINESS -> persistNotification(message);
                default -> {
                    log.warn("[DatabasePersistHandler] 未知的消息类别 - Category: {}", message.getCategory());
                    return false;
                }
            }

            log.info("[DatabasePersistHandler] 消息持久化成功 - Category: {}, MessageId: {}",
                    message.getCategory(), message.getMessageId());
            return true;

        } catch (Exception e) {
            log.error("[DatabasePersistHandler] 消息持久化失败 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 持久化P2P消息（Kafka消费后更新消息状态为已送达）
     */
    private void persistP2PMessage(UnifiedMessage message) {
        try {
            String kfMsgId = message.getMessageId();

            // 1. 根据 kfMsgId 查询已存在的消息记录
            ChatMessage existingMessage = chatMessageMapper.selectByKfMsgId(kfMsgId);

            if (existingMessage == null) {
                // 消息不存在，可能是系统消息或其他来源的消息，直接插入
                log.warn("[DatabasePersistHandler] 消息不存在，直接插入 - KfMsgId: {}", kfMsgId);
                ChatMessage chatMessage = convertToChatMessage(message, SourceType.USER);
                chatMessageMapper.insert(chatMessage);
                log.info("[DatabasePersistHandler] P2P消息插入成功 - KfMsgId: {}, From: {}, To: {}",
                        kfMsgId, message.getFromId(), message.getToId());
            } else {
                // 2. 消息已存在，更新状态为已送达（1）
                Integer updatedRows = chatMessageMapper.updateStatusByKfMsgId(kfMsgId, MessageStatus.DELIVERED.getValue());

                if (updatedRows > 0) {
                    log.info("[DatabasePersistHandler] P2P消息状态更新成功 - KfMsgId: {}, From: {}, To: {}, Status: {} -> {}",
                            kfMsgId, message.getFromId(), message.getToId(),
                            existingMessage.getStatus().getValue(), MessageStatus.DELIVERED.getValue());
                } else {
                    log.warn("[DatabasePersistHandler] P2P消息状态更新失败（可能已经不是发送中状态） - KfMsgId: {}, CurrentStatus: {}",
                            kfMsgId, existingMessage.getStatus().getValue());
                }
            }

        } catch (Exception e) {
            log.error("[DatabasePersistHandler] P2P消息持久化失败 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("P2P消息持久化失败", e);
        }
    }

    /**
     * 持久化群聊消息（Kafka消费后更新消息状态为已送达）
     */
    private void persistGroupMessage(UnifiedMessage message) {
        try {
            String kfMsgId = message.getMessageId();

            // 1. 根据 kfMsgId 查询已存在的消息记录
            ChatMessage existingMessage = chatMessageMapper.selectByKfMsgId(kfMsgId);

            if (existingMessage == null) {
                // 消息不存在，直接插入
                log.warn("[DatabasePersistHandler] 群聊消息不存在，直接插入 - KfMsgId: {}", kfMsgId);
                ChatMessage chatMessage = convertToChatMessage(message, SourceType.GROUP);
                chatMessageMapper.insert(chatMessage);
                log.info("[DatabasePersistHandler] 群聊消息插入成功 - KfMsgId: {}, From: {}, GroupId: {}",
                        kfMsgId, message.getFromId(), message.getToId());
            } else {
                // 2. 消息已存在，更新状态为已送达（1）
                Integer updatedRows = chatMessageMapper.updateStatusByKfMsgId(kfMsgId, MessageStatus.DELIVERED.getValue());

                if (updatedRows > 0) {
                    log.info("[DatabasePersistHandler] 群聊消息状态更新成功 - KfMsgId: {}, From: {}, GroupId: {}, Status: {} -> {}",
                            kfMsgId, message.getFromId(), message.getToId(),
                            existingMessage.getStatus().getValue(), MessageStatus.DELIVERED.getValue());
                } else {
                    log.warn("[DatabasePersistHandler] 群聊消息状态更新失败（可能已经不是发送中状态） - KfMsgId: {}, CurrentStatus: {}",
                            kfMsgId, existingMessage.getStatus().getValue());
                }
            }

        } catch (Exception e) {
            log.error("[DatabasePersistHandler] 群聊消息持久化失败 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("群聊消息持久化失败", e);
        }
    }

    /**
     * 将UnifiedMessage转换为ChatMessage实体
     *
     * @param message    统一消息对象
     * @param sourceType 消息源类型
     * @return ChatMessage实体
     */
    private ChatMessage convertToChatMessage(UnifiedMessage message, SourceType sourceType) {
        ChatMessage chatMessage = new ChatMessage();

        // 设置 Kafka 消息 ID
        chatMessage.setKfMsgId(message.getMessageId());

        // 基本信息
        chatMessage.setFromId(message.getFromId());
        chatMessage.setToId(message.getToId());

        // 消息格式（从extraData中获取，如果没有则默认为TEXT）
        MessageFormat messageFormat = parseMessageFormat(message);
        chatMessage.setMessageFormat(messageFormat);

        // 消息类型（普通消息、通知消息、媒体消息）
        MessageType messageType = parseMessageType(message, messageFormat);
        chatMessage.setMessageType(messageType);

        // 构建消息内容（JSON格式）
        ChatMessageContent content = buildChatMessageContent(message);
        chatMessage.setChatMessageContent(content);

        // 是否显示时间（第一条消息或间隔超过5分钟的消息显示时间）
        chatMessage.setIsShowTime(true); // 默认显示，可以根据业务逻辑调整

        // 消息状态（已送达-未读）
        chatMessage.setStatus(MessageStatus.DELIVERED);

        // 消息源类型
        chatMessage.setSourceType(sourceType);

        // 时间信息
        chatMessage.setCreateTime(message.getCreateTime() != null ? message.getCreateTime() : LocalDateTime.now());
        chatMessage.setUpdateTime(LocalDateTime.now());

        return chatMessage;
    }

    /**
     * 解析消息格式
     */
    private MessageFormat parseMessageFormat(UnifiedMessage message) {
        // 从contentType或extraData中获取消息格式
        if (message.getContentType() != null) {
            String contentType = message.getContentType().toLowerCase();
            if (contentType.contains("image")) {
                return MessageFormat.IMAGE;
            } else if (contentType.contains("video")) {
                return MessageFormat.VIDEO;
            } else if (contentType.contains("audio")) {
                return MessageFormat.AUDIO;
            } else if (contentType.contains("file")) {
                return MessageFormat.FILE;
            }
        }

        // 从extraData中获取format字段
        if (message.getExtraData() != null && message.getExtraData().containsKey("format")) {
            String format = String.valueOf(message.getExtraData().get("format"));
            MessageFormat messageFormat = MessageFormat.fromValue(format);
            if (messageFormat != null) {
                return messageFormat;
            }
        }

        // 默认为TEXT
        return MessageFormat.TEXT;
    }

    /**
     * 解析消息类型
     */
    private MessageType parseMessageType(UnifiedMessage message, MessageFormat messageFormat) {
        // 如果是媒体格式，返回MEDIA类型
        if (messageFormat == MessageFormat.IMAGE ||
                messageFormat == MessageFormat.VIDEO ||
                messageFormat == MessageFormat.AUDIO ||
                messageFormat == MessageFormat.FILE) {
            return MessageType.MEDIA;
        }

        // 如果是系统通知，返回NOTIFY类型
        if (message.getFromId() == null || message.getFromId() == 0) {
            return MessageType.NOTIFY;
        }

        // 默认为MESSAGE类型
        return MessageType.MESSAGE;
    }

    /**
     * 构建ChatMessageContent对象
     */
    private ChatMessageContent buildChatMessageContent(UnifiedMessage message) {
        ChatMessageContent content = new ChatMessageContent();

        // 发送方信息
        content.setFormUserId(String.valueOf(message.getFromId()));
        content.setFormUserName(message.getFromName());
        content.setFormUserPortrait(message.getFromAvatar());

        // 消息类型
        content.setType(message.getContentType() != null ? message.getContentType() : "text");

        // 消息内容
        content.setContent(message.getContent());

        // 扩展数据（将extraData转换为JSON字符串）
        if (message.getExtraData() != null && !message.getExtraData().isEmpty()) {
            try {
                content.setExt(objectMapper.writeValueAsString(message.getExtraData()));
            } catch (Exception e) {
                log.warn("[DatabasePersistHandler] 序列化扩展数据失败 - MessageId: {}, Error: {}",
                        message.getMessageId(), e.getMessage());
                content.setExt("{}");
            }
        } else {
            content.setExt("");
        }

        return content;
    }

    /**
     * 持久化通知消息（系统通知、组织通知、业务通知）
     */
    private void persistNotification(UnifiedMessage message) {
        if (message.getToId() != null && message.getToId() != 0) {
            // 单个用户通知
            Notification notification = convertToNotification(message);
            notificationService.saveWithDeduplication(notification);
            log.debug("[DatabasePersistHandler] 通知持久化 - ToUserId: {}", message.getToId());

        } else if (message.getToIds() != null && !message.getToIds().isEmpty()) {
            // 批量用户通知
            List<Notification> notifications = new ArrayList<>();
            for (Long userId : message.getToIds()) {
                Notification notification = convertToNotification(message);
                notification.setToUserId(userId);
                notifications.add(notification);
            }

            // 批量保存
            for (Notification notification : notifications) {
                notificationService.saveWithDeduplication(notification);
            }

            log.debug("[DatabasePersistHandler] 批量通知持久化 - Count: {}", notifications.size());

        } else if (message.getToId() != null && message.getToId() == 0) {
            // 广播通知（暂不保存，或者保存一条标记为广播的记录）
            log.debug("[DatabasePersistHandler] 广播通知不持久化 - MessageId: {}", message.getMessageId());
        }
    }

    /**
     * 将UnifiedMessage转换为Notification实体
     */
    private Notification convertToNotification(UnifiedMessage message) {
        Notification notification = new Notification();

        notification.setMessageId(message.getMessageId());
        notification.setMessageType(message.getMessageType());
        notification.setFromUserId(message.getFromId());
        notification.setFromUsername(message.getFromName());
        notification.setToUserId(message.getToId());
        notification.setTitle(message.getTitle());
        notification.setContent(message.getContent());
        notification.setRelatedId(message.getRelatedId());
        notification.setRelatedType(message.getRelatedType());
        notification.setReadStatus(0); // 未读
        notification.setCreatedTime(message.getCreateTime());

        // 扩展数据
        if (message.getExtraData() != null) {
            notification.setExtraData(message.getExtraData().toString());
        }

        return notification;
    }
}
