package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询本人首页文章列表请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QueryMyHomePageArticleListDto", description = "查询本人首页文章列表请求参数")
public class QueryMyHomePageArticleListDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;

    @Schema(description = "发布者id", example = "1001")
    private Long publishWxId;

    @Schema(description = "文章状态：0-禁用 1-启用", example = "1")
    private Integer articleStatus;
}
