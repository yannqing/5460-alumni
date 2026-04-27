package com.cmswe.alumni.web;

import com.cmswe.alumni.api.system.ActivityRegistrationService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ApplyActivityRegistrationDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.ActivityParticipantVo;
import com.cmswe.alumni.common.vo.ActivityRegistrationStatusVo;
import com.cmswe.alumni.web.aop.PrivacyFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动报名控制器（用户侧）
 *
 * @author CNI Alumni System
 */
@Tag(name = "活动报名", description = "活动报名相关接口（用户侧）")
@Slf4j
@RestController
@RequestMapping("/activityRegistration")
public class ActivityRegistrationController {

    /** 详情页参与者头像默认展示数量上限 */
    private static final int DEFAULT_PARTICIPANTS_LIMIT = 6;

    @Resource
    private ActivityRegistrationService activityRegistrationService;

    /**
     * 用户报名活动
     */
    @PostMapping("/apply")
    @Operation(summary = "用户报名活动")
    public BaseResponse<Boolean> apply(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ApplyActivityRegistrationDto applyDto) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("用户报名活动 - 用户ID: {}, 活动ID: {}", wxId, applyDto.getActivityId());

        boolean result = activityRegistrationService.apply(wxId, applyDto);
        return ResultUtils.success(Code.SUCCESS, result, "报名提交成功");
    }

    /**
     * 用户取消自己的报名
     */
    @PostMapping("/cancel/{registrationId}")
    @Operation(summary = "用户取消自己的报名")
    public BaseResponse<Boolean> cancel(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long registrationId) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("用户取消报名 - 用户ID: {}, 报名ID: {}", wxId, registrationId);

        boolean result = activityRegistrationService.cancel(wxId, registrationId);
        return ResultUtils.success(Code.SUCCESS, result, "取消成功");
    }

    /**
     * 查询当前用户在某活动中的报名状态
     */
    @GetMapping("/myStatus/{activityId}")
    @Operation(summary = "查询当前用户在某活动的报名状态")
    public BaseResponse<ActivityRegistrationStatusVo> getMyStatus(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long activityId) {
        Long wxId = securityUser.getWxUser().getWxId();
        ActivityRegistrationStatusVo statusVo = activityRegistrationService.getMyStatus(wxId, activityId);
        return ResultUtils.success(Code.SUCCESS, statusVo, "查询成功");
    }

    /**
     * 详情页拉取活动已通过的参与者列表（隐私过滤）
     */
    @GetMapping("/participants/{activityId}")
    @Operation(summary = "查询活动已通过的参与者（隐私过滤）")
    @PrivacyFilter(userIdField = "userId")
    public BaseResponse<List<ActivityParticipantVo>> getParticipants(
            @PathVariable Long activityId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        int actualLimit = (limit == null || limit <= 0) ? DEFAULT_PARTICIPANTS_LIMIT : limit;
        List<ActivityParticipantVo> list =
                activityRegistrationService.getApprovedParticipants(activityId, actualLimit);
        return ResultUtils.success(Code.SUCCESS, list, "查询成功");
    }
}
