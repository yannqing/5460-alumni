package com.cmswe.alumni.web;

import com.cmswe.alumni.api.system.ActivityService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.PublishTopicDto;
import com.cmswe.alumni.common.dto.QueryShopActivityDto;
import com.cmswe.alumni.common.dto.UpdateActivityDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.ActivityDetailVo;
import com.cmswe.alumni.common.vo.ActivityListVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 活动管理 Controller
 *
 * @author CNI Alumni System
 */
@Tag(name = "活动管理", description = "活动相关接口")
@Slf4j
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Resource
    private ActivityService activityService;

    /**
     * 商家发布话题
     *
     * @param securityUser    当前登录用户
     * @param publishTopicDto 发布话题请求参数
     * @return 是否成功
     */
    @PostMapping("/publishTopic")
    @Operation(summary = "商家发布话题")
    public BaseResponse<Boolean> publishTopic(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody PublishTopicDto publishTopicDto) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("商家发布话题 - 用户ID: {}, 主办方ID: {}, 活动标题: {}",
                wxId, publishTopicDto.getOrganizerId(), publishTopicDto.getActivityTitle());

        boolean result = activityService.publishTopic(wxId, publishTopicDto);

        if (result) {
            log.info("商家发布话题成功 - 用户ID: {}, 主办方ID: {}, 活动标题: {}",
                    wxId, publishTopicDto.getOrganizerId(), publishTopicDto.getActivityTitle());
            return ResultUtils.success(Code.SUCCESS, true, "发布成功");
        } else {
            log.error("商家发布话题失败 - 用户ID: {}, 主办方ID: {}, 活动标题: {}",
                    wxId, publishTopicDto.getOrganizerId(), publishTopicDto.getActivityTitle());
            return ResultUtils.failure(Code.FAILURE, false, "发布失败");
        }
    }

    /**
     * 根据ID查询活动详情
     *
     * @param activityId 活动ID
     * @return 活动详情
     */
    @GetMapping("/{activityId}")
    @Operation(summary = "根据ID查询活动详情")
    public BaseResponse<ActivityDetailVo> getActivityDetail(@PathVariable Long activityId) {
        log.info("查询活动详情 - 活动ID: {}", activityId);
        ActivityDetailVo activityDetail = activityService.getActivityDetail(activityId);
        return ResultUtils.success(Code.SUCCESS, activityDetail, "查询成功");
    }

    /**
     * 商家根据门店ID查询活动列表
     *
     * @param securityUser 当前登录用户
     * @param queryDto     查询参数
     * @return 活动列表分页数据
     */
    @PostMapping("/shop/list")
    @Operation(summary = "商家根据门店ID查询活动列表")
    public BaseResponse<PageVo<ActivityListVo>> getShopActivities(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody QueryShopActivityDto queryDto) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("商家查询门店活动列表 - 用户ID: {}, 门店ID: {}", wxId, queryDto.getShopId());

        PageVo<ActivityListVo> result = activityService.getShopActivities(wxId, queryDto);

        log.info("商家查询门店活动列表成功 - 用户ID: {}, 门店ID: {}, 共{}条记录",
                wxId, queryDto.getShopId(), result.getTotal());
        return ResultUtils.success(Code.SUCCESS, result, "查询成功");
    }

    /**
     * 商家编辑活动
     *
     * @param securityUser      当前登录用户
     * @param updateActivityDto 编辑活动请求参数
     * @return 是否成功
     */
    @PostMapping("/update")
    @Operation(summary = "商家编辑活动")
    public BaseResponse<Boolean> updateActivity(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody UpdateActivityDto updateActivityDto) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("商家编辑活动 - 用户ID: {}, 活动ID: {}", wxId, updateActivityDto.getActivityId());

        boolean result = activityService.updateActivity(wxId, updateActivityDto);

        if (result) {
            log.info("商家编辑活动成功 - 用户ID: {}, 活动ID: {}", wxId, updateActivityDto.getActivityId());
            return ResultUtils.success(Code.SUCCESS, true, "编辑成功");
        } else {
            log.error("商家编辑活动失败 - 用户ID: {}, 活动ID: {}", wxId, updateActivityDto.getActivityId());
            return ResultUtils.failure(Code.FAILURE, false, "编辑失败");
        }
    }

    /**
     * 商家删除活动
     *
     * @param securityUser 当前登录用户
     * @param activityId   活动ID
     * @return 是否成功
     */
    @DeleteMapping("/{activityId}")
    @Operation(summary = "商家删除活动")
    public BaseResponse<Boolean> deleteActivity(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long activityId) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("商家删除活动 - 用户ID: {}, 活动ID: {}", wxId, activityId);

        boolean result = activityService.deleteActivity(wxId, activityId);

        if (result) {
            log.info("商家删除活动成功 - 用户ID: {}, 活动ID: {}", wxId, activityId);
            return ResultUtils.success(Code.SUCCESS, true, "删除成功");
        } else {
            log.error("商家删除活动失败 - 用户ID: {}, 活动ID: {}", wxId, activityId);
            return ResultUtils.failure(Code.FAILURE, false, "删除失败");
        }
    }
}
