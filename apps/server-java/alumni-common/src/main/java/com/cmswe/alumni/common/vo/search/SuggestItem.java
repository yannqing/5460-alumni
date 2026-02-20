package com.cmswe.alumni.common.vo.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索建议项
 *
 * @author CNI Alumni System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestItem {

    /**
     * 建议文本
     */
    private String text;

    /**
     * 高亮文本
     */
    private String highlightText;

    /**
     * 搜索频率（热度）
     */
    private Integer frequency;

    /**
     * 权重/分数
     */
    private Float score;
}
