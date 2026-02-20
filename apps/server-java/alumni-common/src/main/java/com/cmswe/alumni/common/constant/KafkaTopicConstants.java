package com.cmswe.alumni.common.constant;

/**
 * Kafka Topic 常量类（企业级标准）
 *
 * 命名规范：
 * - 使用点号（.）分隔，不使用连字符（-）
 * - 全部小写
 * - 格式：{domain}.{entity}.{event-type}
 * - 避免 -topic 后缀
 *
 * @author CMSWE
 * @since 2025-12-05
 */
public final class KafkaTopicConstants {

    // ==================== 用户域 Topics ====================

    /**
     * 用户通知 Topic（保留兼容性）
     */
    public static final String USER_NOTIFICATION_TOPIC = "user.notification";

    /**
     * 用户关注事件 Topic
     */
    public static final String USER_FOLLOW_EVENT_TOPIC = "user.follow.event";

    /**
     * 用户在线状态 Topic
     */
    public static final String USER_ONLINE_STATUS_TOPIC = "user.online.status";

    /**
     * 用户资料更新 Topic
     */
    public static final String USER_PROFILE_UPDATE_TOPIC = "user.profile.update";

    // ==================== 消息域 Topics（新增企业级标准）====================

    /**
     * P2P消息 Topic - 用户之间的点对点聊天
     */
    public static final String USER_MESSAGE_P2P = "user.message.p2p";

    /**
     * 群聊消息 Topic - 群组聊天消息
     */
    public static final String GROUP_MESSAGE_CHAT = "group.message.chat";

    /**
     * 系统通知 Topic - 系统级通知
     */
    public static final String SYSTEM_NOTIFICATION = "system.notification";

    /**
     * 组织通知 Topic - 组织发布的通知
     */
    public static final String ORGANIZATION_NOTIFICATION = "organization.notification";

    /**
     * 业务通知 Topic - 业务相关通知（关注、点赞、评论等）
     */
    public static final String BUSINESS_NOTIFICATION = "business.notification";

    // ==================== 聊天域 Topics ====================

    /**
     * 聊天消息 Topic（保留兼容性）
     */
    public static final String CHAT_MESSAGE_TOPIC = "chat.message";

    /**
     * 群组事件 Topic
     */
    public static final String CHAT_GROUP_EVENT_TOPIC = "chat.group.event";

    // ==================== 系统域 Topics ====================

    /**
     * 系统通知 Topic（保留兼容性）
     */
    public static final String SYSTEM_NOTIFICATION_TOPIC = "system.notification";

    /**
     * 系统审计日志 Topic
     */
    public static final String SYSTEM_AUDIT_LOG_TOPIC = "system.audit.log";

    // ==================== 死信队列 Topics（企业级标准）====================

    /**
     * 消息死信队列 Topic
     */
    public static final String MESSAGE_DLQ = "message.dlq";

    /**
     * 通知死信队列 Topic
     */
    public static final String NOTIFICATION_DLQ = "notification.dlq";

    // ==================== 数据同步 Topics（Canal + Kafka 架构）====================

    /**
     * 校友数据变更 Topic - 用于 MySQL → ES 数据同步
     * <p>监听表：wx_users, wx_user_info, alumni_education
     */
    public static final String DATA_SYNC_ALUMNI = "data.sync.alumni";

    /**
     * 校友会数据变更 Topic - 用于 MySQL → ES 数据同步
     * <p>监听表：alumni_association
     */
    public static final String DATA_SYNC_ASSOCIATION = "data.sync.association";

    /**
     * 商户数据变更 Topic - 用于 MySQL → ES 数据同步
     * <p>监听表：merchant
     */
    public static final String DATA_SYNC_MERCHANT = "data.sync.merchant";

    // ==================== Topic 配置常量 ====================

    /**
     * Topic 配置信息
     */
    public static final class Config {
        /**
         * 默认分区数
         */
        public static final int DEFAULT_PARTITIONS = 3;

        /**
         * 默认副本数
         */
        public static final int DEFAULT_REPLICAS = 1;

        /**
         * 高优先级 Topic 分区数（需要更高并发）
         */
        public static final int HIGH_PRIORITY_PARTITIONS = 6;

        /**
         * 消息保留时间 - 7天（毫秒）
         */
        public static final String RETENTION_7_DAYS = "604800000";

        /**
         * 消息保留时间 - 30天（毫秒）
         */
        public static final String RETENTION_30_DAYS = "2592000000";

        private Config() {
            throw new IllegalStateException("Config class");
        }
    }

    // ==================== Consumer Group 常量 ====================

    /**
     * Consumer Group ID 常量
     */
    public static final class ConsumerGroup {
        /**
         * 默认消费者组
         */
        public static final String DEFAULT_GROUP = "alumni-group";

        /**
         * Redis 状态更新消费者组
         */
        public static final String ONLINE_STATUS_REDIS = "online-status-redis-group";

        /**
         * 通知推送消费者组
         */
        public static final String ONLINE_STATUS_NOTIFICATION = "online-status-notification-group";

        /**
         * 离线消息推送消费者组
         */
        public static final String ONLINE_STATUS_OFFLINE_MSG = "online-status-offline-msg-group";

        /**
         * 统计更新消费者组
         */
        public static final String ONLINE_STATUS_STATISTICS = "online-status-statistics-group";

        /**
         * 消息处理消费者组（企业级标准）
         */
        public static final String MESSAGE_PROCESSOR = "message-processor-group";

        /**
         * 通知处理消费者组（企业级标准）
         */
        public static final String NOTIFICATION_PROCESSOR = "notification-processor-group";

        /**
         * 死信队列处理消费者组（企业级标准）
         */
        public static final String DLQ_PROCESSOR = "dlq-processor-group";

        /**
         * 数据同步消费者组（Canal + Kafka 架构 - 已废弃，改用发布订阅模式）
         * @deprecated 使用独立的消费者组：ES_SYNC, CACHE_CLEAR, NOTIFICATION_SYNC
         */
        @Deprecated
        public static final String DATA_SYNC = "data-sync-group";

        /**
         * Elasticsearch 同步消费者组（发布订阅模式）
         * <p>订阅：data.sync.alumni, data.sync.association, data.sync.merchant
         * <p>职责：将数据变更同步到 ES 索引
         */
        public static final String ES_SYNC = "es-sync-group";

        /**
         * 缓存清除消费者组（发布订阅模式）
         * <p>订阅：data.sync.alumni, data.sync.association, data.sync.merchant
         * <p>职责：清除本地和 Redis 缓存
         */
        public static final String CACHE_CLEAR = "cache-clear-group";

        /**
         * 通知推送消费者组（发布订阅模式）
         * <p>订阅：data.sync.alumni, data.sync.association, data.sync.merchant
         * <p>职责：数据变更通知推送（未来扩展）
         */
        public static final String NOTIFICATION_SYNC = "notification-sync-group";

        private ConsumerGroup() {
            throw new IllegalStateException("ConsumerGroup class");
        }
    }

    private KafkaTopicConstants() {
        throw new IllegalStateException("Constant class");
    }
}
