package com.cmswe.alumni.search.service.impl;

import com.cmswe.alumni.api.search.AlumniSearchService;
import com.cmswe.alumni.api.search.UnifiedSearchService;
import com.cmswe.alumni.common.dto.search.UnifiedSearchRequest;
import com.cmswe.alumni.common.entity.SearchHistory;
import com.cmswe.alumni.common.entity.SearchLog;
import com.cmswe.alumni.common.entity.SearchNoResultQuery;
import com.cmswe.alumni.common.entity.SearchSuggestion;
import com.cmswe.alumni.common.enums.SearchType;
import com.cmswe.alumni.common.vo.search.*;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.search.mapper.SearchHistoryMapper;
import com.cmswe.alumni.search.mapper.SearchLogMapper;
import com.cmswe.alumni.search.mapper.SearchNoResultQueryMapper;
import com.cmswe.alumni.search.mapper.SearchSuggestionMapper;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 统一搜索服务实现
 *
 * @author CNI Alumni System
 */
@Slf4j
@Service("unifiedSearchService")
public class UnifiedSearchServiceImpl implements UnifiedSearchService {

    @Autowired
    @Qualifier("alumniSearchService")
    private AlumniSearchService alumniSearchService;

    @Autowired
    @Qualifier("associationSearchService")
    private com.cmswe.alumni.api.search.AssociationSearchService associationSearchService;

    @Autowired
    @Qualifier("merchantSearchService")
    private com.cmswe.alumni.api.search.MerchantSearchService merchantSearchService;

    @Autowired
    @Qualifier("schoolSearchService")
    private com.cmswe.alumni.api.search.SchoolSearchService schoolSearchService;

    @Resource
    private RedisCache redisCache;

    @Resource
    @Qualifier("hotSearchCache")
    private Cache<String, Object> hotSearchCache;

    @Resource
    @Qualifier("suggestCache")
    private Cache<String, Object> suggestCache;

    // 数据库 Mapper
    @Resource
    private SearchHistoryMapper searchHistoryMapper;

    @Resource
    private SearchLogMapper searchLogMapper;

    @Resource
    private SearchSuggestionMapper searchSuggestionMapper;

    @Resource
    private SearchNoResultQueryMapper searchNoResultQueryMapper;

    private static final String HOT_SEARCH_PREFIX = "search:hot:";
    private static final String HISTORY_PREFIX = "search:history:";

    @Override
    public UnifiedSearchResponse search(UnifiedSearchRequest request) {
        long startTime = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().replace("-", "");
        boolean isSuccess = true;
        String errorMsg = null;

        log.info("统一搜索开始: traceId={}, keyword={}, types={}", traceId, request.getKeyword(), request.getTypes());

        Map<SearchType, Long> typeCounts = new HashMap<>();
        List<SearchResultItem> allItems = new ArrayList<>();

        try {
            // 处理 ALL 类型：将 ALL 展开为所有具体类型
            List<SearchType> actualTypes = new ArrayList<>();
            for (SearchType type : request.getTypes()) {
                if (type == SearchType.ALL) {
                    // ALL 表示搜索所有类型，展开为具体类型
                    actualTypes.add(SearchType.ALUMNI);
                    actualTypes.add(SearchType.ASSOCIATION);
                    actualTypes.add(SearchType.MERCHANT);
                    actualTypes.add(SearchType.SCHOOL);
                } else {
                    actualTypes.add(type);
                }
            }

            // 去重（避免重复搜索）
            actualTypes = actualTypes.stream().distinct().collect(Collectors.toList());

            log.debug("实际搜索类型: {}", actualTypes);

            // 并行搜索各个类型
            List<CompletableFuture<Page<SearchResultItem>>> futures = actualTypes.stream()
                    .map(type -> CompletableFuture.supplyAsync(() -> searchByType(type, request)))
                    .toList();

            // 等待所有搜索完成并聚合结果
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            futures.forEach(future -> {
                try {
                    Page<SearchResultItem> page = future.get();
                    if (page != null && page.hasContent()) {
                        allItems.addAll(page.getContent());
                        SearchType type = page.getContent().get(0).getType();
                        typeCounts.put(type, page.getTotalElements());
                    }
                } catch (Exception e) {
                    log.error("获取搜索结果失败", e);
                }
            });

            // 按相关性分数排序
            allItems.sort(Comparator.comparing(SearchResultItem::getScore,
                    Comparator.nullsLast(Comparator.reverseOrder())));

            // 分页截取
            int start = (request.getPageNum() - 1) * request.getPageSize();
            int end = Math.min(start + request.getPageSize(), allItems.size());
            List<SearchResultItem> pagedItems = start < allItems.size() ? allItems.subList(start, end)
                    : Collections.emptyList();

            // 构建响应
            UnifiedSearchResponse response = UnifiedSearchResponse.builder()
                    .total((long) allItems.size())
                    .items(pagedItems)
                    .typeCounts(typeCounts)
                    .metadata(SearchMetadata.builder()
                            .took(System.currentTimeMillis() - startTime)
                            .timedOut(false)
                            .fromCache(false)
                            .build())
                    .build();

            // 如果需要搜索建议
            if (request.getNeedSuggestions() != null && request.getNeedSuggestions()) {
                SuggestResponse suggestions = suggest(
                        request.getKeyword(),
                        request.getTypes().get(0),
                        5);
                response.setSuggestions(
                        suggestions.getSuggestions().stream()
                                .map(SuggestItem::getText)
                                .collect(Collectors.toList()));
            }

            // 记录热搜
            recordHotSearch(request.getKeyword(), request.getTypes().get(0));

            // 记录无结果查询（异步）
            if (response.getTotal() == 0) {
                recordNoResultQuery(request.getKeyword(), request.getTypes().get(0));
            }

            log.info("统一搜索完成: traceId={}, keyword={}, total={}, took={}ms",
                    traceId, request.getKeyword(), response.getTotal(),
                    System.currentTimeMillis() - startTime);

            return response;

        } catch (Exception e) {
            isSuccess = false;
            errorMsg = e.getMessage();
            log.error("统一搜索异常: traceId={}, keyword={}", traceId, request.getKeyword(), e);
            throw e;
        } finally {
            // 异步记录搜索日志（企业级标准）
            recordSearchLog(traceId, request, allItems.size(),
                    System.currentTimeMillis() - startTime, isSuccess, errorMsg);
        }
    }

    /**
     * 根据类型搜索
     */
    private Page<SearchResultItem> searchByType(SearchType type, UnifiedSearchRequest request) {
        try {
            switch (type) {
                case ALUMNI:
                    return alumniSearchService.searchAlumni(
                            request.getKeyword(),
                            request.getFilter(),
                            request.getPageNum(),
                            request.getPageSize(),
                            request.getHighlight());

                case ASSOCIATION:
                    return associationSearchService.searchAssociation(
                            request.getKeyword(),
                            request.getFilter(),
                            request.getPageNum(),
                            request.getPageSize(),
                            request.getHighlight());

                case MERCHANT:
                    return merchantSearchService.searchMerchant(
                            request.getKeyword(),
                            request.getFilter(),
                            request.getPageNum(),
                            request.getPageSize(),
                            request.getHighlight());

                case SCHOOL:
                    return schoolSearchService.searchSchool(
                            request.getKeyword(),
                            request.getFilter(),
                            request.getPageNum(),
                            request.getPageSize(),
                            request.getHighlight());

                default:
                    return Page.empty();
            }
        } catch (Exception e) {
            log.error("搜索失败: type={}, keyword={}", type, request.getKeyword(), e);
            return Page.empty();
        }
    }

    @Override
    public SuggestResponse suggest(String prefix, SearchType type, Integer size) {
        log.debug("搜索建议: prefix={}, type={}", prefix, type);

        // 检查缓存
        String cacheKey = "suggest:" + type.getCode() + ":" + prefix;
        Object cached = suggestCache.getIfPresent(cacheKey);
        if (cached != null) {
            return (SuggestResponse) cached;
        }

        List<SuggestItem> suggestions = new ArrayList<>();

        // 1. 优先从数据库获取人工配置的建议词（权重高）
        try {
            List<SearchSuggestion> manualSuggestions = searchSuggestionMapper.selectByPrefix(
                    prefix, type.getCode(), size);
            manualSuggestions.forEach(suggestion -> suggestions.add(
                    SuggestItem.builder()
                            .text(suggestion.getKeyword())
                            .highlightText(highlightPrefix(suggestion.getKeyword(), prefix))
                            .build()));
        } catch (Exception e) {
            log.warn("查询人工建议词失败: {}", e.getMessage());
        }

        // 2. 如果人工建议词不足，从热搜词中补充
        if (suggestions.size() < size) {
            Set<?> rawKeywords = redisCache.redisTemplate.opsForZSet()
                    .reverseRange(HOT_SEARCH_PREFIX + type.getCode(), 0, 99);
            Set<String> hotKeywords = rawKeywords != null ? rawKeywords.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet()) : Collections.emptySet();

            hotKeywords.stream()
                    .filter(keyword -> keyword.startsWith(prefix))
                    .filter(keyword -> suggestions.stream()
                            .noneMatch(s -> s.getText().equals(keyword))) // 去重
                    .limit(size - suggestions.size())
                    .forEach(keyword -> suggestions.add(
                            SuggestItem.builder()
                                    .text(keyword)
                                    .highlightText(highlightPrefix(keyword, prefix))
                                    .build()));
        }

        SuggestResponse response = SuggestResponse.builder()
                .suggestions(suggestions)
                .build();

        // 存入缓存
        suggestCache.put(cacheKey, response);

        return response;
    }

    @Override
    public void saveSearchHistory(Long userId, String keyword, SearchType type) {
        String key = HISTORY_PREFIX + userId;
        String value = type.getCode() + ":" + keyword;

        // 1. 写入 Redis（实时查询）
        redisCache.redisTemplate.opsForZSet().add(key, value, System.currentTimeMillis());

        // 只保留最近 20 条
        redisCache.redisTemplate.opsForZSet().removeRange(key, 0, -21);

        // 2. 异步写入 MySQL（持久化存储）
        CompletableFuture.runAsync(() -> {
            try {
                SearchHistory searchHistory = SearchHistory.builder()
                        .wxId(userId)
                        .keyword(keyword)
                        .searchType(type.getCode())
                        .resultCount(0) // 暂时为0，后续可以从搜索结果中获取
                        .fromSuggest(0)
                        .createdTime(LocalDateTime.now())
                        .updatedTime(LocalDateTime.now())
                        .build();
                searchHistoryMapper.insert(searchHistory);
                log.debug("搜索历史已持久化: userId={}, keyword={}", userId, keyword);
            } catch (Exception e) {
                log.error("搜索历史持久化失败: userId={}, keyword={}", userId, keyword, e);
            }
        });

        log.debug("保存搜索历史: userId={}, keyword={}, type={}", userId, keyword, type);
    }

    @Override
    public Page<String> getSearchHistory(Long userId, Integer limit) {
        String key = HISTORY_PREFIX + userId;

        // 1. 优先从 Redis 获取（快速查询）
        Set<?> rawHistory = redisCache.redisTemplate.opsForZSet()
                .reverseRange(key, 0, limit - 1);
        Set<String> history = rawHistory != null ? rawHistory.stream()
                .map(Object::toString)
                .collect(Collectors.toSet()) : Collections.emptySet();

        List<String> historyList;

        if (history != null && !history.isEmpty()) {
            // Redis 有数据，直接返回
            historyList = history.stream()
                    .map(s -> s.split(":", 2)[1]) // 去掉类型前缀
                    .collect(Collectors.toList());
        } else {
            // 2. Redis 为空，从 MySQL 加载并回填 Redis
            try {
                List<SearchHistory> dbHistory = searchHistoryMapper.selectUserHistory(userId, limit);
                historyList = dbHistory.stream()
                        .map(SearchHistory::getKeyword)
                        .collect(Collectors.toList());

                // 回填 Redis
                dbHistory.forEach(h -> {
                    String value = h.getSearchType() + ":" + h.getKeyword();
                    long timestamp = h.getCreatedTime().atZone(java.time.ZoneId.systemDefault())
                            .toInstant().toEpochMilli();
                    redisCache.redisTemplate.opsForZSet().add(key, value, timestamp);
                });

                log.debug("从MySQL加载搜索历史并回填Redis: userId={}, count={}", userId, dbHistory.size());
            } catch (Exception e) {
                log.error("从MySQL加载搜索历史失败: userId={}", userId, e);
                historyList = Collections.emptyList();
            }
        }

        return new PageImpl<>(historyList, PageRequest.of(0, limit), historyList.size());
    }

    @Override
    public void deleteSearchHistory(Long userId, String keyword) {
        String key = HISTORY_PREFIX + userId;

        if (keyword == null) {
            // 删除全部历史（Redis + MySQL）
            redisCache.deleteObject(key);

            // 异步删除 MySQL 数据（逻辑删除）
            CompletableFuture.runAsync(() -> {
                try {
                    searchHistoryMapper.delete(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchHistory>()
                                    .eq(SearchHistory::getWxId, userId));
                    log.info("MySQL搜索历史已清空: userId={}", userId);
                } catch (Exception e) {
                    log.error("MySQL搜索历史清空失败: userId={}", userId, e);
                }
            });

            log.info("清空搜索历史: userId={}", userId);
        } else {
            // 删除指定关键词（Redis + MySQL）
            Set<?> rawHistory = redisCache.redisTemplate.opsForZSet().range(key, 0, -1);
            if (rawHistory != null) {
                rawHistory.stream()
                        .map(Object::toString)
                        .filter(s -> s.endsWith(":" + keyword))
                        .forEach(s -> redisCache.redisTemplate.opsForZSet().remove(key, s));
            }

            // 异步删除 MySQL 数据
            CompletableFuture.runAsync(() -> {
                try {
                    searchHistoryMapper.deleteByWxIdAndKeyword(userId, keyword);
                    log.info("MySQL搜索历史已删除: userId={}, keyword={}", userId, keyword);
                } catch (Exception e) {
                    log.error("MySQL搜索历史删除失败: userId={}, keyword={}", userId, keyword, e);
                }
            });

            log.info("删除搜索历史: userId={}, keyword={}", userId, keyword);
        }
    }

    @Override
    public Page<String> getHotSearch(SearchType type, Integer limit) {
        String key = HOT_SEARCH_PREFIX + (type != null ? type.getCode() : "global");

        // 检查本地缓存
        Object cached = hotSearchCache.getIfPresent(key);
        if (cached != null) {
            return (Page<String>) cached;
        }

        // 从 Redis 获取
        Set<?> rawKeywords = redisCache.redisTemplate.opsForZSet()
                .reverseRange(key, 0, limit - 1);
        Set<String> keywords = rawKeywords != null ? rawKeywords.stream()
                .map(Object::toString)
                .collect(Collectors.toSet()) : Collections.emptySet();

        List<String> hotList = keywords != null ? new ArrayList<>(keywords) : Collections.emptyList();

        PageImpl<String> result = new PageImpl<>(
                hotList,
                PageRequest.of(0, limit),
                hotList.size());

        // 存入本地缓存
        hotSearchCache.put(key, result);

        return result;
    }

    /**
     * 记录热搜词
     */
    private void recordHotSearch(String keyword, SearchType type) {
        String key = HOT_SEARCH_PREFIX + type.getCode();
        String globalKey = HOT_SEARCH_PREFIX + "global";

        // 增加计数
        redisCache.redisTemplate.opsForZSet().incrementScore(key, keyword, 1);
        redisCache.redisTemplate.opsForZSet().incrementScore(globalKey, keyword, 1);

        // 设置过期时间（24小时）
        redisCache.expire(key, 24, TimeUnit.HOURS);
        redisCache.expire(globalKey, 24, TimeUnit.HOURS);
    }

    /**
     * 记录搜索日志（异步，企业级标准）
     */
    private void recordSearchLog(String traceId, UnifiedSearchRequest request,
            long resultCount, long searchTimeMs,
            boolean isSuccess, String errorMsg) {
        CompletableFuture.runAsync(() -> {
            try {
                SearchLog searchLog = SearchLog.builder()
                        .traceId(traceId)
                        .wxId(null) // TODO: 从请求上下文中获取用户ID
                        .keyword(request.getKeyword())
                        .searchType(request.getTypes().get(0).getCode())
                        .filters(request.getFilter() != null ? request.getFilter().toString() : null)
                        .pageNum(request.getPageNum())
                        .pageSize(request.getPageSize())
                        .resultCount(resultCount)
                        .searchTimeMs((int) searchTimeMs)
                        .esTimeMs(0) // TODO: 需要在搜索时记录ES耗时
                        .cacheHit(0) // TODO: 需要在搜索时记录缓存命中情况
                        .isSuccess(isSuccess ? 1 : 0)
                        .errorMsg(errorMsg)
                        .ipAddress(null) // TODO: 从请求上下文中获取IP
                        .userAgent(null) // TODO: 从请求上下文中获取User-Agent
                        .createdTime(LocalDateTime.now())
                        .build();

                searchLogMapper.insert(searchLog);
                log.debug("搜索日志已记录: traceId={}, keyword={}", traceId, request.getKeyword());
            } catch (Exception e) {
                log.error("搜索日志记录失败: traceId={}", traceId, e);
            }
        });
    }

    /**
     * 记录无结果查询（异步，用于优化搜索召回率）
     */
    private void recordNoResultQuery(String keyword, SearchType type) {
        CompletableFuture.runAsync(() -> {
            try {
                // 使用 increment 方法更新计数（如果不存在则插入）
                int affected = searchNoResultQueryMapper.incrementQueryCount(keyword, type.getCode());

                if (affected == 0) {
                    // 如果更新失败，可能是记录不存在，尝试插入
                    SearchNoResultQuery query = SearchNoResultQuery.builder()
                            .keyword(keyword)
                            .searchType(type.getCode())
                            .queryCount(1L)
                            .lastQueryTime(LocalDateTime.now())
                            .status("PENDING")
                            .createdTime(LocalDateTime.now())
                            .updatedTime(LocalDateTime.now())
                            .build();
                    searchNoResultQueryMapper.insert(query);
                }

                log.debug("无结果查询已记录: keyword={}, type={}", keyword, type.getCode());
            } catch (Exception e) {
                log.warn("无结果查询记录失败: keyword={}, type={}", keyword, type.getCode(), e);
            }
        });
    }

    /**
     * 高亮前缀
     */
    private String highlightPrefix(String text, String prefix) {
        if (text.startsWith(prefix)) {
            return "<em class='highlight'>" + prefix + "</em>" +
                    text.substring(prefix.length());
        }
        return text;
    }
}
