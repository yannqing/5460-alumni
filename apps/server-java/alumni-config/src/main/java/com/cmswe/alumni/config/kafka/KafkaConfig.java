package com.cmswe.alumni.config.kafka;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka配置类
 * 配置生产者、消费者和Topic
 *
 * @author CMSWE
 * @since 2025-12-05
 */
@Slf4j
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:alumni-group}")
    private String consumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    /**
     * 生产者配置
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate用于发送消息
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * String类型的生产者工厂（用于Canal等场景）
     */
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * String类型的KafkaTemplate（用于Canal等场景）
     */
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    /**
     * 消费者配置
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka监听容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 并发数
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    // ==================== Topic 自动创建配置（Canal + Kafka 数据同步）====================

    /**
     * 校友数据同步 Topic
     * <p>监听表：wx_users, wx_user_info, alumni_education
     * <p>消费者组：es-sync-group, cache-clear-group
     */
    @Bean
    @ConditionalOnProperty(name = "canal.enabled", havingValue = "true")
    public NewTopic dataSyncAlumniTopic() {
        return TopicBuilder.name(KafkaTopicConstants.DATA_SYNC_ALUMNI)
                .partitions(KafkaTopicConstants.Config.DEFAULT_PARTITIONS)  // 3个分区，支持并行消费
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)      // 1个副本（生产环境建议≥2）
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_7_DAYS)  // 保留7天
                .config("compression.type", "lz4")  // 压缩类型
                .build();
    }

    /**
     * 校友会数据同步 Topic
     * <p>监听表：alumni_association
     * <p>消费者组：es-sync-group, cache-clear-group
     */
    @Bean
    @ConditionalOnProperty(name = "canal.enabled", havingValue = "true")
    public NewTopic dataSyncAssociationTopic() {
        return TopicBuilder.name(KafkaTopicConstants.DATA_SYNC_ASSOCIATION)
                .partitions(KafkaTopicConstants.Config.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_7_DAYS)
                .config("compression.type", "lz4")
                .build();
    }

    /**
     * 商户数据同步 Topic
     * <p>监听表：merchant
     * <p>消费者组：es-sync-group, cache-clear-group
     */
    @Bean
    @ConditionalOnProperty(name = "canal.enabled", havingValue = "true")
    public NewTopic dataSyncMerchantTopic() {
        return TopicBuilder.name(KafkaTopicConstants.DATA_SYNC_MERCHANT)
                .partitions(KafkaTopicConstants.Config.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_7_DAYS)
                .config("compression.type", "lz4")
                .build();
    }
}
