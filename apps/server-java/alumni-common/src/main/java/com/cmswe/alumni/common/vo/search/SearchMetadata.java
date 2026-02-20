package com.cmswe.alumni.common.vo.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索元数据
 *
 * @author CNI Alumni System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMetadata {

    /**
     * 搜索耗时（毫秒）
     */
    private Long took;

    /**
     * 是否超时
     */
    private Boolean timedOut;

    /**
     * 是否来自缓存
     */
    private Boolean fromCache;

    /**
     * 最大分数
     */
    private Float maxScore;
}
