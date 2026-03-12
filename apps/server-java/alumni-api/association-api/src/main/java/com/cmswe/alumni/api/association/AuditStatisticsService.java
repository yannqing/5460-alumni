package com.cmswe.alumni.api.association;

import com.cmswe.alumni.common.vo.AuditStatisticsVo;

/**
 * 审核待办统计服务
 */
public interface AuditStatisticsService {

    /**
     * 获取当前用户的审核待办统计
     *
     * @param wxId 用户ID
     * @return 统计结果
     */
    AuditStatisticsVo getAuditTodoStatistics(Long wxId);
}
