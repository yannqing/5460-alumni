package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 移除校友会管理员请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "移除校友会管理员请求")
public class RemoveAlumniAssociationAdminDto implements Serializable {

    @Schema(description = "校友会ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotNull(message = "校友会ID不能为空")
    private Long alumniAssociationId;

    @Schema(description = "用户ID（被移除管理员权限的用户）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotNull(message = "用户ID不能为空")
    private Long wxId;

    @Serial
    private static final long serialVersionUID = 1L;
}
