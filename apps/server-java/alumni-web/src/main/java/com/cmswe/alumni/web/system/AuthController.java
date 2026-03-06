package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.user.AuthService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.GetPhoneNumberRequest;
import com.cmswe.alumni.common.dto.RegisterUserDto;
import com.cmswe.alumni.common.dto.WxInitRequest;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.GetPhoneNumberResponse;
import com.cmswe.alumni.common.vo.WxInitResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Operation(summary = "获取用户手机号")
    @PostMapping("/getPhoneNumber")
    public BaseResponse<GetPhoneNumberResponse> getPhoneNumber(@Valid @RequestBody GetPhoneNumberRequest request) {
        GetPhoneNumberResponse response = authService.getPhoneNumber(request);

        return ResultUtils.success(Code.SUCCESS, response, "获取手机号成功");
    }

    @Operation(summary = "测试登录（本地调试用，传入微信用户ID）")
    @PostMapping("/testLogin")
    public BaseResponse<WxInitResponse> testLogin(@RequestBody java.util.Map<String, Long> request) throws JsonProcessingException {
        Long wxId = request.get("wxId");
        if (wxId == null) {
            return ResultUtils.failure(Code.FAILURE, null, "参数错误：wxId不能为空");
        }

        WxInitResponse wxInitResponse = authService.testLogin(wxId);

        return ResultUtils.success(Code.SUCCESS, wxInitResponse, "测试登录成功");
    }

    @Operation(summary = "用户注册（完善基本信息）")
    @PostMapping("/register")
    public BaseResponse<Boolean> register(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody RegisterUserDto registerDto) {

        // 检查用户是否已登录
        if (securityUser == null || securityUser.getWxUser() == null) {
            return ResultUtils.failure(Code.FAILURE, false, "用户未登录，请先登录");
        }

        Long wxId = securityUser.getWxUser().getWxId();

        boolean result = authService.registerUser(
                wxId,
                registerDto.getName(),
                registerDto.getSchoolId(),
                registerDto.getGender(),
                registerDto.getPhone()
        );

        return ResultUtils.success(Code.SUCCESS, result, "注册成功");
    }

    /**
     * 1. 用户登录的时候，需要初始化的一些信息
     *      1. 用户隐私设置
     */
}
