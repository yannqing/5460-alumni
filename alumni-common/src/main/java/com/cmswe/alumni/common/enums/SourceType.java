package com.cmswe.alumni.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 消息源
 */
@Getter
@AllArgsConstructor
public enum SourceType implements IEnum<String> {
    USER("user"),
    SYSTEM("system"),
    GROUP("group"),
    NOTIFICATION("notification");

    @EnumValue  // 标记这个字段用于数据库映射
    private final String value;
    public static SourceType valueOfAll(String value) {
        for (SourceType sourceType : SourceType.values()) {
            if(Objects.equals(value,sourceType.getValue())){
                return sourceType;
            }

        }
        return null;
    }

    public static SourceType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SourceType sourceType : SourceType.values()) {
            if (sourceType.value.equalsIgnoreCase(value)) {
                return sourceType;
            }
        }
        return null;
    }
}
