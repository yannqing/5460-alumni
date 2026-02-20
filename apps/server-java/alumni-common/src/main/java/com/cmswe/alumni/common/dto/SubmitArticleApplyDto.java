package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 提交文章审核请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SubmitArticleApplyDto", description = "提交文章审核请求参数")
public class SubmitArticleApplyDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "首页文章id", required = true)
    @NotNull(message = "文章ID不能为空")
    private Long homeArticleId;
}
