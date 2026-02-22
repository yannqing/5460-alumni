package com.cmswe.alumni.common.websocket;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 端点
 * 替代原来的 Netty WebSocket Server (9100端口)
 * 现在与主应用共用同一端口，通过 /ws 路径访问
 */
@Slf4j
@Component
@ServerEndpoint(value = "/ws", configurator = WebSocketEndpointConfig.class)
public class WebSocketEndpoint {

    // Session 与 token/ip 的映射
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> SESSION_TOKEN_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> SESSION_IP_MAP = new ConcurrentHashMap<>();

    // 用于适配 Netty Channel 的映射（保持与现有业务逻辑兼容）
    private static final Map<Session, NettyChannelAdapter> SESSION_CHANNEL_ADAPTER = new ConcurrentHashMap<>();

    private IWebSocketHandler webSocketHandler;

    /**
     * 连接建立成功
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String sessionId = session.getId();

        // 获取握手时的参数
        Map<String, Object> userProperties = config.getUserProperties();
        String token = (String) userProperties.get("token");
        String ip = (String) userProperties.get("ip");

        log.info("WebSocket连接建立，sessionId: {}, token: {}, ip: {}", sessionId, token, ip);

        // 保存 session 和关联信息
        SESSION_MAP.put(sessionId, session);
        if (token != null) {
            SESSION_TOKEN_MAP.put(sessionId, token);
        }
        if (ip != null) {
            SESSION_IP_MAP.put(sessionId, ip);
        }

        // 获取业务处理器
        if (webSocketHandler == null) {
            webSocketHandler = SpringUtil.getBean(IWebSocketHandler.class);
        }

        try {
            // 创建 Netty Channel 适配器，保持与现有业务逻辑兼容
            NettyChannelAdapter channelAdapter = new NettyChannelAdapter(session);
            SESSION_CHANNEL_ADAPTER.put(session, channelAdapter);

            // 调用原有的业务逻辑
            webSocketHandler.online(channelAdapter, token);
        } catch (Exception e) {
            log.error("用户上线处理失败: {}", e.getMessage(), e);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "连接错误"));
            } catch (IOException ex) {
                log.error("关闭会话失败", ex);
            }
        }
    }

    /**
     * 接收到消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        String sessionId = session.getId();
        String token = SESSION_TOKEN_MAP.get(sessionId);
        String ip = SESSION_IP_MAP.get(sessionId);

        log.info("收到WebSocket消息，sessionId: {}, token: {}, ip: {}, message: {}",
                sessionId, token, ip, message);

        // 这里可以处理客户端发来的消息（目前系统设计是只推送，不接收客户端消息）
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.info("WebSocket连接关闭，sessionId: {}, reason: {}", session.getId(), reason);

        // 调用业务层的下线处理
        NettyChannelAdapter channelAdapter = SESSION_CHANNEL_ADAPTER.get(session);
        if (channelAdapter != null && webSocketHandler != null) {
            webSocketHandler.offline(channelAdapter);
        }

        removeSession(session);
    }

    /**
     * 发生错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误，sessionId: {}", session.getId(), error);
        removeSession(session);
    }

    /**
     * 清理 session 相关信息
     */
    private void removeSession(Session session) {
        String sessionId = session.getId();
        SESSION_MAP.remove(sessionId);
        SESSION_TOKEN_MAP.remove(sessionId);
        SESSION_IP_MAP.remove(sessionId);
        SESSION_CHANNEL_ADAPTER.remove(session);
    }

    /**
     * 获取所有活跃的 session
     */
    public static Map<String, Session> getAllSessions() {
        return SESSION_MAP;
    }
}
