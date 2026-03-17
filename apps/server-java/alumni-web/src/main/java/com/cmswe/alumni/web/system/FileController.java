package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.user.FileService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.SaveFileRecordDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.FilesVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

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
     * 上传文档接口
     * @param document 文档文件
     * @return 返回文件vo
     * @throws IOException IO 异常
     */
    @Operation(summary = "上传文档文件", description = "支持pdf、doc、docx、xls、xlsx、ppt、pptx、txt、md、csv、rtf、odt、ods、odp格式")
    @PostMapping("/upload/document")
    public BaseResponse<FilesVo> uploadDocument(@RequestParam("document") MultipartFile document,
                                                HttpServletRequest request) throws IOException {
        if (document != null && document.getSize() > MAX_FILE_SIZE_BYTES) {
            return ResultUtils.failure("文件大小不能超过5MB");
        }
        FilesVo filesVo = fileService.uploadDocumentAndReturnVo(document, request);

        return ResultUtils.success(Code.SUCCESS, filesVo, "上传成功！");
    }

    /**
     * 获取COS临时凭证（供前端直传COS使用）
     */
    @Operation(summary = "获取COS临时凭证", description = "前端直传对象存储时，先调用此接口获取临时凭证")
    @GetMapping("/cos/credential")
    public BaseResponse<Map<String, Object>> getCosCredential() {
        Map<String, Object> credential = fileService.getCosCredential();
        return ResultUtils.success(Code.SUCCESS, credential, "获取成功");
    }

    /**
     * 前端直传COS后，保存文件记录到数据库
     */
    @Operation(summary = "保存文件记录", description = "前端直传COS成功后，调用此接口将文件信息保存到数据库")
    @PostMapping("/cos/saveRecord")
    public BaseResponse<FilesVo> saveFileRecord(@Valid @RequestBody SaveFileRecordDto dto,
                                                 HttpServletRequest request) throws JsonProcessingException {
        FilesVo filesVo = fileService.saveFileRecord(dto, request);
        return ResultUtils.success(Code.SUCCESS, filesVo, "文件记录保存成功");
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
