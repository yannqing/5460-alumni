package com.cmswe.alumni.service.user.schedule;

import com.cmswe.alumni.api.user.FollowStatisticsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 个人关注统计（user_follow → follow_statistics 用户维度）定时校准
 */
@Slf4j
@Component
public class PersonalFollowStatisticsSchedule {

    @Resource
    private FollowStatisticsService followStatisticsService;

    /**
     * 每天凌晨 1:00 执行（服务器默认时区）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void reconcilePersonalFollowStatistics() {
        try {
            followStatisticsService.reconcilePersonalFollowStatisticsForDate(LocalDate.now());
        } catch (Exception e) {
            log.error("个人关注统计定时校准失败", e);
        }
    }
}
