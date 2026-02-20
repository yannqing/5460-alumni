package com.cmswe.alumni.auth.handler;

import com.alibaba.fastjson2.JSON;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.common.utils.ResultUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

@Slf4j
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {

    private final RedisCache redisCache;

    public MyLogoutSuccessHandler(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    /**
     * 退出成功处理器：删除redis中的token即可
     * @param request 会话请求
     * @param response 会话响应
     * @param authentication 认证信息
     * @throws IOException IO 异常
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String token = request.getHeader("token");
        redisCache.deleteObject("token:"+token);

        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(ResultUtils.success(Code.LOGOUT_SUCCESS,null,"退出成功！")));
        log.info("退出成功!");
    }
}
