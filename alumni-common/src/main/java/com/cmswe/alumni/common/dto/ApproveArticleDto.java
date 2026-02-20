package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审核文章请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ApproveArticleDto", description = "审核文章请求参数")
public class ApproveArticleDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "审核记录id", required = true)
    @NotNull(message = "审核记录ID不能为空")
    private Long homeArticleApplyId;

    @Schema(description = "审核状态 1-审核通过，2-审核拒绝", required = true, example = "1")
    @NotNull(message = "审核状态不能为空")
    private Integer applyStatus;

    @Schema(description = "审批意见", example = "文章内容优质，同意发布")
    private String appliedDescription;
}
