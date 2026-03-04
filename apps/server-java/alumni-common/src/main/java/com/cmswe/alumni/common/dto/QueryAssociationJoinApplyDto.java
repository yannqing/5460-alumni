package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "查询校友会加入校促会申请列表请求参数")
public class QueryAssociationJoinApplyDto implements Serializable {

    @Schema(description = "当前页码")
    private Integer current = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "校促会ID")
    private Long platformId;

    @Schema(description = "审核状态(0待审核,1已通过,2已拒绝)")
    private Integer status;

    private static final long serialVersionUID = 1L;
}
