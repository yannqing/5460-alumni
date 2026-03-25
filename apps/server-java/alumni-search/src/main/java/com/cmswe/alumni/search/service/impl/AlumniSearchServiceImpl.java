package com.cmswe.alumni.search.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmswe.alumni.api.search.AlumniSearchService;
import com.cmswe.alumni.common.dto.QueryAlumniListDto;
import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.entity.AlumniInfo;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.UserListResponse;
import com.cmswe.alumni.common.vo.search.SearchResultItem;
import com.cmswe.alumni.search.converter.AlumniConverter;
import com.cmswe.alumni.search.document.AlumniDocument;
import com.cmswe.alumni.search.repository.AlumniDocumentRepository;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.search.util.SearchQueryBuilder;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 校友搜索服务实现
 *
 * @author CNI Alumni System
 */
@Slf4j
@Service("alumniSearchService")
public class AlumniSearchServiceImpl implements AlumniSearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private AlumniDocumentRepository alumniRepository;

    @Resource
    private RedisCache redisCache;

    @Autowired
    @Qualifier("searchResultCache")
    private Cache<String, Object> localCache;

    @Resource
    private com.cmswe.alumni.service.user.mapper.WxUserMapper wxUserMapper;

    @Resource
    private com.cmswe.alumni.service.user.mapper.WxUserInfoMapper wxUserInfoMapper;

    @Resource
    private com.cmswe.alumni.search.service.sync.DataMergeService dataMergeService;

    @Resource
    private com.cmswe.alumni.api.user.UserFollowService userFollowService;

    @Resource
    private com.cmswe.alumni.api.user.UserPrivacySettingService userPrivacySettingService;

    @Resource
    private com.cmswe.alumni.search.service.enrich.UserDataEnrichService userDataEnrichService;

    private static final String CACHE_PREFIX = "search:alumni:";
    private static final String ALUMNI_LIST_CACHE_PREFIX = "search:alumniList:";
    private static final long CACHE_TTL = 5; // 分钟

    @Override
    public org.springframework.data.domain.Page<SearchResultItem> searchAlumni(
            String keyword,
            SearchFilter filter,
            Integer pageNum,
            Integer pageSize,
            Boolean highlight) {

        long startTime = System.currentTimeMillis();

        // 1. 构建缓存 Key
        String cacheKey = SearchQueryBuilder.buildCacheKey(
                CACHE_PREFIX,
                keyword,
                filter,
                pageNum,
                pageSize,
                highlight);

        // 2. 检查本地缓存（L1）
        Object cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("命中本地缓存, key={}, took={}ms", cacheKey, System.currentTimeMillis() - startTime);
            return (org.springframework.data.domain.Page<SearchResultItem>) cached;
        }

        // 3. 检查 Redis 缓存（L2）
        cached = redisCache.getCacheObject(cacheKey);
        if (cached != null) {
            log.debug("命中Redis缓存, key={}, took={}ms", cacheKey, System.currentTimeMillis() - startTime);
            localCache.put(cacheKey, cached);
            return (org.springframework.data.domain.Page<SearchResultItem>) cached;
        }

        // 4. 构建 ES 查询
        NativeQuery query = SearchQueryBuilder.buildAlumniQuery(
                keyword, filter, pageNum, pageSize, highlight);

        // 5. 执行搜索
        SearchHits<AlumniDocument> searchHits = elasticsearchOperations.search(
                query,
                AlumniDocument.class);

        log.info("ES查询完成, keyword={}, total={}, took={}ms",
                keyword, searchHits.getTotalHits(), System.currentTimeMillis() - startTime);

        // 6. 转换结果并应用隐私过滤
        List<SearchResultItem> items = searchHits.getSearchHits().stream()
                .filter(hit -> hit.getContent().getSearchable() != null && hit.getContent().getSearchable())
                .map(AlumniConverter::toSearchResultItem)
                .collect(Collectors.toList());

        // 7. 构建分页结果
        PageImpl<SearchResultItem> result = new PageImpl<>(
                items,
                PageRequest.of(pageNum - 1, pageSize),
                searchHits.getTotalHits());

        // 8. 存入缓存
        redisCache.setCacheObject(cacheKey, result, (int) CACHE_TTL, TimeUnit.MINUTES);
        localCache.put(cacheKey, result);

        log.info("搜索完成, keyword={}, total={}, returned={}, took={}ms",
                keyword, result.getTotalElements(), items.size(),
                System.currentTimeMillis() - startTime);

        return result;
    }

    @Override
    public org.springframework.data.domain.Page<SearchResultItem> searchNearbyAlumni(
            Double latitude,
            Double longitude,
            Integer radius,
            Integer pageNum,
            Integer pageSize) {

        log.info("地理位置搜索: lat={}, lon={}, radius={}km", latitude, longitude, radius);

        // 构建地理位置查询
        NativeQuery query = SearchQueryBuilder.buildGeoQuery(
                latitude, longitude, radius, pageNum, pageSize);

        // 执行搜索
        SearchHits<AlumniDocument> searchHits = elasticsearchOperations.search(
                query,
                AlumniDocument.class);

        // 转换结果
        List<SearchResultItem> items = AlumniConverter.toSearchResultItems(
                searchHits.getSearchHits());

        return new PageImpl<>(
                items,
                PageRequest.of(pageNum - 1, pageSize),
                searchHits.getTotalHits());
    }

    @Override
    public void indexAlumni(Long alumniId) {
        try {
            log.info("索引校友数据: alumniId={}", alumniId);

            // 1. 从数据库查询校友信息（实际项目中使用 Mapper）
            // AlumniInfo alumniInfo = alumniInfoMapper.selectById(alumniId);
            // WxUser wxUser = wxUserMapper.selectById(alumniInfo.getUserId());
            // WxUserInfo wxUserInfo = wxUserInfoMapper.selectById(alumniInfo.getUserId());

            // 示例：这里使用模拟数据
            AlumniInfo alumniInfo = new AlumniInfo();
            alumniInfo.setAlumniId(alumniId);
            alumniInfo.setUserId(1L);
            alumniInfo.setRealName("张三");
            alumniInfo.setCertificationStatus(1); // 1-已认证

            WxUser wxUser = new WxUser();
            WxUserInfo wxUserInfo = new WxUserInfo();

            // 2. 转换为 ES Document
            AlumniDocument document = AlumniConverter.toDocument(alumniInfo, wxUser, wxUserInfo);

            // 3. 保存到 ES
            alumniRepository.save(document);

            // 4. 清除相关缓存
            evictCache(alumniId);

            log.info("校友索引成功: alumniId={}", alumniId);

        } catch (Exception e) {
            log.error("索引校友失败: alumniId={}", alumniId, e);
            throw new RuntimeException("索引校友失败", e);
        }
    }

    @Override
    public void batchIndexAlumni(Iterable<Long> alumniIds) {
        log.info("批量索引校友数据");

        List<AlumniDocument> documents = new ArrayList<>();
        for (Long alumniId : alumniIds) {
            try {
                // 查询并转换（实际项目中批量查询优化）
                // 这里简化处理
                AlumniInfo alumniInfo = new AlumniInfo();
                alumniInfo.setAlumniId(alumniId);

                AlumniDocument document = AlumniConverter.toDocument(
                        alumniInfo, new WxUser(), new WxUserInfo());
                documents.add(document);

            } catch (Exception e) {
                log.error("转换校友文档失败: alumniId={}", alumniId, e);
            }
        }

        // 批量保存
        alumniRepository.saveAll(documents);

        // 清除所有搜索缓存
        evictAllCache();

        log.info("批量索引完成, count={}", documents.size());
    }

    @Override
    public void deleteAlumni(Long alumniId) {
        log.info("删除校友索引: alumniId={}", alumniId);

        alumniRepository.deleteById(alumniId);
        evictCache(alumniId);

        log.info("删除校友索引成功: alumniId={}", alumniId);
    }

    @Override
    public void rebuildIndex() {
        log.info("========================================");
        log.info("开始全量重建 Elasticsearch 索引");
        log.info("========================================");

        long startTime = System.currentTimeMillis();
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            // 1. 删除旧索引数据
            log.info("步骤 1/3: 清空旧索引数据...");
            alumniRepository.deleteAll();
            log.info("旧索引数据已清空");

            // 2. 查询所有微信用户（未删除的）
            log.info("步骤 2/3: 查询数据库中的用户数据...");
            List<WxUser> allUsers = wxUserMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WxUser>()
                            .eq(WxUser::getIsDelete, 0));
            totalCount = allUsers.size();
            log.info("查询到 {} 个用户，准备同步到 ES...", totalCount);

            // 3. 批量处理，使用 DataMergeService 合并数据并索引
            log.info("步骤 3/3: 批量索引到 Elasticsearch...");
            List<AlumniDocument> documents = new ArrayList<>();

            for (WxUser wxUser : allUsers) {
                try {
                    Long wxId = wxUser.getWxId();

                    // 使用 DataMergeService 合并多表数据
                    AlumniDocument document = dataMergeService.mergeToDocument(wxId);

                    if (document != null) {
                        documents.add(document);
                        successCount++;

                        // 每 100 条批量保存一次
                        if (documents.size() >= 100) {
                            alumniRepository.saveAll(documents);
                            log.debug("已索引 {} 个文档", successCount);
                            documents.clear();
                        }
                    } else {
                        log.warn("数据合并失败，跳过 wxId: {}", wxId);
                        failCount++;
                    }

                } catch (Exception e) {
                    log.error("索引用户失败: wxId={}", wxUser.getWxId(), e);
                    failCount++;
                }
            }

            // 保存剩余的文档
            if (!documents.isEmpty()) {
                alumniRepository.saveAll(documents);
                log.debug("已索引剩余 {} 个文档", documents.size());
            }

            // 4. 清除所有搜索缓存
            evictAllCache();

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            log.info("========================================");
            log.info("索引重建完成！");
            log.info("总用户数: {}", totalCount);
            log.info("成功索引: {}", successCount);
            log.info("失败数量: {}", failCount);
            log.info("耗时: {} 秒", duration);
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("重建索引失败");
            log.error("已处理: {}/{}", successCount, totalCount);
            log.error("========================================", e);
            throw new RuntimeException("重建索引失败", e);
        }
    }

    /**
     * 清除指定校友的缓存
     */
    private void evictCache(Long alumniId) {
        // 删除所有包含该校友的缓存（模糊匹配）
        String pattern = CACHE_PREFIX + "*";
        Collection<String> keys = redisCache.keys(pattern);
        if (keys != null) {
            keys.forEach(key -> {
                if (key.contains(alumniId.toString())) {
                    redisCache.deleteObject(key);
                }
            });
        }

        // 清除本地缓存（全部清除，因为 Caffeine 不支持模糊删除）
        localCache.invalidateAll();
    }

    /**
     * 清除所有搜索缓存
     */
    private void evictAllCache() {
        String pattern = CACHE_PREFIX + "*";
        Collection<String> keys = redisCache.keys(pattern);
        if (keys != null) {
            keys.forEach(redisCache::deleteObject);
        }
        localCache.invalidateAll();
    }

    /**
     * 查询校友列表（兼容原 MySQL 查询接口）
     * 使用 Elasticsearch 查询，与 UserService.queryAlumniList() 保持入参出参一致
     *
     * @param queryAlumniListDto 查询条件（与 MySQL 查询保持一致）
     * @param wxId 当前用户ID（用于"我的关注"筛选，可为null）
     * @return 用户列表（与 MySQL 返回格式一致，使用 MyBatis-Plus 的 Page）
     */
    @Override
    public Page<UserListResponse> queryAlumniList(QueryAlumniListDto queryAlumniListDto, Long wxId) {
        long startTime = System.currentTimeMillis();

        // 1. 参数校验
        if (queryAlumniListDto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        log.info("ES 查询校友列表开始 - 参数: {}, 用户ID: {}", queryAlumniListDto, wxId);

        // 2. 处理"我的关注"筛选：查询用户关注的校友 ID 列表
        List<Long> followedUserIds = null;
        Integer myFollow = queryAlumniListDto.getMyFollow();
        if (myFollow != null && myFollow == 1 && wxId != null) {
            followedUserIds = userFollowService.getFollowedTargetIds(wxId, 1); // 1-用户

            // 如果用户没有关注任何校友，直接返回空结果
            if (followedUserIds.isEmpty()) {
                log.info("用户 {} 未关注任何校友，返回空结果", wxId);
                Page<UserListResponse> emptyPage = new Page<>(
                        queryAlumniListDto.getCurrent(),
                        queryAlumniListDto.getPageSize(),
                        0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
            log.debug("用户 {} 关注了 {} 个校友", wxId, followedUserIds.size());
        }

        // 3. 构建 ES 查询
        NativeQuery esQuery = SearchQueryBuilder.buildAlumniListQuery(
                queryAlumniListDto, followedUserIds);

        // 4. 执行 ES 查询
        SearchHits<AlumniDocument> searchHits = elasticsearchOperations.search(
                esQuery,
                AlumniDocument.class);

        long esQueryTime = System.currentTimeMillis() - startTime;
        log.info("ES 查询完成 - 总命中数: {}, 耗时: {}ms",
                searchHits.getTotalHits(), esQueryTime);

        // 5. 转换结果为 UserListResponse 格式
        List<AlumniDocument> documents = searchHits.getSearchHits().stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .collect(Collectors.toList());

        List<UserListResponse> responses = AlumniConverter.toUserListResponses(documents);

        // 6. 数据增强：补充学校信息和完整教育经历（从数据库查询）
        userDataEnrichService.enrichUserListResponses(responses);

        // 7. 批量预热隐私设置缓存（性能优化：避免 AOP 处理时的 N+1 查询）
        if (!responses.isEmpty()) {
            List<Long> wxIds = responses.stream()
                    .map(r -> Long.parseLong(r.getWxId()))
                    .distinct()
                    .collect(Collectors.toList());
            userPrivacySettingService.batchWarmupCache(wxIds);
            log.debug("批量预热隐私缓存完成 - 用户数: {}", wxIds.size());
        }

        // 8. 构建 MyBatis-Plus Page 对象（与 MySQL 查询返回格式一致）
        Page<UserListResponse> resultPage = new Page<>(
                queryAlumniListDto.getCurrent(),
                queryAlumniListDto.getPageSize(),
                searchHits.getTotalHits());
        resultPage.setRecords(responses);

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("ES 查询校友列表完成 - 返回 {} 条记录，总耗时: {}ms", responses.size(), totalTime);

        return resultPage;
    }
}
