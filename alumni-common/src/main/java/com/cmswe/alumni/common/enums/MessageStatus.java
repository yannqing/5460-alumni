package com.cmswe.alumni.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息状态枚举
 *
 * @author CMSWE
 * @since 2025-12-17
 */
@Getter
@AllArgsConstructor
public enum MessageStatus implements IEnum<Integer> {

    /**
     * 发送中（消息刚创建，正在发送到服务器）
     */
    SENDING(0, "发送中"),

    /**
     * 已送达（消息已送达服务器，但对方未读）
     */
    DELIVERED(1, "已送达"),

    /**
     * 已读（对方已读该消息）
     */
    READ(2, "已读"),

    /**
     * 发送失败（网络错误、服务器异常等）
     */
    FAILED(3, "发送失败"),

    /**
     * 已撤回（发送方撤回该消息）
     */
    RECALLED(4, "已撤回");

    @EnumValue  // 标记这个字段用于数据库映射
    private final Integer value;

    private final String description;

    /**
     * 根据值获取枚举
     */
    public static MessageStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (MessageStatus status : MessageStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为未读状态（发送中或已送达）
     */
    public static boolean isUnread(Integer status) {
        return status != null && (status == SENDING.value || status == DELIVERED.value);
    }

    /**
     * 判断是否为已读状态
     */
    public static boolean isRead(Integer status) {
        return status != null && status.equals(READ.value);
    }

    /**
     * 判断是否为已撤回状态
     */
    public static boolean isRecalled(Integer status) {
        return status != null && status.equals(RECALLED.value);
    }
}
