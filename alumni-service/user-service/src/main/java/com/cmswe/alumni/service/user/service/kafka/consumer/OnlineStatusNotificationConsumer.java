package com.cmswe.alumni.service.user.service.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.UserOnlineStatusKaf;
import com.cmswe.alumni.service.system.service.impl.WebSocketService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 在线状态通知消费者
 *
 * 职责：
 * 1. 用户上线时，通过 WebSocket 广播通知给好友
 * 2. 用户下线时，通知好友用户已离线
 *
 * Consumer Group: online-status-notification-group
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class OnlineStatusNotificationConsumer {

    @Resource
    private WebSocketService webSocketService;

    /**
     * 消费用户在线状态事件 - 发送 WebSocket 通知
     *
     * @param message 消息体（JSON 格式的 UserOnlineStatusKaf）
     * @param partition 分区号
     * @param offset 偏移量
     */
    @KafkaListener(
            topics = KafkaTopicConstants.USER_ONLINE_STATUS_TOPIC,
            groupId = "online-status-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void sendOnlineNotification(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            log.info("[Notification Consumer] 收到在线状态事件 - Partition: {}, Offset: {}, Message: {}",
                    partition, offset, message);

            // 1. 反序列化消息
            UserOnlineStatusKaf event = parseMessage(message);
            if (event == null) {
                log.warn("[Notification Consumer] 消息解析失败，跳过处理 - Partition: {}, Offset: {}",
                        partition, offset);
                return;
            }

            Long userId = event.getWxId();
            UserOnlineStatusKaf.StatusAction action = event.getAction();

            // 2. 根据动作类型发送不同的通知
            switch (action) {
                case ONLINE:
                    sendUserOnlineNotification(userId, event);
                    break;
                case OFFLINE:
                    sendUserOfflineNotification(userId, event);
                    break;
                case HEARTBEAT:
                    // 心跳事件不需要通知
                    log.debug("[Notification Consumer] 心跳事件，跳过通知 - UserId: {}", userId);
                    break;
                default:
                    log.warn("[Notification Consumer] 未知动作类型: {} - UserId: {}", action, userId);
            }

            log.info("[Notification Consumer] 通知发送成功 - UserId: {}, Action: {}, Partition: {}, Offset: {}",
                    userId, action, partition, offset);

        } catch (Exception e) {
            log.error("[Notification Consumer] 处理失败 - Partition: {}, Offset: {}, Message: {}, Error: {}",
                    partition, offset, message, e.getMessage(), e);

            // 注意：这里不抛异常，避免消息重复消费
        }
    }

    /**
     * 发送用户上线通知
     *
     * @param userId 用户ID
     * @param event 事件对象
     */
    private void sendUserOnlineNotification(Long userId, UserOnlineStatusKaf event) {
        try {
            // 构建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "user_online");
            notification.put("userId", userId);
            notification.put("timestamp", event.getTimestamp());
            notification.put("serverId", event.getServerId());
            notification.put("message", "用户已上线");

            // TODO: 这里可以根据好友关系，只通知好友
            // List<Long> friendIds = friendService.getFriendIds(userId);
            // for (Long friendId : friendIds) {
            //     webSocketService.sendNotifyToUser(notification, String.valueOf(friendId));
            // }

            // 暂时发送给用户本人（可以用于多端同步）
            webSocketService.sendNotifyToUser(notification, String.valueOf(userId));

            log.info("[Notification Consumer] 用户上线通知已发送 - UserId: {}, ServerId: {}",
                    userId, event.getServerId());

        } catch (Exception e) {
            log.error("[Notification Consumer] 发送上线通知失败 - UserId: {}, Error: {}",
                    userId, e.getMessage(), e);
        }
    }

    /**
     * 发送用户下线通知
     *
     * @param userId 用户ID
     * @param event 事件对象
     */
    private void sendUserOfflineNotification(Long userId, UserOnlineStatusKaf event) {
        try {
            // 构建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "user_offline");
            notification.put("userId", userId);
            notification.put("timestamp", event.getTimestamp());
            notification.put("serverId", event.getServerId());
            notification.put("message", "用户已下线");

            // TODO: 这里可以根据好友关系，只通知好友
            // List<Long> friendIds = friendService.getFriendIds(userId);
            // for (Long friendId : friendIds) {
            //     webSocketService.sendNotifyToUser(notification, String.valueOf(friendId));
            // }

            // 暂时发送给用户本人（可以用于多端同步）
            webSocketService.sendNotifyToUser(notification, String.valueOf(userId));

            log.info("[Notification Consumer] 用户下线通知已发送 - UserId: {}, ServerId: {}",
                    userId, event.getServerId());

        } catch (Exception e) {
            log.error("[Notification Consumer] 发送下线通知失败 - UserId: {}, Error: {}",
                    userId, e.getMessage(), e);
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

                log.warn("[Notification Consumer] 使用简单格式兼容模式 - UserId: {}", userId);
                return event;
            } catch (Exception e2) {
                log.error("[Notification Consumer] 消息解析失败 - Message: {}, Error: {}",
                        message, e2.getMessage());
                return null;
            }
        }
    }
}