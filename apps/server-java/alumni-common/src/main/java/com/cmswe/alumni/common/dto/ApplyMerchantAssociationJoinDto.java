package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 商户申请加入校友会 DTO
 */
@Data
@Schema(description = "商户申请加入校友会入参")
public class ApplyMerchantAssociationJoinDto implements Serializable {

    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID")
    private Long merchantId;

    @NotNull(message = "校友会ID不能为空")
    @Schema(description = "校友会ID")
    private Long alumniAssociationId;
}
