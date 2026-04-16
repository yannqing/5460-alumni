package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除商户成员请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMerchantMemberDto implements Serializable {

    /**
     * 商户 ID（字符串形式的雪花 ID，与小程序等端一致，避免 JS Number 精度丢失）
     */
    @Schema(description = "商户 ID（字符串形式的雪花 ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商户 ID不能为空")
    private String merchantId;

    /**
     * 成员用户 wxId（字符串形式的雪花 ID）
     */
    @Schema(description = "成员用户 wxId（字符串形式的雪花 ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "成员用户 ID不能为空")
    private String wxId;

    @Serial
    private static final long serialVersionUID = 1L;
}
