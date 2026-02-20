package com.cmswe.alumni.web.AlumniAssociataion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationMemberListRequest;
import com.cmswe.alumni.common.dto.QueryOrganizationTreeDto;
import com.cmswe.alumni.common.entity.Activity;
import com.cmswe.alumni.common.entity.ActivityRegistration;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationListDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniAssociationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;

import com.cmswe.alumni.common.vo.OrganizationTreeVo;
import com.cmswe.alumni.common.vo.OrganizationTreeV2Vo;
import com.cmswe.alumni.common.vo.OrganizationMemberResponse;
import com.cmswe.alumni.service.system.mapper.ActivityMapper;
import com.cmswe.alumni.service.system.mapper.ActivityRegistrationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "校友会")
@Slf4j
@RestController
@RequestMapping("/AlumniAssociation")
public class AlumniAssociationController {

    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ActivityRegistrationMapper activityRegistrationMapper;

    @Resource
    private WxUserInfoService wxUserInfoService;

    @PostMapping("/page")
    @Operation(summary = "分页查询校友会列表")
    public BaseResponse<PageVo<AlumniAssociationListVo>> selectPage(
            @RequestBody QueryAlumniAssociationListDto alumniAssociationListDto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        // 获取当前用户ID（如果未登录则为null）
        Long currentUserId = securityUser != null && securityUser.getWxUser() != null
                ? securityUser.getWxUser().getWxId()
                : null;

        PageVo<AlumniAssociationListVo> pageVo = alumniAssociationService.selectByPage(alumniAssociationListDto, currentUserId);
        return ResultUtils.success(Code.SUCCESS, pageVo, "分页查询成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 id查询校友会详情")
    public BaseResponse<AlumniAssociationDetailVo> getAlumniAssociationDetailVoById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        // 获取当前用户ID，如果未登录则为null
        Long wxId = securityUser != null && securityUser.getWxUser() != null
                ? securityUser.getWxUser().getWxId()
                : null;
        AlumniAssociationDetailVo alumniAssociationDetailVo = alumniAssociationService
                .getAlumniAssociationDetailVoById(id, wxId);
        return ResultUtils.success(Code.SUCCESS, alumniAssociationDetailVo, "查询成功");
    }

    @PostMapping("/member/page")
    @Operation(summary = "查看校友会下的成员列表")
    public BaseResponse<Page<OrganizationMemberResponse>> getAlumniAssociationMemberPage(
            @RequestBody QueryAlumniAssociationMemberListRequest queryAlumniAssociationMemberListRequest,
            @AuthenticationPrincipal SecurityUser securityUser) {
        // 获取当前用户ID（如果未登录则为null）
        Long currentUserId = securityUser != null && securityUser.getWxUser() != null
                ? securityUser.getWxUser().getWxId()
                : null;
        Page<OrganizationMemberResponse> queryAlumniAssociationMemberListResponsePage = alumniAssociationService
                .getAlumniAssociationMemberPage(queryAlumniAssociationMemberListRequest, currentUserId);
        return ResultUtils.success(Code.SUCCESS, queryAlumniAssociationMemberListResponsePage);
    }

    @PostMapping("/my-president/page")
    @Operation(summary = "查询本人是会长的校友会列表（超级管理员可查看所有）")
    public BaseResponse<PageVo<AlumniAssociationListVo>> getMyPresidentAssociationPage(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody QueryAlumniAssociationListDto queryDto) {
        // 从当前登录用户中获取用户ID
        Long wxId = securityUser.getWxUser().getWxId();
        PageVo<AlumniAssociationListVo> pageVo = alumniAssociationService.getMyPresidentAssociationPage(queryDto, wxId);
        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    /**
     * 获取校友会组织架构树
     *
     * @param request 查询请求参数
     * @return 返回组织架构树
     */
    @PostMapping("/organizationTree")
    @Operation(summary = "获取校友会组织架构树")
    public BaseResponse<List<OrganizationTreeVo>> getOrganizationTree(
            @Valid @RequestBody QueryOrganizationTreeDto request) {
        log.info("查询校友会组织架构树，校友会 ID: {}", request.getAlumniAssociationId());

        List<OrganizationTreeVo> organizationTree = alumniAssociationService
                .getOrganizationTree(request.getAlumniAssociationId());

        log.info("查询校友会组织架构树成功，校友会 ID: {}, 根节点数: {}",
                request.getAlumniAssociationId(), organizationTree.size());

        return ResultUtils.success(Code.SUCCESS, organizationTree, "查询成功");
    }

    /**
     * 获取校友会组织架构树 V2（基于username，支持wxId为空）
     *
     * @param request 查询请求参数
     * @return 返回组织架构树 V2
     */
    @PostMapping("/organizationTree/v2")
    @Operation(summary = "获取校友会组织架构树V2（基于username，支持wxId为空）")
    public BaseResponse<List<OrganizationTreeV2Vo>> getOrganizationTreeV2(
            @Valid @RequestBody QueryOrganizationTreeDto request) {
        log.info("查询校友会组织架构树V2，校友会 ID: {}", request.getAlumniAssociationId());

        List<OrganizationTreeV2Vo> organizationTree = alumniAssociationService
                .getOrganizationTreeV2(request.getAlumniAssociationId());

        log.info("查询校友会组织架构树V2成功，校友会 ID: {}, 根节点数: {}",
                request.getAlumniAssociationId(), organizationTree.size());

        return ResultUtils.success(Code.SUCCESS, organizationTree, "查询成功");
    }

    /**
     * 用户报名参加活动
     *
     * @param activityId 活动ID
     * @param securityUser 当前登录用户
     * @return 返回报名结果
     */
    @PostMapping("/activity/register/{activityId}")
    @Operation(summary = "用户报名参加活动")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> registerActivity(
            @PathVariable Long activityId,
            @AuthenticationPrincipal SecurityUser securityUser) {

        // 1. 参数校验
        if (activityId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "活动ID不能为空");
        }

        if (securityUser == null || securityUser.getWxUser() == null) {
            throw new BusinessException(Code.FAILURE, "请先登录");
        }

        Long userId = securityUser.getWxUser().getWxId();
        log.info("用户报名活动，用户 ID: {}, 活动 ID: {}", userId, activityId);

        // 2. 查询活动是否存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("活动不存在，活动 ID: {}", activityId);
            throw new BusinessException(Code.FAILURE, "活动不存在");
        }

        // 3. 检查活动是否需要报名
        if (activity.getIsSignup() == null || activity.getIsSignup() != 1) {
            log.error("该活动无需报名，活动 ID: {}", activityId);
            throw new BusinessException(Code.FAILURE, "该活动无需报名");
        }

        // 4. 检查报名时间
        LocalDateTime now = LocalDateTime.now();
        if (activity.getRegistrationStartTime() != null && now.isBefore(activity.getRegistrationStartTime())) {
            throw new BusinessException(Code.FAILURE, "报名尚未开始");
        }
        if (activity.getRegistrationEndTime() != null && now.isAfter(activity.getRegistrationEndTime())) {
            throw new BusinessException(Code.FAILURE, "报名已截止");
        }

        // 5. 检查是否已经报名
        LambdaQueryWrapper<ActivityRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRegistration::getActivityId, activityId)
                    .eq(ActivityRegistration::getUserId, userId);
        ActivityRegistration existingRegistration = activityRegistrationMapper.selectOne(queryWrapper);

        if (existingRegistration != null) {
            // 如果已取消，可以重新报名
            if (existingRegistration.getRegistrationStatus() == 3) {
                existingRegistration.setRegistrationStatus(
                    activity.getIsNeedReview() != null && activity.getIsNeedReview() == 1 ? 0 : 1
                );
                existingRegistration.setRegistrationTime(now);
                existingRegistration.setAuditTime(null);
                existingRegistration.setAuditReason(null);
                existingRegistration.setUpdateTime(now);

                int updateResult = activityRegistrationMapper.updateById(existingRegistration);
                if (updateResult > 0) {
                    log.info("用户重新报名活动成功，用户 ID: {}, 活动 ID: {}", userId, activityId);
                    return ResultUtils.success(Code.SUCCESS, true, "报名成功");
                } else {
                    throw new BusinessException("报名失败，请重试");
                }
            } else {
                throw new BusinessException(Code.FAILURE, "您已经报名过该活动");
            }
        }

        // 6. 检查是否已满员
        if (activity.getMaxParticipants() != null && activity.getCurrentParticipants() != null) {
            if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
                throw new BusinessException(Code.FAILURE, "活动报名人数已满");
            }
        }

        // 7. 查询用户详细信息
        WxUserInfo wxUserInfo = wxUserInfoService.getOne(
            new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, userId)
        );

        // 8. 创建报名记录
        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setUserName(wxUserInfo != null ? wxUserInfo.getName() : null);
        registration.setUserPhone(wxUserInfo != null ? wxUserInfo.getPhone() : null);
        registration.setRegistrationTime(now);
        // 如果需要审核，状态为待审核(0)，否则直接通过(1)
        registration.setRegistrationStatus(
            activity.getIsNeedReview() != null && activity.getIsNeedReview() == 1 ? 0 : 1
        );
        registration.setCreateTime(now);
        registration.setUpdateTime(now);

        int insertResult = activityRegistrationMapper.insert(registration);

        if (insertResult > 0) {
            // 9. 更新活动当前报名人数（仅当无需审核或审核通过时）
            if (registration.getRegistrationStatus() == 1) {
                activity.setCurrentParticipants(
                    activity.getCurrentParticipants() == null ? 1 : activity.getCurrentParticipants() + 1
                );
                activityMapper.updateById(activity);
            }

            log.info("用户报名活动成功，用户 ID: {}, 活动 ID: {}, 报名 ID: {}",
                    userId, activityId, registration.getRegistrationId());
            return ResultUtils.success(Code.SUCCESS, true, "报名成功");
        } else {
            log.error("报名活动失败，用户 ID: {}, 活动 ID: {}", userId, activityId);
            throw new BusinessException("报名失败，请重试");
        }
    }

    /**
     * 用户取消报名
     *
     * @param activityId 活动ID
     * @param securityUser 当前登录用户
     * @return 返回取消结果
     */
    @DeleteMapping("/activity/cancel-registration/{activityId}")
    @Operation(summary = "用户取消活动报名")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> cancelRegistration(
            @PathVariable Long activityId,
            @AuthenticationPrincipal SecurityUser securityUser) {

        // 1. 参数校验
        if (activityId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "活动ID不能为空");
        }

        if (securityUser == null || securityUser.getWxUser() == null) {
            throw new BusinessException(Code.FAILURE, "请先登录");
        }

        Long userId = securityUser.getWxUser().getWxId();
        log.info("用户取消活动报名，用户 ID: {}, 活动 ID: {}", userId, activityId);

        // 2. 查询活动是否存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("活动不存在，活动 ID: {}", activityId);
            throw new BusinessException(Code.FAILURE, "活动不存在");
        }

        // 3. 查询报名记录
        LambdaQueryWrapper<ActivityRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRegistration::getActivityId, activityId)
                    .eq(ActivityRegistration::getUserId, userId);
        ActivityRegistration registration = activityRegistrationMapper.selectOne(queryWrapper);

        if (registration == null) {
            throw new BusinessException(Code.FAILURE, "您未报名该活动");
        }

        // 4. 检查报名状态（已取消的不能再取消）
        if (registration.getRegistrationStatus() == 3) {
            throw new BusinessException(Code.FAILURE, "报名已取消，无需重复操作");
        }

        // 5. 更新报名状态为已取消(3)
        registration.setRegistrationStatus(3);
        registration.setUpdateTime(LocalDateTime.now());

        int updateResult = activityRegistrationMapper.updateById(registration);

        if (updateResult > 0) {
            // 6. 更新活动当前报名人数（仅当之前是审核通过状态）
            if (registration.getRegistrationStatus() == 1 && activity.getCurrentParticipants() != null && activity.getCurrentParticipants() > 0) {
                activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
                activityMapper.updateById(activity);
            }

            log.info("用户取消活动报名成功，用户 ID: {}, 活动 ID: {}", userId, activityId);
            return ResultUtils.success(Code.SUCCESS, true, "取消报名成功");
        } else {
            log.error("取消报名失败，用户 ID: {}, 活动 ID: {}", userId, activityId);
            throw new BusinessException("取消报名失败，请重试");
        }
    }
}