package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商户收藏切换请求
 */
@Data
@Schema(description = "商户收藏切换请求")
public class ToggleMerchantFavoriteDto {

    @Schema(description = "用户ID（字符串形式雪花ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户ID不能为空")
    private String wxId;

    @Schema(description = "商户ID（字符串形式雪花ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商户ID不能为空")
    private String merchantId;
}
