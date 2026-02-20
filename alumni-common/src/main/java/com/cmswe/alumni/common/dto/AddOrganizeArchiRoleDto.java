package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增组织架构角色请求DTO
 */
@Data
@Schema(name = "AddOrganizeArchiRoleDto", description = "新增组织架构角色请求参数")
public class AddOrganizeArchiRoleDto implements Serializable {

    /**
     * 父id（0表示根节点）
     */
    @Schema(description = "父id，0表示根节点")
    private Long pid;

    /**
     * 组织类型（0校友会，1校处会，2商户）
     */
    @NotNull(message = "组织类型不能为空")
    @Schema(description = "组织类型：0-校友会，1-校处会，2-商户", required = true)
    private Integer organizeType;

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
    @NotBlank(message = "角色代码不能为空")
    @Schema(description = "角色唯一代码")
    private String roleOrCode;

    /**
     * 角色含义
     */
    @Schema(description = "角色含义")
    private String remark;

    @Serial
    private static final long serialVersionUID = 1L;
}
