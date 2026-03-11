package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "校友会申请加入校促会请求参数")
public class ApplyAssociationJoinPlatformDto implements Serializable {

    @Schema(description = "校友会ID")
    @NotNull(message = "校友会ID不能为空")
    private Long alumniAssociationId;

    @Schema(description = "校促会ID")
    @NotNull(message = "校促会ID不能为空")
    private Long platformId;

    private static final long serialVersionUID = 1L;
}
