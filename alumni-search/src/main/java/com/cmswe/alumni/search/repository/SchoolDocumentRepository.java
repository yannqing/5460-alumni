package com.cmswe.alumni.search.repository;

import com.cmswe.alumni.search.document.SchoolDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 母校文档仓库
 *
 * @author CNI Alumni System
 */
@Repository
public interface SchoolDocumentRepository extends ElasticsearchRepository<SchoolDocument, Long> {
}
