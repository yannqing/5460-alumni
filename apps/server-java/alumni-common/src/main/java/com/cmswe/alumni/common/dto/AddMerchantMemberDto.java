package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 添加商户成员请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddMerchantMemberDto implements Serializable {

    /**
     * 商户 ID（雪花 ID，请用字符串传递以避免 JS 等端 Number 精度丢失）
     */
    @Schema(description = "商户 ID（字符串形式的雪花 ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商户 ID不能为空")
    private String merchantId;

    /**
     * 校友用户 wxId（雪花 ID，请用字符串传递）
     */
    @Schema(description = "校友用户 wxId（字符串形式的雪花 ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "校友用户 ID不能为空")
    private String wxId;

    /**
     * 店铺 ID（可选，字符串形式的雪花 ID）
     */
    @Schema(description = "店铺 ID（可选）")
    private String shopId;

    /**
     * 成员架构角色 ID（organize_archi_role.role_or_id，可选，字符串形式的雪花 ID）
     */
    @Schema(description = "成员架构角色 ID（可选）")
    private String roleOrId;

    /**
     * 职务（必填）
     */
    @Schema(description = "职务", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "职务不能为空")
    private String position;

    /**
     * 是否同时将该用户设为所选门店的系统门店管理员（ORGANIZE_SHOP_ADMIN，需已传 shopId）
     */
    @Schema(description = "是否设为该门店管理员（需同时传 shopId）")
    private Boolean setAsShopAdmin;

    @Serial
    private static final long serialVersionUID = 1L;
}
