package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.FileService;
import com.cmswe.alumni.common.entity.Files;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.CosUtils;
import com.cmswe.alumni.common.utils.FileUtils;
import com.cmswe.alumni.common.utils.JwtUtils;
import com.cmswe.alumni.common.vo.FilesVo;
import com.cmswe.alumni.service.user.mapper.FilesMapper;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.cmswe.alumni.common.constant.Constant.*;

@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FilesMapper, Files> implements FileService {

    @Value("${app.url}")
    private String appUrl;

    @Value("${file.upload-image-url}")
    private String uploadImagePath;

    @Value("${file.upload-resume-url}")
    private String uploadResumePath;

    @Value("${file.upload-audio-url}")
    private String uploadAudioPath;

    @Value("${file.storage-type:local}")
    private String storageType;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private FileUtils fileUtils;

    @Autowired(required = false)
    private CosUtils cosUtils;

    @Resource
    private WxUserMapper wxUserMapper;

    @Override
    public FilesVo uploadImageAndReturnVo(MultipartFile image, HttpServletRequest request) throws IOException {
        return uploadFile(image, uploadImagePath, IMAGE_FILE_TYPE, request);
    }

    @Override
    public FilesVo uploadAudioAndReturnVo(MultipartFile audio, HttpServletRequest request) throws IOException {
        return uploadFile(audio, uploadAudioPath, AUDIO_FILE_TYPE, request);
    }

    @Override
    public ResponseEntity<FileSystemResource> downloadFile(Long fileId, HttpServletRequest request) {
        Optional.ofNullable(fileId)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Files downloadFile = this.getById(fileId);

        Optional.ofNullable(downloadFile)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        ResponseEntity<FileSystemResource> downloadFileResult = fileUtils.downloadFile(downloadFile);

        // 更新文件的下载次数 +1
        downloadFile.setDownloadCount(downloadFile.getDownloadCount() + 1);
        this.updateById(downloadFile);

        return downloadFileResult;
    }

    /**
     * 通用文件上传方法（返回FilesVo，支持自定义原始文件名）TODO 后续同步为 kafka 异步解耦处理
     * @param file 文件
     * @param subPath 子路径（如image, audio）
     * @param type 文件类型
     * @return FilesVo
     * @throws IOException IO异常
     */
    private FilesVo uploadFile(MultipartFile file, String subPath, String type, HttpServletRequest request) throws IOException {
        // 参数校验
        if (file == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL.getMessage());
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL.getMessage());
        }
        
        // 校验文件类型
        String fileExtension = getFileExtension(fileName);

        // 生成新 UUID 文件名
        UUID uuid = UUID.randomUUID();
        String newFilename = replaceFilename(fileName, uuid.toString());

        // 获取用户 ID 和 openid
        Long wxId = jwtUtils.getUserIdFromToken(request.getHeader("token"));
        String openid = null;

        // 如果使用云托管 COS，需要获取用户的 openid
        if ("cos".equalsIgnoreCase(storageType) && wxId != null) {
            WxUser wxUser = wxUserMapper.selectById(wxId);
            if (wxUser != null) {
                openid = wxUser.getOpenid();
                log.info("获取到用户 openid: {}", openid);
            }
        }

        String accessUrl = fileUtils.uploadFile(file, subPath, type, fileName, newFilename, fileExtension, openid);

        // 获取文件 md5 和 SHA-256
        String fileMd5 = DigestUtils.md5Hex(file.getInputStream());
        String fileHash = DigestUtils.sha256Hex(file.getInputStream());

        // 根据存储类型生成完整的访问 URL
        String fullUrl;
        if ("cos".equalsIgnoreCase(storageType)) {
            // COS 存储：使用 COS 域名
            fullUrl = cosUtils.getFileUrl(accessUrl);
        } else {
            // 本地存储：使用应用域名
            fullUrl = appUrl + accessUrl;
        }

        // 保存文件信息到数据库并返回FilesVo
        FilesVo filesVo = saveFileRecord(
            wxId,
            newFilename,
            fileName,
            fileExtension,
            accessUrl,
            fullUrl,
            fileMd5,
            fileHash,
            file.getSize(),
            file.getContentType(),
            type,
            request
        );
        
        log.info("文件上传成功并保存到数据库，文件ID：{}", filesVo.getFileId());
        return filesVo;
    }

    /**
     * 自定义方法，用来替换文件名
     * @param filename 原文件名
     * @param uuid 新文件名
     * @return 替换结果
     */
    private String replaceFilename(String filename, String uuid) {
        int index = filename.lastIndexOf(".");
        return uuid + filename.substring(index);
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 文件扩展名（不包含点号）
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 保存文件到数据库
     * @param wxId
     * @param fileName
     * @param OriginalName
     * @param fileExtension
     * @param filePath
     * @param fileUrl
     * @param fileMd5
     * @param fileHash
     * @param fileSize
     * @param mimeType
     * @param fileType
     * @param request
     * @return
     */
    private FilesVo saveFileRecord(Long wxId, String fileName, String OriginalName, String fileExtension, String filePath, String fileUrl, String fileMd5, String fileHash, Long fileSize, String mimeType, String fileType, HttpServletRequest request) {
        Files file = new Files();
        file.setWxId(wxId);
        file.setFileType(fileType);
        file.setFileName(fileName);
        file.setOriginalName(OriginalName);
        file.setDisplayName(OriginalName);
        file.setFileExtension(fileExtension);
        file.setFileUrl(fileUrl);
        file.setFilePath(filePath);
        file.setFileSize(fileSize);
        file.setFileMd5(fileMd5);
        file.setFileHash(fileHash);
        file.setMimeType(mimeType);
        file.setDownloadCount(0);
        file.setPreviewCount(0);
        file.setAccessCount(0);
        file.setShareCount(0);
        file.setStatus(1); // 1-成功
        file.setUploadIp(getRealClientIp(request));

        this.save(file);

        log.info("保存文件记录成功: wxId={}, fileName={}, fileUrl={}", wxId, fileName, fileUrl);

        return FilesVo.objToVo(file);
    }

    /**
     * 获取真实 ip TODO 后续换位置（多个地方使用）
     * @param request
     * @return
     */
    private String getRealClientIp(HttpServletRequest request) {
        // 优先从 X-Forwarded-For 获取
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 最后才使用 getRemoteAddr
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For 可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
