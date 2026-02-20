package com.cmswe.alumni.kafka.consumer;

import com.cmswe.alumni.kafka.handler.HandlerChain;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 消息消费者抽象基类（企业级通用标准）
 *
 * <p>提供完整的消息消费流程：
 * <ol>
 *   <li>反序列化：JSON → 对象</li>
 *   <li>消息验证：必填字段检查</li>
 *   <li>消息去重：幂等性保证（可选）</li>
 *   <li>责任链处理：通过责任链处理消息</li>
 *   <li>异常处理：失败消息进入死信队列</li>
 * </ol>
 *
 * <p>模板方法模式：
 * <ul>
 *   <li>consume() 定义标准流程</li>
 *   <li>子类实现个性化逻辑（反序列化、验证、去重等）</li>
 * </ul>
 *
 * @param <T> 消息类型
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
public abstract class AbstractMessageConsumer<T> implements MessageConsumer<T> {

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 责任链（由子类初始化）
     */
    protected HandlerChain<T> handlerChain;

    @Override
    public void consume(String message, int partition, long offset) {
        T deserializedMessage = null;

        try {
            log.info("[{}] 接收到消息 - Topic: {}, Partition: {}, Offset: {}",
                    getConsumerName(), getConsumedTopic(), partition, offset);

            // 1. 反序列化消息
            deserializedMessage = deserializeMessage(message);
            if (deserializedMessage == null) {
                log.error("[{}] 消息反序列化失败，返回 null - Partition: {}, Offset: {}",
                        getConsumerName(), partition, offset);
                handleFailedRawMessage(message, "消息反序列化返回 null");
                return;
            }

            log.debug("[{}] 消息反序列化成功 - Type: {}",
                    getConsumerName(), deserializedMessage.getClass().getSimpleName());

            // 2. 消息验证
            if (!validateMessage(deserializedMessage)) {
                log.warn("[{}] 消息验证失败，发送到死信队列",
                        getConsumerName());
                handleFailedMessage(deserializedMessage, "消息验证失败：缺少必要字段");
                return;
            }

            // 3. 消息去重检查（可选）
            if (enableIdempotent() && isDuplicateMessage(deserializedMessage)) {
                log.warn("[{}] 重复消息，跳过处理",
                        getConsumerName());
                return;
            }

            // 4. 通过责任链处理消息
            long startTime = System.currentTimeMillis();
            boolean success = processMessageWithChain(deserializedMessage);
            long endTime = System.currentTimeMillis();

            if (success) {
                log.info("[{}] 消息处理成功 - Time: {}ms",
                        getConsumerName(), (endTime - startTime));
            } else {
                log.warn("[{}] 消息处理部分失败，发送到死信队列 - Time: {}ms",
                        getConsumerName(), (endTime - startTime));
                handleFailedMessage(deserializedMessage, "消息处理失败：责任链返回 false");
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // 反序列化失败
            log.error("[{}] 消息反序列化异常 - Partition: {}, Offset: {}, Error: {}",
                    getConsumerName(), partition, offset, e.getMessage(), e);
            handleFailedRawMessage(message, "消息反序列化异常：" + e.getMessage());

        } catch (Exception e) {
            log.error("[{}] 消息处理异常 - Partition: {}, Offset: {}, Error: {}",
                    getConsumerName(), partition, offset, e.getMessage(), e);

            // 发送到死信队列
            if (deserializedMessage != null) {
                handleFailedMessage(deserializedMessage, "消息处理异常：" + e.getMessage());
            } else {
                handleFailedRawMessage(message, "消息处理异常：" + e.getMessage());
            }
        }
    }

    /**
     * 反序列化消息（子类实现）
     *
     * <p>将 JSON 字符串转换为对象
     *
     * @param message JSON 字符串
     * @return 消息对象
     * @throws com.fasterxml.jackson.core.JsonProcessingException 反序列化失败
     */
    protected abstract T deserializeMessage(String message)
            throws com.fasterxml.jackson.core.JsonProcessingException;

    /**
     * 验证消息（子类实现）
     *
     * <p>检查必填字段、业务规则等
     *
     * @param message 消息对象
     * @return true-验证通过；false-验证失败
     */
    protected abstract boolean validateMessage(T message);

    /**
     * 通过责任链处理消息
     *
     * @param message 消息对象
     * @return 是否处理成功
     */
    protected boolean processMessageWithChain(T message) {
        if (handlerChain == null) {
            log.error("[{}] 责任链未初始化，无法处理消息", getConsumerName());
            return false;
        }

        return handlerChain.execute(message);
    }

    /**
     * 是否启用幂等性检查（子类可重写）
     *
     * @return true-启用；false-禁用（默认禁用）
     */
    protected boolean enableIdempotent() {
        return false;
    }

    /**
     * 消息去重检查（子类可重写）
     *
     * <p>子类需要实现具体的去重逻辑（如基于 Redis）
     *
     * @param message 消息对象
     * @return true-重复消息；false-非重复消息
     */
    protected boolean isDuplicateMessage(T message) {
        return false;
    }

    /**
     * 处理失败的消息（子类可重写）
     *
     * <p>默认实现：记录日志
     * <p>子类可以实现发送到死信队列等逻辑
     *
     * @param message 消息对象
     * @param reason  失败原因
     */
    protected void handleFailedMessage(T message, String reason) {
        log.error("[{}] 消息处理失败 - Reason: {}, Message: {}",
                getConsumerName(), reason, message);
    }

    /**
     * 处理原始消息失败（无法反序列化的消息）
     *
     * <p>默认实现：记录日志
     *
     * @param rawMessage 原始消息 JSON 字符串
     * @param reason     失败原因
     */
    protected void handleFailedRawMessage(String rawMessage, String reason) {
        log.error("[{}] 原始消息处理失败 - Reason: {}, RawMessage: {}",
                getConsumerName(), reason, rawMessage);
    }
}
