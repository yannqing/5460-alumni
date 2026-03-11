package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除校友会成员请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAlumniAssociationMemberDto implements Serializable {

    /**
     * 校友会ID
     */
    @Schema(description = "校友会ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校友会ID不能为空")
    private Long alumniAssociationId;

    /**
     * 成员记录ID（当删除未注册成员时使用此字段）
     */
    @Schema(description = "成员记录ID")
    private Long id;

    /**
     * 成员用户ID（当删除已注册成员时使用此字段）
     */
    @Schema(description = "成员用户ID")
    private Long wxId;

    @Serial
    private static final long serialVersionUID = 1L;
}
