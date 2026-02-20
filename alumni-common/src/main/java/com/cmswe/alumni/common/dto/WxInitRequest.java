package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信小程序静默登录请求DTO
 */
@Data
@Schema(description = "微信小程序静默登录请求")
public class WxInitRequest {

    @Schema(description = "微信登录临时凭证code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "code不能为空")
    private String code;

    @Schema(description = "邀请人的UUID（可选）")
    private String inviterWxUuid;
}
