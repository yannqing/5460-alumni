package com.cmswe.alumni.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信云托管对象存储配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudbase.cos")
public class CloudBaseProperties {

    /**
     * 存储桶地域
     */
    private String region;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 访问域名（tcb.qcloud.la 域名）
     */
    private String baseUrl;

    /**
     * 文件上传路径前缀
     */
    private String uploadPath;
}
