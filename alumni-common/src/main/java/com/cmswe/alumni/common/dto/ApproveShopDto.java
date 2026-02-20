package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审批店铺申请 DTO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ApproveShopDto", description = "审批店铺申请请求参数")
public class ApproveShopDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "店铺ID不能为空")
    @Schema(description = "店铺ID", example = "123456789")
    private Long shopId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1-审核通过，2-审核失败", example = "1")
    private Integer reviewStatus;

    @Schema(description = "审核原因（审核失败时必填）", example = "店铺信息不完整")
    private String reviewReason;
}
