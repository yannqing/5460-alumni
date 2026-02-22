package com.cmswe.alumni.common.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类
 * 启用 Jakarta WebSocket (JSR-356) 支持
 * 替代原来的 Netty WebSocket Server (9100端口)
 */
@Configuration
public class ServerEndpointExporterConfig {

    /**
     * 注册 ServerEndpointExporter
     * 用于扫描和注册 @ServerEndpoint 注解的端点
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
