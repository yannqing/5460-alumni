package com.cmswe.alumni.search.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.cmswe.alumni.api.search.SchoolSearchService;
import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.enums.SearchType;
import com.cmswe.alumni.common.vo.search.SearchResultItem;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.search.document.SchoolDocument;
import com.cmswe.alumni.search.repository.SchoolDocumentRepository;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 母校搜索服务实现
 *
 * @author CNI Alumni System
 */
@Slf4j
@Service("schoolSearchService")
public class SchoolSearchServiceImpl implements SchoolSearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private SchoolDocumentRepository schoolRepository;

    @Resource
    private RedisCache redisCache;

    @Autowired
    @Qualifier("searchResultCache")
    private Cache<String, Object> localCache;

    @Resource
    private com.cmswe.alumni.service.association.mapper.SchoolMapper schoolMapper;

    private static final String CACHE_PREFIX = "search:school:";
    private static final long CACHE_TTL = 5; // 分钟

    @Override
    public Page<SearchResultItem> searchSchool(
            String keyword,
            SearchFilter filter,
            Integer pageNum,
            Integer pageSize,
            Boolean highlight) {

        long startTime = System.currentTimeMillis();

        // 1. 构建缓存 Key
        String cacheKey = buildCacheKey(keyword, filter, pageNum, pageSize, highlight);

        // 2. 检查本地缓存（L1）
        Object cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("命中本地缓存, key={}, took={}ms", cacheKey, System.currentTimeMillis() - startTime);
            return (Page<SearchResultItem>) cached;
        }

        // 3. 检查 Redis 缓存（L2）
        cached = redisCache.getCacheObject(cacheKey);
        if (cached != null) {
            log.debug("命中Redis缓存, key={}, took={}ms", cacheKey, System.currentTimeMillis() - startTime);
            localCache.put(cacheKey, cached);
            return (Page<SearchResultItem>) cached;
        }

        // 4. 构建 ES 查询
        NativeQuery query = buildSchoolQuery(keyword, filter, pageNum, pageSize, highlight);

        // 5. 执行搜索
        SearchHits<SchoolDocument> searchHits = elasticsearchOperations.search(
                query,
                SchoolDocument.class);

        log.info("ES查询完成(母校), keyword={}, total={}, took={}ms",
                keyword, searchHits.getTotalHits(), System.currentTimeMillis() - startTime);

        // 6. 转换结果
        List<SearchResultItem> items = searchHits.getSearchHits().stream()
                .map(hit -> {
                    SchoolDocument doc = hit.getContent();

                    // 构建高亮文本
                    String highlightText = buildHighlightText(hit);

                    // 构建额外信息 Map
                    Map<String, Object> extra = new HashMap<>();
                    extra.put("description", doc.getDescription());
                    extra.put("level", doc.getLevel());
                    extra.put("city", doc.getCity());
                    extra.put("province", doc.getProvince());
                    extra.put("location", doc.getLocation());
                    extra.put("foundingDate", doc.getFoundingDate());
                    extra.put("officialCertification", doc.getOfficialCertification());
                    extra.put("schoolCode", doc.getSchoolCode());
                    extra.put("previousName", doc.getPreviousName());
                    extra.put("mergedInstitutions", doc.getMergedInstitutions());

                    return SearchResultItem.builder()
                            .type(SearchType.SCHOOL)
                            .id(String.valueOf(doc.getSchoolId()))
                            .title(doc.getSchoolName())
                            .subtitle(doc.getLevel())
                            .avatar(doc.getLogo())
                            .highlightText(highlightText)
                            .score(hit.getScore())
                            .extra(extra)
                            .createTime(doc.getCreateTime() != null ? doc.getCreateTime().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());

        // 7. 构建分页结果
        PageImpl<SearchResultItem> result = new PageImpl<>(
                items,
                PageRequest.of(pageNum - 1, pageSize),
                searchHits.getTotalHits());

        // 8. 存入缓存
        redisCache.setCacheObject(cacheKey, result, (int) CACHE_TTL, TimeUnit.MINUTES);
        localCache.put(cacheKey, result);

        log.info("搜索完成(母校), keyword={}, total={}, returned={}, took={}ms",
                keyword, result.getTotalElements(), items.size(),
                System.currentTimeMillis() - startTime);

        return result;
    }

    /**
     * 构建母校搜索查询
     */
    private NativeQuery buildSchoolQuery(
            String keyword,
            SearchFilter filter,
            Integer pageNum,
            Integer pageSize,
            Boolean highlight) {

        NativeQueryBuilder queryBuilder = NativeQuery.builder();

        // 1. 构建复合查询
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 1.1 多字段搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            boolQueryBuilder.must(Query.of(q -> q.multiMatch(m -> m
                    .query(keyword)
                    .fields("schoolName^3", "previousName^2", "description", "location", "mergedInstitutions^1.5",
                            "level^1.5")
                    .fuzziness("AUTO")
                    .prefixLength(1))));
        }

        // 1.2 只搜索启用的母校
        boolQueryBuilder.filter(f -> f.term(t -> t.field("status").value(1)));

        // 1.3 应用过滤条件
        if (filter != null) {
            if (filter.getProvince() != null && !filter.getProvince().isEmpty()) {
                boolQueryBuilder.filter(f -> f.term(t -> t.field("province").value(filter.getProvince())));
            }
            if (filter.getCity() != null && !filter.getCity().isEmpty()) {
                boolQueryBuilder.filter(f -> f.term(t -> t.field("city").value(filter.getCity())));
            }
        }

        queryBuilder.withQuery(Query.of(q -> q.bool(boolQueryBuilder.build())));

        // 2. 分页
        queryBuilder.withPageable(PageRequest.of(pageNum - 1, pageSize));

        // 3. 高亮
        if (highlight != null && highlight) {
            queryBuilder.withHighlightQuery(new HighlightQuery(
                    new Highlight(List.of(
                            new HighlightField("schoolName", HighlightFieldParameters.builder()
                                    .withPreTags("<em class='highlight'>")
                                    .withPostTags("</em>")
                                    .build()),
                            new HighlightField("description", HighlightFieldParameters.builder()
                                    .withPreTags("<em class='highlight'>")
                                    .withPostTags("</em>")
                                    .build()))),
                    SchoolDocument.class));
        }

        return queryBuilder.build();
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String keyword, SearchFilter filter, Integer pageNum, Integer pageSize,
            Boolean highlight) {
        return CACHE_PREFIX + keyword + ":" +
                (filter != null ? filter.toString() : "null") + ":" +
                pageNum + ":" + pageSize + ":" + highlight;
    }

    @Override
    public void indexSchool(Long schoolId) {
        try {
            log.info("索引母校数据: schoolId={}", schoolId);

            // 1. 从数据库查询母校信息
            com.cmswe.alumni.common.entity.School school = schoolMapper.selectById(schoolId);

            if (school == null) {
                log.warn("母校不存在: schoolId={}", schoolId);
                return;
            }

            // 2. 转换为 ES Document
            SchoolDocument document = SchoolDocument.builder()
                    .schoolId(school.getSchoolId())
                    .logo(school.getLogo())
                    .schoolName(school.getSchoolName())
                    .schoolCode(school.getSchoolCode())
                    .headquartersId(school.getHeadquartersId())
                    .province(school.getProvince())
                    .city(school.getCity())
                    .level(school.getLevel())
                    .mergedInstitutions(school.getMergedInstitutions())
                    .previousName(school.getPreviousName())
                    .otherInfo(school.getOtherInfo())
                    .description(school.getDescription())
                    .foundingDate(school.getFoundingDate())
                    .location(school.getLocation())
                    .officialCertification(school.getOfficialCertification())
                    .status(school.getStatus())
                    .createTime(school.getCreateTime())
                    .updateTime(school.getUpdateTime())
                    .build();

            // 3. 保存到 ES
            schoolRepository.save(document);

            log.info("母校索引成功: schoolId={}", schoolId);

        } catch (Exception e) {
            log.error("索引母校失败: schoolId={}", schoolId, e);
            throw new RuntimeException("索引母校失败", e);
        }
    }

    @Override
    public void batchIndexSchool(Iterable<Long> schoolIds) {
        log.info("批量索引母校数据");
        schoolIds.forEach(this::indexSchool);
    }

    @Override
    public void deleteSchool(Long schoolId) {
        schoolRepository.deleteById(schoolId);
        evictAllCache();
        log.info("删除母校索引: schoolId={}", schoolId);
    }

    @Override
    public void rebuildIndex() {
        log.info("========================================");
        log.info("开始全量重建母校 Elasticsearch 索引");
        log.info("========================================");

        long startTime = System.currentTimeMillis();
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            // 1. 删除旧索引数据
            log.info("步骤 1/3: 清空旧索引数据...");
            schoolRepository.deleteAll();
            log.info("旧索引数据已清空");

            // 2. 查询所有母校（未删除的）
            log.info("步骤 2/3: 查询数据库中的母校数据...");
            List<com.cmswe.alumni.common.entity.School> allSchools = schoolMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.cmswe.alumni.common.entity.School>()
                            .eq(com.cmswe.alumni.common.entity.School::getIsDelete, 0));
            totalCount = allSchools.size();
            log.info("查询到 {} 个母校，准备同步到 ES...", totalCount);

            // 3. 批量处理
            log.info("步骤 3/3: 批量索引到 Elasticsearch...");
            List<SchoolDocument> documents = new ArrayList<>();

            for (com.cmswe.alumni.common.entity.School school : allSchools) {
                try {
                    SchoolDocument document = SchoolDocument.builder()
                            .schoolId(school.getSchoolId())
                            .logo(school.getLogo())
                            .schoolName(school.getSchoolName())
                            .schoolCode(school.getSchoolCode())
                            .headquartersId(school.getHeadquartersId())
                            .province(school.getProvince())
                            .city(school.getCity())
                            .level(school.getLevel())
                            .mergedInstitutions(school.getMergedInstitutions())
                            .previousName(school.getPreviousName())
                            .otherInfo(school.getOtherInfo())
                            .description(school.getDescription())
                            .foundingDate(school.getFoundingDate())
                            .location(school.getLocation())
                            .officialCertification(school.getOfficialCertification())
                            .status(school.getStatus())
                            .createTime(school.getCreateTime())
                            .updateTime(school.getUpdateTime())
                            .build();

                    documents.add(document);
                    successCount++;

                    // 每 100 条批量保存一次
                    if (documents.size() >= 100) {
                        schoolRepository.saveAll(documents);
                        log.debug("已索引 {} 个母校", successCount);
                        documents.clear();
                    }

                } catch (Exception e) {
                    log.error("索引母校失败: schoolId={}", school.getSchoolId(), e);
                    failCount++;
                }
            }

            // 保存剩余的文档
            if (!documents.isEmpty()) {
                schoolRepository.saveAll(documents);
                log.debug("已索引剩余 {} 个母校", documents.size());
            }

            // 4. 清除所有搜索缓存
            evictAllCache();

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            log.info("========================================");
            log.info("母校索引重建完成！");
            log.info("总母校数: {}", totalCount);
            log.info("成功索引: {}", successCount);
            log.info("失败数量: {}", failCount);
            log.info("耗时: {} 秒", duration);
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("重建母校索引失败");
            log.error("已处理: {}/{}", successCount, totalCount);
            log.error("========================================", e);
            throw new RuntimeException("重建母校索引失败", e);
        }
    }

    /**
     * 构建高亮文本
     */
    private String buildHighlightText(org.springframework.data.elasticsearch.core.SearchHit<SchoolDocument> hit) {
        if (hit.getHighlightFields() == null || hit.getHighlightFields().isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        hit.getHighlightFields().forEach((field, highlights) -> {
            if (!highlights.isEmpty()) {
                sb.append(highlights.get(0)).append("... ");
            }
        });

        return sb.length() > 0 ? sb.toString().trim() : null;
    }

    /**
     * 清除所有缓存
     */
    private void evictAllCache() {
        String pattern = CACHE_PREFIX + "*";
        Collection<String> keys = redisCache.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            keys.forEach(redisCache::deleteObject);
            log.info("已清除 {} 个母校搜索缓存", keys.size());
        }
        localCache.invalidateAll();
    }
}
