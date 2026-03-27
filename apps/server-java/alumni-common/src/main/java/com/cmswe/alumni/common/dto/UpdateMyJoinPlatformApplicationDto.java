package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新「校友会加入校促会申请」参数（仅允许修改 platformId）
 */
@Data
@Schema(description = "更新校友会加入校促会申请参数")
public class UpdateMyJoinPlatformApplicationDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "platformId不能为空")
    @Schema(description = "目标校促会ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long platformId;
}

