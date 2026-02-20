package com.cmswe.alumni.auth;


import com.cmswe.alumni.common.constant.Constant;
import com.cmswe.alumni.auth.filter.JwtAuthenticationTokenFilter;
import com.cmswe.alumni.auth.filter.RequestSignatureFilter;
import com.cmswe.alumni.auth.handler.MyLogoutSuccessHandler;
import com.cmswe.alumni.redis.utils.RedisCache;
import jakarta.annotation.Resource;
import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Resource
    private RedisCache redisCache;

    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //允许一些请求匿名访问，其他的均需要认证
        http.authorizeHttpRequests((authorize)->authorize
                .requestMatchers(Constant.anonymousConstant)
                .permitAll()
                .requestMatchers(Constant.anonymousMatch)
                .permitAll()
                // 添加这一行，直接允许定时任务的请求
                .requestMatchers("/scheduled/**").permitAll()
                .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                .anyRequest()
                .authenticated()
        );

        //关闭session
        http.sessionManagement((sessionManagement)->
                sessionManagement
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    // 添加这行：支持异步请求
                    .enableSessionUrlRewriting(true)
        );

        // ⭐⭐⭐ 关键配置：过滤器顺序 ⭐⭐⭐
        // 执行顺序：
        // 1. RequestSignatureFilter (签名验证，@Order(1)自动排在最前)
        // 2. JwtAuthenticationTokenFilter (JWT认证)
        // 3. Spring Security 其他过滤器
        // 注：RequestSignatureFilter 通过 @Order(1) 自动排在最前，无需手动添加

        //设置用户名密码认证前的jwt过滤器
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        //csrf
        http.csrf(AbstractHttpConfigurer::disable);

        // 禁用表单登录和HTTP Basic认证
        // 微信小程序使用自定义登录逻辑（在 Controller 的业务层处理）
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        //设置退出logout过滤器
        http.logout((logout)->logout
                .logoutUrl("/auth/logout")
                .logoutSuccessHandler(new MyLogoutSuccessHandler(redisCache))
        );

        //关闭跨域拦截--适用于前后端分离，另创建跨域拦截的类
        http.cors(Customizer.withDefaults());

        return http.build();
    }
    /**
     * 对密码进行BCrypt加密
     * @return 返回 BCryptEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("Access Denied: {}", accessDeniedException.getMessage());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write("{\"message\": \"Access Denied\"}");
        };
    }
}
