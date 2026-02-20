package com.cmswe.alumni.common.enums;

import com.cmswe.alumni.common.constant.BaseTypeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperationType implements BaseTypeInterface<String> {

    // 操作类型大类
    SYSTEM_OPERATION("SYSTEM", "系统操作"),
    USER_OPERATION("USER", "用户操作"),
    RESUME_OPERATION("RESUME", "简历操作"),


    RESUME_FORWARD_OPERATION("forward", "简历转发"),
    RESUME_FAVORITE_OPERATION("favorite", "简历收藏"),
    ;
    // 唯一代码
    private final String code;
    // 错误信息
    private final String message;
}
