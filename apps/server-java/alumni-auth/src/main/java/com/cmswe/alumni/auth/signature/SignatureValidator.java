package com.cmswe.alumni.auth.signature;

import com.cmswe.alumni.auth.config.SignatureConfig;
import com.cmswe.alumni.redis.utils.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 签名验证器
 *
 * 功能：
 * 1. 验证请求签名是否正确（防篡改）
 * 2. 验证请求时间戳是否在有效期内（防重放）
 * 3. 验证 nonce 是否已使用（防重放）
 */
@Slf4j
@Component
public class SignatureValidator {

    @Resource
    private RedisCache redisCache;

    @Resource
    private SignatureConfig signatureConfig;

    /**
     * 验证请求签名
     *
     * @param params 请求参数（包含 timestamp, nonce, signature）
     * @return true-验证通过, false-验证失败
     */
    public boolean validateSignature(Map<String, String> params) {

        try {
            // 1. 提取必需参数
            String clientSignature = params.get("signature");
            String timestamp = params.get("timestamp");
            String nonce = params.get("nonce");

            // 1.1 参数完整性检查
            if (clientSignature == null || clientSignature.isEmpty()) {
                log.warn("[签名验证] 缺少 signature 参数");
                return false;
            }

            if (timestamp == null || timestamp.isEmpty()) {
                log.warn("[签名验证] 缺少 timestamp 参数");
                return false;
            }

            if (nonce == null || nonce.isEmpty()) {
                log.warn("[签名验证] 缺少 nonce 参数");
                return false;
            }

            // 2. 验证时间戳（防重放攻击）
            if (!validateTimestamp(timestamp)) {
                return false;
            }

            // 3. 验证 nonce（防重放攻击）
            if (!validateNonce(nonce)) {
                return false;
            }

            // 4. 计算服务端签名
            String serverSignature = calculateSignature(params);

            // 5. 比对签名（使用常量时间比较，防时序攻击）
            boolean valid = constantTimeEquals(serverSignature, clientSignature);

            if (valid) {
                // 验证通过，记录 nonce
                recordNonce(nonce);
                log.debug("[签名验证] 验证成功, nonce={}", nonce);
            } else {
                log.warn("[签名验证] 签名不匹配, nonce={}, expected={}, actual={}",
                    nonce, serverSignature, clientSignature);
            }

            return valid;

        } catch (NumberFormatException e) {
            log.error("[签名验证] 时间戳格式错误", e);
            return false;
        } catch (Exception e) {
            log.error("[签名验证] 验证过程异常", e);
            return false;
        }
    }

    /**
     * 验证时间戳（±5分钟内有效）
     */
    private boolean validateTimestamp(String timestamp) {
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            long timeDiff = Math.abs(currentTime - requestTime);

            long tolerance = signatureConfig.getTimeTolerance();

            if (timeDiff > tolerance) {
                log.warn("[签名验证] 请求时间戳超出允许范围: {}ms (最大允许: {}ms)",
                    timeDiff, tolerance);
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            log.error("[签名验证] 时间戳格式错误: {}", timestamp);
            return false;
        }
    }

    /**
     * 验证 nonce 是否已被使用
     */
    private boolean validateNonce(String nonce) {
        String nonceKey = "api:nonce:" + nonce;

        try {
            Object exists = redisCache.getCacheObject(nonceKey);

            if (exists != null) {
                log.warn("[签名验证] 检测到重放攻击，nonce 已使用: {}", nonce);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("[签名验证] Redis查询nonce失败，降级处理：跳过nonce验证", e);
            // 降级：Redis不可用时，仅验证时间戳
            return true;
        }
    }

    /**
     * 记录 nonce（防止重复使用）
     */
    private void recordNonce(String nonce) {
        String nonceKey = "api:nonce:" + nonce;

        try {
            // 有效期：时间容差的2倍（确保过期请求的nonce也能被拦截）
            long expireTime = signatureConfig.getTimeTolerance() * 2;

            redisCache.setCacheObject(nonceKey, "1", (int) expireTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("[签名验证] 记录nonce到Redis失败", e);
            // 不抛异常，避免影响正常业务
        }
    }

    /**
     * 计算请求签名
     *
     * 步骤：
     * 1. 移除 signature 参数
     * 2. 参数按 key 字典序排序
     * 3. 拼接成 key1=value1&key2=value2 格式
     * 4. 追加密钥 &key=SECRET
     * 5. HMAC-SHA256 计算签名
     *
     * @param params 请求参数
     * @return 签名字符串（十六进制小写）
     */
    public String calculateSignature(Map<String, String> params) {

        // 1. 使用 TreeMap 自动按 key 排序
        Map<String, String> sortedParams = new TreeMap<>(params);

        // 2. 移除 signature 参数本身
        sortedParams.remove("signature");

        // 3. 拼接参数
        StringBuilder signContent = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 跳过空值
            if (value == null || value.isEmpty()) {
                continue;
            }

            signContent.append(key)
                      .append("=")
                      .append(value)
                      .append("&");
        }

        // 4. 追加密钥
        signContent.append("key=").append(signatureConfig.getSecret());

        // 5. 计算 HMAC-SHA256
        String signature = hmacSha256(signContent.toString());

        log.debug("[签名计算] 待签名字符串: {}", signContent);
        log.debug("[签名计算] 计算结果: {}", signature);

        return signature;
    }

    /**
     * HMAC-SHA256 加密
     *
     * @param data 待加密数据
     * @return 十六进制签名字符串（小写）
     */
    private String hmacSha256(String data) {
        try {
            // 初始化 HMAC-SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                signatureConfig.getSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKey);

            // 计算哈希值
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            return bytesToHex(hash);

        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 加密失败", e);
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * 常量时间字符串比较（防时序攻击）
     *
     * 原理：无论字符串是否相等，比较时间都相同
     * 防止攻击者通过响应时间判断签名的正确性
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }
}
