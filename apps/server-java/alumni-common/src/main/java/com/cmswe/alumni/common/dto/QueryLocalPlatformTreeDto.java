package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询校处会组织架构树请求DTO
 */
@Data
@Schema(name = "QueryLocalPlatformTreeDto", description = "查询校处会组织架构树请求参数")
public class QueryLocalPlatformTreeDto implements Serializable {

    /**
     * 校处会ID
     */
    @NotNull(message = "校处会ID不能为空")
    @Schema(description = "校处会ID", required = true)
    private Long localPlatformId;

    @Serial
    private static final long serialVersionUID = 1L;
}
