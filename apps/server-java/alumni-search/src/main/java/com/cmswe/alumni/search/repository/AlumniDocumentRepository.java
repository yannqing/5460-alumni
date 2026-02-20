package com.cmswe.alumni.search.repository;

import com.cmswe.alumni.search.document.AlumniDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 校友文档 Repository
 *
 * @author CNI Alumni System
 */
@Repository
public interface AlumniDocumentRepository extends ElasticsearchRepository<AlumniDocument, Long> {

    /**
     * 根据学校ID查询
     */
    Page<AlumniDocument> findBySchoolId(Long schoolId, Pageable pageable);

    /**
     * 根据毕业年份范围查询
     */
    Page<AlumniDocument> findByGraduationYearBetween(Integer startYear, Integer endYear, Pageable pageable);

    /**
     * 根据可搜索状态查询
     */
    Page<AlumniDocument> findBySearchable(Boolean searchable, Pageable pageable);

    /**
     * 根据省份和城市查询
     */
    Page<AlumniDocument> findByProvinceAndCity(String province, String city, Pageable pageable);

    /**
     * 根据标签查询
     */
    Page<AlumniDocument> findByTagsContaining(String tag, Pageable pageable);
}
