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
     * 成员用户ID
     */
    @Schema(description = "成员用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员用户ID不能为空")
    private Long wxId;

    @Serial
    private static final long serialVersionUID = 1L;
}
