package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取微信用户手机号请求DTO
 */
@Data
@Schema(description = "获取微信用户手机号请求")
public class GetPhoneNumberRequest {

    @Schema(description = "动态令牌code（从button组件回调中获取）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "code不能为空")
    private String code;
}
