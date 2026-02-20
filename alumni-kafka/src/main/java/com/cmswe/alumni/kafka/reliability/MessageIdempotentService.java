package com.cmswe.alumni.kafka.reliability;

import com.cmswe.alumni.redis.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 消息幂等性服务（企业级通用标准）
 *
 * <p>功能：
 * <ul>
 *   <li>基于 Redis 实现消息去重</li>
 *   <li>防止消息重复消费</li>
 *   <li>支持自定义过期时间</li>
 *   <li>适用于所有 Kafka 消息场景</li>
 * </ul>
 *
 * <p>使用场景：
 * <ul>
 *   <li>Kafka 消息消费去重</li>
 *   <li>接口幂等性保证</li>
 *   <li>分布式锁实现</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
public class MessageIdempotentService {

    private final RedisCache redisCache;

    /**
     * 消息去重记录的 Key 前缀
     */
    private static final String MESSAGE_PROCESSED_KEY_PREFIX = "message:processed:";

    /**
     * 默认过期时间：1 小时
     */
    private static final int DEFAULT_EXPIRE_HOURS = 1;

    public MessageIdempotentService(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    /**
     * 检查消息是否已处理
     *
     * @param messageId 消息 ID
     * @return true-已处理；false-未处理
     */
    public boolean isMessageProcessed(String messageId) {
        String key = buildKey(messageId);
        boolean processed = redisCache.hasKey(key);

        if (processed) {
            log.warn("[MessageIdempotent] 检测到重复消息 - MessageId: {}", messageId);
        }

        return processed;
    }

    /**
     * 标记消息为已处理
     *
     * @param messageId 消息 ID
     * @return true-标记成功；false-标记失败（可能已存在）
     */
    public boolean markMessageAsProcessed(String messageId) {
        return markMessageAsProcessed(messageId, DEFAULT_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    /**
     * 标记消息为已处理（自定义过期时间）
     *
     * @param messageId  消息 ID
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     * @return true-标记成功；false-标记失败
     */
    public boolean markMessageAsProcessed(String messageId, long expireTime, TimeUnit timeUnit) {
        String key = buildKey(messageId);

        try {
            // 检查是否已存在
            if (redisCache.hasKey(key)) {
                log.warn("[MessageIdempotent] 消息已被标记为已处理 - MessageId: {}", messageId);
                return false;
            }

            // 标记为已处理（存储处理时间戳）
            redisCache.setCacheObject(key, System.currentTimeMillis(), (int) expireTime, timeUnit);

            log.debug("[MessageIdempotent] 消息标记为已处理 - MessageId: {}, Expire: {} {}",
                    messageId, expireTime, timeUnit);

            return true;

        } catch (Exception e) {
            log.error("[MessageIdempotent] 标记消息失败 - MessageId: {}, Error: {}",
                    messageId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除消息处理记录（用于重试等场景）
     *
     * @param messageId 消息 ID
     * @return true-删除成功；false-删除失败
     */
    public boolean removeProcessedMark(String messageId) {
        String key = buildKey(messageId);

        try {
            boolean removed = redisCache.deleteObject(key);
            log.debug("[MessageIdempotent] 删除消息处理标记 - MessageId: {}, Result: {}",
                    messageId, removed);
            return removed;

        } catch (Exception e) {
            log.error("[MessageIdempotent] 删除消息处理标记失败 - MessageId: {}, Error: {}",
                    messageId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取消息处理时间
     *
     * @param messageId 消息 ID
     * @return 处理时间戳（毫秒），如果未处理则返回 null
     */
    public Long getProcessedTime(String messageId) {
        String key = buildKey(messageId);
        return redisCache.getCacheObject(key);
    }

    /**
     * 构建 Redis Key
     *
     * @param messageId 消息 ID
     * @return Redis Key
     */
    private String buildKey(String messageId) {
        return MESSAGE_PROCESSED_KEY_PREFIX + messageId;
    }
}
