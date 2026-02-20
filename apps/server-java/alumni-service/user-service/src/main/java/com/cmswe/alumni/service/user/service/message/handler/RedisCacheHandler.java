package com.cmswe.alumni.service.user.service.message.handler;

import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.handler.AbstractMessageHandler;
import com.cmswe.alumni.redis.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redis缓存处理器（责任链模式）
 *
 * <p>负责将消息相关的统计信息缓存到Redis
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Component
public class RedisCacheHandler extends AbstractMessageHandler<UnifiedMessage> {

    private final RedisCache redisCache;

    public RedisCacheHandler(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    @Override
    public String getHandlerName() {
        return "RedisCacheHandler";
    }

    @Override
    protected boolean doHandle(UnifiedMessage message) {
        try {
            // 1. 更新未读消息计数
            updateUnreadCount(message);

            // 2. 缓存最近消息（可选）
            cacheRecentMessage(message);

            log.debug("[RedisCacheHandler] 缓存更新成功 - MessageId: {}", message.getMessageId());
            return true;

        } catch (Exception e) {
            log.error("[RedisCacheHandler] 缓存更新失败 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新未读消息计数
     */
    private void updateUnreadCount(UnifiedMessage message) {
        try {
            // 根据消息类别更新不同的未读计数
            switch (message.getCategory()) {
                case P2P -> {
                    // 更新P2P未读消息计数
                    if (message.getToId() != null) {
                        String key = "unread:p2p:" + message.getToId();
                        redisCache.increment(key);
                        log.debug("[RedisCacheHandler] P2P未读计数+1 - UserId: {}", message.getToId());
                    }
                }
                case GROUP -> {
                    // 更新群聊未读消息计数
                    if (message.getToId() != null) {
                        String key = "unread:group:" + message.getToId();
                        redisCache.increment(key);
                        log.debug("[RedisCacheHandler] 群聊未读计数+1 - GroupId: {}", message.getToId());
                    }
                }
                case SYSTEM, ORGANIZATION, BUSINESS -> {
                    // 更新通知未读计数
                    if (message.getToId() != null && message.getToId() != 0) {
                        String key = "unread:notification:" + message.getToId();
                        redisCache.increment(key);
                        log.debug("[RedisCacheHandler] 通知未读计数+1 - UserId: {}", message.getToId());
                    } else if (message.getToIds() != null) {
                        for (Long userId : message.getToIds()) {
                            String key = "unread:notification:" + userId;
                            redisCache.increment(key);
                        }
                        log.debug("[RedisCacheHandler] 批量通知未读计数+1 - Count: {}", message.getToIds().size());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[RedisCacheHandler] 更新未读计数失败 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage());
        }
    }

    /**
     * 缓存最近消息（可选，用于快速查询最近的消息）
     */
    private void cacheRecentMessage(UnifiedMessage message) {
        try {
            // 根据需要缓存最近的消息列表
            // 例如：缓存用户最近的10条消息
            // TODO: 根据业务需求实现
            log.debug("[RedisCacheHandler] 最近消息缓存 - MessageId: {}", message.getMessageId());
        } catch (Exception e) {
            log.error("[RedisCacheHandler] 缓存最近消息失败 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage());
        }
    }
}
