package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 拉黑操作DTO
 */
@Data
@Schema(description = "拉黑操作DTO")
public class BlacklistDto {

    @NotNull(message = "被拉黑用户ID不能为空")
    @Schema(description = "被拉黑用户ID")
    private Long blockedWxId;

    @Schema(description = "拉黑类型：1-单向拉黑 2-双向拉黑")
    private Integer blockType;

    @Schema(description = "拉黑原因")
    private String blockReason;
}
