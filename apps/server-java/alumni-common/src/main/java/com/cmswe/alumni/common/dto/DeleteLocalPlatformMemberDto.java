package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除校处会成员请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteLocalPlatformMemberDto implements Serializable {

    /**
     * 校处会ID
     */
    @Schema(description = "校处会ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校处会ID不能为空")
    private Long localPlatformId;

    /**
     * 成员用户ID
     */
    @Schema(description = "成员用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员用户ID不能为空")
    private Long wxId;

    @Serial
    private static final long serialVersionUID = 1L;
}
