package com.cmswe.alumni.kafka.handler;

import java.util.Arrays;
import java.util.List;

/**
 * 责任链构建器（企业级标准）
 *
 * <p>提供流式 API 构建责任链，使代码更优雅
 *
 * <p>使用示例：
 * <pre>{@code
 * HandlerChain<UnifiedMessage> chain = HandlerChainBuilder
 *     .create("消息处理链")
 *     .addHandler(webSocketHandler)
 *     .addHandler(databaseHandler)
 *     .addHandler(redisHandler)
 *     .build();
 *
 * chain.execute(message);
 * }</pre>
 *
 * @param <T> 消息类型
 * @author CMSWE
 * @since 2025-12-09
 */
public class HandlerChainBuilder<T> {

    private final HandlerChain<T> chain;

    private HandlerChainBuilder(String chainName) {
        this.chain = new HandlerChain<>(chainName);
    }

    /**
     * 创建构建器
     *
     * @param chainName 责任链名称
     * @param <T>       消息类型
     * @return 构建器实例
     */
    public static <T> HandlerChainBuilder<T> create(String chainName) {
        return new HandlerChainBuilder<>(chainName);
    }

    /**
     * 添加处理器
     *
     * @param handler 处理器
     * @return 构建器（支持链式调用）
     */
    public HandlerChainBuilder<T> addHandler(MessageHandler<T> handler) {
        chain.addHandler(handler);
        return this;
    }

    /**
     * 批量添加处理器
     *
     * @param handlers 处理器列表
     * @return 构建器（支持链式调用）
     */
    @SafeVarargs
    public final HandlerChainBuilder<T> addHandlers(MessageHandler<T>... handlers) {
        if (handlers != null) {
            Arrays.stream(handlers).forEach(chain::addHandler);
        }
        return this;
    }

    /**
     * 批量添加处理器（List 版本）
     *
     * @param handlers 处理器列表
     * @return 构建器（支持链式调用）
     */
    public HandlerChainBuilder<T> addHandlers(List<MessageHandler<T>> handlers) {
        if (handlers != null) {
            handlers.forEach(chain::addHandler);
        }
        return this;
    }

    /**
     * 构建责任链
     *
     * @return 责任链对象
     */
    public HandlerChain<T> build() {
        chain.build();
        return chain;
    }
}
