package com.cmswe.alumni.kafka.consumer;

/**
 * 消息消费者接口（企业级通用标准）
 *
 * <p>设计模式：模板方法模式
 * <p>适用场景：所有 Kafka 消息消费场景
 * <p>核心流程：反序列化 → 验证 → 去重 → 责任链处理 → 异常处理
 *
 * <p>企业级标准特性：
 * <ul>
 *   <li>消息验证：防止非法消息进入业务逻辑</li>
 *   <li>幂等性保证：基于消息 ID 的去重机制</li>
 *   <li>异常隔离：消费失败不影响其他消息</li>
 *   <li>死信队列：失败消息自动进入 DLQ，支持后续人工处理</li>
 * </ul>
 *
 * @param <T> 消息类型（支持泛型）
 * @author CMSWE
 * @since 2025-12-09
 */
public interface MessageConsumer<T> {

    /**
     * 消费消息（核心方法）
     *
     * <p>标准处理流程：
     * <ol>
     *   <li>反序列化消息</li>
     *   <li>消息验证</li>
     *   <li>消息去重检查</li>
     *   <li>通过责任链处理</li>
     *   <li>失败进入死信队列</li>
     * </ol>
     *
     * @param message   消息 JSON 字符串
     * @param partition 分区号
     * @param offset    偏移量
     */
    void consume(String message, int partition, long offset);

    /**
     * 获取消费者名称
     *
     * <p>用于日志记录、监控埋点
     *
     * @return 消费者名称
     */
    String getConsumerName();

    /**
     * 获取消费的 Topic
     *
     * <p>用于日志记录、监控
     *
     * @return Topic 名称
     */
    String getConsumedTopic();
}
