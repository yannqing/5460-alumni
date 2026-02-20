package com.cmswe.alumni.kafka.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 责任链容器（企业级标准）
 *
 * <p>功能：
 * <ul>
 *   <li>管理多个处理器</li>
 *   <li>按顺序执行处理器</li>
 *   <li>支持动态添加/移除处理器</li>
 *   <li>链路追踪：记录整个链条的执行情况</li>
 * </ul>
 *
 * @param <T> 消息类型
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
public class HandlerChain<T> {

    /**
     * 处理器列表
     */
    private final List<MessageHandler<T>> handlers = new ArrayList<>();

    /**
     * 责任链的头节点
     */
    private MessageHandler<T> head;

    /**
     * 责任链名称
     */
    private final String chainName;

    public HandlerChain(String chainName) {
        this.chainName = chainName;
    }

    /**
     * 添加处理器到责任链
     *
     * @param handler 处理器
     * @return 当前链对象（支持链式调用）
     */
    public HandlerChain<T> addHandler(MessageHandler<T> handler) {
        if (handler == null) {
            log.warn("[HandlerChain-{}] 尝试添加空处理器，已忽略", chainName);
            return this;
        }

        handlers.add(handler);
        log.debug("[HandlerChain-{}] 添加处理器 - Handler: {}, Order: {}",
                chainName, handler.getHandlerName(), handler.getOrder());

        return this;
    }

    /**
     * 构建责任链
     *
     * <p>将所有处理器按照 Order 排序后串联成链
     */
    public void build() {
        if (handlers.isEmpty()) {
            log.warn("[HandlerChain-{}] 责任链为空，无需构建", chainName);
            return;
        }

        // 按照 Order 排序
        handlers.sort((h1, h2) -> Integer.compare(h1.getOrder(), h2.getOrder()));

        // 串联成链
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }

        // 设置头节点
        head = handlers.get(0);

        // 打印责任链结构
        logChainStructure();
    }

    /**
     * 执行责任链
     *
     * @param message 消息对象
     * @return 是否处理成功
     */
    public boolean execute(T message) {
        if (head == null) {
            log.warn("[HandlerChain-{}] 责任链未构建或为空，无法执行", chainName);
            return false;
        }

        log.debug("[HandlerChain-{}] 开始执行责任链", chainName);
        long startTime = System.currentTimeMillis();

        boolean result = head.handle(message);

        long endTime = System.currentTimeMillis();
        log.info("[HandlerChain-{}] 责任链执行完成 - Result: {}, Time: {}ms",
                chainName, result, (endTime - startTime));

        return result;
    }

    /**
     * 获取责任链头节点
     *
     * @return 头节点
     */
    public MessageHandler<T> getHead() {
        return head;
    }

    /**
     * 获取所有处理器
     *
     * @return 处理器列表
     */
    public List<MessageHandler<T>> getHandlers() {
        return new ArrayList<>(handlers);
    }

    /**
     * 打印责任链结构（用于日志）
     */
    private void logChainStructure() {
        StringBuilder structure = new StringBuilder();
        structure.append("[HandlerChain-").append(chainName).append("] 责任链结构: ");

        for (int i = 0; i < handlers.size(); i++) {
            structure.append(handlers.get(i).getHandlerName());
            if (i < handlers.size() - 1) {
                structure.append(" -> ");
            }
        }

        log.info(structure.toString());
    }
}
