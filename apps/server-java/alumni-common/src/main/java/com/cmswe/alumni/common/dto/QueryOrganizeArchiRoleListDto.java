package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询组织架构角色列表请求DTO
 */
@Data
@Schema(name = "QueryOrganizeArchiRoleListDto", description = "查询组织架构角色列表请求参数")
public class QueryOrganizeArchiRoleListDto implements Serializable {

    /**
     * 组织id（校友会ID）
     */
    @NotNull(message = "组织ID不能为空")
    @Schema(description = "组织ID（校友会ID）", required = true)
    private Long organizeId;

    /**
     * 组织类型（0校友会，1校处会，2商户）
     */
    @Schema(description = "组织类型：0-校友会，1-校处会，2-商户")
    private Integer organizeType;

    /**
     * 角色名（模糊查询）
     */
    @Schema(description = "角色名（模糊查询）")
    private String roleOrName;

    /**
     * 状态：0-禁用 1-启用
     */
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;
}
