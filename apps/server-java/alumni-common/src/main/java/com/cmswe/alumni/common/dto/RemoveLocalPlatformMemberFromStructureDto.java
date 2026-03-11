package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 将成员从校促会架构移除请求 DTO
 * 清空 role_or_id，使成员不再具有校处会架构角色（非逻辑删除）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "将成员从校促会架构移除请求")
public class RemoveLocalPlatformMemberFromStructureDto implements Serializable {

    @Schema(description = "校促会 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校促会 ID 不能为空")
    private Long localPlatformId;

    @Schema(description = "成员 ID（local_platform_member 表的主键 id）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员 ID 不能为空")
    private Long memberId;

    @Serial
    private static final long serialVersionUID = 1L;
}
