package com.cmswe.alumni.search.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.cmswe.alumni.api.search.MerchantSearchService;
import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.enums.SearchType;
import com.cmswe.alumni.common.vo.search.SearchResultItem;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.search.document.MerchantDocument;
import com.cmswe.alumni.search.repository.MerchantDocumentRepository;
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
 * 商户搜索服务实现
 *
 * @author CNI Alumni System
 */
@Slf4j
@Service("merchantSearchService")
public class MerchantSearchServiceImpl implements MerchantSearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private MerchantDocumentRepository merchantRepository;

    @Resource
    private RedisCache redisCache;

    @Autowired
    @Qualifier("searchResultCache")
    private Cache<String, Object> localCache;

    @Resource
    private com.cmswe.alumni.search.mapper.MerchantMapper merchantMapper;

    private static final String CACHE_PREFIX = "search:merchant:";
    private static final long CACHE_TTL = 5; // 分钟

    @Override
    public Page<SearchResultItem> searchMerchant(
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
        NativeQuery query = buildMerchantQuery(keyword, filter, pageNum, pageSize, highlight);

        // 5. 执行搜索
        SearchHits<MerchantDocument> searchHits = elasticsearchOperations.search(
                query,
                MerchantDocument.class);

        log.info("ES查询完成(商户), keyword={}, total={}, took={}ms",
                keyword, searchHits.getTotalHits(), System.currentTimeMillis() - startTime);

        // 6. 转换结果
        List<SearchResultItem> items = searchHits.getSearchHits().stream()
                .map(hit -> {
                    MerchantDocument doc = hit.getContent();

                    // 构建高亮文本
                    String highlightText = buildHighlightText(hit);

                    // 构建额外信息 Map
                    Map<String, Object> extra = new HashMap<>();
                    extra.put("description", doc.getDescription());
                    extra.put("industry", doc.getIndustry());
                    extra.put("city", doc.getCity());
                    extra.put("address", doc.getAddress());
                    extra.put("memberTier", doc.getMemberTier());
                    extra.put("rating", doc.getRating());
                    extra.put("ownerName", doc.getOwnerName());
                    extra.put("contactPhone", doc.getContactPhone());

                    return SearchResultItem.builder()
                            .type(SearchType.MERCHANT)
                            .id(String.valueOf(doc.getMerchantId()))
                            .title(doc.getMerchantName())
                            .subtitle(doc.getIndustry())
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

        log.info("搜索完成(商户), keyword={}, total={}, returned={}, took={}ms",
                keyword, result.getTotalElements(), items.size(),
                System.currentTimeMillis() - startTime);

        return result;
    }

    /**
     * 构建商户搜索查询
     */
    private NativeQuery buildMerchantQuery(
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
                    .fields("merchantName^3", "industry^2", "description", "ownerName", "address", "tags^1.5")
                    .fuzziness("AUTO")
                    .prefixLength(1))));
        }

        // 1.2 只搜索审核通过的商户
        boolQueryBuilder.filter(f -> f.term(t -> t.field("reviewStatus").value("APPROVED")));

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
                            new HighlightField("merchantName", HighlightFieldParameters.builder()
                                    .withPreTags("<em class='highlight'>")
                                    .withPostTags("</em>")
                                    .build()),
                            new HighlightField("description", HighlightFieldParameters.builder()
                                    .withPreTags("<em class='highlight'>")
                                    .withPostTags("</em>")
                                    .build()))),
                    MerchantDocument.class));
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
    public void indexMerchant(Long merchantId) {
        try {
            log.info("索引商户数据: merchantId={}", merchantId);

            // 1. 从数据库查询商户信息
            com.cmswe.alumni.common.entity.Merchant merchant = merchantMapper.selectById(merchantId);

            if (merchant == null) {
                log.warn("商户不存在: merchantId={}", merchantId);
                return;
            }

            // 2. 转换为 ES Document
            MerchantDocument document = MerchantDocument.builder()
                    .merchantId(merchant.getMerchantId())
                    .merchantName(merchant.getMerchantName())
                    .merchantType(merchant.getMerchantType() != null ? merchant.getMerchantType().toString() : null)
                    .industry(merchant.getBusinessCategory())
                    .description(merchant.getBusinessScope())
                    .ownerName(merchant.getLegalPerson())
                    .userId(merchant.getUserId())
                    .businessLicense(merchant.getUnifiedSocialCreditCode())
                    .contactPhone(merchant.getContactPhone())
                    .email(merchant.getContactEmail())
                    .memberTier(merchant.getMemberTier() != null ? merchant.getMemberTier().toString() : null)
                    .rating(merchant.getRatingScore() != null ? merchant.getRatingScore().floatValue() : null)
                    .reviewStatus(merchant.getReviewStatus() != null ? merchant.getReviewStatus().toString() : "0")
                    .status(merchant.getStatus() != null ? merchant.getStatus().toString() : "0")
                    .createTime(merchant.getCreateTime())
                    .updateTime(merchant.getUpdateTime())
                    .build();

            // 3. 保存到 ES
            merchantRepository.save(document);

            log.info("商户索引成功: merchantId={}", merchantId);

        } catch (Exception e) {
            log.error("索引商户失败: merchantId={}", merchantId, e);
            throw new RuntimeException("索引商户失败", e);
        }
    }

    @Override
    public void batchIndexMerchant(Iterable<Long> merchantIds) {
        log.info("批量索引商户数据");
        merchantIds.forEach(this::indexMerchant);
    }

    @Override
    public void deleteMerchant(Long merchantId) {
        merchantRepository.deleteById(merchantId);
        evictAllCache();
        log.info("删除商户索引: merchantId={}", merchantId);
    }

    @Override
    public void rebuildIndex() {
        log.info("========================================");
        log.info("开始全量重建商户 Elasticsearch 索引");
        log.info("========================================");

        long startTime = System.currentTimeMillis();
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            // 1. 删除旧索引数据
            log.info("步骤 1/3: 清空旧索引数据...");
            merchantRepository.deleteAll();
            log.info("旧索引数据已清空");

            // 2. 查询所有商户（未删除且已审核通过的）
            log.info("步骤 2/3: 查询数据库中的商户数据...");
            List<com.cmswe.alumni.common.entity.Merchant> allMerchants = merchantMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.cmswe.alumni.common.entity.Merchant>()
                            .eq(com.cmswe.alumni.common.entity.Merchant::getIsDelete, 0));
            totalCount = allMerchants.size();
            log.info("查询到 {} 个商户，准备同步到 ES...", totalCount);

            // 3. 批量处理
            log.info("步骤 3/3: 批量索引到 Elasticsearch...");
            List<MerchantDocument> documents = new ArrayList<>();

            for (com.cmswe.alumni.common.entity.Merchant merchant : allMerchants) {
                try {
                    MerchantDocument document = MerchantDocument.builder()
                            .merchantId(merchant.getMerchantId())
                            .merchantName(merchant.getMerchantName())
                            .merchantType(
                                    merchant.getMerchantType() != null ? merchant.getMerchantType().toString() : null)
                            .industry(merchant.getBusinessCategory()) // 经营类目映射到industry
                            .description(merchant.getBusinessScope()) // 经营范围映射到description
                            .ownerName(merchant.getLegalPerson()) // 法人姓名映射到ownerName
                            .userId(merchant.getUserId())
                            .businessLicense(merchant.getUnifiedSocialCreditCode())
                            .contactPhone(merchant.getContactPhone())
                            .email(merchant.getContactEmail())
                            .memberTier(merchant.getMemberTier() != null ? merchant.getMemberTier().toString() : null)
                            .rating(merchant.getRatingScore() != null ? merchant.getRatingScore().floatValue() : null)
                            .reviewStatus(
                                    merchant.getReviewStatus() != null ? merchant.getReviewStatus().toString() : "0")
                            .status(merchant.getStatus() != null ? merchant.getStatus().toString() : "0")
                            .createTime(merchant.getCreateTime())
                            .updateTime(merchant.getUpdateTime())
                            .build();

                    documents.add(document);
                    successCount++;

                    // 每 100 条批量保存一次
                    if (documents.size() >= 100) {
                        merchantRepository.saveAll(documents);
                        log.debug("已索引 {} 个商户", successCount);
                        documents.clear();
                    }

                } catch (Exception e) {
                    log.error("索引商户失败: merchantId={}", merchant.getMerchantId(), e);
                    failCount++;
                }
            }

            // 保存剩余的文档
            if (!documents.isEmpty()) {
                merchantRepository.saveAll(documents);
                log.debug("已索引剩余 {} 个商户", documents.size());
            }

            // 4. 清除所有搜索缓存
            evictAllCache();

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            log.info("========================================");
            log.info("商户索引重建完成！");
            log.info("总商户数: {}", totalCount);
            log.info("成功索引: {}", successCount);
            log.info("失败数量: {}", failCount);
            log.info("耗时: {} 秒", duration);
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("重建商户索引失败");
            log.error("已处理: {}/{}", successCount, totalCount);
            log.error("========================================", e);
            throw new RuntimeException("重建商户索引失败", e);
        }
    }

    /**
     * 构建高亮文本
     */
    private String buildHighlightText(org.springframework.data.elasticsearch.core.SearchHit<MerchantDocument> hit) {
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
            log.info("已清除 {} 个商户搜索缓存", keys.size());
        }
        localCache.invalidateAll();
    }
}
