package com.cmswe.alumni.common.enums;

import com.cmswe.alumni.common.constant.BaseTypeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogType implements BaseTypeInterface<Integer> {

    AUTH_LOG(1, "认证操作")
    ;

    private final Integer code;
    private final String message;
}
