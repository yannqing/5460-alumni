package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 编辑待审核的校友会创建申请（与 {@link ApplyCreateAlumniAssociationDto} 字段一致，并携带申请 ID）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "编辑待审核的校友会创建申请")
public class UpdatePendingAlumniAssociationApplicationDto extends ApplyCreateAlumniAssociationDto {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "申请ID不能为空")
    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long applicationId;
}
