package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 关注统计表（支持实时和历史分析）
 * @TableName follow_statistics
 */
@TableName(value = "follow_statistics")
@Data
public class FollowStatistics implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目标类型
     */
    @TableField(value = "target_type")
    private Integer targetType;

    /**
     * 目标ID
     */
    @TableField(value = "target_id")
    private Long targetId;

    /**
     * 粉丝数量
     */
    @TableField(value = "follower_count")
    private Integer followerCount;

    /**
     * 关注数量（仅用户）
     */
    @TableField(value = "following_count")
    private Integer followingCount;

    /**
     * 好友数量
     */
    @TableField(value = "friend_count")
    private Integer friendCount;

    /**
     * 今日新增粉丝
     */
    @TableField(value = "daily_new_followers")
    private Integer dailyNewFollowers;

    /**
     * 本周新增粉丝
     */
    @TableField(value = "weekly_new_followers")
    private Integer weeklyNewFollowers;

    /**
     * 本月新增粉丝
     */
    @TableField(value = "monthly_new_followers")
    private Integer monthlyNewFollowers;

    /**
     * 活跃粉丝数（30天内有互动）
     */
    @TableField(value = "active_followers")
    private Integer activeFollowers;

    /**
     * VIP粉丝数
     */
    @TableField(value = "vip_followers")
    private Integer vipFollowers;

    /**
     * 统计日期（用于每日快照）
     */
    @TableField(value = "stat_date")
    private LocalDate statDate;

    /**
     * 最后计算时间
     */
    @TableField(value = "last_calc_time")
    private LocalDateTime lastCalcTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
