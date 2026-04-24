package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审核商户加入校友会申请 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "审核商户加入校友会申请请求参数")
public class ReviewMerchantAssociationJoinApplyDto implements Serializable {

    @Schema(description = "申请ID")
    @NotNull(message = "申请ID不能为空")
    private Long id;

    @Schema(description = "审核结果：1-通过 2-拒绝")
    @NotNull(message = "审核结果不能为空")
    private Integer status;

    @Schema(description = "审核意见")
    private String reviewComment;

    @Serial
    private static final long serialVersionUID = 1L;
}
