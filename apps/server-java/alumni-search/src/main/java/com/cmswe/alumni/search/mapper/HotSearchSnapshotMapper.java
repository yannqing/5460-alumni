package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.HotSearchSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 热搜榜快照 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Mapper
public interface HotSearchSnapshotMapper extends BaseMapper<HotSearchSnapshot> {

    /**
     * 查询指定日期的热搜榜
     *
     * @param searchType    搜索类型
     * @param snapshotDate  快照日期
     * @param snapshotHour  快照小时（NULL表示日快照）
     * @param limit         查询数量
     * @return 热搜列表（按排名升序）
     */
    List<HotSearchSnapshot> selectByDate(@Param("searchType") String searchType,
                                         @Param("snapshotDate") LocalDate snapshotDate,
                                         @Param("snapshotHour") Integer snapshotHour,
                                         @Param("limit") Integer limit);

    /**
     * 查询关键词的热搜趋势（最近N天）
     *
     * @param keyword      关键词
     * @param searchType   搜索类型
     * @param days         天数
     * @return 趋势列表（按日期升序）
     */
    List<HotSearchSnapshot> selectTrendByKeyword(@Param("keyword") String keyword,
                                                 @Param("searchType") String searchType,
                                                 @Param("days") Integer days);

    /**
     * 批量插入热搜快照
     *
     * @param snapshots 快照列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("snapshots") List<HotSearchSnapshot> snapshots);

    /**
     * 删除过期快照（保留最近N天）
     *
     * @param retentionDays 保留天数
     * @return 删除的记录数
     */
    int deleteExpiredSnapshots(@Param("retentionDays") Integer retentionDays);
}
