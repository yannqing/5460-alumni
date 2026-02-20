package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新商户成员角色请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantMemberRoleDto implements Serializable {

    /**
     * 商户 ID
     */
    @Schema(description = "商户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商户 ID不能为空")
    private Long merchantId;

    /**
     * 成员用户 ID
     */
    @Schema(description = "成员用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员用户 ID不能为空")
    private Long wxId;

    /**
     * 新的角色 ID
     */
    @Schema(description = "新的角色 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色 ID不能为空")
    private Long roleOrId;

    @Serial
    private static final long serialVersionUID = 1L;
}
