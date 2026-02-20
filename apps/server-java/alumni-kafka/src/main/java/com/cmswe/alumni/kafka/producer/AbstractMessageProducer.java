package com.cmswe.alumni.kafka.producer;

import com.cmswe.alumni.kafka.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 消息生产者抽象基类（企业级通用标准）
 *
 * <p>提供通用的消息发送能力，包括：
 * <ul>
 *   <li>消息预处理（生成 ID、设置时间戳等）</li>
 *   <li>消息验证（必填字段检查）</li>
 *   <li>异步/同步发送</li>
 *   <li>批量发送</li>
 *   <li>异常处理</li>
 * </ul>
 *
 * <p>模板方法模式：
 * <ul>
 *   <li>通用逻辑在父类实现</li>
 *   <li>个性化逻辑由子类实现（Topic 路由、验证规则等）</li>
 * </ul>
 *
 * @param <T> 消息类型（支持泛型）
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
public abstract class AbstractMessageProducer<T> implements MessageProducer<T> {

    @Autowired
    protected KafkaUtils kafkaUtils;

    @Override
    public boolean sendAsync(T message) {
        try {
            // 1. 消息预处理（子类实现）
            preprocessMessage(message);

            // 2. 消息验证（子类实现）
            validateMessage(message);

            // 3. 获取目标 Topic
            String topic = getTargetTopic(message);

            // 4. 获取消息 Key（用于分区路由）
            String messageKey = extractMessageKey(message);

            // 5. 发送消息
            kafkaUtils.sendAsync(topic, messageKey, message);

            log.info("[{}] 消息发送成功 - Topic: {}, Key: {}",
                    getProducerName(), topic, messageKey);

            return true;

        } catch (Exception e) {
            log.error("[{}] 消息发送失败 - Error: {}",
                    getProducerName(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendSync(T message) {
        try {
            // 1. 消息预处理
            preprocessMessage(message);

            // 2. 消息验证
            validateMessage(message);

            // 3. 获取目标 Topic
            String topic = getTargetTopic(message);

            // 4. 获取消息 Key
            String messageKey = extractMessageKey(message);

            // 5. 同步发送消息
            kafkaUtils.sendSync(topic, messageKey, message);

            log.info("[{}] 消息同步发送成功 - Topic: {}, Key: {}",
                    getProducerName(), topic, messageKey);

            return true;

        } catch (Exception e) {
            log.error("[{}] 消息同步发送失败 - Error: {}",
                    getProducerName(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int batchSendAsync(List<T> messages) {
        if (messages == null || messages.isEmpty()) {
            log.warn("[{}] 批量发送消息列表为空", getProducerName());
            return 0;
        }

        int successCount = 0;
        for (T message : messages) {
            if (sendAsync(message)) {
                successCount++;
            }
        }

        log.info("[{}] 批量发送完成 - Total: {}, Success: {}, Failed: {}",
                getProducerName(), messages.size(), successCount, messages.size() - successCount);

        return successCount;
    }

    /**
     * 消息预处理（子类可重写）
     *
     * <p>典型场景：
     * <ul>
     *   <li>生成消息 ID</li>
     *   <li>设置时间戳</li>
     *   <li>设置来源服务</li>
     *   <li>设置消息类别</li>
     * </ul>
     *
     * @param message 消息对象
     */
    protected void preprocessMessage(T message) {
        // 默认空实现，子类按需重写
    }

    /**
     * 消息验证（子类必须实现）
     *
     * <p>验证消息必填字段，防止非法消息进入 Kafka
     *
     * @param message 消息对象
     * @throws IllegalArgumentException 验证失败时抛出异常
     */
    protected abstract void validateMessage(T message);

    /**
     * 提取消息 Key（子类可重写）
     *
     * <p>用于 Kafka 分区路由，相同 Key 的消息会进入同一分区
     * <p>典型实现：
     * <ul>
     *   <li>P2P 消息：使用用户 ID</li>
     *   <li>群聊消息：使用群 ID</li>
     *   <li>通知消息：使用消息 ID</li>
     * </ul>
     *
     * @param message 消息对象
     * @return 消息 Key（默认返回 null，Kafka 会随机分区）
     */
    protected String extractMessageKey(T message) {
        return null;
    }
}
