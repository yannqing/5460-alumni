package com.cmswe.alumni.service.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.system.ActivityRegistrationService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.dto.ApplyActivityRegistrationDto;
import com.cmswe.alumni.common.dto.QueryActivityRegistrationListDto;
import com.cmswe.alumni.common.dto.ReviewActivityRegistrationDto;
import com.cmswe.alumni.common.entity.Activity;
import com.cmswe.alumni.common.entity.ActivityRegistration;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.ActivityParticipantVo;
import com.cmswe.alumni.common.vo.ActivityRegistrationListVo;
import com.cmswe.alumni.common.vo.ActivityRegistrationStatusVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.service.system.mapper.ActivityMapper;
import com.cmswe.alumni.service.system.mapper.ActivityRegistrationMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动报名服务实现
 *
 * @author CNI Alumni System
 * @since 2026-04-26
 */
@Slf4j
@Service
public class ActivityRegistrationServiceImpl
        extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration>
        implements ActivityRegistrationService {

    /** 活动开始前禁止取消报名的时间（小时） */
    private static final long CANCEL_LOCK_HOURS_BEFORE_START = 2L;

    /** 主办方类型：1-校友会 */
    private static final Integer ORGANIZER_TYPE_ALUMNI_ASSOCIATION = 1;

    /** 报名状态：0-待审核 1-审核通过 2-审核拒绝 3-已取消 */
    private static final Integer STATUS_PENDING = 0;
    private static final Integer STATUS_APPROVED = 1;
    private static final Integer STATUS_REJECTED = 2;
    private static final Integer STATUS_CANCELLED = 3;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Resource
    private UserService userService;

    // ============== 用户侧 ==============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(Long wxId, ApplyActivityRegistrationDto applyDto) {
        if (wxId == null || applyDto == null || applyDto.getActivityId() == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 1. 校验活动存在性与可报名性
        Activity activity = activityMapper.selectById(applyDto.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在或已被删除");
        }
        if (activity.getIsSignup() == null || activity.getIsSignup() != 1) {
            throw new BusinessException("该活动无需报名");
        }
        if (activity.getStatus() == null || activity.getStatus() != 1) {
            throw new BusinessException("活动当前不在报名期");
        }

        // 2. 校验报名时间窗
        LocalDateTime now = LocalDateTime.now();
        if (activity.getRegistrationStartTime() != null && now.isBefore(activity.getRegistrationStartTime())) {
            throw new BusinessException("报名尚未开始");
        }
        if (activity.getRegistrationEndTime() != null && now.isAfter(activity.getRegistrationEndTime())) {
            throw new BusinessException("报名已截止");
        }

        // 3. 校验是否已存在有效的报名（待审核或已通过）
        long existingActiveCount = this.count(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, applyDto.getActivityId())
                .eq(ActivityRegistration::getUserId, wxId)
                .in(ActivityRegistration::getRegistrationStatus, STATUS_PENDING, STATUS_APPROVED));
        if (existingActiveCount > 0) {
            throw new BusinessException("您已报名该活动，请勿重复报名");
        }

        // 4. 从用户资料中取真实姓名与手机号
        WxUserInfo userInfo = wxUserInfoService.getOne(
                new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, wxId).last("LIMIT 1"));
        if (userInfo == null) {
            throw new BusinessException("未找到用户资料，请稍后重试");
        }
        String realName = userInfo.getName();
        String phone = userInfo.getPhone();
        if (realName == null || realName.trim().isEmpty()) {
            throw new BusinessException("请先在个人资料中填写真实姓名");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("请先在个人资料中绑定手机号");
        }

        // 5. 创建报名记录
        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(applyDto.getActivityId());
        registration.setUserId(wxId);
        registration.setUserName(realName);
        registration.setUserPhone(phone);
        registration.setRegistrationTime(now);
        registration.setRemark(applyDto.getRemark());

        boolean needReview = activity.getIsNeedReview() != null && activity.getIsNeedReview() == 1;

        if (needReview) {
            // 需要审核：先入待审，不增计数
            registration.setRegistrationStatus(STATUS_PENDING);
            boolean saveResult = this.save(registration);
            if (!saveResult) {
                throw new BusinessException("报名失败，请稍后重试");
            }
            log.info("用户报名活动（待审核） - 用户ID: {}, 活动ID: {}, 报名ID: {}",
                    wxId, applyDto.getActivityId(), registration.getRegistrationId());
            return true;
        }

        // 无需审核：原子尝试 +1，若超员则失败
        int updated = activityMapper.tryIncrementParticipants(applyDto.getActivityId());
        if (updated == 0) {
            throw new BusinessException("活动名额已满");
        }

        registration.setRegistrationStatus(STATUS_APPROVED);
        registration.setAuditTime(now);
        boolean saveResult = this.save(registration);
        if (!saveResult) {
            // 保存失败，回滚刚才的计数
            activityMapper.decrementParticipants(applyDto.getActivityId());
            throw new BusinessException("报名失败，请稍后重试");
        }

        log.info("用户报名活动（自动通过） - 用户ID: {}, 活动ID: {}, 报名ID: {}",
                wxId, applyDto.getActivityId(), registration.getRegistrationId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long wxId, Long registrationId) {
        if (wxId == null || registrationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        ActivityRegistration registration = this.getById(registrationId);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!wxId.equals(registration.getUserId())) {
            throw new BusinessException("无权操作他人的报名记录");
        }
        Integer status = registration.getRegistrationStatus();
        if (status == null || status.equals(STATUS_REJECTED) || status.equals(STATUS_CANCELLED)) {
            throw new BusinessException("当前状态不可取消");
        }

        // 已通过的报名取消：受时间限制
        if (status.equals(STATUS_APPROVED)) {
            Activity activity = activityMapper.selectById(registration.getActivityId());
            if (activity != null && activity.getStartTime() != null) {
                Duration toStart = Duration.between(LocalDateTime.now(), activity.getStartTime());
                if (toStart.toHours() < CANCEL_LOCK_HOURS_BEFORE_START) {
                    throw new BusinessException(
                            "活动开始前 " + CANCEL_LOCK_HOURS_BEFORE_START + " 小时内不可取消报名");
                }
            }
            // 释放名额
            activityMapper.decrementParticipants(registration.getActivityId());
        }

        ActivityRegistration update = new ActivityRegistration();
        update.setRegistrationId(registrationId);
        update.setRegistrationStatus(STATUS_CANCELLED);
        boolean result = this.updateById(update);

        // 同时逻辑删除该行，避免遗留干扰唯一性判断
        if (result) {
            this.removeById(registrationId);
        }

        log.info("用户取消报名 - 用户ID: {}, 报名ID: {}, 原状态: {}", wxId, registrationId, status);
        return result;
    }

    @Override
    public ActivityRegistrationStatusVo getMyStatus(Long wxId, Long activityId) {
        if (wxId == null || activityId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        ActivityRegistration latest = this.getOne(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getUserId, wxId)
                .orderByDesc(ActivityRegistration::getRegistrationTime)
                .last("LIMIT 1"));

        if (latest == null) {
            return ActivityRegistrationStatusVo.builder()
                    .hasRegistered(false)
                    .build();
        }
        return ActivityRegistrationStatusVo.builder()
                .hasRegistered(true)
                .registrationStatus(latest.getRegistrationStatus())
                .registrationId(String.valueOf(latest.getRegistrationId()))
                .auditReason(latest.getAuditReason())
                .build();
    }

    // ============== 管理员侧 ==============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean review(Long auditorId, ReviewActivityRegistrationDto reviewDto) {
        if (auditorId == null || reviewDto == null
                || reviewDto.getRegistrationId() == null || reviewDto.getReviewResult() == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }
        Integer reviewResult = reviewDto.getReviewResult();
        if (!STATUS_APPROVED.equals(reviewResult) && !STATUS_REJECTED.equals(reviewResult)) {
            throw new BusinessException("审核结果非法");
        }
        if (STATUS_REJECTED.equals(reviewResult)
                && (reviewDto.getAuditReason() == null || reviewDto.getAuditReason().trim().isEmpty())) {
            throw new BusinessException("拒绝时必须填写审核理由");
        }

        ActivityRegistration registration = this.getById(reviewDto.getRegistrationId());
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!STATUS_PENDING.equals(registration.getRegistrationStatus())) {
            throw new BusinessException("该报名记录已被处理");
        }

        // 权限校验：审核人必须是该活动校友会的管理员
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        ensureAuditorPermission(auditorId, activity);

        LocalDateTime now = LocalDateTime.now();

        if (STATUS_APPROVED.equals(reviewResult)) {
            // 通过：原子尝试 +1，若超员则失败
            int updated = activityMapper.tryIncrementParticipants(registration.getActivityId());
            if (updated == 0) {
                throw new BusinessException("活动名额已满，无法通过");
            }
        }

        ActivityRegistration update = new ActivityRegistration();
        update.setRegistrationId(registration.getRegistrationId());
        update.setRegistrationStatus(reviewResult);
        update.setAuditTime(now);
        update.setAuditorId(auditorId);
        update.setAuditReason(reviewDto.getAuditReason());

        boolean result = this.updateById(update);
        if (!result) {
            // 失败时回滚已增加的计数
            if (STATUS_APPROVED.equals(reviewResult)) {
                activityMapper.decrementParticipants(registration.getActivityId());
            }
            throw new BusinessException("审核操作失败");
        }

        log.info("管理员审核报名 - 审核人: {}, 报名ID: {}, 结果: {}",
                auditorId, registration.getRegistrationId(), reviewResult);
        return true;
    }

    @Override
    public PageVo<ActivityRegistrationListVo> queryPage(Long wxId, QueryActivityRegistrationListDto queryDto) {
        if (wxId == null || queryDto == null || queryDto.getActivityId() == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        Activity activity = activityMapper.selectById(queryDto.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        ensureAuditorPermission(wxId, activity);

        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityRegistration::getActivityId, queryDto.getActivityId());
        if (queryDto.getRegistrationStatus() != null) {
            wrapper.eq(ActivityRegistration::getRegistrationStatus, queryDto.getRegistrationStatus());
        }
        if (StringUtils.hasText(queryDto.getKeyword())) {
            String kw = queryDto.getKeyword().trim();
            wrapper.and(w -> w
                    .like(ActivityRegistration::getUserName, kw)
                    .or().like(ActivityRegistration::getUserPhone, kw));
        }
        wrapper.orderByDesc(ActivityRegistration::getRegistrationTime);

        Page<ActivityRegistration> pageResult = this.page(
                new Page<>(queryDto.getCurrent(), queryDto.getPageSize()), wrapper);

        // 批量补全 wx_user_info（头像/昵称）
        Map<Long, WxUserInfo> userInfoMap = batchLoadWxUserInfo(
                pageResult.getRecords().stream()
                        .map(ActivityRegistration::getUserId)
                        .collect(Collectors.toSet()));

        List<ActivityRegistrationListVo> voList = pageResult.getRecords().stream()
                .map(r -> toListVo(r, userInfoMap.get(r.getUserId())))
                .collect(Collectors.toList());

        Page<ActivityRegistrationListVo> voPage = new Page<>(
                queryDto.getCurrent(), queryDto.getPageSize(), pageResult.getTotal());
        voPage.setRecords(voList);

        return PageVo.of(voPage);
    }

    @Override
    public List<ActivityParticipantVo> getApprovedParticipants(Long activityId, Integer limit) {
        if (activityId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getRegistrationStatus, STATUS_APPROVED)
                .orderByDesc(ActivityRegistration::getAuditTime);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }

        List<ActivityRegistration> registrations = this.list(wrapper);
        if (registrations.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = registrations.stream()
                .map(ActivityRegistration::getUserId)
                .collect(Collectors.toSet());
        Map<Long, WxUserInfo> userInfoMap = batchLoadWxUserInfo(userIds);

        // 隐私过滤由 @PrivacyFilter 切面在 Controller 层基于 userId 字段统一处理
        return registrations.stream()
                .map(r -> toParticipantVo(r, userInfoMap.get(r.getUserId())))
                .collect(Collectors.toList());
    }

    @Override
    public Long getApprovedCount(Long activityId) {
        if (activityId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }
        return this.count(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getRegistrationStatus, STATUS_APPROVED));
    }

    // ============== 内部辅助方法 ==============

    /**
     * 校验当前操作人是否有权审核/查询该活动的报名。
     * 目前仅支持 校友会主办（organizer_type=1）的活动。
     * <p>使用 {@code getManagedOrganizations(wxId, 0)}（默认 roleScopedOnly=false），
     * 系统超级管理员（SYSTEM_SUPER_ADMIN）会被展开为全部启用校友会，因此也具备审核权限。</p>
     */
    private void ensureAuditorPermission(Long auditorId, Activity activity) {
        if (!ORGANIZER_TYPE_ALUMNI_ASSOCIATION.equals(activity.getOrganizerType())) {
            throw new BusinessException("暂不支持该类型活动的报名审核");
        }
        Set<Long> managedIds = userService.getManagedOrganizations(auditorId, 0).stream()
                .map(com.cmswe.alumni.common.vo.ManagedOrganizationListVo::getId)
                .collect(Collectors.toSet());
        if (managedIds.isEmpty() || !managedIds.contains(activity.getOrganizerId())) {
            throw new BusinessException("无权操作该活动");
        }
    }

    private Map<Long, WxUserInfo> batchLoadWxUserInfo(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<WxUserInfo> userInfos = wxUserInfoService.list(
                new LambdaQueryWrapper<WxUserInfo>().in(WxUserInfo::getWxId, userIds));
        return userInfos.stream()
                .collect(Collectors.toMap(WxUserInfo::getWxId, info -> info, (a, b) -> a));
    }

    private ActivityRegistrationListVo toListVo(ActivityRegistration r, WxUserInfo userInfo) {
        return ActivityRegistrationListVo.builder()
                .registrationId(String.valueOf(r.getRegistrationId()))
                .activityId(r.getActivityId() == null ? null : String.valueOf(r.getActivityId()))
                .userId(r.getUserId() == null ? null : String.valueOf(r.getUserId()))
                .userName(r.getUserName())
                .userPhone(r.getUserPhone())
                .userAvatar(userInfo != null ? userInfo.getAvatarUrl() : null)
                .userNickname(userInfo != null ? userInfo.getNickname() : null)
                .registrationTime(r.getRegistrationTime())
                .registrationStatus(r.getRegistrationStatus())
                .auditTime(r.getAuditTime())
                .auditReason(r.getAuditReason())
                .remark(r.getRemark())
                .build();
    }

    /**
     * 组装 ActivityParticipantVo。隐私字段（nickname / gender）由 @PrivacyFilter
     * 切面在 Controller 层基于 wx_id 自动置 null，本方法只负责"无 user_info 兜底"。
     */
    private ActivityParticipantVo toParticipantVo(ActivityRegistration r, WxUserInfo userInfo) {
        LocalDate joinDate = r.getAuditTime() != null
                ? r.getAuditTime().toLocalDate()
                : (r.getRegistrationTime() != null ? r.getRegistrationTime().toLocalDate() : null);

        ActivityParticipantVo.ActivityParticipantVoBuilder builder = ActivityParticipantVo.builder()
                .userId(r.getUserId() == null ? null : String.valueOf(r.getUserId()))
                .joinDate(joinDate);

        if (userInfo != null) {
            builder.nickname(userInfo.getNickname() != null ? userInfo.getNickname() : "匿名校友")
                    .avatarUrl(userInfo.getAvatarUrl())
                    .gender(userInfo.getGender());
        } else {
            builder.nickname("匿名校友");
        }
        return builder.build();
    }
}
