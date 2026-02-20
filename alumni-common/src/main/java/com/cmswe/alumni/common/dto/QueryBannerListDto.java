package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询轮播图列表请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QueryBannerListDto", description = "查询轮播图列表请求参数")
public class QueryBannerListDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "轮播图标题（模糊搜索）", example = "校友")
    private String bannerTitle;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer bannerStatus;

    @Schema(description = "跳转类型：1-无跳转，2-内部路径，3-第三方链接，4-文章详情", example = "1")
    private Integer bannerType;
}
