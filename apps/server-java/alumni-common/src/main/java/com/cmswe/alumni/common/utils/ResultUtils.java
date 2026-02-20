package com.cmswe.alumni.common.utils;


import com.cmswe.alumni.common.constant.Code;

public class ResultUtils {

    public static <T> BaseResponse<T> success(){
        return new BaseResponse<>(Code.SUCCESS, null);
    }
    public static <T> BaseResponse<T> success(String msg){
        return new BaseResponse<>(Code.SUCCESS, null, msg);
    }
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(Code.SUCCESS, data, "success");
    }

    public static <T> BaseResponse<T> success(Integer code, T data){
        return new BaseResponse<>(code, data, "success");
    }

    public static <T> BaseResponse<T> success(Integer code, T data, String msg){
        return new BaseResponse<>(code, data, msg);
    }

    public static <T> BaseResponse<T> failure(){
        return new BaseResponse<>(Code.FAILURE,null,"服务器错误");
    }

    public static <T> BaseResponse<T> failure(Integer errorCode, T data){
        return new BaseResponse<>(errorCode, data);
    }

    public static <T> BaseResponse<T> failure(Integer errorCode, T data, String msg){
        return new BaseResponse<>(errorCode, data, msg);
    }

    public static <T> BaseResponse<T> failure(String msg){
        return new BaseResponse<>(Code.FAILURE, null, msg);
    }
}
