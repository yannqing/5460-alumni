package com.cmswe.alumni.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> implements Serializable {
    private Integer code;
    private T data;
    private String msg;

    public BaseResponse(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(200);
        response.setData(data);
        response.setMsg("success");
        return response;
    }

    public static <T> BaseResponse<T> error(String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(500);
        response.setData(null);
        response.setMsg(message);
        return response;
    }
}
