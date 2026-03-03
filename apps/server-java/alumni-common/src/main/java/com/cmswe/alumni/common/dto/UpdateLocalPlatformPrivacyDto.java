package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新校促会隐私设置请求DTO
 */
@Data
@Schema(name = "UpdateLocalPlatformPrivacyDto", description = "更新校促会隐私设置请求")
public class UpdateLocalPlatformPrivacyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 校促会ID
     */
    @NotNull(message = "校促会ID不能为空")
    @Schema(description = "校促会ID")
    private Long platformId;

    /**
     * 字段代码
     */
    @NotBlank(message = "字段代码不能为空")
    @Schema(description = "字段代码")
    private String fieldCode;

    /**
     * 可见性: 0 不可见；1 可见
     */
    @NotNull(message = "可见性设置不能为空")
    @Schema(description = "可见性: 0 不可见；1 可见")
    private Integer visibility;
}
