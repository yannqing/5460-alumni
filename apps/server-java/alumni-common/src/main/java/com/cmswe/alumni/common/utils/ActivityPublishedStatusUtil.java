package com.cmswe.alumni.common.utils;

import com.cmswe.alumni.common.exception.BusinessException;

import java.time.LocalDateTime;

/**
 * 已发布活动（非草稿）按当前时间推算的业务状态。
 * 6-未开始 1-报名中 2-报名结束 3-进行中 4-已结束
 */
public final class ActivityPublishedStatusUtil {

    private ActivityPublishedStatusUtil() {
    }

    /**
     * @param isSignup 是否需要报名：0-否 1-是
     */
    public static int compute(LocalDateTime now, Integer isSignup,
                              LocalDateTime startTime, LocalDateTime endTime,
                              LocalDateTime registrationStartTime, LocalDateTime registrationEndTime) {
        if (now == null || startTime == null || endTime == null) {
            throw new BusinessException("活动开始时间、结束时间不能为空");
        }
        if (!now.isBefore(endTime)) {
            return 4;
        }

        if (isSignup != null && isSignup == 1) {
            if (registrationStartTime == null || registrationEndTime == null) {
                throw new BusinessException("需要报名时，报名开始时间和截止时间不能为空");
            }
            if (now.isBefore(registrationStartTime)) {
                return 6;
            }
            if (now.isBefore(registrationEndTime)) {
                return 1;
            }
            if (now.isBefore(startTime)) {
                return 2;
            }
            return 3;
        }

        if (now.isBefore(startTime)) {
            return 6;
        }
        return 3;
    }
}
