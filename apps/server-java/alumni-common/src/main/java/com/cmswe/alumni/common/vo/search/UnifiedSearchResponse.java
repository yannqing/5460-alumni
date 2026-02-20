package com.cmswe.alumni.common.vo.search;

import com.cmswe.alumni.common.enums.SearchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 统一搜索响应
 *
 * @author CNI Alumni System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedSearchResponse {

    /**
     * 总结果数
     */
    private Long total;

    /**
     * 搜索结果列表
     */
    private List<SearchResultItem> items;

    /**
     * 各类型结果数量统计
     */
    private Map<SearchType, Long> typeCounts;

    /**
     * 搜索建议（可选）
     */
    private List<String> suggestions;

    /**
     * 搜索元数据
     */
    private SearchMetadata metadata;
}
