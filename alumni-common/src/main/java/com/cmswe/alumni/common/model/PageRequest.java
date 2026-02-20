package com.cmswe.alumni.common.model;

import com.cmswe.alumni.common.constant.CommonConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页请求
 *
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    private int current = 1;

    /**
     * 页面大小
     */
    @Schema(description = "每页数量", example = "10", defaultValue = "10")
    private int pageSize = 10;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "createTime", defaultValue = "createTime")
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    @Schema(description = "排序顺序 - ascend(升序) / descend(降序)",
            example = "descend",
            defaultValue = CommonConstant.SORT_ORDER_ASC,
            allowableValues = {"ascend", "descend"})
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}