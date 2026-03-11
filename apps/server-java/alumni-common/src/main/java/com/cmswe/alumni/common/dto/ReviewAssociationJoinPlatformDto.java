package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "审核校友会加入校促会申请请求参数")
public class ReviewAssociationJoinPlatformDto implements Serializable {

    @Schema(description = "申请记录ID")
    @NotNull(message = "申请ID不能为空")
    private Long id;

    @Schema(description = "审核结果：1-通过 2-拒绝")
    @NotNull(message = "审核结果不能为空")
    private Integer status;

    @Schema(description = "审核意见")
    private String reviewComment;

    private static final long serialVersionUID = 1L;
}
