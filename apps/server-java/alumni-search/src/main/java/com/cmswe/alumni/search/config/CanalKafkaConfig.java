package com.cmswe.alumni.search.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Canal Kafka 消费者配置
 *
 * <p>专门用于消费 Canal Server 发送到 Kafka 的 Binlog 数据
 *
 * @author CNI Alumni System
 * @since 2025-03-18
 */
@Slf4j
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "canal.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class CanalKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${canal.kafka.group-id:alumni-search-canal-consumer}")
    private String groupId;

    @Value("${canal.kafka.auto-offset-reset:latest}")
    private String autoOffsetReset;

    @Value("${canal.kafka.max-poll-records:500}")
    private Integer maxPollRecords;

    @Value("${canal.kafka.session-timeout-ms:30000}")
    private Integer sessionTimeoutMs;

    @Value("${canal.kafka.heartbeat-interval-ms:10000}")
    private Integer heartbeatIntervalMs;

    /**
     * Canal Kafka 消费者工厂
     */
    @Bean
    public ConsumerFactory<String, String> canalConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Kafka 服务器地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // 消费者组 ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Key 反序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Value 反序列化器（Canal 发送的是 JSON 字符串）
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // 自动提交偏移量（设置为 false，使用手动 ACK）
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 自动偏移量重置策略
        // - earliest: 从最早的消息开始消费（历史数据）
        // - latest: 从最新的消息开始消费（只消费新数据）
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        // 单次拉取最大记录数
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);

        // 会话超时时间（消费者心跳超时时间）
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);

        // 心跳间隔时间
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatIntervalMs);

        // 最大拉取间隔时间（避免消费者被认为已死）
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 分钟

        log.info("Canal Kafka 消费者配置初始化 - BootstrapServers: {}, GroupId: {}, AutoOffsetReset: {}",
                bootstrapServers, groupId, autoOffsetReset);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Canal Kafka 监听器容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> canalKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(canalConsumerFactory());

        // 设置为手动 ACK 模式（保证消息不丢失）
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // 设置并发数（默认 3 个消费线程）
        factory.setConcurrency(3);

        // 设置批量消费（关闭，使用单条消费）
        factory.setBatchListener(false);

        // 设置错误处理器（使用 DefaultErrorHandler）
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
                (consumerRecord, exception) -> {
                    log.error("Canal Kafka 消费异常 - Topic: {}, Partition: {}, Offset: {}, Key: {}",
                            consumerRecord.topic(),
                            consumerRecord.partition(),
                            consumerRecord.offset(),
                            consumerRecord.key(),
                            exception);
                }
        ));

        log.info("Canal Kafka 监听器容器工厂初始化完成");

        return factory;
    }
}
