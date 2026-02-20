package com.cmswe.alumni.kafka.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka 工具类
 *
 * 提供统一的 Kafka 消息发送方法，包括：
 * - 异步发送（带日志回调）
 * - 同步发送
 * - 自定义超时的同步发送
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Component
public class KafkaUtils {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaUtils(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 异步发送消息（带日志回调）
     *
     * 推荐使用此方法，性能最佳，且自动记录日志
     *
     * @param topic 主题
     * @param key 消息键（用于分区路由，建议使用用户ID等）
     * @param message 消息体
     */
    public void sendAsync(String topic, String key, Object message) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Kafka 消息发送成功 - Topic: {}, Key: {}, Partition: {}, Offset: {}",
                            topic,
                            key,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Kafka 消息发送失败 - Topic: {}, Key: {}, Error: {}",
                            topic,
                            key,
                            ex.getMessage(),
                            ex);
                }
            });
        } catch (Exception e) {
            log.error("Kafka 消息发送异常 - Topic: {}, Key: {}, Error: {}",
                    topic,
                    key,
                    e.getMessage(),
                    e);
        }
    }

    /**
     * 异步发送消息（不需要 key）
     *
     * @param topic 主题
     * @param message 消息体
     */
    public void sendAsync(String topic, Object message) {
        sendAsync(topic, null, message);
    }

    /**
     * 同步发送消息（阻塞等待，默认超时 5 秒）
     *
     * 适用于关键业务场景，需要确保消息发送成功
     *
     * @param topic 主题
     * @param key 消息键
     * @param message 消息体
     * @return 发送结果
     * @throws RuntimeException 发送失败时抛出异常
     */
    public SendResult<String, Object> sendSync(String topic, String key, Object message) {
        return sendSync(topic, key, message, 5, TimeUnit.SECONDS);
    }

    /**
     * 同步发送消息（自定义超时时间）
     *
     * @param topic 主题
     * @param key 消息键
     * @param message 消息体
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 发送结果
     * @throws RuntimeException 发送失败时抛出异常
     */
    public SendResult<String, Object> sendSync(String topic, String key, Object message, long timeout, TimeUnit unit) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);
            SendResult<String, Object> result = future.get(timeout, unit);

            log.info("Kafka 消息同步发送成功 - Topic: {}, Key: {}, Partition: {}, Offset: {}",
                    topic,
                    key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Kafka 消息同步发送被中断 - Topic: {}, Key: {}", topic, key, e);
            throw new RuntimeException("Kafka 消息发送被中断", e);

        } catch (ExecutionException e) {
            log.error("Kafka 消息同步发送失败 - Topic: {}, Key: {}, Error: {}",
                    topic,
                    key,
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage(),
                    e);
            throw new RuntimeException("Kafka 消息发送失败", e.getCause() != null ? e.getCause() : e);

        } catch (TimeoutException e) {
            log.error("Kafka 消息同步发送超时 - Topic: {}, Key: {}, Timeout: {} {}",
                    topic,
                    key,
                    timeout,
                    unit,
                    e);
            throw new RuntimeException("Kafka 消息发送超时", e);
        }
    }

    /**
     * 同步发送消息（不需要 key）
     *
     * @param topic 主题
     * @param message 消息体
     * @return 发送结果
     */
    public SendResult<String, Object> sendSync(String topic, Object message) {
        return sendSync(topic, null, message);
    }

    /**
     * 发送消息到指定分区
     *
     * @param topic 主题
     * @param partition 分区号
     * @param key 消息键
     * @param message 消息体
     */
    public void sendToPartition(String topic, int partition, String key, Object message) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, partition, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Kafka 消息发送到指定分区成功 - Topic: {}, Partition: {}, Key: {}, Offset: {}",
                            topic,
                            partition,
                            key,
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Kafka 消息发送到指定分区失败 - Topic: {}, Partition: {}, Key: {}, Error: {}",
                            topic,
                            partition,
                            key,
                            ex.getMessage(),
                            ex);
                }
            });
        } catch (Exception e) {
            log.error("Kafka 消息发送到指定分区异常 - Topic: {}, Partition: {}, Key: {}, Error: {}",
                    topic,
                    partition,
                    key,
                    e.getMessage(),
                    e);
        }
    }
}
