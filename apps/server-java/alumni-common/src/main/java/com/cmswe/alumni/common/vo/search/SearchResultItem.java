package com.cmswe.alumni.common.vo.search;

import com.cmswe.alumni.common.enums.SearchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 搜索结果项
 *
 * @author CNI Alumni System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultItem {

    /**
     * 搜索类型
     */
    private SearchType type;

    /**
     * 结果ID
     */
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 头像/封面图
     */
    private String avatar;

    /**
     * 高亮文本（HTML格式）
     */
    private String highlightText;

    /**
     * 距离（地理位置搜索时返回，单位：公里）
     */
    private Double distance;

    /**
     * 相关性分数
     */
    private Float score;

    /**
     * 额外信息（根据类型不同，包含不同字段）
     */
    private Map<String, Object> extra;

    /**
     * 创建时间
     */
    private String createTime;
}
