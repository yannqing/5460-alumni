package com.cmswe.alumni.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 时间范围枚举（用于热搜榜等）
 *
 * @author CNI Alumni System
 */
@Getter
public enum TimeRange {

    /**
     * 今日
     */
    TODAY("TODAY", "今日", 1),

    /**
     * 本周
     */
    WEEK("WEEK", "本周", 7),

    /**
     * 本月
     */
    MONTH("MONTH", "本月", 30);

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    private final int days;

    TimeRange(String code, String description, int days) {
        this.code = code;
        this.description = description;
        this.days = days;
    }

    public static TimeRange fromCode(String code) {
        for (TimeRange range : values()) {
            if (range.code.equals(code)) {
                return range;
            }
        }
        return TODAY;
    }
}
