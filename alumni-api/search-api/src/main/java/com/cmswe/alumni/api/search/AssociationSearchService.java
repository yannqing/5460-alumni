package com.cmswe.alumni.api.search;

import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.vo.search.SearchResultItem;
import org.springframework.data.domain.Page;

/**
 * 校友会搜索服务接口
 *
 * @author CNI Alumni System
 */
public interface AssociationSearchService {

    /**
     * 搜索校友会
     *
     * @param keyword 搜索关键词
     * @param filter 过滤条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param highlight 是否高亮
     * @return 搜索结果
     */
    Page<SearchResultItem> searchAssociation(String keyword, SearchFilter filter,
                                              Integer pageNum, Integer pageSize, Boolean highlight);

    /**
     * 索引校友会数据
     *
     * @param associationId 校友会ID
     */
    void indexAssociation(Long associationId);

    /**
     * 批量索引校友会数据
     *
     * @param associationIds 校友会ID列表
     */
    void batchIndexAssociation(Iterable<Long> associationIds);

    /**
     * 删除校友会索引
     *
     * @param associationId 校友会ID
     */
    void deleteAssociation(Long associationId);

    /**
     * 全量索引重建
     */
    void rebuildIndex();
}
