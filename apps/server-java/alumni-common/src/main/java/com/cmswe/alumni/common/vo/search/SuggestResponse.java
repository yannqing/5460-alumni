package com.cmswe.alumni.common.vo.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索建议响应
 *
 * @author CNI Alumni System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestResponse {

    /**
     * 建议列表
     */
    private List<SuggestItem> suggestions;
}
