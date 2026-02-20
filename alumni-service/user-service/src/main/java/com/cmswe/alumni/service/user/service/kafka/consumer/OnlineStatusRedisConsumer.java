package com.cmswe.alumni.service.user.service.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.UserOnlineStatusKaf;
import com.cmswe.alumni.redis.utils.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 在线状态 Redis 消费者
 *
 * 职责：
 * 1. 更新用户在线状态到 Redis（带过期时间）
 * 2. 维护在线用户集合（使用原子操作）
 *
 * Consumer Group: online-status-redis-group
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class OnlineStatusRedisConsumer {

    @Resource
    private RedisCache redisCache;

    /**
     * 在线状态过期时间（30分钟无心跳视为离线）
     */
    private static final int STATUS_EXPIRE_MINUTES = 30;

    /**
     * 消费用户在线状态事件 - 更新 Redis
     *
     * @param message 消息体（JSON 格式的 UserOnlineStatusKaf）
     * @param partition 分区号
     * @param offset 偏移量
     */
    @KafkaListener(
            topics = KafkaTopicConstants.USER_ONLINE_STATUS_TOPIC,
            groupId = "online-status-redis-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void updateRedisStatus(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            log.info("[Redis Consumer] 收到在线状态事件 - Partition: {}, Offset: {}, Message: {}",
                    partition, offset, message);

            // 1. 反序列化消息
            UserOnlineStatusKaf event = parseMessage(message);
            if (event == null) {
                log.warn("[Redis Consumer] 消息解析失败，跳过处理 - Partition: {}, Offset: {}",
                        partition, offset);
                return;
            }

            Long userId = event.getWxId();
            UserOnlineStatusKaf.StatusAction action = event.getAction();

            // 2. 根据动作类型处理
            switch (action) {
                case ONLINE:
                case HEARTBEAT:
                    handleOnlineOrHeartbeat(userId, event);
                    break;
                case OFFLINE:
                    handleOffline(userId, event);
                    break;
                default:
                    log.warn("[Redis Consumer] 未知动作类型: {} - UserId: {}", action, userId);
            }

            log.info("[Redis Consumer] Redis 状态更新成功 - UserId: {}, Action: {}, Partition: {}, Offset: {}",
                    userId, action, partition, offset);

        } catch (Exception e) {
            log.error("[Redis Consumer] 处理失败 - Partition: {}, Offset: {}, Message: {}, Error: {}",
                    partition, offset, message, e.getMessage(), e);

            // 注意：这里不抛异常，避免消息重复消费
            // 如果需要重试，应该发送到重试队列
        }
    }

    /**
     * 处理用户上线或心跳事件
     *
     * @param userId 用户ID
     * @param event 事件对象
     */
    private void handleOnlineOrHeartbeat(Long userId, UserOnlineStatusKaf event) {
        LocalDateTime now = LocalDateTime.now();

        try {
            // 1. 更新在线状态（带过期时间，自动续期）
            redisCache.setUserOnlineStatus(userId, now, STATUS_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 2. 添加到在线用户集合（原子操作，避免并发问题）
            redisCache.addUserToOnlineSet(userId);

            log.debug("[Redis Consumer] 用户在线状态已更新 - UserId: {}, Action: {}, Time: {}, TTL: {}min, ServerId: {}",
                    userId, event.getAction(), now, STATUS_EXPIRE_MINUTES, event.getServerId());

        } catch (Exception e) {
            log.error("[Redis Consumer] 更新 Redis 失败 - UserId: {}, Action: {}, Error: {}",
                    userId, event.getAction(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 处理用户下线事件
     *
     * @param userId 用户ID
     * @param event 事件对象
     */
    private void handleOffline(Long userId, UserOnlineStatusKaf event) {
        try {
            // 1. 删除在线状态
            boolean deleted = redisCache.deleteUserOnlineStatus(userId);

            // 2. 从在线用户集合移除（原子操作）
            Long removed = redisCache.removeUserFromOnlineSet(userId);

            log.info("[Redis Consumer] 用户已下线 - UserId: {}, StatusDeleted: {}, SetRemoved: {}, ServerId: {}",
                    userId, deleted, removed, event.getServerId());

        } catch (Exception e) {
            log.error("[Redis Consumer] 删除 Redis 状态失败 - UserId: {}, Error: {}",
                    userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 解析消息
     *
     * 支持两种格式：
     * 1. 完整格式：UserOnlineStatusKaf 对象
     * 2. 简单格式：只有 userId（兼容旧版本）
     *
     * @param message 消息字符串
     * @return 解析后的事件对象
     */
    private UserOnlineStatusKaf parseMessage(String message) {
        try {
            // 尝试解析为完整事件
            return JSON.parseObject(message, UserOnlineStatusKaf.class);
        } catch (Exception e1) {
            try {
                // 兼容旧格式：只有 userId
                Long userId = JSON.parseObject(message, Long.class);
                UserOnlineStatusKaf event = new UserOnlineStatusKaf();
                event.setWxId(userId);
                event.setAction(UserOnlineStatusKaf.StatusAction.ONLINE);
                event.setTimestamp(System.currentTimeMillis());

                log.warn("[Redis Consumer] 使用简单格式兼容模式 - UserId: {}", userId);
                return event;
            } catch (Exception e2) {
                log.error("[Redis Consumer] 消息解析失败 - Message: {}, Error: {}",
                        message, e2.getMessage());
                return null;
            }
        }
    }
}
