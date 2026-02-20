package com.cmswe.alumni.search.consumer;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.DataChangeEvent;
import com.cmswe.alumni.kafka.reliability.DeadLetterQueueService;
import com.cmswe.alumni.kafka.reliability.MessageIdempotentService;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.search.service.sync.DataMergeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 缓存清除消费者（发布订阅模式）
 *
 * <p>架构设计：
 * <ul>
 *   <li>独立消费者组：cache-clear-group</li>
 *   <li>职责单一：只负责清除缓存（Caffeine L1 + Redis L2）</li>
 *   <li>高性能：缓存清除通常很快，单实例即可</li>
 *   <li>故障隔离：缓存清除失败不影响 ES 同步</li>
 * </ul>
 *
 * <p>数据流：
 * MySQL变更 → Canal → Kafka Topic → 本消费者 → 清除缓存
 *
 * <p>缓存策略：
 * <ul>
 *   <li>Caffeine（L1）：全部失效（invalidateAll）</li>
 *   <li>Redis（L2）：模糊匹配删除（search:alumni:*）</li>
 * </ul>
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "canal.enabled", havingValue = "true")
public class CacheClearConsumer {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private DataMergeService dataMergeService;

    @Resource
    private RedisCache redisCache;

    @Resource
    @Qualifier("searchResultCache")
    private Cache<String, Object> searchResultCache;

    @Resource
    private MessageIdempotentService messageIdempotentService;

    @Resource
    private DeadLetterQueueService deadLetterQueueService;

    /**
     * 消费校友数据变更事件，清除相关缓存
     *
     * <p>企业级可靠性保障：
     * <ul>
     *   <li>幂等性检查：虽然缓存清除本身是幂等的，但仍防止重复处理以节省资源</li>
     *   <li>死信队列：记录清除失败的消息，便于监控和排查问题</li>
     *   <li>异常隔离：异常不抛出，避免阻塞 Kafka 消费进度</li>
     *   <li>容错性：最坏情况下缓存不一致，但会在 TTL 后自动失效</li>
     * </ul>
     *
     * @param message   消息内容（JSON格式的 DataChangeEvent）
     * @param partition Kafka 分区
     * @param offset    消息偏移量
     */
    @KafkaListener(
            topics = KafkaTopicConstants.DATA_SYNC_ALUMNI,
            groupId = KafkaTopicConstants.ConsumerGroup.CACHE_CLEAR,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void clearCacheOnDataChange(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[缓存清除] 接收到数据变更事件 - Partition: {}, Offset: {}", partition, offset);

        DataChangeEvent event = null;
        String eventId = null;

        try {
            // 1. 反序列化事件
            event = objectMapper.readValue(message, DataChangeEvent.class);
            eventId = event.getEventId();

            // 2. 【幂等性检查】虽然缓存清除本身是幂等的，但避免重复处理以节省资源
            if (messageIdempotentService.isMessageProcessed(eventId)) {
                log.warn("[缓存清除] 检测到重复消息，跳过处理 - EventId: {}", eventId);
                return;
            }

            // 3. 验证事件（可选，缓存清除对数据完整性要求不高）
            if (!dataMergeService.validateEvent(event)) {
                log.warn("[缓存清除] 事件验证失败，依然执行缓存清除 - EventId: {}", eventId);
            }

            log.info("[缓存清除] 数据变更详情 - EventId: {}, Table: {}, EventType: {}",
                    eventId, event.getTable(), event.getEventType());

            long startTime = System.currentTimeMillis();

            // 4. 清除 Caffeine 本地缓存（L1）
            clearLocalCache();

            // 5. 清除 Redis 缓存（L2）
            clearRedisCache();

            // 6. 【标记消息为已处理】保证幂等性
            messageIdempotentService.markMessageAsProcessed(eventId);

            long endTime = System.currentTimeMillis();
            log.info("[缓存清除] 清除成功 - EventId: {}, Time: {}ms",
                    eventId, (endTime - startTime));

        } catch (Exception e) {
            log.error("[缓存清除] 清除异常 - Partition: {}, Offset: {}, EventId: {}",
                    partition, offset, eventId, e);

            // 7. 【发送到死信队列】虽然缓存清除失败影响不大，但发送到 DLQ 便于监控
            if (event != null && eventId != null) {
                try {
                    deadLetterQueueService.sendToDeadLetterQueue(
                            KafkaTopicConstants.DATA_SYNC_ALUMNI,
                            eventId,
                            event,
                            "缓存清除失败: " + e.getMessage()
                    );
                    log.warn("[缓存清除] 消息已发送到死信队列 - EventId: {}, DLQ Topic: {}",
                            eventId, KafkaTopicConstants.DATA_SYNC_ALUMNI + ".dlq");
                } catch (Exception dlqException) {
                    log.error("[缓存清除] 发送到死信队列失败 - EventId: {}", eventId, dlqException);
                }
            }

            // 缓存清除失败不抛出异常，不影响消费进度
            // 最坏情况：缓存不一致，但会在 TTL 后自动失效
        }
    }

    /**
     * 清除 Caffeine 本地缓存
     */
    private void clearLocalCache() {
        try {
            long beforeSize = searchResultCache.estimatedSize();
            searchResultCache.invalidateAll();
            log.debug("[缓存清除] Caffeine缓存已清除 - 清除前大小: {}", beforeSize);
        } catch (Exception e) {
            log.error("[缓存清除] Caffeine缓存清除失败", e);
        }
    }

    /**
     * 清除 Redis 缓存（模糊匹配）
     */
    private void clearRedisCache() {
        try {
            // 清除搜索结果缓存
            String searchPattern = "search:alumni:*";
            long deletedCount = redisCache.keys(searchPattern).stream()
                    .peek(redisCache::deleteObject)
                    .count();

            log.debug("[缓存清除] Redis缓存已清除 - Pattern: {}, 删除数量: {}", searchPattern, deletedCount);

        } catch (Exception e) {
            log.error("[缓存清除] Redis缓存清除失败", e);
        }
    }
}
