package com.cmswe.alumni.common.utils;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;
    
    /**
     * JWT专用的ObjectMapper
     * 注意：这与全局的ObjectMapper配置不同，专门针对JWT内部序列化场景：
     * 1. JWT需要处理时间戳格式（节省token空间）
     * 2. Web API使用可读的日期字符串格式
     * 3. 两种配置服务于不同的业务场景，配置重复是合理的
     */
    private static final ObjectMapper JWT_OBJECT_MAPPER = createJwtObjectMapper();

    @Value("${jwt.expire-times}")
    private Long expireDays;

    @Resource
    private RedisCache redisCache;

    /**
     * 创建专门用于JWT的ObjectMapper配置
     * 支持时间戳和字符串两种时间格式的反序列化
     */
    private static ObjectMapper createJwtObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 基础配置
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 配置Java8时间类型处理
        com.fasterxml.jackson.datatype.jsr310.JavaTimeModule javaTimeModule = 
            new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule();
        
        // 添加时间戳支持的LocalDateTime反序列化器
        javaTimeModule.addDeserializer(java.time.LocalDateTime.class,
                new com.fasterxml.jackson.databind.JsonDeserializer<>() {
                    @Override
                    public java.time.LocalDateTime deserialize(
                            com.fasterxml.jackson.core.JsonParser p,
                            com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {

                        if (p.hasToken(com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT)) {
                            // JWT场景：处理时间戳（毫秒）- 节省token空间
                            long timestamp = p.getLongValue();
                            return java.time.LocalDateTime.ofInstant(
                                    java.time.Instant.ofEpochMilli(timestamp),
                                    java.time.ZoneId.systemDefault()
                            );
                        } else if (p.hasToken(com.fasterxml.jackson.core.JsonToken.VALUE_STRING)) {
                            // 兼容场景：处理字符串格式
                            String dateString = p.getText();
                            if (dateString == null || dateString.trim().isEmpty()) {
                                return null;
                            }
                            try {
                                return java.time.LocalDateTime.parse(dateString.trim(),
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            } catch (java.time.format.DateTimeParseException e) {
                                try {
                                    return java.time.LocalDateTime.parse(dateString.trim());
                                } catch (java.time.format.DateTimeParseException e2) {
                                    throw new java.io.IOException("JWT时间解析失败: " + dateString, e2);
                                }
                            }
                        } else {
                            throw new java.io.IOException("JWT不支持的时间数据类型: " + p.getCurrentToken());
                        }
                    }
                });
        
        // 添加时间戳支持的LocalDate反序列化器
        javaTimeModule.addDeserializer(java.time.LocalDate.class,
                new com.fasterxml.jackson.databind.JsonDeserializer<>() {
                    @Override
                    public java.time.LocalDate deserialize(
                            com.fasterxml.jackson.core.JsonParser p,
                            com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {

                        if (p.hasToken(com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT)) {
                            // JWT场景：处理时间戳（毫秒）- 节省token空间
                            long timestamp = p.getLongValue();
                            return java.time.LocalDate.ofInstant(
                                    java.time.Instant.ofEpochMilli(timestamp),
                                    java.time.ZoneId.systemDefault()
                            );
                        } else if (p.hasToken(com.fasterxml.jackson.core.JsonToken.VALUE_STRING)) {
                            // 兼容场景：处理字符串格式
                            String dateString = p.getText();
                            if (dateString == null || dateString.trim().isEmpty()) {
                                return null;
                            }
                            try {
                                return java.time.LocalDate.parse(dateString.trim(),
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            } catch (java.time.format.DateTimeParseException e) {
                                try {
                                    return java.time.LocalDate.parse(dateString.trim());
                                } catch (java.time.format.DateTimeParseException e2) {
                                    throw new java.io.IOException("JWT日期解析失败: " + dateString, e2);
                                }
                            }
                        } else {
                            throw new java.io.IOException("JWT不支持的日期数据类型: " + p.getCurrentToken());
                        }
                    }
                });
        
        mapper.registerModule(javaTimeModule);
        return mapper;
    }

    /**
     *  根据认证信息Authentication生成JWT token
     */
    public String token(Authentication authentication){
        String token =  JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + expireDays))  //设置过期时间:单位毫秒
                .withAudience(JSON.toJSONString(authentication)) //设置接受方信息，一般时登录用户
                .sign(Algorithm.HMAC256(secret));

        redisCache.setCacheObject("token:" + token, authentication);
        return token;
    }

    /**
     * 根据用户详细信息，权限信息  生成token
     * @param userInfo 用户详细信息，密码为空
     * @param roles 用户角色
     * @return 返回token
     */

    public String token(String userInfo, String roles){
        String token = JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + expireDays))  //设置过期时间:单位毫秒
                .withClaim("userInfo",userInfo)
                .withClaim("roles",roles)
                .sign(Algorithm.HMAC256(secret));

        redisCache.setCacheObject("token:" + token, userInfo);
        return token;
    }

    /**
     * 根据用户详细信息，权限信息  生成token
     * @param userInfo 用户详细信息，密码为空
     * @param roles 用户角色
     * @param time 过期时间（单位毫秒）
     * @return 返回token
     */

    public String token(String userInfo, String roles, Long time){
        String token = JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + time))  //设置过期时间:单位毫秒
                .withClaim("userInfo",userInfo)
                .withClaim("roles",roles)
                .sign(Algorithm.HMAC256(secret));

        redisCache.setCacheObject("token:" + token, userInfo);
        return token;
    }

    /**
     * 根据指定日期返回token
     * @param authentication 认证信息
     * @param time 过期时间 单位毫秒
     * @return 返回token
     */
    public String token(Authentication authentication, Long time){
        String token =  JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + time))  //设置过期时间:单位毫秒
                .withAudience(JSON.toJSONString(authentication)) //设置接受方信息，一般时登录用户
                .sign(Algorithm.HMAC256(secret));

        redisCache.setCacheObject("token:" + token, authentication);
        return token;
    }

    /**
     * 验证token合法性
     */
    public void tokenVerify(String token){
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build();
        jwtVerifier.verify(token);//没报错说明验证成功
//        log.info("token校验成功！");
    }

    /**
     * 刷新token
     * @param token 用户 token
     * @return 刷新后的 token
     */
    public String refreshToken(String token){
        this.tokenVerify(token);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return this.token(authentication);
    }

    public WxUser getUserFromToken(String token) throws JsonProcessingException {
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(token);
        String userInfo =  decodedJWT.getClaim("userInfo").asString();
        
        // 添加调试日志
        log.info("Parsing userInfo from token: {}", userInfo);
        
        // 使用JWT专用的ObjectMapper，支持时间戳格式
        return JWT_OBJECT_MAPPER.readValue(userInfo, WxUser.class);
    }

    public Long getUserIdFromToken(String token) throws JsonProcessingException {
        WxUser loginUser = getUserFromToken(token);
        return loginUser.getWxId();
    }

    public List<String> getUserAuthorizationFromToken(String token){
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            return decodedJWT.getClaim("authList").asList(String.class);
        } catch (Exception e) {
            return null;
        }
    }
}