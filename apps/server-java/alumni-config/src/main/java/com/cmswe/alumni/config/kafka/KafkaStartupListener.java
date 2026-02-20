package com.cmswe.alumni.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Kafka 启动监听器 - 诊断 Kafka Listener 容器启动问题
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final KafkaListenerEndpointRegistry registry;

    public KafkaStartupListener(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
        log.info("========================================");
        log.info("[KafkaStartupListener] Bean 已创建！");
        log.info("========================================");
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("========================================");
        log.info("[KafkaStartupListener] 应用启动完成，检查 Kafka Listener 容器...");

        var listenerContainers = registry.getListenerContainers();
        log.info("[KafkaStartupListener] 总共注册了 {} 个 Kafka Listener 容器", listenerContainers.size());

        if (listenerContainers.isEmpty()) {
            log.error("========================================");
            log.error("[KafkaStartupListener] ❌ 没有任何 Kafka Listener 容器被注册！");
            log.error("[KafkaStartupListener] 可能原因：");
            log.error("[KafkaStartupListener] 1. @EnableKafka 没有生效");
            log.error("[KafkaStartupListener] 2. @KafkaListener 注解的类没有被扫描到");
            log.error("[KafkaStartupListener] 3. @ConditionalOnProperty 条件不满足");
            log.error("========================================");
        } else {
            for (MessageListenerContainer container : listenerContainers) {
                String listenerId = container.getListenerId();
                boolean running = container.isRunning();

                log.info("[KafkaStartupListener] Listener: {} - Running: {}",
                    listenerId, running);

                if (!running) {
                    log.warn("[KafkaStartupListener] ⚠️  容器 {} 没有运行！", listenerId);
                }
            }
        }

        log.info("========================================");
    }
}
