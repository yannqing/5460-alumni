package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.FollowStatistics;
import com.cmswe.alumni.common.vo.UserFollowStatisticsVo;

import java.time.LocalDate;

/**
 * 关注统计服务接口
 */
public interface FollowStatisticsService extends IService<FollowStatistics> {

    /**
     * 获取当前用户的关注统计
     * @param wxId 用户 id
     * @return 关注统计信息
     */
    UserFollowStatisticsVo getCurrentUserStatistics(Long wxId);

    /**
     * 更新目标的粉丝统计
     * @param targetType 目标类型
     * @param targetId 目标 ID
     * @param increment 增量（关注+1，取关-1）
     */
    void updateFollowerCount(Integer targetType, Long targetId, int increment);

    /**
     * 更新用户的关注数统计
     * @param wxId 用户 ID
     * @param increment 增量（关注+1，取关-1）
     */
    void updateFollowingCount(Long wxId, int increment);

    /**
     * 按 user_follow 事实表校准指定日期、用户维度（target_type=1）的 follower_count / following_count。
     *
     * @param statDate 统计日期（通常为当日）
     */
    void reconcilePersonalFollowStatisticsForDate(LocalDate statDate);
}
