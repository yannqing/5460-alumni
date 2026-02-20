package com.cmswe.alumni.service.user.service.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.UserOnlineStatusKaf;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.service.system.service.impl.WebSocketService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 在线状态离线消息消费者
 * <p>
 * 职责：
 * 1. 用户上线时，检查并推送离线消息
 * 2. 推送完成后清除离线消息队列
 * <p>
 * Consumer Group: online-status-offline-msg-group
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class OnlineStatusOfflineMessageConsumer {

    @Resource
    private RedisCache redisCache;

    @Resource
    private WebSocketService webSocketService;

    /**
     * 消费用户在线状态事件 - 推送离线消息
     *
     * @param message 消息体（JSON 格式的 UserOnlineStatusKaf）
     * @param partition 分区号
     * @param offset 偏移量
     */
    @KafkaListener(
            topics = KafkaTopicConstants.USER_ONLINE_STATUS_TOPIC,
            groupId = "online-status-offline-msg-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void pushOfflineMessages(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            log.info("[OfflineMsg Consumer] 收到在线状态事件 - Partition: {}, Offset: {}, Message: {}",
                    partition, offset, message);

            // 1. 反序列化消息
            UserOnlineStatusKaf event = parseMessage(message);
            if (event == null) {
                log.warn("[OfflineMsg Consumer] 消息解析失败，跳过处理 - Partition: {}, Offset: {}",
                        partition, offset);
                return;
            }

            Long userId = event.getWxId();
            UserOnlineStatusKaf.StatusAction action = event.getAction();

            // 2. 只在用户上线时推送离线消息（心跳和下线不处理）
            if (action == UserOnlineStatusKaf.StatusAction.ONLINE) {
                pushOfflineMessagesToUser(userId, event);
            } else {
                log.debug("[OfflineMsg Consumer] 非上线事件，跳过处理 - UserId: {}, Action: {}",
                        userId, action);
            }

            log.info("[OfflineMsg Consumer] 离线消息处理完成 - UserId: {}, Action: {}, Partition: {}, Offset: {}",
                    userId, action, partition, offset);

        } catch (Exception e) {
            log.error("[OfflineMsg Consumer] 处理失败 - Partition: {}, Offset: {}, Message: {}, Error: {}",
                    partition, offset, message, e.getMessage(), e);

            // 注意：这里不抛异常，避免消息重复消费
        }
    }

    /**
     * 推送离线消息给用户
     *
     * @param userId 用户 ID
     * @param event 事件对象
     */
    private void pushOfflineMessagesToUser(Long userId, UserOnlineStatusKaf event) {
        try {
            // 1. 推送离线聊天消息
            int messageCount = pushOfflineChatMessages(userId);

            // 2. 推送离线通知
            int notificationCount = pushOfflineNotifications(userId);

            int totalCount = messageCount + notificationCount;

            if (totalCount > 0) {
                log.info("[OfflineMsg Consumer] 离线消息推送完成 - UserId: {}, Messages: {}, Notifications: {}, Total: {}",
                        userId, messageCount, notificationCount, totalCount);
            } else {
                log.debug("[OfflineMsg Consumer] 无离线消息 - UserId: {}", userId);
            }

        } catch (Exception e) {
            log.error("[OfflineMsg Consumer] 推送离线消息失败 - UserId: {}, Error: {}",
                    userId, e.getMessage(), e);
        }
    }

    /**
     * 推送离线聊天消息
     *
     * @param userId 用户ID
     * @return 推送的消息数量
     */
    private int pushOfflineChatMessages(Long userId) {
        try {
            // 1. 从 Redis 获取并清除离线消息
            List<Object> offlineMessages = redisCache.getAndClearOfflineMessages(userId);

            if (offlineMessages == null || offlineMessages.isEmpty()) {
                log.debug("[OfflineMsg Consumer] 无离线聊天消息 - UserId: {}", userId);
                return 0;
            }

            log.info("[OfflineMsg Consumer] 检测到离线聊天消息 - UserId: {}, Count: {}",
                    userId, offlineMessages.size());

            // 2. 推送每条离线消息
            for (Object msg : offlineMessages) {
                try {
                    webSocketService.sendMsgToUser(msg, String.valueOf(userId));
                } catch (Exception e) {
                    log.error("[OfflineMsg Consumer] 推送单条离线消息失败 - UserId: {}, Error: {}",
                            userId, e.getMessage());
                }
            }

            log.info("[OfflineMsg Consumer] 离线聊天消息推送完成 - UserId: {}, Count: {}",
                    userId, offlineMessages.size());

            return offlineMessages.size();

        } catch (Exception e) {
            log.error("[OfflineMsg Consumer] 推送离线聊天消息失败 - UserId: {}, Error: {}",
                    userId, e.getMessage());
            return 0;
        }
    }

    /**
     * 推送离线通知
     *
     * @param userId 用户 ID
     * @return 推送的通知数量
     */
    private int pushOfflineNotifications(Long userId) {
        try {
            // 1. 从 Redis 获取并清除离线通知
            List<Object> offlineNotifications = redisCache.getAndClearOfflineNotifications(userId);

            if (offlineNotifications == null || offlineNotifications.isEmpty()) {
                log.debug("[OfflineMsg Consumer] 无离线通知 - UserId: {}", userId);
                return 0;
            }

            log.info("[OfflineMsg Consumer] 检测到离线通知 - UserId: {}, Count: {}",
                    userId, offlineNotifications.size());

            // 2. 推送每条离线通知
            for (Object notification : offlineNotifications) {
                try {
                    webSocketService.sendNotifyToUser(notification, String.valueOf(userId));
                } catch (Exception e) {
                    log.error("[OfflineMsg Consumer] 推送单条离线通知失败 - UserId: {}, Error: {}",
                            userId, e.getMessage());
                }
            }

            log.info("[OfflineMsg Consumer] 离线通知推送完成 - UserId: {}, Count: {}",
                    userId, offlineNotifications.size());

            return offlineNotifications.size();

        } catch (Exception e) {
            log.error("[OfflineMsg Consumer] 推送离线通知失败 - UserId: {}, Error: {}",
                    userId, e.getMessage());
            return 0;
        }
    }

    /**
     * 解析消息
     *
     * @param message 消息字符串
     * @return 解析后的事件对象
     */
    private UserOnlineStatusKaf parseMessage(String message) {
        try {
            return JSON.parseObject(message, UserOnlineStatusKaf.class);
        } catch (Exception e1) {
            try {
                // 兼容旧格式：只有 userId
                Long userId = JSON.parseObject(message, Long.class);
                UserOnlineStatusKaf event = new UserOnlineStatusKaf();
                event.setWxId(userId);
                event.setAction(UserOnlineStatusKaf.StatusAction.ONLINE);
                event.setTimestamp(System.currentTimeMillis());

                log.warn("[OfflineMsg Consumer] 使用简单格式兼容模式 - UserId: {}", userId);
                return event;
            } catch (Exception e2) {
                log.error("[OfflineMsg Consumer] 消息解析失败 - Message: {}, Error: {}",
                        message, e2.getMessage());
                return null;
            }
        }
    }
}