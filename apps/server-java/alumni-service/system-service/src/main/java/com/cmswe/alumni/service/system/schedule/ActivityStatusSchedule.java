package com.cmswe.alumni.service.system.schedule;

import com.cmswe.alumni.api.system.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 活动状态定时更新任务
 */
@Slf4j
@Component
public class ActivityStatusSchedule {

    @Resource
    private ActivityService activityService;

    /**
     * 每分钟执行一次，更新活动状态
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateActivityStatus() {
        try {
            activityService.updateActivityStatus();
        } catch (Exception e) {
            log.error("执行活动状态更新定时任务失败", e);
        }
    }
}
