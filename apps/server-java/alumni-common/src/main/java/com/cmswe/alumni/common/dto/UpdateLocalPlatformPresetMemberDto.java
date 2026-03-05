package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新校促会预设成员请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLocalPlatformPresetMemberDto implements Serializable {

    /**
     * 成员 ID
     */
    @Schema(description = "成员 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员 ID不能为空")
    private Long memberId;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户 ID不能为空")
    private Long wxId;

    @Serial
    private static final long serialVersionUID = 1L;
}