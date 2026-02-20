package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新组织架构角色请求DTO
 */
@Data
@Schema(name = "UpdateOrganizeArchiRoleDto", description = "更新组织架构角色请求参数")
public class UpdateOrganizeArchiRoleDto implements Serializable {

    /**
     * 架构角色id
     */
    @NotNull(message = "角色ID不能为空")
    @Schema(description = "架构角色id", required = true)
    private Long roleOrId;

    /**
     * 父id（0表示根节点）
     */
    @Schema(description = "父id，0表示根节点")
    private Long pid;

    /**
     * 组织id（校友会ID）
     */
    @NotNull(message = "组织ID不能为空")
    @Schema(description = "组织ID（校友会ID）", required = true)
    private Long organizeId;

    /**
     * 角色名
     */
    @NotBlank(message = "角色名不能为空")
    @Schema(description = "角色名", required = true)
    private String roleOrName;

    /**
     * 角色唯一代码
     */
    @Schema(description = "角色唯一代码")
    private String roleOrCode;

    /**
     * 角色含义
     */
    @Schema(description = "角色含义")
    private String remark;

    /**
     * 状态：0-禁用 1-启用
     */
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;
}
