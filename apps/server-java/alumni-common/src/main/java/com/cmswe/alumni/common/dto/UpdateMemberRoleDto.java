package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新校友会成员角色请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMemberRoleDto implements Serializable {

    /**
     * 校友会 ID
     */
    @Schema(description = "校友会 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校友会 ID不能为空")
    private Long alumniAssociationId;

    /**
     * 成员用户 ID
     */
    @Schema(description = "成员用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员用户 ID不能为空")
    private Long wxId;

    /**
     * 新的组织架构角色 ID
     */
    @Schema(description = "新的组织架构角色 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "组织架构角色 ID不能为空")
    private Long roleOrId;

    @Serial
    private static final long serialVersionUID = 1L;
}
