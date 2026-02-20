package com.cmswe.alumni.service.user.service.message;

import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.enums.MessagePriority;
import com.cmswe.alumni.common.enums.NotificationType;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.producer.MessageProducer;
import com.cmswe.alumni.service.user.service.message.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 统一消息服务（企业级标准）
 *
 * <p>门面模式：对外提供统一的消息发送接口，内部根据消息类别选择对应的生产者
 * <p>策略模式：根据消息类别动态选择处理策略
 *
 * <p>主要功能：
 * <ul>
 *   <li>P2P消息发送</li>
 *   <li>群聊消息发送</li>
 *   <li>系统通知发送</li>
 *   <li>组织通知发送</li>
 *   <li>业务通知发送</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
public class UnifiedMessageService {

    private final Map<MessageCategory, MessageProducer> producerMap = new EnumMap<>(MessageCategory.class);

    /**
     * 构造函数：初始化各个消息生产者
     */
    public UnifiedMessageService(
            P2PMessageProducer p2pMessageProducer,
            GroupMessageProducer groupMessageProducer,
            SystemNotificationProducer systemNotificationProducer,
            OrganizationNotificationProducer organizationNotificationProducer,
            BusinessNotificationProducer businessNotificationProducer) {

        producerMap.put(MessageCategory.P2P, p2pMessageProducer);
        producerMap.put(MessageCategory.GROUP, groupMessageProducer);
        producerMap.put(MessageCategory.SYSTEM, systemNotificationProducer);
        producerMap.put(MessageCategory.ORGANIZATION, organizationNotificationProducer);
        producerMap.put(MessageCategory.BUSINESS, businessNotificationProducer);

        log.info("[UnifiedMessageService] 消息生产者初始化完成 - ProducerCount: {}", producerMap.size());
    }

    // ==================== 核心发送方法 ====================

    /**
     * 发送消息（异步）
     *
     * @param message 统一消息对象
     * @return 是否发送成功
     */
    public boolean sendMessage(UnifiedMessage message) {
        try {
            MessageProducer producer = getProducer(message.getCategory());
            return producer.sendAsync(message);
        } catch (Exception e) {
            log.error("[UnifiedMessageService] 消息发送失败 - Category: {}, Error: {}",
                    message.getCategory(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送消息（同步）
     *
     * @param message 统一消息对象
     * @return 是否发送成功
     */
    public boolean sendMessageSync(UnifiedMessage message) {
        try {
            MessageProducer producer = getProducer(message.getCategory());
            return producer.sendSync(message);
        } catch (Exception e) {
            log.error("[UnifiedMessageService] 消息同步发送失败 - Category: {}, Error: {}",
                    message.getCategory(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量发送消息
     *
     * @param messages 消息列表
     * @return 成功发送的消息数量
     */
    public int batchSendMessages(List<UnifiedMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }

        // 按消息类别分组
        Map<MessageCategory, List<UnifiedMessage>> groupedMessages = groupMessagesByCategory(messages);

        int totalSuccess = 0;
        for (Map.Entry<MessageCategory, List<UnifiedMessage>> entry : groupedMessages.entrySet()) {
            MessageProducer producer = getProducer(entry.getKey());
            totalSuccess += producer.batchSendAsync(entry.getValue());
        }

        return totalSuccess;
    }

    // ==================== P2P消息发送（用户私聊）====================

    /**
     * 发送P2P消息
     *
     * @param fromUserId   发送方用户ID
     * @param fromUsername 发送方用户名
     * @param toUserId     接收方用户ID
     * @param content      消息内容
     * @return 是否发送成功
     */
    public boolean sendP2PMessage(Long fromUserId, String fromUsername, Long toUserId, String content) {
        UnifiedMessage message = UnifiedMessage.builder()
                .category(MessageCategory.P2P)
                .messageType("CHAT")
                .fromId(fromUserId)
                .fromType("USER")
                .fromName(fromUsername)
                .toId(toUserId)
                .toType("USER")
                .content(content)
                .createTime(LocalDateTime.now())
                .needPersist(true)
                .needPush(true)
                .needOfflineStore(true)
                .build();

        return sendMessage(message);
    }

    // ==================== 群聊消息发送 ====================

    /**
     * 发送群聊消息
     *
     * @param fromUserId   发送方用户ID
     * @param fromUsername 发送方用户名
     * @param groupId      群组ID
     * @param content      消息内容
     * @return 是否发送成功
     */
    public boolean sendGroupMessage(Long fromUserId, String fromUsername, Long groupId, String content) {
        UnifiedMessage message = UnifiedMessage.builder()
                .category(MessageCategory.GROUP)
                .messageType("GROUP_CHAT")
                .fromId(fromUserId)
                .fromType("USER")
                .fromName(fromUsername)
                .toId(groupId)
                .toType("GROUP")
                .content(content)
                .createTime(LocalDateTime.now())
                .needPersist(true)
                .needPush(true)
                .needOfflineStore(true)
                .build();

        return sendMessage(message);
    }

    // ==================== 系统通知发送 ====================

    /**
     * 发送系统通知给单个用户
     *
     * @param userId           接收方用户ID
     * @param notificationType 通知类型
     * @param title            通知标题
     * @param content          通知内容
     * @return 是否发送成功
     */
    public boolean sendSystemNotification(Long userId, NotificationType notificationType,
                                          String title, String content) {
        return sendSystemNotification(userId, notificationType, title, content, null, null);
    }

    /**
     * 发送系统通知给单个用户（带关联业务信息）
     *
     * @param userId           接收方用户ID
     * @param notificationType 通知类型
     * @param title            通知标题
     * @param content          通知内容
     * @param relatedId        关联业务ID
     * @param relatedType      关联业务类型
     * @return 是否发送成功
     */
    public boolean sendSystemNotification(Long userId, NotificationType notificationType,
                                          String title, String content,
                                          Long relatedId, String relatedType) {
        // 生成消息唯一ID（用于Kafka去重）
        String messageId = generateMessageId("SYSTEM", 0L, userId);

        UnifiedMessage message = UnifiedMessage.builder()
                .messageId(messageId)
                .category(MessageCategory.SYSTEM)
                .messageType(notificationType.getCode())
                .fromId(0L) // 系统
                .fromType("SYSTEM")
                .fromName("系统通知")
                .toId(userId)
                .toType("USER")
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .priority(MessagePriority.HIGH)
                .createTime(LocalDateTime.now())
                .needPersist(true)
                .needPush(true)
                .needOfflineStore(true)
                .build();

        return sendMessage(message);
    }

    /**
     * 发送系统通知给所有用户（广播）
     *
     * @param notificationType 通知类型
     * @param title            通知标题
     * @param content          通知内容
     * @return 是否发送成功
     */
    public boolean broadcastSystemNotification(NotificationType notificationType,
                                               String title, String content) {
        // 生成消息唯一ID（用于Kafka去重）
        String messageId = generateMessageId("BROADCAST", 0L, 0L);

        UnifiedMessage message = UnifiedMessage.builder()
                .messageId(messageId)
                .category(MessageCategory.SYSTEM)
                .messageType(notificationType.getCode())
                .fromId(0L)
                .fromType("SYSTEM")
                .fromName("系统通知")
                .toId(0L) // 0表示全体用户
                .toType("ALL")
                .title(title)
                .content(content)
                .priority(MessagePriority.HIGH)
                .createTime(LocalDateTime.now())
                .needPersist(true)
                .needPush(true)
                .needOfflineStore(true)
                .build();

        return sendMessage(message);
    }

    /**
     * 批量发送系统通知
     *
     * @param userIds          接收方用户ID列表
     * @param notificationType 通知类型
     * @param title            通知标题
     * @param content          通知内容
     * @return 成功发送的数量
     */
    public int batchSendSystemNotification(List<Long> userIds, NotificationType notificationType,
                                           String title, String content) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }

        List<UnifiedMessage> messages = new ArrayList<>();
        for (Long userId : userIds) {
            // 生成消息唯一ID（用于Kafka去重）
            String messageId = generateMessageId("SYSTEM", 0L, userId);

            UnifiedMessage message = UnifiedMessage.builder()
                    .messageId(messageId)
                    .category(MessageCategory.SYSTEM)
                    .messageType(notificationType.getCode())
                    .fromId(0L)
                    .fromType("SYSTEM")
                    .fromName("系统通知")
                    .toId(userId)
                    .toType("USER")
                    .title(title)
                    .content(content)
                    .priority(MessagePriority.HIGH)
                    .createTime(LocalDateTime.now())
                    .needPersist(true)
                    .needPush(true)
                    .needOfflineStore(true)
                    .build();
            messages.add(message);
        }

        return batchSendMessages(messages);
    }

    // ==================== 组织通知发送 ====================

    /**
     * 发送组织通知
     *
     * @param organizationId   组织ID
     * @param organizationName 组织名称
     * @param followersIds     关注者ID列表
     * @param notificationType 通知类型
     * @param title            通知标题
     * @param content          通知内容
     * @param relatedId        关联业务ID（如活动ID）
     * @param relatedType      关联业务类型（如EVENT）
     * @return 是否发送成功
     */
    public boolean sendOrganizationNotification(Long organizationId, String organizationName,
                                                List<Long> followersIds, NotificationType notificationType,
                                                String title, String content,
                                                Long relatedId, String relatedType) {
        UnifiedMessage message = UnifiedMessage.builder()
                .category(MessageCategory.ORGANIZATION)
                .messageType(notificationType.getCode())
                .fromId(organizationId)
                .fromType("ORGANIZATION")
                .fromName(organizationName)
                .toIds(followersIds)
                .toType("USER_BATCH")
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .priority(MessagePriority.NORMAL)
                .createTime(LocalDateTime.now())
                .needPersist(true)
                .needPush(true)
                .needOfflineStore(true)
                .build();

        return sendMessage(message);
    }

    // ==================== 业务通知发送 ====================

    /**
     * 发送用户关注通知
     *
     * @param fromUserId   关注者用户ID
     * @param fromUsername 关注者用户名
     * @param toUserId     被关注者用户ID
     * @return 是否发送成功
     */
    public boolean sendFollowNotification(Long fromUserId, String fromUsername, Long toUserId) {
        // 生成消息唯一ID（用于Kafka去重）
        String messageId = generateMessageId("FOLLOW", fromUserId, toUserId);

        UnifiedMessage message = UnifiedMessage.builder()
                .messageId(messageId)
                .category(MessageCategory.BUSINESS)
                .messageType(NotificationType.USER_FOLLOW.getCode())
                .fromId(fromUserId)
                .fromType("USER")
                .fromName(fromUsername)
                .toId(toUserId)
                .toType("USER")
                .title("新的关注")
                .content(fromUsername + " 关注了你")
                .relatedId(fromUserId)
                .relatedType("USER")
                .priority(MessagePriority.NORMAL)
                .createTime(LocalDateTime.now())
                .needPersist(true)
                .needPush(true)
                .needOfflineStore(true)
                .build();

        return sendMessage(message);
    }

    /**
     * 发送评论通知
     *
     * @param fromUserId   评论者用户ID
     * @param fromUsername 评论者用户名
     * @param toUserId     被评论者用户ID
     * @param commentId    评论ID
     * @param content      评论内容
     * @return 是否发送成功
     */
    public boolean sendCommentNotification(Long fromUserId, String fromUsername,
                                           Long toUserId, Long commentId, String content) {
        // 生成消息唯一ID（用于Kafka去重）
        String messageId = generateMessageId("COMMENT", fromUserId, toUserId);

        UnifiedMessage message = UnifiedMessage.builder()
                .messageId(messageId)
                .category(MessageCategory.BUSINESS)
                .messageType(NotificationType.COMMENT.getCode())
                .fromId(fromUserId)
                .fromType("USER")
                .fromName(fromUsername)
                .toId(toUserId)
                .toType("USER")
                .title("新的评论")
                .content(fromUsername + " 评论了你: " + content)
                .relatedId(commentId)
                .relatedType("COMMENT")
                .priority(MessagePriority.NORMAL)
                .createTime(LocalDateTime.now())
                .needPersist(true)
                .needPush(true)
                .needOfflineStore(true)
                .build();

        return sendMessage(message);
    }

    /**
     * 发送点赞通知
     *
     * @param fromUserId   点赞者用户ID
     * @param fromUsername 点赞者用户名
     * @param toUserId     被点赞者用户ID
     * @param targetId     点赞目标ID
     * @param targetType   点赞目标类型
     * @return 是否发送成功
     */
    public boolean sendLikeNotification(Long fromUserId, String fromUsername,
                                        Long toUserId, Long targetId, String targetType) {
        // 生成消息唯一ID（用于Kafka去重）
        String messageId = generateMessageId("LIKE", fromUserId, toUserId);

        UnifiedMessage message = UnifiedMessage.builder()
                .messageId(messageId)
                .category(MessageCategory.BUSINESS)
                .messageType(NotificationType.LIKE.getCode())
                .fromId(fromUserId)
                .fromType("USER")
                .fromName(fromUsername)
                .toId(toUserId)
                .toType("USER")
                .title("新的点赞")
                .content(fromUsername + " 赞了你的" + targetType)
                .relatedId(targetId)
                .relatedType(targetType)
                .priority(MessagePriority.LOW)
                .createTime(LocalDateTime.now())
                .needPersist(true)
                .needPush(false) // 点赞通知可以不推送，减少打扰
                .needOfflineStore(true)
                .build();

        return sendMessage(message);
    }

    /**
     * 发送业务通知给单个用户（通用方法）
     *
     * @param userId      接收方用户ID
     * @param messageType 业务消息类型
     * @param title       通知标题
     * @param content     通知内容
     * @param relatedId   关联业务ID
     * @param relatedType 关联业务类型
     * @return 是否发送成功
     */
    public boolean sendBusinessNotification(Long userId, String messageType,
                                            String title, String content,
                                            Long relatedId, String relatedType) {
        // 生成消息唯一ID（用于Kafka去重）
        String messageId = generateMessageId(messageType, 0L, userId);

        UnifiedMessage message = UnifiedMessage.builder()
                .messageId(messageId)
                .category(MessageCategory.BUSINESS)
                .messageType(messageType)
                .fromId(0L) // 系统触发
                .fromType("SYSTEM")
                .fromName("系统")
                .toId(userId)
                .toType("USER")
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .priority(MessagePriority.NORMAL)
                .createTime(LocalDateTime.now())
                .needPersist(true)
                .needPush(true)
                .needOfflineStore(true)
                .build();

        return sendMessage(message);
    }

    // ==================== 群组通知发送 ====================

    /**
     * 发送群主转移通知
     *
     * @param groupId     群组ID
     * @param oldOwnerId  原群主ID
     * @param newOwnerId  新群主ID
     * @param newOwnerName 新群主名称
     * @return 是否发送成功
     */
    public boolean sendGroupOwnerTransferNotification(Long groupId, Long oldOwnerId,
                                                      Long newOwnerId, String newOwnerName) {
        // 生成消息唯一ID（用于Kafka去重）
        String messageIdToOldOwner = generateMessageId("GROUP_TRANSFER_OLD", groupId, oldOwnerId);
        String messageIdToNewOwner = generateMessageId("GROUP_TRANSFER_NEW", groupId, newOwnerId);

        // 发送给原群主
        UnifiedMessage messageToOldOwner = UnifiedMessage.builder()
                .messageId(messageIdToOldOwner)
                .category(MessageCategory.SYSTEM)
                .messageType(NotificationType.GROUP_OWNER_TRANSFER.getCode())
                .fromId(0L)
                .fromType("SYSTEM")
                .fromName("系统通知")
                .toId(oldOwnerId)
                .toType("USER")
                .title("群主转移")
                .content("你已将群主转移给 " + newOwnerName)
                .relatedId(groupId)
                .relatedType("GROUP")
                .priority(MessagePriority.HIGH)
                .createTime(LocalDateTime.now())
                .build();

        // 发送给新群主
        UnifiedMessage messageToNewOwner = UnifiedMessage.builder()
                .messageId(messageIdToNewOwner)
                .category(MessageCategory.SYSTEM)
                .messageType(NotificationType.GROUP_OWNER_TRANSFER.getCode())
                .fromId(0L)
                .fromType("SYSTEM")
                .fromName("系统通知")
                .toId(newOwnerId)
                .toType("USER")
                .title("群主转移")
                .content("你已成为新的群主")
                .relatedId(groupId)
                .relatedType("GROUP")
                .priority(MessagePriority.HIGH)
                .createTime(LocalDateTime.now())
                .build();

        boolean result1 = sendMessage(messageToOldOwner);
        boolean result2 = sendMessage(messageToNewOwner);

        return result1 && result2;
    }

    // ==================== 辅助方法 ====================

    /**
     * 根据消息类别获取生产者
     */
    private MessageProducer getProducer(MessageCategory category) {
        MessageProducer producer = producerMap.get(category);
        if (producer == null) {
            throw new IllegalArgumentException("不支持的消息类别: " + category);
        }
        return producer;
    }

    /**
     * 按消息类别分组
     */
    private Map<MessageCategory, List<UnifiedMessage>> groupMessagesByCategory(List<UnifiedMessage> messages) {
        Map<MessageCategory, List<UnifiedMessage>> grouped = new EnumMap<>(MessageCategory.class);
        for (UnifiedMessage message : messages) {
            grouped.computeIfAbsent(message.getCategory(), k -> new ArrayList<>()).add(message);
        }
        return grouped;
    }

    /**
     * 生成消息唯一ID（用于Kafka去重和数据库幂等性）
     *
     * @param prefix 消息前缀（如 FOLLOW、COMMENT、LIKE）
     * @param fromId 发送方ID
     * @param toId   接收方ID
     * @return 消息唯一ID
     */
    private String generateMessageId(String prefix, Long fromId, Long toId) {
        return "MSG_" + prefix + "_" + fromId + "_" + toId + "_"
                + System.currentTimeMillis() + "_"
                + UUID.randomUUID().toString().substring(0, 8);
    }
}
