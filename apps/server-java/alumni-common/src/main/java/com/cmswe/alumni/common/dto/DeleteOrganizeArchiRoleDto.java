package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除组织架构角色请求DTO
 */
@Data
@Schema(name = "DeleteOrganizeArchiRoleDto", description = "删除组织架构角色请求参数")
public class DeleteOrganizeArchiRoleDto implements Serializable {

    /**
     * 架构角色id
     */
    @NotNull(message = "角色ID不能为空")
    @Schema(description = "架构角色id", required = true)
    private Long roleOrId;

    /**
     * 组织id（校友会ID）- 用于权限校验
     */
    @NotNull(message = "组织ID不能为空")
    @Schema(description = "组织ID（校友会ID）", required = true)
    private Long organizeId;

    @Serial
    private static final long serialVersionUID = 1L;
}
