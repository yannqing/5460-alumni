package com.cmswe.alumni.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 搜索类型枚举
 *
 * @author CNI Alumni System
 */
@Getter
public enum SearchType {

    /**
     * 校友搜索
     */
    ALUMNI("ALUMNI", "校友"),

    /**
     * 校友会搜索
     */
    ASSOCIATION("ASSOCIATION", "校友会"),

    /**
     * 商户搜索
     */
    MERCHANT("MERCHANT", "商户"),

    /**
     * 母校搜索
     */
    SCHOOL("SCHOOL", "母校"),

    /**
     * 全部（统一搜索）
     */
    ALL("ALL", "全部");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    SearchType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static SearchType fromCode(String code) {
        for (SearchType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
