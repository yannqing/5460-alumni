package com.cmswe.alumni.kafka.producer;

import java.util.List;

/**
 * 消息生产者接口（企业级通用标准）
 *
 * <p>设计模式：策略模式
 * <p>适用场景：所有 Kafka 消息发送场景
 * <p>扩展性：支持同步/异步、批量发送、自定义消息类型
 *
 * @param <T> 消息类型（支持泛型，适配不同业务场景）
 * @author CMSWE
 * @since 2025-12-09
 */
public interface MessageProducer<T> {

    /**
     * 发送消息（异步）
     *
     * <p>推荐使用，性能最佳，适用于大多数业务场景
     *
     * @param message 消息对象
     * @return 是否发送成功
     */
    boolean sendAsync(T message);

    /**
     * 发送消息（同步）
     *
     * <p>适用于关键业务场景，需要确保消息发送成功
     *
     * @param message 消息对象
     * @return 是否发送成功
     */
    boolean sendSync(T message);

    /**
     * 批量发送消息（异步）
     *
     * <p>适用于批量数据导入、批量通知等场景
     *
     * @param messages 消息列表
     * @return 成功发送的消息数量
     */
    int batchSendAsync(List<T> messages);

    /**
     * 获取生产者名称
     *
     * <p>用于日志记录、监控埋点等
     *
     * @return 生产者名称
     */
    String getProducerName();

    /**
     * 获取目标 Topic
     *
     * <p>子类实现具体的 Topic 路由逻辑
     *
     * @param message 消息对象
     * @return Topic 名称
     */
    String getTargetTopic(T message);
}
