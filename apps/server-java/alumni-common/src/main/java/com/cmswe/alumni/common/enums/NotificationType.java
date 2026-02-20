package com.cmswe.alumni.common.enums;

import lombok.Getter;

/**
 * 通知类型枚举
 * 企业级标准：细化各种业务通知类型
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Getter
public enum NotificationType {

    // ==================== 用户相关通知 ====================
    /**
     * 用户关注通知
     */
    USER_FOLLOW("USER_FOLLOW", "用户关注", "有人关注了你"),

    /**
     * 用户取消关注通知
     */
    USER_UNFOLLOW("USER_UNFOLLOW", "取消关注", "有人取消了对你的关注"),

    // ==================== 互动相关通知 ====================
    /**
     * 评论通知
     */
    COMMENT("COMMENT", "评论通知", "有人评论了你的内容"),

    /**
     * 点赞通知
     */
    LIKE("LIKE", "点赞通知", "有人点赞了你的内容"),

    /**
     * @提及通知
     */
    MENTION("MENTION", "提及通知", "有人@了你"),

    /**
     * 回复通知
     */
    REPLY("REPLY", "回复通知", "有人回复了你的评论"),

    // ==================== 系统相关通知 ====================
    /**
     * 系统公告
     */
    SYSTEM_ANNOUNCEMENT("SYSTEM_ANNOUNCEMENT", "系统公告", "系统发布了新公告"),

    /**
     * 会员升级通知
     */
    MEMBER_UPGRADE("MEMBER_UPGRADE", "会员升级", "恭喜您的会员等级提升"),

    /**
     * 会员到期提醒
     */
    MEMBER_EXPIRING("MEMBER_EXPIRING", "会员到期", "您的会员即将到期"),

    /**
     * 优惠券到期提醒
     */
    COUPON_EXPIRING("COUPON_EXPIRING", "优惠券到期", "您的优惠券即将过期"),

    /**
     * 优惠券发放通知
     */
    COUPON_ISSUED("COUPON_ISSUED", "优惠券发放", "您获得了新的优惠券"),

    /**
     * 账号安全通知
     */
    ACCOUNT_SECURITY("ACCOUNT_SECURITY", "账号安全", "您的账号安全提醒"),

    // ==================== 群组相关通知 ====================
    /**
     * 群主转移通知
     */
    GROUP_OWNER_TRANSFER("GROUP_OWNER_TRANSFER", "群主转移", "群主已转移"),

    /**
     * 群成员加入通知
     */
    GROUP_MEMBER_JOIN("GROUP_MEMBER_JOIN", "新成员加入", "有新成员加入群组"),

    /**
     * 群成员退出通知
     */
    GROUP_MEMBER_LEAVE("GROUP_MEMBER_LEAVE", "成员退出", "有成员退出群组"),

    /**
     * 群公告通知
     */
    GROUP_ANNOUNCEMENT("GROUP_ANNOUNCEMENT", "群公告", "群组发布了新公告"),

    /**
     * 群解散通知
     */
    GROUP_DISBANDED("GROUP_DISBANDED", "群解散", "群组已解散"),

    // ==================== 组织相关通知 ====================
    /**
     * 组织活动发布
     */
    ORGANIZATION_EVENT("ORGANIZATION_EVENT", "活动发布", "组织发布了新活动"),

    /**
     * 组织公告发布
     */
    ORGANIZATION_ANNOUNCEMENT("ORGANIZATION_ANNOUNCEMENT", "组织公告", "组织发布了新公告"),

    /**
     * 组织新闻发布
     */
    ORGANIZATION_NEWS("ORGANIZATION_NEWS", "组织新闻", "组织发布了新闻"),

    /**
     * 活动报名成功
     */
    EVENT_REGISTRATION_SUCCESS("EVENT_REGISTRATION_SUCCESS", "报名成功", "您已成功报名活动"),

    /**
     * 活动即将开始
     */
    EVENT_STARTING_SOON("EVENT_STARTING_SOON", "活动提醒", "您报名的活动即将开始"),

    /**
     * 活动取消通知
     */
    EVENT_CANCELLED("EVENT_CANCELLED", "活动取消", "活动已被取消");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 默认模板
     */
    private final String defaultTemplate;

    NotificationType(String code, String name, String defaultTemplate) {
        this.code = code;
        this.name = name;
        this.defaultTemplate = defaultTemplate;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 枚举值
     */
    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type code: " + code);
    }
}
