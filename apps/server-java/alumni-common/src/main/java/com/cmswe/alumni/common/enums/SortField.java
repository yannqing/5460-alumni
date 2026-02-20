package com.cmswe.alumni.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 搜索排序字段枚举
 *
 * @author CNI Alumni System
 */
@Getter
public enum SortField {

    /**
     * 相关性排序（默认）
     */
    RELEVANCE("RELEVANCE", "_score", "相关性"),

    /**
     * 创建时间
     */
    CREATE_TIME("CREATE_TIME", "createTime", "创建时间"),

    /**
     * 更新时间
     */
    UPDATE_TIME("UPDATE_TIME", "updateTime", "更新时间"),

    /**
     * 距离（地理位置搜索时）
     */
    DISTANCE("DISTANCE", "_geo_distance", "距离"),

    /**
     * 成员数量（校友会）
     */
    MEMBER_COUNT("MEMBER_COUNT", "memberCount", "成员数量"),

    /**
     * 评分（商户）
     */
    RATING("RATING", "rating", "评分");

    @EnumValue
    @JsonValue
    private final String code;

    private final String fieldName;

    private final String description;

    SortField(String code, String fieldName, String description) {
        this.code = code;
        this.fieldName = fieldName;
        this.description = description;
    }

    public static SortField fromCode(String code) {
        for (SortField field : values()) {
            if (field.code.equals(code)) {
                return field;
            }
        }
        return RELEVANCE;
    }
}
