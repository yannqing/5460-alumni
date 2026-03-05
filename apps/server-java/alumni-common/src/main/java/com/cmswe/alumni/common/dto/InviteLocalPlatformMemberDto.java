package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邀请成员加入校处会请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InviteLocalPlatformMemberDto implements Serializable {

    /**
     * 校处会 ID
     */
    @Schema(description = "校处会 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校处会 ID不能为空")
    private Long localPlatformId;

    /**
     * 成员用户 ID
     */
    @Schema(description = "成员用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员用户 ID不能为空")
    private Long wxId;

    /**
     * 组织架构角色 ID
     */
    @Schema(description = "组织架构角色 ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long roleOrId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String username;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String roleName;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String contactInformation;

    /**
     * 社会职务
     */
    @Schema(description = "社会职务", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String socialDuties;

    @Serial
    private static final long serialVersionUID = 1L;
}
