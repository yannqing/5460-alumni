package com.cmswe.alumni.api.search;

import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.dto.search.UnifiedSearchRequest;
import com.cmswe.alumni.common.enums.SearchType;
import com.cmswe.alumni.common.vo.search.SuggestResponse;
import com.cmswe.alumni.common.vo.search.UnifiedSearchResponse;
import org.springframework.data.domain.Page;

/**
 * 统一搜索服务接口
 *
 * @author CNI Alumni System
 */
public interface UnifiedSearchService {

    /**
     * 统一搜索入口
     *
     * @param request 搜索请求
     * @return 搜索响应
     */
    UnifiedSearchResponse search(UnifiedSearchRequest request);

    /**
     * 搜索建议（自动补全）
     *
     * @param prefix 输入前缀
     * @param type 搜索类型
     * @param size 返回数量
     * @return 建议列表
     */
    SuggestResponse suggest(String prefix, SearchType type, Integer size);

    /**
     * 保存搜索历史
     *
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param type 搜索类型
     */
    void saveSearchHistory(Long userId, String keyword, SearchType type);

    /**
     * 获取用户搜索历史
     *
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 搜索历史列表
     */
    Page<String> getSearchHistory(Long userId, Integer limit);

    /**
     * 删除搜索历史
     *
     * @param userId 用户ID
     * @param keyword 关键词（可选，null表示删除全部）
     */
    void deleteSearchHistory(Long userId, String keyword);

    /**
     * 获取热搜榜
     *
     * @param type 搜索类型
     * @param limit 返回数量
     * @return 热搜关键词列表
     */
    Page<String> getHotSearch(SearchType type, Integer limit);
}
