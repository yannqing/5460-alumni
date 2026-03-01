package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询组织文章列表请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QueryOrganizationArticleListDto", description = "查询组织文章列表请求参数")
public class QueryOrganizationArticleListDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;

    @Schema(description = "组织ID（校友会或校促会ID）", example = "123456")
    private Long organizationId;

    @Schema(description = "文章标题（模糊搜索）", example = "校友会活动")
    private String articleTitle;
}
