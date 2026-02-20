package com.cmswe.alumni.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 热搜趋势类型枚举
 *
 * @author CNI Alumni System
 */
@Getter
public enum TrendType {

    /**
     * 上升
     */
    UP("UP", "上升"),

    /**
     * 下降
     */
    DOWN("DOWN", "下降"),

    /**
     * 新词
     */
    NEW("NEW", "新词"),

    /**
     * 持平
     */
    SAME("SAME", "持平");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    TrendType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
