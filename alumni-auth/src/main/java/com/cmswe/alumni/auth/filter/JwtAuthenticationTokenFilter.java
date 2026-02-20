package com.cmswe.alumni.auth.filter;

import com.alibaba.fastjson2.JSON;

import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.constant.Constant;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.utils.JwtUtils;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.utils.Tools;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private RedisCache redisCache;
    
    @Resource
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //匿名地址，直接放行
        String requestURI = request.getRequestURI();
        if (Tools.contains(requestURI, Constant.anonymousConstant) || Constant.isMatch(requestURI)) {
            filterChain.doFilter(request,response);
            return;
        }

        String token = request.getHeader("token");
        //验证token的合法性，不报错即合法

        Object redisTokenObj = redisCache.getCacheObject("token:" + token);
        
        if (redisTokenObj == null) {
            response.setStatus(500);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSON.toJSONString(ResultUtils.failure(Code.TOKEN_EXPIRE, null, "您已退出，请重新登录")));
            log.error("您已退出，请重新登录");
            return;
        }

        if (token!=null){
            try {
                //验证token的合法性，不抛异常则合法
                log.info("开始验证token: {}", token.substring(0, Math.min(50, token.length())) + "...");
                jwtUtils.tokenVerify(token);
                log.info("token格式验证通过");
                
                //从token中获取到用户的信息，以及对应用户的权限信息
                WxUser wxUser = jwtUtils.getUserFromToken(token);
                log.info("成功从token获取用户信息: wxId={}, openId={}", wxUser.getWxId(), wxUser.getOpenid());

                // 将 WxUser 包装成 SecurityUser
                SecurityUser securityUser = new SecurityUser(wxUser);

//                List<String> userAuthorization = jwtUtils.getUserAuthorizationFromToken(token);
//                List<SimpleGrantedAuthority> authorities = userAuthorization.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                //放行后面的用户名密码过滤器，使用 SecurityUser 作为 principal
//                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user,null,authorities);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(securityUser, null, null);
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                log.info("JWT验证成功，用户: wxId={}, openId={}", wxUser.getWxId(), wxUser.getOpenid());
            }catch (Exception e){
                response.setStatus(200);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JSON.toJSONString(ResultUtils.failure(Code.TOKEN_AUTHENTICATE_FAILURE,null,"非法token")));
                log.error("非法token({}) - 错误详情: {}", token, e.getMessage());
                return;
            }
        }
        filterChain.doFilter(request,response);
    }
}
