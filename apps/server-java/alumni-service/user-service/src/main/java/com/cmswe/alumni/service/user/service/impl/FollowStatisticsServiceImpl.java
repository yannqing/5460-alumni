package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.FollowStatisticsService;
import com.cmswe.alumni.common.entity.FollowStatistics;
import com.cmswe.alumni.common.entity.UserFollow;
import com.cmswe.alumni.common.utils.JwtUtils;
import com.cmswe.alumni.common.vo.UserFollowStatisticsVo;
import com.cmswe.alumni.service.user.mapper.FollowStatisticsMapper;
import com.cmswe.alumni.service.user.mapper.UserFollowMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 关注统计服务实现类
 */
@Slf4j
@Service
public class FollowStatisticsServiceImpl extends ServiceImpl<FollowStatisticsMapper, FollowStatistics>
        implements FollowStatisticsService {

    @Resource
    private UserFollowMapper userFollowMapper;

    @Override
    public UserFollowStatisticsVo getCurrentUserStatistics(Long wxId) {
        // 查询或创建今日统计记录
        FollowStatistics statistics = getOrCreateTodayStatistics(1, wxId);

        UserFollowStatisticsVo vo = new UserFollowStatisticsVo();
        vo.setFollowerCount(statistics.getFollowerCount());
        vo.setFollowingCount(statistics.getFollowingCount());
        vo.setFriendCount(statistics.getFriendCount());
        vo.setDailyNewFollowers(statistics.getDailyNewFollowers());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFollowerCount(Integer targetType, Long targetId, int increment) {
        // 获取或创建今日统计记录
        FollowStatistics todayStatistics = getOrCreateTodayStatistics(targetType, targetId);

        // 更新粉丝总数
        int newCount = todayStatistics.getFollowerCount() + increment;
        todayStatistics.setFollowerCount(Math.max(0, newCount));

        // 更新今日、本周、本月新增（支持正负增量，记录净新增）
        // increment > 0 表示新增关注，increment < 0 表示取消关注
        todayStatistics.setDailyNewFollowers(Math.max(0, todayStatistics.getDailyNewFollowers() + increment));
        todayStatistics.setWeeklyNewFollowers(Math.max(0, todayStatistics.getWeeklyNewFollowers() + increment));
        todayStatistics.setMonthlyNewFollowers(Math.max(0, todayStatistics.getMonthlyNewFollowers() + increment));

        todayStatistics.setLastCalcTime(LocalDateTime.now());
        this.updateById(todayStatistics);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFollowingCount(Long wxId, int increment) {
        // 获取或创建今日统计记录
        FollowStatistics todayStatistics = getOrCreateTodayStatistics(1, wxId);

        // 更新关注总数
        int newCount = todayStatistics.getFollowingCount() + increment;
        todayStatistics.setFollowingCount(Math.max(0, newCount));

        // 更新今日、本周、本月新增（支持正负增量，记录净新增）
        // increment > 0 表示新增关注，increment < 0 表示取消关注
        todayStatistics.setDailyNewFollowers(Math.max(0, todayStatistics.getDailyNewFollowers() + increment));
        todayStatistics.setWeeklyNewFollowers(Math.max(0, todayStatistics.getWeeklyNewFollowers() + increment));
        todayStatistics.setMonthlyNewFollowers(Math.max(0, todayStatistics.getMonthlyNewFollowers() + increment));

        todayStatistics.setLastCalcTime(LocalDateTime.now());
        this.updateById(todayStatistics);
    }

    /**
     * 获取或创建今日统计记录
     * 逻辑：
     * 1. 先查询今日是否有记录，有则直接返回
     * 2. 没有则创建今日记录，创建时需要判断：
     *    - 查询最近的一条统计记录（不限日期）
     *    - 今日新增：从0开始
     *    - 本周新增：
     *      ① 如果今天是周一，从0开始
     *      ② 如果最近的记录在本周内（>= 本周一），继承其本周新增值
     *      ③ 如果最近的记录在本周之前或没有记录，从0开始
     *    - 本月新增：
     *      ① 如果今天是1号，从0开始
     *      ② 如果最近的记录在本月内（>= 本月1号），继承其本月新增值
     *      ③ 如果最近的记录在本月之前或没有记录，从0开始
     *    - 粉丝数/关注数：实时从 user_follow 表统计
     */
    private FollowStatistics getOrCreateTodayStatistics(Integer targetType, Long targetId) {
        LocalDate today = LocalDate.now();

        // 查询今日统计记录
        LambdaQueryWrapper<FollowStatistics> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(FollowStatistics::getTargetType, targetType)
                .eq(FollowStatistics::getTargetId, targetId)
                .eq(FollowStatistics::getStatDate, today);

        FollowStatistics todayStatistics = this.getOne(todayQuery);

        if (todayStatistics != null) {
            return todayStatistics;
        }

        // 今日记录不存在，需要创建
        todayStatistics = new FollowStatistics();
        todayStatistics.setTargetType(targetType);
        todayStatistics.setTargetId(targetId);
        todayStatistics.setStatDate(today);
        todayStatistics.setDailyNewFollowers(0);

        // 计算本周第一天（周一）
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        // 查询最近的一条统计记录（不限日期）
        LambdaQueryWrapper<FollowStatistics> lastQuery = new LambdaQueryWrapper<>();
        lastQuery.eq(FollowStatistics::getTargetType, targetType)
                .eq(FollowStatistics::getTargetId, targetId)
                .lt(FollowStatistics::getStatDate, today)
                .orderByDesc(FollowStatistics::getStatDate)
                .last("LIMIT 1");

        FollowStatistics lastStats = this.getOne(lastQuery);

        // 处理本周新增逻辑
        if (today.equals(weekStart)) {
            // 今天是本周第一天（周一），本周新增从0开始
            todayStatistics.setWeeklyNewFollowers(0);
        } else if (lastStats != null && !lastStats.getStatDate().isBefore(weekStart)) {
            // 最近的记录在本周内，继承其本周新增值
            todayStatistics.setWeeklyNewFollowers(lastStats.getWeeklyNewFollowers());
        } else {
            // 最近的记录不在本周内（或没有记录），说明本周还没有数据，从0开始
            todayStatistics.setWeeklyNewFollowers(0);
        }

        // 计算本月第一天
        LocalDate monthStart = LocalDate.of(today.getYear(), today.getMonth(), 1);

        // 处理本月新增逻辑
        if (today.equals(monthStart)) {
            // 今天是本月第一天，本月新增从0开始
            todayStatistics.setMonthlyNewFollowers(0);
        } else if (lastStats != null && !lastStats.getStatDate().isBefore(monthStart)) {
            // 最近的记录在本月内，继承其本月新增值
            todayStatistics.setMonthlyNewFollowers(lastStats.getMonthlyNewFollowers());
        } else {
            // 最近的记录不在本月内（或没有记录），说明本月还没有数据，从0开始
            todayStatistics.setMonthlyNewFollowers(0);
        }

        // 实时计算当前的总数据
        LambdaQueryWrapper<UserFollow> followQueryWrapper = new LambdaQueryWrapper<>();

        // 计算粉丝数（有多少人关注了这个目标）
        followQueryWrapper.eq(UserFollow::getTargetType, targetType)
                .eq(UserFollow::getTargetId, targetId);
        long followerCount = userFollowMapper.selectCount(followQueryWrapper);
        todayStatistics.setFollowerCount((int) followerCount);

        // 如果是用户类型，计算关注数（这个用户关注了多少人）
        if (targetType == 1) {
            LambdaQueryWrapper<UserFollow> followingQueryWrapper = new LambdaQueryWrapper<>();
            followingQueryWrapper.eq(UserFollow::getWxId, targetId);
            long followingCount = userFollowMapper.selectCount(followingQueryWrapper);
            todayStatistics.setFollowingCount((int) followingCount);
        } else {
            todayStatistics.setFollowingCount(0);
        }

        // 其他字段暂时设置为0或从昨天继承
        todayStatistics.setFriendCount(0);
        todayStatistics.setActiveFollowers(0);
        todayStatistics.setVipFollowers(0);
        todayStatistics.setLastCalcTime(LocalDateTime.now());

        this.save(todayStatistics);
        return todayStatistics;
    }
}
