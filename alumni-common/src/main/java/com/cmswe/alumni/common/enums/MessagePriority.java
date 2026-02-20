package com.cmswe.alumni.common.enums;

import lombok.Getter;

/**
 * 消息优先级枚举
 * 企业级标准：支持消息优先级处理
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Getter
public enum MessagePriority {

    /**
     * 低优先级 - 普通通知消息
     */
    LOW(1, "低优先级"),

    /**
     * 普通优先级 - 常规消息
     */
    NORMAL(5, "普通优先级"),

    /**
     * 高优先级 - 重要消息（如系统通知、群主转移等）
     */
    HIGH(8, "高优先级"),

    /**
     * 紧急 - 极重要消息（如账号安全、支付相关等）
     */
    URGENT(10, "紧急");

    /**
     * 优先级等级（1-10，数字越大优先级越高）
     */
    private final int level;

    /**
     * 优先级描述
     */
    private final String description;

    MessagePriority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    /**
     * 根据等级获取枚举
     *
     * @param level 等级
     * @return 枚举值
     */
    public static MessagePriority fromLevel(int level) {
        for (MessagePriority priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        return NORMAL;
    }
}
