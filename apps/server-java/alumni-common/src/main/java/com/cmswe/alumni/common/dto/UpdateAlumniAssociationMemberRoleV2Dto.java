package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新校友会成员角色请求 DTO V2版本（基于username）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAlumniAssociationMemberRoleV2Dto implements Serializable {

    /**
     * 校友会 ID
     */
    @Schema(description = "校友会 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校友会 ID不能为空")
    private Long alumniAssociationId;

    /**
     * 成员主键ID (新增时为空)
     */
    @Schema(description = "成员主键ID (新增时为空)")
    private Long id;

    /**
     * 成员用户名
     */
    @Schema(description = "成员用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "成员用户名不能为空")
    private String username;

    /**
     * 新s的组织架构角色 ID
     */
    @Schema(description = "新的组织架构角色 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "组织架构角色 ID不能为空")
    private Long roleOrId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    @Serial
    private static final long serialVersionUID = 1L;
}
