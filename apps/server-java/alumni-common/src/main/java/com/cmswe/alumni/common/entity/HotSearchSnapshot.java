package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 热搜榜快照表（定时任务生成，支持历史追溯）
 *
 * @author CNI Alumni System
 * @TableName hot_search_snapshot
 */
@TableName(value = "hot_search_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotSearchSnapshot implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 搜索关键词
     */
    @TableField(value = "keyword")
    private String keyword;

    /**
     * 搜索类型（ALUMNI/ASSOCIATION/MERCHANT/GLOBAL-全局）
     */
    @TableField(value = "search_type")
    private String searchType;

    /**
     * 搜索次数（该周期内累计）
     */
    @TableField(value = "search_count")
    private Long searchCount;

    /**
     * 排名（1表示第一名）
     */
    @TableField(value = "ranking")
    private Integer ranking;

    /**
     * 趋势（UP-上升 DOWN-下降 STABLE-稳定 NEW-新上榜）
     */
    @TableField(value = "trend")
    private String trend;

    /**
     * 快照日期（YYYY-MM-DD）
     */
    @TableField(value = "snapshot_date")
    private LocalDate snapshotDate;

    /**
     * 快照小时（0-23，NULL表示日快照）
     */
    @TableField(value = "snapshot_hour")
    private Integer snapshotHour;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
