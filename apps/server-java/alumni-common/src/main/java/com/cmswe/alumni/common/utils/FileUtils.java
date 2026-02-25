package com.cmswe.alumni.common.utils;

import com.cmswe.alumni.common.entity.Files;
import com.cmswe.alumni.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.cmswe.alumni.common.constant.Constant.*;
import static com.cmswe.alumni.common.constant.Constant.ALLOWED_AUDIO_EXTENSIONS;
import static com.cmswe.alumni.common.constant.Constant.ALLOWED_RESUME_EXTENSIONS;
import static com.cmswe.alumni.common.constant.Constant.AUDIO_FILE_TYPE;

@Slf4j
@Component
public class FileUtils {

    @Value("${file.upload-common-url}")
    private String uploadCommonPath;

    @Value("${file.upload-prefix-url}")
    private String uploadPrefixPath;

    @Value("${file.storage-type:local}")
    private String storageType;

    @Autowired(required = false)
    private CosUtils cosUtils;

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("FileUtils 初始化");
        log.info("storage-type: {}", storageType);
        log.info("cosUtils 是否注入: {}", cosUtils != null);
        log.info("========================================");
    }

    /**
     * 上传文件通用工具类 TODO 缺少大文件处理（分片处理）
     * @param file
     * @param subPath
     * @param type
     * @param fileName
     * @param newFileName
     * @param fileExtension
     * @param openid 用户 openid（云托管需要，本地存储忽略）
     * @return
     * @throws IOException
     */
    public String uploadFile(MultipartFile file, String subPath, String type, String fileName, String newFileName, String fileExtension, String openid) throws IOException {

        // 验证文件的一级类型
        switch (type) {
            case IMAGE_FILE_TYPE -> {
                if (!ALLOWED_IMAGE_EXTENSIONS.contains(fileExtension)) {
                    throw new BusinessException("文件类型不支持，仅支持以下格式：" + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
                }
                // 验证图片尺寸
//                validateImageDimensions(file);
            }
            case RESUME_FILE_TYPE -> {
                if (!ALLOWED_RESUME_EXTENSIONS.contains(fileExtension)) {
                    throw new BusinessException("文件类型不支持，仅支持以下格式：" + String.join(", ", ALLOWED_RESUME_EXTENSIONS));
                }
            }
            case AUDIO_FILE_TYPE -> {
                if (!ALLOWED_AUDIO_EXTENSIONS.contains(fileExtension)) {
                    throw new BusinessException("文件类型不支持，仅支持以下格式：" + String.join(", ", ALLOWED_AUDIO_EXTENSIONS));
                }
            }
        }

        // 根据存储类型选择存储方式
        if ("cos".equalsIgnoreCase(storageType)) {
            // 使用微信云托管对象存储
            if (cosUtils == null) {
                throw new BusinessException("微信云托管 COS 未配置或初始化失败");
            }
            log.info("使用微信云托管 COS 上传文件: {}", newFileName);
            return cosUtils.uploadFile(file, subPath, newFileName, openid);
        } else {
            // 使用本地文件存储
            log.info("使用本地存储上传文件: {}", newFileName);
            return uploadFileToLocal(file, subPath, newFileName);
        }
    }

    /**
     * 上传文件到本地服务器
     */
    private String uploadFileToLocal(MultipartFile file, String subPath, String newFileName) throws IOException {
        // 生成基于日期的目录结构
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 构建完整的目录路径
        String fullDirectoryPath = uploadCommonPath + File.separator + subPath + File.separator + datePath;

        // 创建目录（如果不存在）
        File uploadDir = new File(fullDirectoryPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                log.info("文件夹{}创建成功", fullDirectoryPath);
            } else {
                log.error("文件夹{}创建失败", fullDirectoryPath);
                throw new BusinessException("文件夹创建失败");
            }
        }

        // 保存文件
        Path filePath = Paths.get(fullDirectoryPath + File.separator + newFileName);
        byte[] bytes = file.getBytes();
        java.nio.file.Files.write(filePath, bytes);

        // 构建访问URL
        String accessUrl = uploadPrefixPath + File.separator + subPath + File.separator + datePath + File.separator + newFileName;

        return accessUrl;
    }

    /**
     * 根据文件名称，路径，下载图片
     * @param files 文件
     * @return 返回文件
     */
    public ResponseEntity<FileSystemResource> downloadFile(Files files) {
        // 如果使用 COS 存储，则重定向到 COS URL
        if ("cos".equalsIgnoreCase(storageType)) {
            if (cosUtils == null) {
                throw new BusinessException("腾讯云 COS 未配置或初始化失败");
            }
            // COS 文件直接通过 URL 访问，这里返回重定向
            // 前端可以直接使用 fileUrl 访问，不需要经过服务器下载
            log.info("COS 文件访问，文件路径: {}", files.getFilePath());
            // 对于 COS 文件，建议前端直接使用 fileUrl 访问
            // 如果必须通过服务器，可以使用 HTTP 302 重定向
            throw new BusinessException("COS 文件请直接使用文件 URL 访问");
        }

        // 本地文件下载
        File imageFile = new File(uploadCommonPath + files.getFilePath().replace(uploadPrefixPath, ""));

        if (imageFile.exists()) {
            // 根据文件扩展名设置正确的 Content-Type
            String contentType = files.getMimeType();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType)); // 设置正确的 Content-Type
            headers.setContentDispositionFormData("inline", files.getFileName()); // 使用 "inline" 而不是 "attachment"

            // 设置缓存相关的头
            headers.setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic().getHeaderValue());
            headers.setExpires(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)); // 7 天缓存

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new FileSystemResource(imageFile));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 验证图片尺寸
     * @param file 图片文件
     * @throws BusinessException 当图片尺寸不符合要求时抛出异常
     */
    private void validateImageDimensions(MultipartFile file) throws BusinessException {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                int maxDimension = Math.max(width, height);
                int minDimension = Math.min(width, height);
                log.info("图片最长边：{}", maxDimension);
                log.info("图片最短边：{}", minDimension);

                if (maxDimension > 8000) {
                    throw new BusinessException("图片最长边不能超过8000px，当前为：" + maxDimension + "px");
                }

                if (minDimension < 20) {
                    throw new BusinessException("图片最短边不能小于20px，当前为：" + minDimension + "px");
                }
            }
        } catch (IOException e) {
            log.error("读取图片尺寸失败: {}", e.getMessage());
            throw new BusinessException("图片文件损坏或格式不正确");
        }
    }
}
