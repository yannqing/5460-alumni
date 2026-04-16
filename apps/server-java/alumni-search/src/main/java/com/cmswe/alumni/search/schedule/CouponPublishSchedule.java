package com.cmswe.alumni.search.schedule;

import com.cmswe.alumni.search.mapper.CouponMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 优惠券定时发布/结束任务
 */
@Slf4j
@Component
public class CouponPublishSchedule {

    @Resource
    private CouponMapper couponMapper;

    /**
     * 每分钟执行一次：
     * 1) 将到发布时间的定时发布券置为已发布（status=1）
     * 2) 将超过有效期的已发布券置为已结束（status=2）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void syncCouponPublishStatus() {
        LocalDateTime now = LocalDateTime.now();
        try {
            int published = couponMapper.publishScheduledCoupons(now);
            int closed = couponMapper.closeExpiredPublishedCoupons(now);
            if (published > 0 || closed > 0) {
                log.info("优惠券定时任务完成 - 自动发布: {}, 自动结束: {}, 执行时间: {}", published, closed, now);
            }
        } catch (Exception e) {
            log.error("优惠券定时发布任务执行失败", e);
        }
    }
}
