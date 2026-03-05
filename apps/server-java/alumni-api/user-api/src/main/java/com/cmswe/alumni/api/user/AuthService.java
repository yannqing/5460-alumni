package com.cmswe.alumni.api.user;

import com.cmswe.alumni.common.dto.GetPhoneNumberRequest;
import com.cmswe.alumni.common.dto.WxInitRequest;
import com.cmswe.alumni.common.vo.GetPhoneNumberResponse;
import com.cmswe.alumni.common.vo.WxInitResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    WxInitResponse wxInit(WxInitRequest wxInitRequest, HttpServletRequest request) throws JsonProcessingException;

    /**
     * 获取微信用户手机号
     *
     * @param request 获取手机号请求参数
     * @return 用户手机号信息
     */
    GetPhoneNumberResponse getPhoneNumber(GetPhoneNumberRequest request);

    /**
     * 测试登录接口（本地调试用）
     *
     * @param wxId 微信用户ID
     * @return 登录响应信息（与正式登录接口返回一致）
     * @throws JsonProcessingException JSON处理异常
     */
    WxInitResponse testLogin(Long wxId) throws JsonProcessingException;
}
