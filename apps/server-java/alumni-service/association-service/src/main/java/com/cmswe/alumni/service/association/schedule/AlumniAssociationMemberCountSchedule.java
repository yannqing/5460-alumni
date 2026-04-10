package com.cmswe.alumni.service.association.schedule;

import com.cmswe.alumni.api.association.AlumniAssociationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 校友会 member_count 与成员表事实数据定时校准
 */
@Slf4j
@Component
public class AlumniAssociationMemberCountSchedule {

    @Resource
    private AlumniAssociationService alumniAssociationService;

    /**
     * 每天凌晨 0:00（服务器默认时区）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void reconcileMemberCounts() {
        try {
            alumniAssociationService.reconcileMemberCountsFromMemberTable();
        } catch (Exception e) {
            log.error("校友会成员人数定时校准失败", e);
        }
    }
}
