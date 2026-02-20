package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.UploadFileDto;
import com.cmswe.alumni.common.entity.Files;
import com.cmswe.alumni.common.vo.FilesVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService extends IService<Files> {

    /**
     * 下载文件
     * @param fileId 文件id
     * @return 返回图片
     */
    ResponseEntity<FileSystemResource> downloadFile(Long fileId, HttpServletRequest request);

    /**
     * 上传图片并返回FilesVo
     * @param image 图片
     * @return 返回FilesVo
     * @throws IOException IO 异常
     */
    FilesVo uploadImageAndReturnVo(MultipartFile image, HttpServletRequest request) throws IOException;

    /**
     * 上传音频文件并返回FilesVo
     * @param audio 音频文件
     * @return 返回FilesVo
     * @throws IOException IO 异常
     */
    FilesVo uploadAudioAndReturnVo(MultipartFile audio, HttpServletRequest request) throws IOException;
}
