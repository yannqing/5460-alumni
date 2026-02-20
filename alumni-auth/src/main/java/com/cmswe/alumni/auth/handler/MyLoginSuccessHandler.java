package com.cmswe.alumni.auth.handler;

import com.alibaba.fastjson2.JSON;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.auth.LoginVo;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.utils.JwtUtils;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private RedisCache redisCache;
    
    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 登录成功处理器：返回用户信息，对应用户的权限信息，登录生成token
     * @param request 会话请求
     * @param response 会话响应
     * @param authentication 认证信息
     * @throws IOException IO 异常
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        WxUser user = securityUser.getWxUser();
        String userInfo = objectMapper.writeValueAsString(user);

        List<Role> roles = securityUser.getRole();
        //生成token
        String token = jwtUtils.token(userInfo, JSON.toJSONString(roles));
        //将token存入redis中，并设置token过期时间：3小时
        redisCache.setCacheObject("token:"+token,String.valueOf(authentication),60*60*3, TimeUnit.SECONDS);

        LoginVo userInfoVo = new LoginVo(user, token, roles);

        response.getWriter().write(JSON.toJSONString(ResultUtils.success(Code.LOGIN_SUCCESS, userInfoVo, "登录成功")));
        log.info("用户{}登录成功！", user.getOpenid());
    }
}
