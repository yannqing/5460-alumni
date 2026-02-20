package com.cmswe.alumni.api.search;

import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.vo.search.SearchResultItem;
import org.springframework.data.domain.Page;

/**
 * 母校搜索服务接口
 *
 * @author CNI Alumni System
 */
public interface SchoolSearchService {

    /**
     * 搜索母校
     *
     * @param keyword 搜索关键词
     * @param filter 过滤条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param highlight 是否高亮
     * @return 搜索结果
     */
    Page<SearchResultItem> searchSchool(String keyword, SearchFilter filter,
                                         Integer pageNum, Integer pageSize, Boolean highlight);

    /**
     * 索引母校数据
     *
     * @param schoolId 母校ID
     */
    void indexSchool(Long schoolId);

    /**
     * 批量索引母校数据
     *
     * @param schoolIds 母校ID列表
     */
    void batchIndexSchool(Iterable<Long> schoolIds);

    /**
     * 删除母校索引
     *
     * @param schoolId 母校ID
     */
    void deleteSchool(Long schoolId);

    /**
     * 全量索引重建
     */
    void rebuildIndex();
}
