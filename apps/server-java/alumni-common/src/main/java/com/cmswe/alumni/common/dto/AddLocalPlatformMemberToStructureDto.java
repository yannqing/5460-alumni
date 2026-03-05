package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 为校促会架构添加成员请求 DTO
 * 将已有成员分配到组织架构角色，更新 local_platform_member 的 role_or_id
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "为校促会架构添加成员请求")
public class AddLocalPlatformMemberToStructureDto implements Serializable {

    @Schema(description = "校促会 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校促会 ID 不能为空")
    private Long localPlatformId;

    @Schema(description = "成员 ID（local_platform_member 表的主键 id）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员 ID 不能为空")
    private Long memberId;

    @Schema(description = "组织架构角色 ID（role_or_id）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "组织架构角色 ID 不能为空")
    private Long roleOrId;

    @Serial
    private static final long serialVersionUID = 1L;
}
