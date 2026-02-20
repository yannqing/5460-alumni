package com.cmswe.alumni.common.dto.search;

import com.cmswe.alumni.common.enums.SearchType;
import com.cmswe.alumni.common.enums.SortField;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 统一搜索请求
 *
 * @author CNI Alumni System
 */
@Data
public class UnifiedSearchRequest {

    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    @Size(min = 1, max = 50, message = "搜索关键词长度必须在1-50之间")
    private String keyword;

    /**
     * 搜索类型列表
     */
    @NotNull(message = "搜索类型不能为空")
    @Size(min = 1, message = "至少选择一种搜索类型")
    private List<SearchType> types;

    /**
     * 搜索过滤条件
     */
    private SearchFilter filter;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    @Max(value = 100, message = "页码不能超过100")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private SortField sortField = SortField.RELEVANCE;

    /**
     * 排序方向
     */
    private Sort.Direction sortOrder = Sort.Direction.DESC;

    /**
     * 是否需要高亮
     */
    private Boolean highlight = true;

    /**
     * 是否需要搜索建议
     */
    private Boolean needSuggestions = false;
}
