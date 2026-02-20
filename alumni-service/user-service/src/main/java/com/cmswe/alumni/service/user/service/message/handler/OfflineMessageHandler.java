package com.cmswe.alumni.service.user.service.message.handler;

import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.handler.AbstractMessageHandler;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.service.system.service.impl.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 离线消息处理器（责任链模式）
 *
 * <p>负责处理离线用户的消息存储
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Component
public class OfflineMessageHandler extends AbstractMessageHandler<UnifiedMessage> {

    private final RedisCache redisCache;
    private final WebSocketService webSocketService;

    /**
     * 离线消息过期时间（天）
     */
    private static final int OFFLINE_MESSAGE_EXPIRE_DAYS = 7;

    public OfflineMessageHandler(RedisCache redisCache, WebSocketService webSocketService) {
        this.redisCache = redisCache;
        this.webSocketService = webSocketService;
    }

    @Override
    public String getHandlerName() {
        return "OfflineMessageHandler";
    }

    @Override
    protected boolean doHandle(UnifiedMessage message) {
        // 检查是否需要离线存储
        if (!message.getNeedOfflineStore()) {
            log.debug("[OfflineMessageHandler] 消息不需要离线存储 - MessageId: {}", message.getMessageId());
            return true;
        }

        try {
            // 根据消息类别处理离线消息
            switch (message.getCategory()) {
                case P2P -> handleP2POfflineMessage(message);
                case GROUP -> handleGroupOfflineMessage(message);
                case SYSTEM, ORGANIZATION, BUSINESS -> handleNotificationOfflineMessage(message);
                default -> {
                    log.warn("[OfflineMessageHandler] 未知的消息类别 - Category: {}", message.getCategory());
                    return false;
                }
            }

            log.debug("[OfflineMessageHandler] 离线消息处理成功 - MessageId: {}", message.getMessageId());
            return true;

        } catch (Exception e) {
            log.error("[OfflineMessageHandler] 离线消息处理失败 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理P2P离线消息
     */
    private void handleP2POfflineMessage(UnifiedMessage message) {
        if (message.getToId() == null) {
            return;
        }

        // 检查用户是否在线
        boolean isOnline = isUserOnline(message.getToId());

        if (!isOnline) {
            // 用户离线，存储消息到Redis
            redisCache.addOfflineMessage(message.getToId(), message, OFFLINE_MESSAGE_EXPIRE_DAYS);
            log.info("[OfflineMessageHandler] P2P离线消息已存储 - ToUserId: {}, MessageId: {}",
                    message.getToId(), message.getMessageId());
        } else {
            log.debug("[OfflineMessageHandler] 用户在线，无需存储离线消息 - ToUserId: {}", message.getToId());
        }
    }

    /**
     * 处理群聊离线消息
     */
    private void handleGroupOfflineMessage(UnifiedMessage message) {
        // 群聊消息需要检查每个群成员的在线状态
        // TODO: 获取群成员列表并检查在线状态
        // 这里暂时不处理，因为群聊消息的离线处理比较复杂
        log.debug("[OfflineMessageHandler] 群聊离线消息处理 - GroupId: {}", message.getToId());
    }

    /**
     * 处理通知离线消息
     */
    private void handleNotificationOfflineMessage(UnifiedMessage message) {
        if (message.getToId() != null && message.getToId() != 0) {
            // 单个用户通知
            boolean isOnline = isUserOnline(message.getToId());
            if (!isOnline) {
                redisCache.addOfflineNotification(message.getToId(), message, OFFLINE_MESSAGE_EXPIRE_DAYS);
                log.info("[OfflineMessageHandler] 离线通知已存储 - ToUserId: {}, MessageId: {}",
                        message.getToId(), message.getMessageId());
            }

        } else if (message.getToIds() != null && !message.getToIds().isEmpty()) {
            // 批量用户通知
            int offlineCount = 0;
            for (Long userId : message.getToIds()) {
                boolean isOnline = isUserOnline(userId);
                if (!isOnline) {
                    redisCache.addOfflineNotification(userId, message, OFFLINE_MESSAGE_EXPIRE_DAYS);
                    offlineCount++;
                }
            }

            if (offlineCount > 0) {
                log.info("[OfflineMessageHandler] 批量离线通知已存储 - Count: {}, MessageId: {}",
                        offlineCount, message.getMessageId());
            }
        }
    }

    /**
     * 检查用户是否在线
     */
    private boolean isUserOnline(Long userId) {
        // 方法1：通过WebSocketService检查
        List channels = WebSocketService.Online_User.get(String.valueOf(userId));
        boolean isOnlineViaWebSocket = channels != null && !channels.isEmpty();

        // 方法2：通过Redis检查（更准确）
        boolean isOnlineViaRedis = redisCache.isUserOnline(userId);

        // 优先使用WebSocket检查，因为更实时
        return isOnlineViaWebSocket || isOnlineViaRedis;
    }
}
