package com.cmswe.alumni.kafka.reliability;

import com.cmswe.alumni.kafka.utils.KafkaUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 死信队列服务（企业级通用标准）
 *
 * <p>功能：
 * <ul>
 *   <li>处理失败的消息发送到死信队列</li>
 *   <li>支持后续人工介入处理</li>
 *   <li>记录详细的失败原因</li>
 *   <li>适用于所有 Kafka 消息场景</li>
 * </ul>
 *
 * <p>使用场景：
 * <ul>
 *   <li>消息消费失败</li>
 *   <li>消息验证失败</li>
 *   <li>消息处理超时</li>
 *   <li>消息重试次数超限</li>
 * </ul>
 *
 * <p>死信队列命名规范：
 * <ul>
 *   <li>原 Topic + ".dlq" 后缀</li>
 *   <li>例如：user.message.p2p.dlq</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
public class DeadLetterQueueService {

    private final KafkaUtils kafkaUtils;

    /**
     * 死信队列 Topic 后缀
     */
    private static final String DLQ_SUFFIX = ".dlq";

    public DeadLetterQueueService(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }

    /**
     * 发送消息到死信队列
     *
     * <p>自动根据原 Topic 生成死信队列 Topic
     *
     * @param originalTopic 原始 Topic
     * @param messageId     消息 ID
     * @param originalMessage 原始消息
     * @param failureReason 失败原因
     * @param <T>           消息类型
     */
    public <T> void sendToDeadLetterQueue(String originalTopic, String messageId,
                                           T originalMessage, String failureReason) {
        sendToDeadLetterQueue(originalTopic, messageId, originalMessage, failureReason, 0);
    }

    /**
     * 发送消息到死信队列（包含重试次数）
     *
     * @param originalTopic   原始 Topic
     * @param messageId       消息 ID
     * @param originalMessage 原始消息
     * @param failureReason   失败原因
     * @param retryCount      重试次数
     * @param <T>             消息类型
     */
    public <T> void sendToDeadLetterQueue(String originalTopic, String messageId,
                                           T originalMessage, String failureReason,
                                           Integer retryCount) {
        try {
            // 构建死信消息
            DeadLetterMessage<T> dlqMessage = DeadLetterMessage.<T>builder()
                    .originalTopic(originalTopic)
                    .messageId(messageId)
                    .originalMessage(originalMessage)
                    .failureReason(failureReason)
                    .failureTime(System.currentTimeMillis())
                    .retryCount(retryCount != null ? retryCount : 0)
                    .build();

            // 生成死信队列 Topic
            String dlqTopic = generateDLQTopic(originalTopic);

            // 发送到死信队列
            kafkaUtils.sendAsync(dlqTopic, messageId, dlqMessage);

            log.warn("[DeadLetterQueue] 消息已发送到死信队列 - OriginalTopic: {}, DLQTopic: {}, MessageId: {}, Reason: {}",
                    originalTopic, dlqTopic, messageId, failureReason);

        } catch (Exception e) {
            log.error("[DeadLetterQueue] 发送消息到死信队列失败 - OriginalTopic: {}, MessageId: {}, Error: {}",
                    originalTopic, messageId, e.getMessage(), e);
        }
    }

    /**
     * 发送消息到指定的死信队列 Topic
     *
     * @param dlqTopic        死信队列 Topic
     * @param originalTopic   原始 Topic
     * @param messageId       消息 ID
     * @param originalMessage 原始消息
     * @param failureReason   失败原因
     * @param retryCount      重试次数
     * @param <T>             消息类型
     */
    public <T> void sendToSpecificDLQ(String dlqTopic, String originalTopic, String messageId,
                                       T originalMessage, String failureReason,
                                       Integer retryCount) {
        try {
            // 构建死信消息
            DeadLetterMessage<T> dlqMessage = DeadLetterMessage.<T>builder()
                    .originalTopic(originalTopic)
                    .messageId(messageId)
                    .originalMessage(originalMessage)
                    .failureReason(failureReason)
                    .failureTime(System.currentTimeMillis())
                    .retryCount(retryCount != null ? retryCount : 0)
                    .build();

            // 发送到指定的死信队列
            kafkaUtils.sendAsync(dlqTopic, messageId, dlqMessage);

            log.warn("[DeadLetterQueue] 消息已发送到指定死信队列 - OriginalTopic: {}, DLQTopic: {}, MessageId: {}, Reason: {}",
                    originalTopic, dlqTopic, messageId, failureReason);

        } catch (Exception e) {
            log.error("[DeadLetterQueue] 发送消息到指定死信队列失败 - DLQTopic: {}, MessageId: {}, Error: {}",
                    dlqTopic, messageId, e.getMessage(), e);
        }
    }

    /**
     * 生成死信队列 Topic 名称
     *
     * <p>规则：原 Topic + ".dlq" 后缀
     *
     * @param originalTopic 原始 Topic
     * @return 死信队列 Topic
     */
    private String generateDLQTopic(String originalTopic) {
        if (originalTopic == null || originalTopic.isEmpty()) {
            return "unknown" + DLQ_SUFFIX;
        }

        // 如果已经是死信队列 Topic，则不再添加后缀
        if (originalTopic.endsWith(DLQ_SUFFIX)) {
            return originalTopic;
        }

        return originalTopic + DLQ_SUFFIX;
    }

    /**
     * 死信消息模型（通用）
     *
     * @param <T> 原始消息类型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeadLetterMessage<T> {
        /**
         * 原始 Topic
         */
        private String originalTopic;

        /**
         * 消息 ID
         */
        private String messageId;

        /**
         * 原始消息
         */
        private T originalMessage;

        /**
         * 失败原因
         */
        private String failureReason;

        /**
         * 失败时间（时间戳）
         */
        private Long failureTime;

        /**
         * 重试次数
         */
        private Integer retryCount;
    }
}
