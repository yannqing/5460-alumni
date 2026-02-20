package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审核校友企业/场所申请 DTO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ApproveAlumniPlaceApplicationDto", description = "审核校友企业/场所申请请求参数")
public class ApproveAlumniPlaceApplicationDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "申请ID不能为空")
    @Schema(description = "申请ID", example = "123456789")
    private Long applicationId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1-审核通过 2-审核拒绝", example = "1")
    private Integer applicationStatus;

    @Schema(description = "审核备注（审核拒绝时必填）", example = "企业信息不完整")
    private String reviewRemark;
}
