package com.cmswe.alumni.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 诊断控制器 - 用于检查 Kafka Listener 状态
 */
@Slf4j
@RestController
@RequestMapping("/diagnostic")
public class DiagnosticController {

    private final KafkaListenerEndpointRegistry registry;

    public DiagnosticController(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/kafka-listeners")
    public Map<String, Object> getKafkaListeners() {
        Map<String, Object> result = new HashMap<>();

        // 获取所有注册的 Listener 容器
        var listenerContainers = registry.getListenerContainers();
        result.put("totalListeners", listenerContainers.size());

        Map<String, Map<String, Object>> listeners = new HashMap<>();
        for (MessageListenerContainer container : listenerContainers) {
            Map<String, Object> info = new HashMap<>();
            info.put("running", container.isRunning());
            info.put("listenerId", container.getListenerId());

            String listenerId = container.getListenerId();
            if (listenerId != null) {
                listeners.put(listenerId, info);
                log.info("[Diagnostic] Kafka Listener: {} - Running: {}",
                    listenerId, container.isRunning());
            }
        }

        result.put("listeners", listeners);
        return result;
    }
}
