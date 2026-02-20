package com.cmswe.alumni.common.enums;

import com.cmswe.alumni.common.constant.BaseTypeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorType implements BaseTypeInterface<Integer> {
    // TODO 类型统一摆放
    // common 一般异常
    SYSTEM_ERROR(50000, "系统错误！"),
    ARGS_NOT_NULL(50001, "参数不能为空，请重试！"),
    SYSTEM_USER_ERROR(50002, "系统用户无法修改，请重试！"),
    OPERATION_FAILED(50005, "操作失败，请重试！"),
    ARGS_ERROR(50006, "参数格式错误，请重试！"),
    NOT_FOUND_ERROR(50007, "资源不存在，请重试！"),
    OPERATION_ERROR(50008, "操作错误，请重试！"),

    // 权限异常
    NO_AUTH_ERROR(50050, "您没有权限，请重试！"),
    FORBIDDEN_ERROR(50051, "禁止访问，请重试！"),

    //业务异常
    CHAT_SESSION_NOT_EXISTS(50101, "聊天会话不存在，请重试！"),
    CHAT_SESSION_NOT_ACTIVE(50102, "聊天会话不可用，请重试！"),
    RESUME_NOT_EXISTS(50201, "简历不存在，请重试！"),
    FILES_COUNT_REACH_MAX(50301, "文件数量过多，请重试！"),
    OPERATION_NOT_EXISTS(50401, "对应操作日志不存在，请重试！"),
    ;
    // 异常码
    private final Integer code;
    // 错误信息
    private final String message;
}
