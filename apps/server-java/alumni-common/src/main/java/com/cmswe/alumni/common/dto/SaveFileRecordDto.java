package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "前端直传COS后保存文件记录请求")
public class SaveFileRecordDto {

    @NotBlank(message = "文件名不能为空")
    @Schema(description = "存储文件名（UUID生成的）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @NotBlank(message = "原始文件名不能为空")
    @Schema(description = "原始文件名（用户上传时的名称）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalName;

    @NotBlank(message = "文件扩展名不能为空")
    @Schema(description = "文件扩展名（不含点号，如 jpg、png）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileExtension;

    @NotBlank(message = "文件类型不能为空")
    @Schema(description = "文件类型：image、audio、document", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileType;

    @NotBlank(message = "文件相对路径不能为空")
    @Schema(description = "COS存储路径（不含域名，如 /cni-alumni/images/2026/03/17/xxx.jpg）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filePath;

    @NotBlank(message = "文件完整URL不能为空")
    @Schema(description = "文件完整访问URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "MIME类型（如 image/jpeg）")
    private String mimeType;
}
