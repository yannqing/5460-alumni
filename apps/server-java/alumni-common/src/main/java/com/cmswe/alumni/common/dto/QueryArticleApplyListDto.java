package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询文章审核记录列表请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QueryArticleApplyListDto", description = "查询文章审核记录列表请求参数")
public class QueryArticleApplyListDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;

    @Schema(description = "审核状态 0-审核中，1-审核通过，2-审核拒绝", example = "0")
    private Integer applyStatus;
}
