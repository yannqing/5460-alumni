package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 关注操作DTO
 */
@Data
@Schema(description = "关注操作DTO")
public class FollowDto {

    @NotNull(message = "目标类型不能为空")
    @Schema(description = "关注目标类型：1-用户，2-校友会，3-母校，4-商户")
    private Integer targetType;

    @NotNull(message = "目标ID不能为空")
    @Schema(description = "关注目标ID")
    private Long targetId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "关注状态：1-正常关注 2-特别关注 3-免打扰 4-已取消")
    private Integer followStatus;
}
