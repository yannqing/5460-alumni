package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 退出校友会请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "退出校友会请求")
public class QuitAlumniAssociationDto implements Serializable {

    @Schema(description = "校友会ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotNull(message = "校友会ID不能为空")
    private Long alumniAssociationId;

    @Serial
    private static final long serialVersionUID = 1L;
}
