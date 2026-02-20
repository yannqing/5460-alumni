package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "上传文件请求")
public class UploadFileDto {

    @Schema(description = "要上传的文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    @Schema(description = "会话id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sessionId;
}
