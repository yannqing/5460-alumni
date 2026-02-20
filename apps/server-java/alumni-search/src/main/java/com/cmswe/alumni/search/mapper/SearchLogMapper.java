package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.SearchLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 搜索日志 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Mapper
public interface SearchLogMapper extends BaseMapper<SearchLog> {

    /**
     * 查询指定时间范围内的搜索日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     查询数量
     * @return 搜索日志列表
     */
    List<SearchLog> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("limit") Integer limit);

    /**
     * 统计搜索成功率（成功数/总数）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 成功率统计 {total: 总数, success: 成功数}
     */
    Map<String, Long> calculateSuccessRate(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计平均搜索耗时
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 平均耗时（毫秒）
     */
    Double calculateAvgSearchTime(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计缓存命中率
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 缓存命中统计 {total: 总数, l1_hit: L1命中数, l2_hit: L2命中数}
     */
    Map<String, Long> calculateCacheHitRate(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询慢查询日志（耗时超过阈值的查询）
     *
     * @param threshold 耗时阈值（毫秒）
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     查询数量
     * @return 慢查询列表
     */
    List<SearchLog> selectSlowQueries(@Param("threshold") Integer threshold,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("limit") Integer limit);
}
