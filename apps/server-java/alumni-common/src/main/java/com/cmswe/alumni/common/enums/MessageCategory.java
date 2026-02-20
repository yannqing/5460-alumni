package com.cmswe.alumni.common.enums;

import lombok.Getter;

/**
 * 消息类别枚举
 * 企业级标准：区分不同的消息业务场景
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Getter
public enum MessageCategory {

    /**
     * 点对点消息（P2P）- 用户之间的私聊消息
     */
    P2P("P2P", "点对点消息", "user.message.p2p"),

    /**
     * 群聊消息 - 群组内的聊天消息
     */
    GROUP("GROUP", "群聊消息", "group.message.chat"),

    /**
     * 系统通知 - 系统级别的通知消息
     */
    SYSTEM("SYSTEM", "系统通知", "system.notification"),

    /**
     * 组织通知 - 组织发布的公告、活动等通知
     */
    ORGANIZATION("ORGANIZATION", "组织通知", "organization.notification"),

    /**
     * 业务通知 - 业务相关的通知（关注、点赞、评论等）
     */
    BUSINESS("BUSINESS", "业务通知", "business.notification");

    /**
     * 消息类别代码
     */
    private final String code;

    /**
     * 消息类别描述
     */
    private final String description;

    /**
     * 对应的Kafka Topic
     */
    private final String topic;

    MessageCategory(String code, String description, String topic) {
        this.code = code;
        this.description = description;
        this.topic = topic;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 枚举值
     */
    public static MessageCategory fromCode(String code) {
        for (MessageCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown message category code: " + code);
    }
}
