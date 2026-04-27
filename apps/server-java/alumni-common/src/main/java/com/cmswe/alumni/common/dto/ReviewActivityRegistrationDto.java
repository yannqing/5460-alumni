package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审核活动报名请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "审核活动报名请求DTO")
public class ReviewActivityRegistrationDto implements Serializable {

    /**
     * 报名记录ID
     */
    @NotNull(message = "报名记录ID不能为空")
    @Schema(description = "报名记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long registrationId;

    /**
     * 审核结果：1-通过 2-拒绝
     */
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果：1-通过 2-拒绝", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reviewResult;

    /**
     * 审核理由（拒绝时必填）
     */
    @Size(max = 200, message = "审核理由长度不能超过200个字符")
    @Schema(description = "审核理由（拒绝时必填）")
    private String auditReason;

    @Serial
    private static final long serialVersionUID = 1L;
}
