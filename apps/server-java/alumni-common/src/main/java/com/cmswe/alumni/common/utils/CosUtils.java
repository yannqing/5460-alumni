package com.cmswe.alumni.common.utils;

import com.cmswe.alumni.common.config.CloudBaseProperties;
import com.cmswe.alumni.common.exception.BusinessException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 微信云托管对象存储工具类
 * 使用临时密钥和文件元数据
 */
@Slf4j
@Component
public class CosUtils {

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("CosUtils 初始化成功");
        log.info("存储桶: {}", cloudBaseProperties.getBucketName());
        log.info("地域: {}", cloudBaseProperties.getRegion());
        log.info("========================================");
    }

    @Resource
    private CloudBaseProperties cloudBaseProperties;

    @Resource
    private CloudBaseAuthUtils cloudBaseAuthUtils;

    /**
     * 获取 COS 客户端（使用临时密钥）
     */
    private COSClient getCOSClient() {
        try {
            // 获取临时密钥
            CloudBaseAuthUtils.TempCredential credential = cloudBaseAuthUtils.getTempCredential();

            // 使用临时密钥创建凭证
            COSCredentials cred = new BasicSessionCredentials(
                    credential.getTmpSecretId(),
                    credential.getTmpSecretKey(),
                    credential.getToken()
            );

            // 设置地域
            Region region = new Region(cloudBaseProperties.getRegion());
            ClientConfig clientConfig = new ClientConfig(region);

            return new COSClient(cred, clientConfig);
        } catch (Exception e) {
            log.error("创建 COS 客户端失败: {}", e.getMessage());
            throw new BusinessException("创建 COS 客户端失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到 COS（云托管方式，带文件元数据）
     *
     * @param file 文件
     * @param subPath 子路径（如 images, audios）
     * @param newFileName 新文件名
     * @param openid 用户 openid，管理端传 null 或空字符串
     * @return 文件访问路径（相对路径）
     * @throws IOException IO异常
     */
    public String uploadFile(MultipartFile file, String subPath, String newFileName, String openid) throws IOException {
        // 生成基于日期的目录结构
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 构建完整的文件路径: uploadPath/subPath/yyyy/MM/dd/newFileName
        String filePath = cloudBaseProperties.getUploadPath() + "/" + subPath + "/" + datePath + "/" + newFileName;

        COSClient cosClient = null;
        try {
            // 获取文件元数据
            String metaId = cloudBaseAuthUtils.getFileMetaId(
                    openid,
                    cloudBaseProperties.getBucketName(),
                    "/" + filePath
            );

            // 获取 COS 客户端
            cosClient = getCOSClient();

            try (InputStream inputStream = file.getInputStream()) {
                // 设置文件元数据
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());
                // 添加文件元数据 header（关键！）
                metadata.setHeader("x-cos-meta-fileid", metaId);

                // 创建上传请求
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        cloudBaseProperties.getBucketName(),
                        filePath,
                        inputStream,
                        metadata
                );

                // 上传文件
                PutObjectResult result = cosClient.putObject(putObjectRequest);
                log.info("文件上传到云托管 COS 成功: {}, ETag: {}", filePath, result.getETag());

                // 返回访问路径（相对路径，用于保存到数据库）
                return "/" + filePath;
            }
        } catch (Exception e) {
            log.error("文件上传到云托管 COS 失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        } finally {
            // 关闭客户端
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    /**
     * 生成文件的完整访问 URL
     *
     * @param filePath 文件路径（相对路径）
     * @return 完整的访问 URL（使用 tcb.qcloud.la 域名）
     */
    public String getFileUrl(String filePath) {
        // 确保 baseUrl 不以 / 结尾，filePath 以 / 开头
        String baseUrl = cloudBaseProperties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        return baseUrl + filePath;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     */
    public void deleteFile(String filePath) {
        COSClient cosClient = null;
        try {
            // 移除开头的 /
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }

            cosClient = getCOSClient();
            cosClient.deleteObject(cloudBaseProperties.getBucketName(), filePath);
            log.info("文件从云托管 COS 删除成功: {}", filePath);
        } catch (Exception e) {
            log.error("文件从云托管 COS 删除失败: {}", e.getMessage());
            throw new BusinessException("文件删除失败: " + e.getMessage());
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }
}
