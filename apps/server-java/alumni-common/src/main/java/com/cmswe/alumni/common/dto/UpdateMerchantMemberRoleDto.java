package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新商户成员（职务必填；架构角色可选，不传则不修改）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantMemberRoleDto implements Serializable {

    /**
     * 商户 ID（字符串形式的雪花 ID，避免 JS 精度丢失）
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

    /**
     * 成员架构角色 ID（可选；不传则不修改原角色）
     */
    @Schema(description = "成员架构角色 ID（可选，字符串形式的雪花 ID）")
    private String roleOrId;

    /**
     * 店铺 ID（可选；不传则不修改成员所属店铺）
     */
    @Schema(description = "店铺 ID（可选，字符串形式的雪花 ID）")
    private String shopId;

    /**
     * 职务（必填）
     */
    @Schema(description = "职务", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "职务不能为空")
    private String position;

    @Serial
    private static final long serialVersionUID = 1L;
}
