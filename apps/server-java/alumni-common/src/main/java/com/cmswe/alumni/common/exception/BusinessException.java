package com.cmswe.alumni.common.exception;

import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.enums.ErrorType;
import lombok.Getter;


/**
 * 自定义异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = Code.FAILURE;
    }

    public BusinessException(ErrorType errorType) {
        super(errorType.getMessage());
        this.code = errorType.getCode();
    }
    
    public BusinessException(ErrorType errorType, String message) {
        super(message);
        this.code = errorType.getCode();
    }
}
