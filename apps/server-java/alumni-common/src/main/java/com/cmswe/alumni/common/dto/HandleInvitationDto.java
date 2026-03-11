package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 处理邀请请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandleInvitationDto implements Serializable {

    /**
     * 邀请记录ID
     */
    @Schema(description = "邀请记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "邀请记录ID不能为空")
    private Long invitationId;

    /**
     * 通知ID
     */
    @Schema(description = "通知ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "通知ID不能为空")
    private Long notificationId;

    /**
     * 是否同意：true-同意, false-拒绝
     */
    @Schema(description = "是否同意（true-同意，false-拒绝）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否同意不能为空")
    private Boolean agree;

    @Serial
    private static final long serialVersionUID = 1L;
}
