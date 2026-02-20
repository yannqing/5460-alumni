package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询组织架构树请求DTO
 */
@Data
@Schema(name = "QueryOrganizationTreeDto", description = "查询组织架构树请求参数")
public class QueryOrganizationTreeDto implements Serializable {

    /**
     * 校友会ID
     */
    @NotNull(message = "校友会ID不能为空")
    @Schema(description = "校友会ID", required = true)
    private Long alumniAssociationId;

    @Serial
    private static final long serialVersionUID = 1L;
}
