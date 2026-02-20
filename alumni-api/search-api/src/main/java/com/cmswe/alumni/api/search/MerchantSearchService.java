package com.cmswe.alumni.api.search;

import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.vo.search.SearchResultItem;
import org.springframework.data.domain.Page;

/**
 * 商户搜索服务接口
 *
 * @author CNI Alumni System
 */
public interface MerchantSearchService {

    /**
     * 搜索商户
     *
     * @param keyword 搜索关键词
     * @param filter 过滤条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param highlight 是否高亮
     * @return 搜索结果
     */
    Page<SearchResultItem> searchMerchant(String keyword, SearchFilter filter,
                                           Integer pageNum, Integer pageSize, Boolean highlight);

    /**
     * 索引商户数据
     *
     * @param merchantId 商户ID
     */
    void indexMerchant(Long merchantId);

    /**
     * 批量索引商户数据
     *
     * @param merchantIds 商户ID列表
     */
    void batchIndexMerchant(Iterable<Long> merchantIds);

    /**
     * 删除商户索引
     *
     * @param merchantId 商户ID
     */
    void deleteMerchant(Long merchantId);

    /**
     * 全量索引重建
     */
    void rebuildIndex();
}
