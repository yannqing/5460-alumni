package com.cmswe.alumni.kafka.handler;

/**
 * 消息处理器接口（责任链模式 - 企业级通用标准）
 *
 * <p>设计模式：责任链模式（Chain of Responsibility）
 * <p>适用场景：消息消费后的多步骤处理流程
 * <p>典型处理链：推送 → 持久化 → 缓存 → 离线处理
 * <p>扩展性：支持动态组装责任链，可插拔式处理器
 *
 * <p>企业级标准特性：
 * <ul>
 *   <li>单一职责：每个处理器只负责一个步骤</li>
 *   <li>失败隔离：某个处理器失败不影响链条继续执行</li>
 *   <li>可观测性：每个处理器返回处理结果，便于监控</li>
 * </ul>
 *
 * @param <T> 消息类型（支持泛型）
 * @author CMSWE
 * @since 2025-12-09
 */
public interface MessageHandler<T> {

    /**
     * 处理消息
     *
     * <p>核心处理逻辑，子类实现具体业务
     *
     * @param message 消息对象
     * @return 是否处理成功（true-成功继续链条；false-失败但继续链条）
     */
    boolean handle(T message);

    /**
     * 设置下一个处理器
     *
     * <p>用于构建责任链
     *
     * @param next 下一个处理器
     */
    void setNext(MessageHandler<T> next);

    /**
     * 获取下一个处理器
     *
     * @return 下一个处理器
     */
    MessageHandler<T> getNext();

    /**
     * 获取处理器名称
     *
     * <p>用于日志记录、监控埋点、链路追踪
     *
     * @return 处理器名称
     */
    String getHandlerName();

    /**
     * 获取处理器顺序（可选）
     *
     * <p>用于自动排序责任链，数值越小优先级越高
     *
     * @return 处理器顺序（默认 0）
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 是否启用该处理器
     *
     * <p>支持动态开关处理器
     *
     * @return true-启用；false-禁用（跳过该处理器）
     */
    default boolean isEnabled() {
        return true;
    }
}
