package com.cmswe.alumni.search.repository;

import com.cmswe.alumni.search.document.MerchantDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 商户文档 Repository
 *
 * @author CNI Alumni System
 */
@Repository
public interface MerchantDocumentRepository extends ElasticsearchRepository<MerchantDocument, Long> {

    /**
     * 根据行业查询
     */
    Page<MerchantDocument> findByIndustry(String industry, Pageable pageable);

    /**
     * 根据评分范围查询
     */
    Page<MerchantDocument> findByRatingGreaterThanEqual(Float minRating, Pageable pageable);

    /**
     * 根据会员等级查询
     */
    Page<MerchantDocument> findByMemberTier(String memberTier, Pageable pageable);

    /**
     * 根据审核状态查询
     */
    Page<MerchantDocument> findByReviewStatus(String reviewStatus, Pageable pageable);

    /**
     * 根据省份和城市查询
     */
    Page<MerchantDocument> findByProvinceAndCity(String province, String city, Pageable pageable);
}
