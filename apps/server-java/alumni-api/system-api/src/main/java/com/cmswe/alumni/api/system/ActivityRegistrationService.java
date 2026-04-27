package com.cmswe.alumni.api.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.ApplyActivityRegistrationDto;
import com.cmswe.alumni.common.dto.QueryActivityRegistrationListDto;
import com.cmswe.alumni.common.dto.ReviewActivityRegistrationDto;
import com.cmswe.alumni.common.entity.ActivityRegistration;
import com.cmswe.alumni.common.vo.ActivityParticipantVo;
import com.cmswe.alumni.common.vo.ActivityRegistrationListVo;
import com.cmswe.alumni.common.vo.ActivityRegistrationStatusVo;
import com.cmswe.alumni.common.vo.PageVo;

import java.util.List;

/**
 * 活动报名服务接口
 *
 * @author CNI Alumni System
 * @since 2026-04-26
 */
public interface ActivityRegistrationService extends IService<ActivityRegistration> {

    /**
     * 用户报名活动
     *
     * @param wxId      操作人微信ID
     * @param applyDto  报名请求
     * @return 是否成功
     */
    boolean apply(Long wxId, ApplyActivityRegistrationDto applyDto);

    /**
     * 用户取消自己的报名
     *
     * @param wxId           操作人微信ID
     * @param registrationId 报名记录ID
     * @return 是否成功
     */
    boolean cancel(Long wxId, Long registrationId);

    /**
     * 管理员审核报名
     *
     * @param auditorId 审核人微信ID
     * @param reviewDto 审核请求
     * @return 是否成功
     */
    boolean review(Long auditorId, ReviewActivityRegistrationDto reviewDto);

    /**
     * 管理员分页查询某活动的报名列表
     *
     * @param wxId     操作人微信ID（用于权限校验）
     * @param queryDto 查询请求
     * @return 报名列表分页数据
     */
    PageVo<ActivityRegistrationListVo> queryPage(Long wxId, QueryActivityRegistrationListDto queryDto);

    /**
     * 查询当前用户在某活动中的报名状态
     *
     * @param wxId       当前用户ID
     * @param activityId 活动ID
     * @return 报名状态VO
     */
    ActivityRegistrationStatusVo getMyStatus(Long wxId, Long activityId);

    /**
     * 查询某活动已审核通过的参与者列表（C 端展示，受隐私设置过滤）
     *
     * @param activityId 活动ID
     * @param limit      最多返回多少条（&lt;=0 表示不限）
     * @return 参与者列表
     */
    List<ActivityParticipantVo> getApprovedParticipants(Long activityId, Integer limit);

    /**
     * 查询某活动已审核通过的报名总人数
     *
     * @param activityId 活动ID
     * @return 已通过报名总数
     */
    Long getApprovedCount(Long activityId);
}
