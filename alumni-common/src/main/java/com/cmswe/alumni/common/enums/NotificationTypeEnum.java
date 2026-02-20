package com.cmswe.alumni.common.enums;

import lombok.Getter;

/**
 * 通知类型枚举
 *
 * @author CMSWE
 * @since 2025-12-05
 */
@Getter
public enum NotificationTypeEnum {

    /**
     * 用户关注
     */
    USER_FOLLOW("USER_FOLLOW", "用户关注"),

    /**
     * 系统通知
     */
    SYSTEM_NOTICE("SYSTEM_NOTICE", "系统通知"),

    /**
     * 评论通知
     */
    COMMENT("COMMENT", "评论通知"),

    /**
     * 点赞通知
     */
    LIKE("LIKE", "点赞通知"),

    /**
     * 私信通知
     */
    PRIVATE_MESSAGE("PRIVATE_MESSAGE", "私信通知"),

    /**
     * 校友会通知
     */
    ASSOCIATION_NOTICE("ASSOCIATION_NOTICE", "校友会通知");

    private final String code;
    private final String description;

    NotificationTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
