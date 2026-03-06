package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 确认邀请请求DTO
 */
@Data
@Schema(description = "确认邀请请求")
public class ConfirmInvitationDto {

    @Schema(description = "邀请人wxid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "邀请人wxid不能为空")
    private Long inviterWxId;

    @Schema(description = "被邀请人wxid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "被邀请人wxid不能为空")
    private Long inviteeWxId;
}
