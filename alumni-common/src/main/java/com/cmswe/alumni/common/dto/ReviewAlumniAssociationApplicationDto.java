package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审核校友会创建申请 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewAlumniAssociationApplicationDto implements Serializable {

    /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    /**
     * 审核结果：1-通过 2-拒绝
     */
    @Schema(description = "审核结果：1-通过 2-拒绝")
    @NotNull(message = "审核结果不能为空")
    private Integer reviewResult;

    /**
     * 审核意见（拒绝时必填）
     */
    @Schema(description = "审核意见（拒绝时必填）")
    private String reviewComment;

    @Serial
    private static final long serialVersionUID = 1L;
}
