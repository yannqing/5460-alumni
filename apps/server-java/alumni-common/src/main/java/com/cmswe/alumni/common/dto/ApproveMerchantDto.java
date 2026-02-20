package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审批商户入驻申请 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ApproveMerchantDto", description = "审批商户入驻申请请求参数")
public class ApproveMerchantDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID", example = "123456789")
    private Long merchantId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1-审核通过，2-审核失败", example = "1")
    private Integer reviewStatus;

    @Schema(description = "审核原因（审核失败时必填）", example = "营业执照信息不清晰")
    private String reviewReason;
}
