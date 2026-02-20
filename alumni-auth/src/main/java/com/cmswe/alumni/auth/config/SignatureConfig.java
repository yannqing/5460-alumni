package com.cmswe.alumni.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 签名验证配置类
 * 从 application.yaml 读取签名相关配置
 *
 * 注意：白名单路径已统一在 Constant.java 中定义
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "api.signature")
public class SignatureConfig {

    /**
     * 签名密钥（从配置文件或环境变量读取）
     * 生产环境必须使用环境变量！
     */
    private String secret;

    /**
     * 时间容差（毫秒），默认5分钟
     * 只接受 ±5分钟 内的请求
     */
    private Long timeTolerance = 300000L;

    /**
     * 是否启用签名验证
     * 默认启用，可在开发环境关闭
     */
    private Boolean enabled = true;

    /**
     * 开发模式（允许使用特殊nonce跳过签名验证）
     * 默认关闭，仅在开发环境启用
     */
    private Boolean devMode = false;

    /**
     * 开发模式下的特殊nonce值（用于跳过签名验证）
     * 默认值：mock, dev, test
     */
    private List<String> devModeNonces = new ArrayList<>(List.of("mock", "dev", "test"));
}
