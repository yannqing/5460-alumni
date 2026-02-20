package com.cmswe.alumni.service.user.service.message.handler;

import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.handler.AbstractMessageHandler;
import com.cmswe.alumni.service.system.service.impl.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * WebSocket推送处理器（责任链模式）
 *
 * <p>负责将消息通过WebSocket推送给在线用户
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Component
public class WebSocketPushHandler extends AbstractMessageHandler<UnifiedMessage> {

    private final WebSocketService webSocketService;

    public WebSocketPushHandler(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Override
    public String getHandlerName() {
        return "WebSocketPushHandler";
    }

    @Override
    protected boolean doHandle(UnifiedMessage message) {
        // 检查是否需要推送
        if (!message.getNeedPush()) {
            log.debug("[WebSocketPushHandler] 消息不需要推送 - MessageId: {}", message.getMessageId());
            return true;
        }

        try {
            // 根据消息类别选择不同的推送策略
            switch (message.getCategory()) {
                case P2P -> pushP2PMessage(message);
                case GROUP -> pushGroupMessage(message);
                case SYSTEM -> pushSystemNotification(message);
                case ORGANIZATION -> pushOrganizationNotification(message);
                case BUSINESS -> pushBusinessNotification(message);
                default -> {
                    log.warn("[WebSocketPushHandler] 未知的消息类别 - Category: {}", message.getCategory());
                    return false;
                }
            }

            log.info("[WebSocketPushHandler] 消息推送成功 - Category: {}, MessageId: {}",
                    message.getCategory(), message.getMessageId());
            return true;

        } catch (Exception e) {
            log.error("[WebSocketPushHandler] 消息推送失败 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 推送P2P消息
     */
    private void pushP2PMessage(UnifiedMessage message) {
        if (message.getToId() != null) {
            webSocketService.sendMsgToUser(message, String.valueOf(message.getToId()));
            log.debug("[WebSocketPushHandler] P2P消息推送 - ToUserId: {}", message.getToId());
        }
    }

    /**
     * 推送群聊消息
     */
    private void pushGroupMessage(UnifiedMessage message) {
        if (message.getToId() != null) {
            // 方案1：直接推送UnifiedMessage（推荐）
            // 群聊消息直接推送给所有在线群成员
            webSocketService.sendMsgToUser(message, String.valueOf(message.getToId()));

            // 方案2：如果需要使用原有的sendMsgToGroup逻辑，需要转换为ChatMessage
            // 但这需要完整的ChatMessage对象构建，这里暂时使用方案1

            log.debug("[WebSocketPushHandler] 群聊消息推送 - GroupId: {}", message.getToId());
        }
    }

    /**
     * 推送系统通知
     */
    private void pushSystemNotification(UnifiedMessage message) {
        if (message.getToId() != null && message.getToId() == 0) {
            // 广播给所有在线用户
            webSocketService.sendNotifyAll(message);
            log.debug("[WebSocketPushHandler] 系统通知广播给所有用户");
        } else if (message.getToId() != null) {
            // 推送给单个用户
            webSocketService.sendNotifyToUser(message, String.valueOf(message.getToId()));
            log.debug("[WebSocketPushHandler] 系统通知推送 - ToUserId: {}", message.getToId());
        } else if (message.getToIds() != null && !message.getToIds().isEmpty()) {
            // 批量推送
            for (Long userId : message.getToIds()) {
                webSocketService.sendNotifyToUser(message, String.valueOf(userId));
            }
            log.debug("[WebSocketPushHandler] 系统通知批量推送 - Count: {}", message.getToIds().size());
        }
    }

    /**
     * 推送组织通知
     */
    private void pushOrganizationNotification(UnifiedMessage message) {
        if (message.getToIds() != null && !message.getToIds().isEmpty()) {
            // 批量推送给关注者
            for (Long userId : message.getToIds()) {
                webSocketService.sendNotifyToUser(message, String.valueOf(userId));
            }
            log.debug("[WebSocketPushHandler] 组织通知批量推送 - Count: {}", message.getToIds().size());
        }
    }

    /**
     * 推送业务通知
     */
    private void pushBusinessNotification(UnifiedMessage message) {
        if (message.getToId() != null) {
            webSocketService.sendNotifyToUser(message, String.valueOf(message.getToId()));
            log.debug("[WebSocketPushHandler] 业务通知推送 - ToUserId: {}", message.getToId());
        }
    }

}
