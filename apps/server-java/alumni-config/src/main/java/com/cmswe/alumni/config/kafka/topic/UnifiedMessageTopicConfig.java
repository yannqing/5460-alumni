package com.cmswe.alumni.config.kafka.topic;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * 统一消息系统Topic配置（企业级标准）
 *
 * <p>自动创建所需的Kafka Topics
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Configuration
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class UnifiedMessageTopicConfig {

    // ==================== 消息类Topic ====================

    /**
     * P2P消息Topic
     */
    @Bean
    public NewTopic userMessageP2PTopic() {
        return TopicBuilder
                .name(KafkaTopicConstants.USER_MESSAGE_P2P)
                .partitions(KafkaTopicConstants.Config.HIGH_PRIORITY_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_7_DAYS)
                .build();
    }

    /**
     * 群聊消息Topic
     */
    @Bean
    public NewTopic groupMessageChatTopic() {
        return TopicBuilder
                .name(KafkaTopicConstants.GROUP_MESSAGE_CHAT)
                .partitions(KafkaTopicConstants.Config.HIGH_PRIORITY_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_7_DAYS)
                .build();
    }

    // ==================== 通知类Topic ====================

    /**
     * 系统通知Topic
     */
    @Bean
    public NewTopic systemNotificationTopic() {
        return TopicBuilder
                .name(KafkaTopicConstants.SYSTEM_NOTIFICATION)
                .partitions(KafkaTopicConstants.Config.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_30_DAYS)
                .build();
    }

    /**
     * 组织通知Topic
     */
    @Bean
    public NewTopic organizationNotificationTopic() {
        return TopicBuilder
                .name(KafkaTopicConstants.ORGANIZATION_NOTIFICATION)
                .partitions(KafkaTopicConstants.Config.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_30_DAYS)
                .build();
    }

    /**
     * 业务通知Topic
     */
    @Bean
    public NewTopic businessNotificationTopic() {
        return TopicBuilder
                .name(KafkaTopicConstants.BUSINESS_NOTIFICATION)
                .partitions(KafkaTopicConstants.Config.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_30_DAYS)
                .build();
    }

    // ==================== 旧系统兼容Topic ====================

    /**
     * 用户在线状态Topic（保留兼容旧系统）
     */
    @Bean
    public NewTopic userOnlineStatusTopic() {
        return TopicBuilder
                .name(KafkaTopicConstants.USER_ONLINE_STATUS_TOPIC)
                .partitions(KafkaTopicConstants.Config.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_7_DAYS)
                .build();
    }

    // ==================== 死信队列Topic ====================

    /**
     * 消息死信队列Topic
     */
    @Bean
    public NewTopic messageDLQTopic() {
        return TopicBuilder
                .name(KafkaTopicConstants.MESSAGE_DLQ)
                .partitions(KafkaTopicConstants.Config.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_30_DAYS)
                .build();
    }

    /**
     * 通知死信队列Topic
     */
    @Bean
    public NewTopic notificationDLQTopic() {
        return TopicBuilder
                .name(KafkaTopicConstants.NOTIFICATION_DLQ)
                .partitions(KafkaTopicConstants.Config.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.Config.DEFAULT_REPLICAS)
                .config("retention.ms", KafkaTopicConstants.Config.RETENTION_30_DAYS)
                .build();
    }
}
