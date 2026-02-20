package com.cmswe.alumni.search.repository;

import com.cmswe.alumni.search.document.AssociationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 校友会文档 Repository
 *
 * @author CNI Alumni System
 */
@Repository
public interface AssociationDocumentRepository extends ElasticsearchRepository<AssociationDocument, Long> {

    /**
     * 根据学校ID查询
     */
    Page<AssociationDocument> findBySchoolId(Long schoolId, Pageable pageable);

    /**
     * 根据省份和城市查询
     */
    Page<AssociationDocument> findByProvinceAndCity(String province, String city, Pageable pageable);

    /**
     * 根据状态查询
     */
    Page<AssociationDocument> findByStatus(String status, Pageable pageable);
}
