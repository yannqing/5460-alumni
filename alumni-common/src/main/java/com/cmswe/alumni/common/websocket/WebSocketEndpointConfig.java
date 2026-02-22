package com.cmswe.alumni.common.websocket;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 端点配置器
 * 用于在握手阶段提取 token 和 IP
 */
@Slf4j
public class WebSocketEndpointConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                               HandshakeRequest request,
                               HandshakeResponse response) {
        // 提取 token（从查询参数中获取）
        Map<String, List<String>> params = request.getParameterMap();
        String token = null;
        if (params.containsKey("x-token") && !params.get("x-token").isEmpty()) {
            token = params.get("x-token").get(0);
        }

        // 提取客户端 IP
        String ip = null;
        Map<String, List<String>> headers = request.getHeaders();

        if (headers.containsKey("x-ip") && !headers.get("x-ip").isEmpty()) {
            ip = headers.get("x-ip").get(0);
        }
        if (ip == null && headers.containsKey("x-forwarded-for") && !headers.get("x-forwarded-for").isEmpty()) {
            ip = headers.get("x-forwarded-for").get(0);
        }
        if (ip == null && headers.containsKey("x-real-ip") && !headers.get("x-real-ip").isEmpty()) {
            ip = headers.get("x-real-ip").get(0);
        }

        // 将 token 和 IP 存储到用户属性中
        if (token != null) {
            config.getUserProperties().put("token", token);
        }
        if (ip != null) {
            config.getUserProperties().put("ip", ip);
        }

        log.debug("WebSocket握手，token: {}, ip: {}", token, ip);
    }
}
