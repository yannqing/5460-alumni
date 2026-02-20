package com.cmswe.alumni.service.user.service.message.consumer;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.entity.MessageDeadLetter;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.reliability.DeadLetterQueueService.DeadLetterMessage;
import com.cmswe.alumni.service.user.mapper.MessageDeadLetterMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * 死信队列消费者（企业级标准）
 *
 * <p>功能：
 * <ul>
 *   <li>消费Kafka死信队列中的消息</li>
 *   <li>将死信消息持久化到数据库</li>
 *   <li>支持后续人工处理和重试</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class DeadLetterQueueConsumer {

    private final ObjectMapper objectMapper;
    private final MessageDeadLetterMapper deadLetterMapper;

    public DeadLetterQueueConsumer(ObjectMapper objectMapper, MessageDeadLetterMapper deadLetterMapper) {
        this.objectMapper = objectMapper;
        this.deadLetterMapper = deadLetterMapper;
    }

    /**
     * 消费消息死信队列
     */
    @KafkaListener(
            topics = KafkaTopicConstants.MESSAGE_DLQ,
            groupId = KafkaTopicConstants.ConsumerGroup.DLQ_PROCESSOR,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMessageDLQ(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[DeadLetterQueueConsumer] 接收到消息死信队列消息 - Partition: {}, Offset: {}", partition, offset);
        processDeadLetterMessage(message, "MESSAGE");
    }

    /**
     * 消费通知死信队列
     */
    @KafkaListener(
            topics = KafkaTopicConstants.NOTIFICATION_DLQ,
            groupId = KafkaTopicConstants.ConsumerGroup.DLQ_PROCESSOR,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotificationDLQ(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[DeadLetterQueueConsumer] 接收到通知死信队列消息 - Partition: {}, Offset: {}", partition, offset);
        processDeadLetterMessage(message, "NOTIFICATION");
    }

    /**
     * 处理死信消息
     *
     * @param message 死信消息JSON字符串
     * @param type    消息类型
     */
    private void processDeadLetterMessage(String message, String type) {
        try {
            // 1. 反序列化死信消息
            DeadLetterMessage dlqMessage = objectMapper.readValue(message, DeadLetterMessage.class);
            @SuppressWarnings("unchecked")
            DeadLetterMessage<UnifiedMessage> typedDlqMessage = (DeadLetterMessage<UnifiedMessage>) dlqMessage;
            UnifiedMessage originalMessage = typedDlqMessage.getOriginalMessage();

            log.info("[DeadLetterQueueConsumer] 处理死信消息 - Type: {}, MessageId: {}, Reason: {}",
                    type, originalMessage.getMessageId(), dlqMessage.getFailureReason());

            // 2. 构建数据库实体
            MessageDeadLetter deadLetter = buildDeadLetterEntity(dlqMessage);

            // 3. 保存到数据库
            deadLetterMapper.insert(deadLetter);

            log.info("[DeadLetterQueueConsumer] 死信消息已保存到数据库 - MessageId: {}, DBId: {}",
                    originalMessage.getMessageId(), deadLetter.getId());

        } catch (Exception e) {
            log.error("[DeadLetterQueueConsumer] 处理死信消息失败 - Type: {}, Error: {}",
                    type, e.getMessage(), e);

            // 注意：这里不能再发送到死信队列，避免无限循环
            // 可以考虑发送告警或者记录到日志文件
        }
    }

    /**
     * 构建死信队列数据库实体
     *
     * @param dlqMessage 死信消息
     * @return 数据库实体
     */
    private MessageDeadLetter buildDeadLetterEntity(DeadLetterMessage dlqMessage) {
        try {
            @SuppressWarnings("unchecked")
            DeadLetterMessage<UnifiedMessage> typedDlqMessage = (DeadLetterMessage<UnifiedMessage>) dlqMessage;
            UnifiedMessage originalMessage = typedDlqMessage.getOriginalMessage();

            MessageDeadLetter deadLetter = new MessageDeadLetter();

            // 基本信息
            deadLetter.setMessageId(originalMessage.getMessageId());
            deadLetter.setMessageCategory(originalMessage.getCategory().name());
            deadLetter.setMessageType(originalMessage.getMessageType());

            // 原始消息（序列化为JSON）
            String originalMessageJson = objectMapper.writeValueAsString(originalMessage);
            deadLetter.setOriginalMessage(originalMessageJson);

            // 失败信息
            deadLetter.setFailureReason(truncate(dlqMessage.getFailureReason(), 512));
            deadLetter.setFailureTime(LocalDateTime.now());
            deadLetter.setRetryCount(dlqMessage.getRetryCount() != null ? dlqMessage.getRetryCount() : 0);

            // 处理状态
            deadLetter.setProcessStatus(0); // 0-未处理
            deadLetter.setCreatedTime(LocalDateTime.now());
            deadLetter.setUpdatedTime(LocalDateTime.now());

            return deadLetter;

        } catch (Exception e) {
            log.error("[DeadLetterQueueConsumer] 构建死信实体失败 - Error: {}", e.getMessage(), e);
            throw new RuntimeException("构建死信实体失败", e);
        }
    }

    /**
     * 获取异常堆栈信息
     *
     * @param throwable 异常对象
     * @return 堆栈字符串
     */
    private String getStackTrace(Throwable throwable) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return truncate(sw.toString(), 2000); // 限制长度
        } catch (Exception e) {
            return "无法获取堆栈信息";
        }
    }

    /**
     * 截断字符串到指定长度
     *
     * @param str       原始字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
