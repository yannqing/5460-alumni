package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.user.FileService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.FilesVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 10MB

    /**
     * 上传图片接口
     * @param image 图片文件
     * @return 返回文件vo
     * @throws IOException IO 异常
     */
    @Operation(summary = "上传图片文件", description = "支持jpg, jpeg, png, gif, bmp, webp, svg, ico, tiff, tif格式")
    @PostMapping("/upload/images")
    public BaseResponse<FilesVo> uploadImage(@RequestParam("image") MultipartFile image,
                                             HttpServletRequest request) throws IOException {
        if (image != null && image.getSize() > MAX_FILE_SIZE_BYTES) {
            return ResultUtils.failure("文件大小不能超过10MB");
        }
        FilesVo filesVo = fileService.uploadImageAndReturnVo(image, request);

        return ResultUtils.success(Code.SUCCESS, filesVo, "上传成功！");
    }

    /**
     * 上传音频接口
     * @param audio 音频文件
     * @return 返回文件vo
     * @throws IOException IO 异常
     */
    @Operation(summary = "上传音频文件", description = "支持mp3、wav、flac、aac、ogg、wma、m4a、opus格式")
    @PostMapping("/upload/audio")
    public BaseResponse<FilesVo> uploadAudio(@RequestParam("audio") MultipartFile audio,
                                           HttpServletRequest request) throws IOException {
        FilesVo filesVo = fileService.uploadAudioAndReturnVo(audio, request);

        return ResultUtils.success(Code.SUCCESS, filesVo, "上传成功！");
    }

    /**
     * 下载文件
     * @param fileId 要下载的文件id
     * @return 返回文件
     */
    @Operation(summary = "下载文件")
    @GetMapping("/download/{fileId}")
    public ResponseEntity<FileSystemResource> downloadImage(@PathVariable("fileId") Long fileId, HttpServletRequest request) {

        return fileService.downloadFile(fileId, request);
    }
}
