package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.user.AuthService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.WxInitRequest;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.WxInitResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "微信认证")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @Operation(summary = "认证登录")
    @PostMapping("/login")
    public BaseResponse<WxInitResponse> login(@RequestBody WxInitRequest wxInitRequest, HttpServletRequest request) throws JsonProcessingException {
        WxInitResponse wxInitResponse = authService.wxInit(wxInitRequest, request);

        return ResultUtils.success(Code.SUCCESS, wxInitResponse);
    }

    /**
     * 1. 用户登录的时候，需要初始化的一些信息
     *      1. 用户隐私设置
     */
}
