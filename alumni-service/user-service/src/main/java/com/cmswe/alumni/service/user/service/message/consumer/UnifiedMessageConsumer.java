package com.cmswe.alumni.service.user.service.message.consumer;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.handler.HandlerChain;
import com.cmswe.alumni.kafka.handler.HandlerChainBuilder;
import com.cmswe.alumni.kafka.handler.MessageHandler;
import com.cmswe.alumni.kafka.reliability.DeadLetterQueueService;
import com.cmswe.alumni.kafka.reliability.MessageIdempotentService;
import com.cmswe.alumni.service.user.service.message.handler.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 统一消息消费者（企业级标准）
 *
 * <p>设计模式：责任链模式
 * <p>消息处理流程：
 * <ol>
 *   <li>WebSocket推送 {@link WebSocketPushHandler}</li>
 *   <li>数据库持久化 {@link DatabasePersistHandler}</li>
 *   <li>校友会加入申请处理 {@link AlumniJoinApprovalHandler}</li>
 *   <li>校友状态更新 {@link AlumniStatusUpdateHandler}</li>
 *   <li>Redis缓存更新 {@link RedisCacheHandler}</li>
 *   <li>离线消息处理 {@link OfflineMessageHandler}</li>
 * </ol>
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class UnifiedMessageConsumer {

    private final ObjectMapper objectMapper;
    private final HandlerChain<UnifiedMessage> handlerChain;
    private final MessageIdempotentService idempotentService;
    private final DeadLetterQueueService deadLetterQueueService;

    /**
     * 构造函数：使用新的 HandlerChainBuilder 构建责任链
     */
    public UnifiedMessageConsumer(
            ObjectMapper objectMapper,
            WebSocketPushHandler webSocketPushHandler,
            DatabasePersistHandler databasePersistHandler,
            AlumniJoinApprovalHandler alumniJoinApprovalHandler,
            AlumniStatusUpdateHandler alumniStatusUpdateHandler,
            RedisCacheHandler redisCacheHandler,
            OfflineMessageHandler offlineMessageHandler,
            MessageIdempotentService idempotentService,
            DeadLetterQueueService deadLetterQueueService) {

        this.objectMapper = objectMapper;
        this.idempotentService = idempotentService;
        this.deadLetterQueueService = deadLetterQueueService;

        // 使用新的 HandlerChainBuilder 构建责任链
        this.handlerChain = HandlerChainBuilder.<UnifiedMessage>create("消息处理责任链")
                .addHandler(webSocketPushHandler)
                .addHandler(databasePersistHandler)
                .addHandler(alumniJoinApprovalHandler)
                .addHandler(alumniStatusUpdateHandler)
                .addHandler(redisCacheHandler)
                .addHandler(offlineMessageHandler)
                .build();

        log.info("========================================");
        log.info("[UnifiedMessageConsumer] Bean 已创建！");
        log.info("[UnifiedMessageConsumer] 消息处理责任链初始化完成");
        log.info("[UnifiedMessageConsumer] 等待 Kafka Listener 注册...");
        log.info("========================================");
    }

    /**
     * Bean 初始化后检查 Kafka Listener 是否注册
     */
    @jakarta.annotation.PostConstruct
    public void checkKafkaListenerRegistration() {
        log.info("========================================");
        log.info("[UnifiedMessageConsumer] @PostConstruct 执行！");
        log.info("[UnifiedMessageConsumer] @KafkaListener 方法应该已被扫描注册");
        log.info("[UnifiedMessageConsumer] 监听 Topic: {}", KafkaTopicConstants.USER_MESSAGE_P2P);
        log.info("[UnifiedMessageConsumer] 消费者组: {}", KafkaTopicConstants.ConsumerGroup.MESSAGE_PROCESSOR);
        log.info("========================================");
    }

    // ==================== P2P消息消费 ====================

    /**
     * 消费P2P消息
     */
    @KafkaListener(
            topics = KafkaTopicConstants.USER_MESSAGE_P2P,
            groupId = KafkaTopicConstants.ConsumerGroup.MESSAGE_PROCESSOR,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeP2PMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[UnifiedMessageConsumer] 接收到P2P消息 - Partition: {}, Offset: {}", partition, offset);
        processMessage(message, "P2P", partition, offset);
    }

    // ==================== 群聊消息消费 ====================

    /**
     * 消费群聊消息
     */
    @KafkaListener(
            topics = KafkaTopicConstants.GROUP_MESSAGE_CHAT,
            groupId = KafkaTopicConstants.ConsumerGroup.MESSAGE_PROCESSOR,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeGroupMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[UnifiedMessageConsumer] 接收到群聊消息 - Partition: {}, Offset: {}", partition, offset);
        processMessage(message, "GROUP", partition, offset);
    }

    // ==================== 系统通知消费 ====================

    /**
     * 消费系统通知
     */
    @KafkaListener(
            topics = KafkaTopicConstants.SYSTEM_NOTIFICATION,
            groupId = KafkaTopicConstants.ConsumerGroup.NOTIFICATION_PROCESSOR,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSystemNotification(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[UnifiedMessageConsumer] 接收到系统通知 - Partition: {}, Offset: {}", partition, offset);
        processMessage(message, "SYSTEM", partition, offset);
    }

    // ==================== 组织通知消费 ====================

    /**
     * 消费组织通知
     */
    @KafkaListener(
            topics = KafkaTopicConstants.ORGANIZATION_NOTIFICATION,
            groupId = KafkaTopicConstants.ConsumerGroup.NOTIFICATION_PROCESSOR,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrganizationNotification(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[UnifiedMessageConsumer] 接收到组织通知 - Partition: {}, Offset: {}", partition, offset);
        processMessage(message, "ORGANIZATION", partition, offset);
    }

    // ==================== 业务通知消费 ====================

    /**
     * 消费业务通知
     */
    @KafkaListener(
            topics = KafkaTopicConstants.BUSINESS_NOTIFICATION,
            groupId = KafkaTopicConstants.ConsumerGroup.NOTIFICATION_PROCESSOR,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBusinessNotification(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[UnifiedMessageConsumer] 接收到业务通知 - Partition: {}, Offset: {}", partition, offset);
        processMessage(message, "BUSINESS", partition, offset);
    }

    // ==================== 核心处理方法 ====================

    /**
     * 处理消息（责任链模式）
     *
     * @param message   消息JSON字符串
     * @param category  消息类别
     * @param partition 分区号
     * @param offset    偏移量
     */
    private void processMessage(String message, String category, int partition, long offset) {
        UnifiedMessage unifiedMessage = null;

        try {
            // 1. 反序列化消息
            unifiedMessage = objectMapper.readValue(message, UnifiedMessage.class);

            log.info("[UnifiedMessageConsumer] 开始处理消息 - Category: {}, MessageId: {}, Type: {}, RawMessage: {}",
                    category, unifiedMessage.getMessageId(), unifiedMessage.getMessageType(), message);

            // 2. 消息验证
            if (!validateMessage(unifiedMessage)) {
                log.warn("[UnifiedMessageConsumer] 消息验证失败，发送到死信队列 - MessageId: {}",
                        unifiedMessage.getMessageId());
                handleFailedMessage(unifiedMessage, "消息验证失败：缺少必要字段");
                return;
            }

            // 3. 消息去重检查
            if (isDuplicateMessage(unifiedMessage)) {
                log.warn("[UnifiedMessageConsumer] 重复消息，跳过处理 - MessageId: {}",
                        unifiedMessage.getMessageId());
                return;
            }

            // 4. 通过责任链处理消息
            long startTime = System.currentTimeMillis();
            boolean success = handlerChain.execute(unifiedMessage);
            long endTime = System.currentTimeMillis();

            if (success) {
                log.info("[UnifiedMessageConsumer] 消息处理成功 - Category: {}, MessageId: {}, Time: {}ms",
                        category, unifiedMessage.getMessageId(), (endTime - startTime));
            } else {
                log.warn("[UnifiedMessageConsumer] 消息处理部分失败，发送到死信队列 - Category: {}, MessageId: {}",
                        category, unifiedMessage.getMessageId());
                handleFailedMessage(unifiedMessage, "消息处理失败：责任链处理返回false");
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // 反序列化失败
            log.error("[UnifiedMessageConsumer] 消息反序列化失败 - Category: {}, Partition: {}, Offset: {}, Error: {}",
                    category, partition, offset, e.getMessage(), e);

            // 构建最小化的UnifiedMessage对象用于死信队列
            handleFailedRawMessage(message, category, "消息反序列化失败：" + e.getMessage(), e);

        } catch (Exception e) {
            log.error("[UnifiedMessageConsumer] 消息处理异常 - Category: {}, Partition: {}, Offset: {}, Error: {}",
                    category, partition, offset, e.getMessage(), e);

            // 企业级标准：记录失败消息到死信队列
            if (unifiedMessage != null) {
                handleFailedMessage(unifiedMessage, "消息处理异常：" + e.getMessage());
            } else {
                handleFailedRawMessage(message, category, "消息处理异常：" + e.getMessage(), e);
            }
        }
    }

    /**
     * 消息验证
     */
    private boolean validateMessage(UnifiedMessage message) {
        if (message == null) {
            log.warn("[UnifiedMessageConsumer] 消息为空");
            return false;
        }

        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
            log.warn("[UnifiedMessageConsumer] 消息ID为空");
            return false;
        }

        if (message.getCategory() == null) {
            log.warn("[UnifiedMessageConsumer] 消息类别为空 - MessageId: {}", message.getMessageId());
            return false;
        }

        return true;
    }

    /**
     * 消息去重检查（企业级标准：基于消息ID的幂等性保证）
     */
    private boolean isDuplicateMessage(UnifiedMessage message) {
        // 使用MessageIdempotentService检查消息是否已处理
        boolean isDuplicate = idempotentService.isMessageProcessed(message.getMessageId());

        if (!isDuplicate) {
            // 标记消息为已处理
            idempotentService.markMessageAsProcessed(message.getMessageId());
        }

        return isDuplicate;
    }

    /**
     * 处理失败的消息（发送到死信队列）
     *
     * @param message 消息
     * @param reason  失败原因
     */
    private void handleFailedMessage(UnifiedMessage message, String reason) {
        try {
            log.error("[UnifiedMessageConsumer] 消息处理失败，发送到死信队列 - MessageId: {}, Reason: {}",
                    message.getMessageId(), reason);

            // 发送到死信队列（使用新的通用方法）
            String topic = getTopicByCategory(message.getCategory());
            deadLetterQueueService.sendToDeadLetterQueue(
                    topic,
                    message.getMessageId(),
                    message,
                    reason,
                    message.getRetryCount()
            );

        } catch (Exception e) {
            log.error("[UnifiedMessageConsumer] 发送消息到死信队列失败 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);
        }
    }

    /**
     * 处理原始消息失败（无法反序列化的消息）
     *
     * @param rawMessage 原始消息JSON字符串
     * @param category   消息类别
     * @param reason     失败原因
     * @param exception  异常信息
     */
    private void handleFailedRawMessage(String rawMessage, String category, String reason, Exception exception) {
        try {
            log.error("[UnifiedMessageConsumer] 原始消息处理失败，发送到死信队列 - Category: {}, Reason: {}",
                    category, reason);

            // 构建最小化的UnifiedMessage对象
            UnifiedMessage fallbackMessage = UnifiedMessage.builder()
                    .messageId("UNKNOWN_" + System.currentTimeMillis())
                    .category(parseCategoryFromString(category))
                    .messageType("UNKNOWN")
                    .content(rawMessage) // 保存原始JSON字符串
                    .createTime(java.time.LocalDateTime.now())
                    .retryCount(0)
                    .build();

            // 构建详细的失败原因（包含异常堆栈）
            String detailedReason = reason + " | 原始消息: " + rawMessage + " | 异常: " + exception.getClass().getName();

            // 发送到死信队列（使用通用方法）
            String topic = "unknown." + category.toLowerCase();
            deadLetterQueueService.sendToDeadLetterQueue(
                    topic,
                    fallbackMessage.getMessageId(),
                    fallbackMessage,
                    detailedReason,
                    0
            );

            log.info("[UnifiedMessageConsumer] 原始消息已发送到死信队列 - Category: {}", category);

        } catch (Exception e) {
            log.error("[UnifiedMessageConsumer] 发送原始消息到死信队列失败 - Category: {}, Error: {}",
                    category, e.getMessage(), e);
        }
    }

    /**
     * 从字符串解析消息类别
     *
     * @param categoryStr 类别字符串
     * @return 消息类别枚举
     */
    private MessageCategory parseCategoryFromString(String categoryStr) {
        try {
            return MessageCategory.valueOf(categoryStr);
        } catch (Exception e) {
            log.warn("[UnifiedMessageConsumer] 无法解析消息类别 - Category: {}, 使用默认值BUSINESS", categoryStr);
            return MessageCategory.BUSINESS;
        }
    }

    /**
     * 根据消息类别获取 Topic
     *
     * @param category 消息类别
     * @return Topic 名称
     */
    private String getTopicByCategory(MessageCategory category) {
        if (category == null) {
            return "unknown";
        }

        return switch (category) {
            case P2P -> KafkaTopicConstants.USER_MESSAGE_P2P;
            case GROUP -> KafkaTopicConstants.GROUP_MESSAGE_CHAT;
            case SYSTEM -> KafkaTopicConstants.SYSTEM_NOTIFICATION;
            case ORGANIZATION -> KafkaTopicConstants.ORGANIZATION_NOTIFICATION;
            case BUSINESS -> KafkaTopicConstants.BUSINESS_NOTIFICATION;
            default -> "unknown";
        };
    }
}
