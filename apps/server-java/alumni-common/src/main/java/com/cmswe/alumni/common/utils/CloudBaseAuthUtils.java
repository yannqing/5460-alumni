package com.cmswe.alumni.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmswe.alumni.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * 微信云托管临时密钥获取工具
 */
@Slf4j
@Component
public class CloudBaseAuthUtils {

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("CloudBaseAuthUtils 初始化成功");
        log.info("临时密钥接口: {}", AUTH_URL);
        log.info("文件元数据接口: {}", METAID_ENCODE_URL);
        log.info("========================================");
    }

    private static final String AUTH_URL = "http://api.weixin.qq.com/_/cos/getauth";
    private static final String METAID_ENCODE_URL = "http://api.weixin.qq.com/_/cos/metaid/encode";

    private final WebClient webClient;

    // 缓存临时密钥
    private volatile TempCredential cachedCredential;
    private volatile long lastFetchTime = 0;

    public CloudBaseAuthUtils() {
        this.webClient = WebClient.builder()
                .baseUrl("http://api.weixin.qq.com")
                .build();
    }

    /**
     * 获取临时密钥（带缓存）
     */
    public TempCredential getTempCredential() {
        // 如果缓存的密钥还有效（距离过期还有 5 分钟以上），直接返回
        if (cachedCredential != null) {
            long now = System.currentTimeMillis() / 1000;
            if (cachedCredential.getExpiredTime() - now > 300) {
                log.debug("使用缓存的临时密钥");
                return cachedCredential;
            }
        }

        // 获取新的临时密钥
        synchronized (this) {
            // 双重检查
            if (cachedCredential != null) {
                long now = System.currentTimeMillis() / 1000;
                if (cachedCredential.getExpiredTime() - now > 300) {
                    return cachedCredential;
                }
            }

            try {
                log.info("从云托管开放接口获取临时密钥");
                String response = webClient.get()
                        .uri(AUTH_URL)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(10))
                        .block();

                if (response == null || response.isEmpty()) {
                    throw new BusinessException("获取临时密钥失败：响应为空");
                }

                JSONObject json = JSON.parseObject(response);

                cachedCredential = new TempCredential();
                cachedCredential.setTmpSecretId(json.getString("TmpSecretId"));
                cachedCredential.setTmpSecretKey(json.getString("TmpSecretKey"));
                cachedCredential.setToken(json.getString("Token"));
                cachedCredential.setExpiredTime(json.getLong("ExpiredTime"));

                lastFetchTime = System.currentTimeMillis();
                log.info("临时密钥获取成功，过期时间: {}", cachedCredential.getExpiredTime());

                return cachedCredential;
            } catch (Exception e) {
                log.error("获取临时密钥失败: {}", e.getMessage(), e);
                throw new BusinessException("获取临时密钥失败: " + e.getMessage());
            }
        }
    }

    /**
     * 获取文件元数据（用于上传）
     *
     * @param openid 用户 openid，管理端传空字符串
     * @param bucket 存储桶名称
     * @param filePath 文件路径（如 /cni-alumni/images/2025/02/25/xxx.jpg）
     * @return 文件元数据 ID
     */
    public String getFileMetaId(String openid, String bucket, String filePath) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("openid", openid == null ? "" : openid);
            requestBody.put("bucket", bucket);
            requestBody.put("paths", new String[]{filePath});

            log.info("获取文件元数据，路径: {}", filePath);

            String response = webClient.post()
                    .uri(METAID_ENCODE_URL)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody.toJSONString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || response.isEmpty()) {
                throw new BusinessException("获取文件元数据失败：响应为空");
            }

            JSONObject json = JSON.parseObject(response);
            int errcode = json.getIntValue("errcode");

            if (errcode != 0) {
                String errmsg = json.getString("errmsg");
                throw new BusinessException("获取文件元数据失败: " + errmsg);
            }

            JSONObject respdata = json.getJSONObject("respdata");
            String metaId = respdata.getJSONArray("x_cos_meta_field_strs").getString(0);

            log.info("文件元数据获取成功: {}", metaId);
            return metaId;

        } catch (Exception e) {
            log.error("获取文件元数据失败: {}", e.getMessage(), e);
            throw new BusinessException("获取文件元数据失败: " + e.getMessage());
        }
    }

    /**
     * 临时密钥对象
     */
    @Data
    public static class TempCredential {
        private String tmpSecretId;
        private String tmpSecretKey;
        private String token;
        private Long expiredTime;
    }
}
