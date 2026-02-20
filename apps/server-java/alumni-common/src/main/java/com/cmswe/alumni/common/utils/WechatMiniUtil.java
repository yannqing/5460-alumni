package com.cmswe.alumni.common.utils;

import com.cmswe.alumni.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 微信小程序工具类
 * 用于调用微信官方API
 */
@Slf4j
@Component
public class WechatMiniUtil {

    @Value("${wechat.mini.app-id}")
    private String appId;

    @Value("${wechat.mini.secret}")
    private String secret;

    @Resource
    private HttpClientUtil httpClientUtil;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 微信登录凭证校验（code2session）
     * 官方文档: <a href=
     * "https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html">...</a>
     * k
     * l @param code 小程序端通过 wx.login() 获取的临时登录凭证
     * 
     * @return 包含 openid、session_key、unionid（如果绑定了开放平台）的Map
     */

    public Map<String, Object> code2session(String code) {
        // 构建请求URL
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, secret, code);

        try {
            log.info("调用微信code2session接口, code={}", code);

            // 发送HTTP GET请求
            String response = httpClientUtil.get(url, String.class);
            log.info("微信code2session响应: {}", response);

            // 解析响应JSON
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            // 检查是否有错误
            if (result.containsKey("errcode")) {
                Integer errcode = (Integer) result.get("errcode");
                if (errcode != 0) {
                    String errmsg = (String) result.get("errmsg");
                    log.error("微信code2session调用失败, errcode={}, errmsg={}", errcode, errmsg);
                    throw new BusinessException(500, "微信登录失败: " + errmsg);
                }
            }

            return result;

        } catch (Exception e) {
            log.error("调用微信code2session接口异常", e);
            throw new BusinessException(500, "调用微信API失败: " + e.getMessage());
        }
    }

    /**
     * 获取小程序码（无限数量，菊花码）
     * 文档：<a href=
     * "https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/qr-code/wxacode.getUnlimited.html">...</a>
     *
     * @param scene 参数（最大32字符）
     * @param page  页面路径（开头不能带/）
     * @param width 宽度
     * @return Base64编码的图片字符串
     */
    public String createWxaCodeUnlimit(String scene, String page, int width) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new BusinessException(500, "获取微信Access Token失败");
        }

        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;

        try {
            // 构建请求参数
            // check_path=false 允许生成未发布页面的码，防止开发环境报错
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("scene", scene);
            params.put("page", page);
            params.put("width", width);
            params.put("check_path", false);
            params.put("env_version", "release"); // 正式版为 release，体验版为 trial，开发版为 develop

            log.info("调用微信生成小程序码接口, scene={}, page={}", scene, page);

            // 获取二进制图片数据
            byte[] imageBytes = httpClientUtil.post(url, params, byte[].class);

            if (imageBytes == null || imageBytes.length == 0) {
                throw new BusinessException(500, "生成小程序码失败：返回为空");
            }

            // 检查是否返回了JSON错误信息（如果是JSON则说明出错了）
            // 简单的检查方法：看首字节是不是 '{' (123)
            if (imageBytes.length < 1024 && imageBytes[0] == 123) {
                String errorResponse = new String(imageBytes);
                log.error("微信生成小程序码失败: {}", errorResponse);
                throw new BusinessException(500, "生成小程序码失败: " + errorResponse);
            }

            // 转Base64
            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            return "data:image/png;base64," + base64;

        } catch (Exception e) {
            log.error("生成小程序码异常", e);
            throw new BusinessException(500, "生成小程序码失败: " + e.getMessage());
        }
    }

    // Access Token 缓存
    private static class AccessToken {
        String token;
        long expireTime;

        public AccessToken(String token, int expiresIn) {
            this.token = token;
            this.expireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L; // 提前5分钟
        }

        public boolean isValid() {
            return System.currentTimeMillis() < expireTime;
        }
    }

    private AccessToken cachedToken;

    /**
     * 获取 Access Token
     */
    private synchronized String getAccessToken() {
        if (cachedToken != null && cachedToken.isValid()) {
            return cachedToken.token;
        }

        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, secret);

        try {
            log.info("获取微信Access Token");
            String response = httpClientUtil.get(url, String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (result.containsKey("access_token")) {
                String token = (String) result.get("access_token");
                Integer expiresIn = (Integer) result.get("expires_in");
                cachedToken = new AccessToken(token, expiresIn);
                return token;
            } else {
                log.error("获取Access Token失败: {}", response);
                return null;
            }
        } catch (Exception e) {
            log.error("获取Access Token异常", e);
            return null;
        }
    }
}
