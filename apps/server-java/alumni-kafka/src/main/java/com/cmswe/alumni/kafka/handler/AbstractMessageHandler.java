package com.cmswe.alumni.kafka.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * 消息处理器抽象基类（责任链模式 - 企业级通用标准）
 *
 * <p>提供责任链的通用能力：
 * <ul>
 *   <li>链条传递：处理完成后自动传递给下一个处理器</li>
 *   <li>异常隔离：处理失败不影响链条继续执行</li>
 *   <li>日志记录：自动记录处理开始/结束/耗时</li>
 *   <li>开关控制：支持动态启用/禁用处理器</li>
 * </ul>
 *
 * <p>模板方法模式：
 * <ul>
 *   <li>handle() 定义标准流程</li>
 *   <li>doHandle() 由子类实现具体逻辑</li>
 * </ul>
 *
 * @param <T> 消息类型（支持泛型）
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
public abstract class AbstractMessageHandler<T> implements MessageHandler<T> {

    /**
     * 下一个处理器
     */
    private MessageHandler<T> next;

    @Override
    public boolean handle(T message) {
        boolean success = false;

        try {
            // 1. 检查处理器是否启用
            if (!isEnabled()) {
                log.debug("[{}] 处理器已禁用，跳过处理", getHandlerName());
                // 处理器禁用时，直接传递给下一个处理器
                if (next != null) {
                    return next.handle(message);
                }
                return true;
            }

            // 2. 执行具体的处理逻辑（子类实现）
            long startTime = System.currentTimeMillis();

            log.debug("[{}] 开始处理消息", getHandlerName());
            success = doHandle(message);
            long endTime = System.currentTimeMillis();

            if (success) {
                log.debug("[{}] 消息处理成功 - Time: {}ms",
                        getHandlerName(), (endTime - startTime));
            } else {
                log.warn("[{}] 消息处理失败，但继续执行链条 - Time: {}ms",
                        getHandlerName(), (endTime - startTime));
            }

        } catch (Exception e) {
            log.error("[{}] 消息处理异常，但继续执行链条 - Error: {}",
                    getHandlerName(), e.getMessage(), e);
        }

        // 3. 传递给下一个处理器（无论成功或失败都继续链条）
        if (next != null) {
            return next.handle(message);
        }

        // 4. 如果是链条的最后一个处理器，返回当前处理器的结果
        return success;
    }

    /**
     * 具体的处理逻辑（子类实现）
     *
     * <p>子类只需关注核心业务逻辑，无需处理异常、日志等
     *
     * @param message 消息对象
     * @return 是否处理成功
     */
    protected abstract boolean doHandle(T message);

    @Override
    public void setNext(MessageHandler<T> next) {
        this.next = next;
    }

    @Override
    public MessageHandler<T> getNext() {
        return this.next;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
