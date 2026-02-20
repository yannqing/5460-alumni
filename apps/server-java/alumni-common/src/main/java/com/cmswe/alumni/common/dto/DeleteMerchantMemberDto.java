package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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

    @Serial
    private static final long serialVersionUID = 1L;
}
