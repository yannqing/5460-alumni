package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索历史 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    /**
     * 查询用户搜索历史（按时间倒序）
     *
     * @param wxId  用户ID
     * @param limit 查询数量
     * @return 搜索历史列表
     */
    List<SearchHistory> selectUserHistory(@Param("wxId") Long wxId,
                                          @Param("limit") Integer limit);

    /**
     * 删除用户指定关键词的搜索历史
     *
     * @param wxId    用户ID
     * @param keyword 关键词
     * @return 删除的记录数
     */
    int deleteByWxIdAndKeyword(@Param("wxId") Long wxId,
                               @Param("keyword") String keyword);

    /**
     * 统计用户在指定时间范围内的搜索次数
     *
     * @param wxId      用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 搜索次数
     */
    Long countUserSearches(@Param("wxId") Long wxId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
}
