package com.cmswe.alumni.service.user.service.impl;

import com.cmswe.alumni.api.user.WechatApiService;
import com.cmswe.alumni.common.utils.HttpClientUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WechatApiServiceImpl implements WechatApiService {

    @Value("${wechat.appid:wx245633547ddb04b2}")
    private String appId;

    @Value("${wechat.appSecret:52ec76cee5cf6537bbe9c253fa54ced5}")
    private String secret;

    // 微信API基础URL（云托管环境使用 http，其他环境使用 https）
    @Value("${wechat.api.base-url:https://api.weixin.qq.com}")
    private String wechatApiBaseUrl;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private HttpClientUtil httpClientUtil;

    // 错误码重试策略
    private static final int MAX_RETRY_TIMES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    // 缓存access_token，避免频繁请求
    private final Map<String, AccessToken> tokenCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> getPhoneNumber(String code) {
        try {
            log.info("获取手机号信息，code: {}", code);

            // 先测试access_token是否有效
            if (!testAccessToken()) {
                log.error("access_token测试失败，可能AppID/AppSecret配置有问题");
                return null;
            }

            // 获取access_token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取access_token失败");
                return null;
            }

            // API接口地址 - 2024年微信官方确认的地址
            String url = String.format(
                    "%s/wxa/business/getuserphonenumber?access_token=%s",
                    wechatApiBaseUrl, accessToken
            );

            // 使用原生HTTP连接调用微信API（解决RestTemplate兼容性问题）
            String response = callWechatApiWithNativeHttp(url, code);
            log.debug("手机号API响应: {}", response);

            JsonNode jsonNode = objectMapper.readTree(response);

            // 检查错误
            if (jsonNode.has("errcode")) {
                int errCode = jsonNode.get("errcode").asInt();
                String errMsg = jsonNode.get("errmsg").asText();

                // 如果errcode为0，表示成功
                if (errCode == 0) {
                    log.debug("手机号API成功: {} - {}", errCode, errMsg);
                } else {
                    log.error("手机号API错误: {} - {}", errCode, errMsg);

                    // 对于40013错误，提供详细的排查信息
                    if (errCode == 40013) {
                        log.error("40013错误排查信息:");
                        log.error("1. AppID: {}", appId);
                        log.error("2. Access Token有效性: 已通过测试API验证");
                        log.error("3. 请求URL: {}", url);
                        log.error("4. Code长度: {} 位", code.length());
                        log.error("5. 可能原因:");
                        log.error("   - 小程序可能没有获取手机号的权限（即使有试用额度）");
                        log.error("   - 小程序可能不是企业认证类型");
                        log.error("   - 接口可能有地域或其他限制");
                        log.error("   - 建议检查微信公众平台的接口权限设置");
                    }

                    // 如果是access_token过期，清除缓存并重试
                    if (errCode == 40001 || errCode == 42001) {
                        log.info("access_token过期，清除缓存重试");
                        tokenCache.clear();
                        return getPhoneNumber(code); // 重试一次
                    }

                    return null;
                }
            }

            // 解析手机号信息
            JsonNode phoneInfoNode = jsonNode.get("phone_info");
            if (phoneInfoNode == null) {
                log.error("未找到phone_info");
                return null;
            }

            Map<String, Object> phoneInfo = new HashMap<>();
            phoneInfo.put("phoneNumber", phoneInfoNode.get("phoneNumber").asText());
            phoneInfo.put("purePhoneNumber", phoneInfoNode.get("purePhoneNumber").asText());
            phoneInfo.put("countryCode", phoneInfoNode.get("countryCode").asText());

            log.info("成功获取手机号: {}", maskPhone(phoneInfo.get("phoneNumber").toString()));
            return phoneInfo;

        } catch (Exception e) {
            log.error("获取手机号异常", e);
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                org.springframework.web.client.HttpClientErrorException httpError = (org.springframework.web.client.HttpClientErrorException) e;
                log.error("HTTP错误状态码: {}", httpError.getStatusCode());
                log.error("HTTP错误响应体: {}", httpError.getResponseBodyAsString());
                log.error("HTTP错误响应头: {}", httpError.getResponseHeaders());
            }
            return null;
        }
    }

    /**
     * 获取微信session信息（openid和session_key）
     * 优化错误处理和重试机制
     */
    public Map<String, String> getSessionInfo(String code) {
        for (int retry = 0; retry < MAX_RETRY_TIMES; retry++) {
            try {
                log.info("获取微信session信息，code: {}", code);

                String url = String.format(
                        "%s/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                        wechatApiBaseUrl, appId, secret, code
                );

                String response = httpClientUtil.get(url, String.class);
                log.debug("微信session响应: {}", response);

                JsonNode jsonNode = objectMapper.readTree(response);

                // 检查错误
                if (jsonNode.has("errcode")) {
                    int errCode = jsonNode.get("errcode").asInt();
                    String errMsg = jsonNode.get("errmsg").asText();

                    log.error("微信session错误: {} - {}", errCode, errMsg);

                    // 如果是40029，不立即重试，等待一段时间
                    if (errCode == 40029 && retry < MAX_RETRY_TIMES - 1) {
                        log.info("40029错误，等待{}ms后重试", RETRY_DELAY_MS * (retry + 1));
                        Thread.sleep(RETRY_DELAY_MS * (retry + 1));
                        continue;
                    }

                    return null;
                }

                // 成功获取session信息
                Map<String, String> sessionInfo = new HashMap<>();
                sessionInfo.put("openid", jsonNode.get("openid").asText());
                sessionInfo.put("session_key", jsonNode.get("session_key").asText());

                if (jsonNode.has("unionid")) {
                    sessionInfo.put("unionid", jsonNode.get("unionid").asText());
                }

                log.info("成功获取session信息，openid: {}", maskOpenid(sessionInfo.get("openid")));
                return sessionInfo;

            } catch (Exception e) {
                log.error("获取微信session异常，重试次数: {}", retry + 1, e);
                if (retry == MAX_RETRY_TIMES - 1) {
                    return null;
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 获取access_token，带缓存机制
     */
    private String getAccessToken() {
        try {
            // 检查缓存
            AccessToken cached = tokenCache.get("access_token");
            if (cached != null && cached.isValid()) {
                log.debug("使用缓存的access_token");
                return cached.getToken();
            }

            log.info("获取新的access_token");

            String url = String.format(
                    "%s/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    wechatApiBaseUrl, appId, secret
            );

            String response = httpClientUtil.get(url, String.class);
            log.debug("access_token响应: {}", response);

            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("errcode")) {
                log.error("获取access_token错误: {}", jsonNode.get("errmsg").asText());
                return null;
            }

            String token = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();

            // 缓存token，提前5分钟过期
            AccessToken accessToken = new AccessToken(token, expiresIn - 300);
            tokenCache.put("access_token", accessToken);

            log.info("成功获取access_token，有效期: {}秒", expiresIn);
            log.info("Access Token详情: appid=[{}], appid长度={}, token前10位={}",
                    appId, appId.length(), token.substring(0, Math.min(10, token.length())));

            // 检查AppID格式
            if (appId.contains("\"") || appId.contains("'") || appId.contains("\n") || appId.contains("\r")) {
                log.error("AppID格式异常，包含特殊字符: {}", appId.replace("\n", "\\n").replace("\r", "\\r"));
            }

            return token;

        } catch (Exception e) {
            log.error("获取access_token异常", e);
            return null;
        }
    }

    /**
     * 测试access_token是否有效（通过调用其他API验证）
     */
    public boolean testAccessToken() {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                return false;
            }

            // 使用一个简单的API来测试token是否有效
            String url = String.format(
                    "%s/cgi-bin/get_api_domain_ip?access_token=%s",
                    wechatApiBaseUrl, accessToken
            );

            String response = httpClientUtil.get(url, String.class);
            log.info("测试API响应: {}", response);

            JsonNode jsonNode = objectMapper.readTree(response);
            // 如果没有errcode字段，说明调用成功
            if (!jsonNode.has("errcode")) {
                log.info("access_token测试成功 - AppID/AppSecret配置正确");
                return true;
            }
            return jsonNode.get("errcode").asInt() == 0;

        } catch (Exception e) {
            log.error("测试access_token失败", e);
            return false;
        }
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * openid脱敏
     */
    private String maskOpenid(String openid) {
        if (openid == null || openid.length() < 10) {
            return openid;
        }
        return openid.substring(0, 6) + "****" + openid.substring(openid.length() - 4);
    }

    /**
     * 使用原生HTTP连接调用微信API（备用方案）
     */
    private String callWechatApiWithNativeHttp(String urlStr, String code) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            // 设置请求方法
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 设置请求头 - 按照微信官方要求
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Java/1.8");

            // 构建请求体
            String jsonBody = String.format("{\"code\":\"%s\"}", code);
            log.info("请求URL: {}", urlStr);
            log.info("请求体: {}", jsonBody);

            // 写入请求体
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 读取响应
            int responseCode = conn.getResponseCode();
            log.info("HTTP响应码: {}", responseCode);

            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                log.error("HTTP错误响应码: {}", responseCode);
            }

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            br.close();

            log.info("API响应内容: {}", response.toString());

            return response.toString();

        } finally {
            conn.disconnect();
        }
    }

    /**
     * access_token缓存类
     */
    private static class AccessToken {
        private final String token;
        private final long expiresAt;

        public AccessToken(String token, int expiresIn) {
            this.token = token;
            this.expiresAt = System.currentTimeMillis() + expiresIn * 1000L;
        }

        public String getToken() {
            return token;
        }

        public boolean isValid() {
            return System.currentTimeMillis() < expiresAt;
        }
    }
}
