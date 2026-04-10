package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.FollowStatisticsService;
import com.cmswe.alumni.common.entity.FollowStatistics;
import com.cmswe.alumni.common.entity.UserFollow;
import com.cmswe.alumni.common.vo.UserFollowStatisticsVo;
import com.cmswe.alumni.service.user.dto.FollowIdCountRow;
import com.cmswe.alumni.service.user.mapper.FollowStatisticsMapper;
import com.cmswe.alumni.service.user.mapper.UserFollowMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 关注统计服务实现类
 */
@Slf4j
@Service
public class FollowStatisticsServiceImpl extends ServiceImpl<FollowStatisticsMapper, FollowStatistics>
        implements FollowStatisticsService {

    private static final int TARGET_TYPE_USER = 1;

    @Resource
    private UserFollowMapper userFollowMapper;

    @Override
    public UserFollowStatisticsVo getCurrentUserStatistics(Long wxId) {
        // 查询或创建今日统计记录
        FollowStatistics statistics = getOrCreateStatisticsForDate(TARGET_TYPE_USER, wxId, LocalDate.now());

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
        FollowStatistics todayStatistics = getOrCreateStatisticsForDate(targetType, targetId, LocalDate.now());

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
        FollowStatistics todayStatistics = getOrCreateStatisticsForDate(TARGET_TYPE_USER, wxId, LocalDate.now());

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reconcilePersonalFollowStatisticsForDate(LocalDate statDate) {
        if (statDate == null) {
            return;
        }

        Map<Long, Long> followerByUser = toCountMap(userFollowMapper.selectFollowerCountGroupByUserTarget());
        Map<Long, Long> followingByWx = toCountMap(userFollowMapper.selectFollowingCountGroupByWxId());

        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(followerByUser.keySet());
        allUserIds.addAll(followingByWx.keySet());

        LambdaQueryWrapper<FollowStatistics> statRowQuery = new LambdaQueryWrapper<>();
        statRowQuery.eq(FollowStatistics::getTargetType, TARGET_TYPE_USER)
                .eq(FollowStatistics::getStatDate, statDate)
                .select(FollowStatistics::getTargetId);
        List<FollowStatistics> existingRows = this.list(statRowQuery);
        for (FollowStatistics row : existingRows) {
            allUserIds.add(row.getTargetId());
        }

        int updated = 0;
        int created = 0;
        for (Long wxId : allUserIds) {
            int followerTruth = followerByUser.getOrDefault(wxId, 0L).intValue();
            int followingTruth = followingByWx.getOrDefault(wxId, 0L).intValue();

            LambdaQueryWrapper<FollowStatistics> todayQuery = new LambdaQueryWrapper<>();
            todayQuery.eq(FollowStatistics::getTargetType, TARGET_TYPE_USER)
                    .eq(FollowStatistics::getTargetId, wxId)
                    .eq(FollowStatistics::getStatDate, statDate);
            FollowStatistics todayRow = this.getOne(todayQuery);

            if (todayRow != null) {
                if (!Objects.equals(todayRow.getFollowerCount(), followerTruth)
                        || !Objects.equals(todayRow.getFollowingCount(), followingTruth)) {
                    todayRow.setFollowerCount(followerTruth);
                    todayRow.setFollowingCount(followingTruth);
                    todayRow.setLastCalcTime(LocalDateTime.now());
                    this.updateById(todayRow);
                    updated++;
                }
            } else if (followerTruth > 0 || followingTruth > 0) {
                getOrCreateStatisticsForDate(TARGET_TYPE_USER, wxId, statDate);
                created++;
            }
        }

        log.info("个人关注统计校准完成 statDate={} 覆盖用户数={} 更新行数={} 新建行数={}",
                statDate, allUserIds.size(), updated, created);
    }

    private static Map<Long, Long> toCountMap(List<FollowIdCountRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        return rows.stream()
                .filter(r -> r.getId() != null && r.getCnt() != null)
                .collect(Collectors.toMap(FollowIdCountRow::getId, FollowIdCountRow::getCnt, (a, b) -> a));
    }

    /**
     * 获取或创建指定日期的统计记录
     * 逻辑：
     * 1. 先查询当日是否有记录，有则直接返回
     * 2. 没有则创建当日记录，创建时需要判断：
     *    - 查询最近的一条统计记录（不限日期）
     *    - 今日新增：从0开始
     *    - 本周新增：
     *      ① 如果当日是周一，从0开始
     *      ② 如果最近的记录在本周内（>= 本周一），继承其本周新增值
     *      ③ 如果最近的记录在本周之前或没有记录，从0开始
     *    - 本月新增：
     *      ① 如果当日是1号，从0开始
     *      ② 如果最近的记录在本月内（>= 本月1号），继承其本月新增值
     *      ③ 如果最近的记录在本月之前或没有记录，从0开始
     *    - 粉丝数/关注数：实时从 user_follow 表统计
     */
    private FollowStatistics getOrCreateStatisticsForDate(Integer targetType, Long targetId, LocalDate statDate) {
        // 查询当日统计记录
        LambdaQueryWrapper<FollowStatistics> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(FollowStatistics::getTargetType, targetType)
                .eq(FollowStatistics::getTargetId, targetId)
                .eq(FollowStatistics::getStatDate, statDate);

        FollowStatistics todayStatistics = this.getOne(todayQuery);

        if (todayStatistics != null) {
            return todayStatistics;
        }

        // 当日记录不存在，需要创建
        todayStatistics = new FollowStatistics();
        todayStatistics.setTargetType(targetType);
        todayStatistics.setTargetId(targetId);
        todayStatistics.setStatDate(statDate);
        todayStatistics.setDailyNewFollowers(0);

        // 计算本周第一天（周一）
        LocalDate weekStart = statDate.minusDays(statDate.getDayOfWeek().getValue() - 1);

        // 查询最近的一条统计记录（不限日期）
        LambdaQueryWrapper<FollowStatistics> lastQuery = new LambdaQueryWrapper<>();
        lastQuery.eq(FollowStatistics::getTargetType, targetType)
                .eq(FollowStatistics::getTargetId, targetId)
                .lt(FollowStatistics::getStatDate, statDate)
                .orderByDesc(FollowStatistics::getStatDate)
                .last("LIMIT 1");

        FollowStatistics lastStats = this.getOne(lastQuery);

        // 处理本周新增逻辑
        if (statDate.equals(weekStart)) {
            // 当日是本周第一天（周一），本周新增从0开始
            todayStatistics.setWeeklyNewFollowers(0);
        } else if (lastStats != null && !lastStats.getStatDate().isBefore(weekStart)) {
            // 最近的记录在本周内，继承其本周新增值
            todayStatistics.setWeeklyNewFollowers(lastStats.getWeeklyNewFollowers());
        } else {
            // 最近的记录不在本周内（或没有记录），说明本周还没有数据，从0开始
            todayStatistics.setWeeklyNewFollowers(0);
        }

        // 计算本月第一天
        LocalDate monthStart = LocalDate.of(statDate.getYear(), statDate.getMonth(), 1);

        // 处理本月新增逻辑
        if (statDate.equals(monthStart)) {
            // 当日是本月第一天，本月新增从0开始
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
        if (targetType == TARGET_TYPE_USER) {
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
