package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.MessageStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 消息统计 Mapper
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Mapper
public interface MessageStatisticsMapper extends BaseMapper<MessageStatistics> {

    /**
     * 查询指定日期范围的统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计列表
     */
    List<MessageStatistics> selectByDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期和类别的统计数据
     *
     * @param statDate        统计日期
     * @param messageCategory 消息类别
     * @return 统计记录
     */
    MessageStatistics selectByDateAndCategory(@Param("statDate") LocalDate statDate,
                                               @Param("messageCategory") String messageCategory);

    /**
     * 增量更新统计数据
     *
     * @param statDate        统计日期
     * @param messageCategory 消息类别
     * @param totalDelta      总数增量
     * @param successDelta    成功数增量
     * @param failedDelta     失败数增量
     * @return 影响行数
     */
    int incrementStatistics(@Param("statDate") LocalDate statDate,
                            @Param("messageCategory") String messageCategory,
                            @Param("totalDelta") Long totalDelta,
                            @Param("successDelta") Long successDelta,
                            @Param("failedDelta") Long failedDelta);

    /**
     * 插入或更新统计数据（ON DUPLICATE KEY UPDATE）
     *
     * @param statistics 统计数据
     * @return 影响行数
     */
    int upsertStatistics(MessageStatistics statistics);
}
