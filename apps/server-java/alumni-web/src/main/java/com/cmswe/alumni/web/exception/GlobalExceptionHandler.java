package com.cmswe.alumni.web.exception;

import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

/**
 * 全局异常处理器
 *
 * @author yannqing
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 权限校验异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse<Object> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},权限校验失败 {}", requestURI, e.getMessage());
        return ResultUtils.failure("没有权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseResponse<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                      HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},不支持 {} 请求", requestURI, e.getMethod());
        return ResultUtils.failure(e.getMessage());
    }

    /**
     * 身份过期
     */
    @ExceptionHandler(IllegalStateException.class)
    public BaseResponse<Object> handleIllegalStateException(IllegalStateException e,
                                                            HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("用户过期，请重新登录：{}", e.getMessage());
        return ResultUtils.failure(Code.FAILURE, null, e.getMessage());
    }
    /**
     * 参数错误
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public BaseResponse<Object> handleIllegalArgumentException(IllegalArgumentException e,
                                                    HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("非法参数=>{}",e.getMessage());
        return ResultUtils.failure(e.getMessage());
    }

    @ExceptionHandler(ClassCastException.class)
    public BaseResponse<Object> handleClassCastException(ClassCastException e,
                                                       HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},权限异常: {}", requestURI, e.getMessage());
        return ResultUtils.failure("您没有权限访问此接口，请联系管理员");
    }

    /**
     * 拦截自定义异常 BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<Object> handleRuntimeException(BusinessException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.error("请求地址 {}, 方法{}, 异常: {}", requestURI, method, e.getMessage());

        return ResultUtils.failure(e.getCode(), null, e.getMessage());
    }

    /**
     * 拦截自定义异常 BusinessException
     */
    @ExceptionHandler(JsonProcessingException.class)
    public BaseResponse<Object> handleJsonProcessingException(JsonProcessingException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {}, JSON 解析异常: {}", requestURI, e.getMessage());

        return ResultUtils.failure(e.getMessage());
    }

    /**
     * IO异常
     */
    @ExceptionHandler(IOException.class)
    public BaseResponse<Object> handleIOException(IOException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},发生IO处理异常.", requestURI, e);
        return ResultUtils.failure(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<Object> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},发生系统异常.", requestURI, e);
        return ResultUtils.failure(e.getMessage());
    }

}
