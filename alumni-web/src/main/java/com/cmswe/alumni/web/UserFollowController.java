package com.cmswe.alumni.web;

import com.cmswe.alumni.api.user.FollowStatisticsService;
import com.cmswe.alumni.api.user.UserBlacklistService;
import com.cmswe.alumni.api.user.UserFollowService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.*;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注/粉丝/好友/黑名单/统计 统一控制器
 */
@Tag(name = "用户关注服务")
@RestController
@RequestMapping("/follow")
public class UserFollowController {

    @Resource
    private UserFollowService userFollowService;

    @Resource
    private UserBlacklistService userBlacklistService;

    @Resource
    private FollowStatisticsService followStatisticsService;

    @PostMapping("/add")
    @Operation(summary = "关注目标")
    public BaseResponse<?> follow(@AuthenticationPrincipal SecurityUser securityUser, @Valid @RequestBody FollowDto followDto) {
        boolean result = userFollowService.follow(securityUser.getWxUser().getWxId(), followDto);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "关注成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "关注失败");
    }

    @DeleteMapping("/remove")
    @Operation(summary = "取消关注")
    public BaseResponse<?> unfollow(@AuthenticationPrincipal SecurityUser securityUser, @RequestBody UnFollowRequest unFollowRequest) {
        boolean result = userFollowService.unfollow(securityUser.getWxUser().getWxId(), unFollowRequest);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "取消关注成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "取消关注失败");
    }

    @PutMapping("/updateStatus")
    @Operation(summary = "更新关注状态")
    public BaseResponse<?> updateFollowStatus(@AuthenticationPrincipal SecurityUser securityUser, @Valid @RequestBody FollowDto followDto) {
        boolean result = userFollowService.updateFollowStatus(securityUser.getWxUser().getWxId(), followDto);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "更新关注状态成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "更新关注状态失败");
    }

    @GetMapping("/check")
    @Operation(summary = "检查是否已关注")
    public BaseResponse<Boolean> isFollowing(@AuthenticationPrincipal SecurityUser securityUser,
                                             @RequestParam Integer targetType,
                                             @RequestParam Long targetId) {
        boolean isFollowing = userFollowService.isFollowing(securityUser.getWxUser().getWxId(), targetType, targetId);
        return ResultUtils.success(Code.SUCCESS, isFollowing, "查询成功");
    }

    @PostMapping("/following/page")
    @Operation(summary = "分页查询我关注的列表（支持多种目标类型：用户、校友会、母校、商户）")
    public BaseResponse<PageVo<FollowingItemVo<?>>> getFollowingList(@AuthenticationPrincipal SecurityUser securityUser,
                                                                       @RequestBody QueryFollowingListDto queryDto) {
        PageVo<FollowingItemVo<?>> pageVo = userFollowService.getFollowingList(securityUser.getWxUser().getWxId(), queryDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    @PostMapping("/follower/page")
    @Operation(summary = "分页查询我的粉丝列表")
    public BaseResponse<PageVo<FollowerItemVo>> getFollowerList(@AuthenticationPrincipal SecurityUser securityUser,
                                                                  @RequestBody QueryFollowerListDto queryDto) {
        PageVo<FollowerItemVo> pageVo = userFollowService.getFollowerList(securityUser.getWxUser().getWxId(), queryDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    @PostMapping("/friend/page")
    @Operation(summary = "分页查询好友列表（互相关注）")
    public BaseResponse<PageVo<FriendItemVo>> getFriendList(@AuthenticationPrincipal SecurityUser securityUser,
                                                              @RequestBody QueryFriendListDto queryDto) {
        PageVo<FriendItemVo> pageVo = userFollowService.getFriendList(securityUser.getWxUser().getWxId(), queryDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    // ==================== 黑名单相关接口 ====================

    @PostMapping("/blacklist/add")
    @Operation(summary = "拉黑用户")
    public BaseResponse<?> blockUser(@AuthenticationPrincipal SecurityUser securityUser, @Valid @RequestBody BlacklistDto blacklistDto) {
        boolean result = userBlacklistService.blockUser(securityUser.getWxUser().getWxId(), blacklistDto);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "拉黑成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "拉黑失败");
    }

    @DeleteMapping("/blacklist/remove")
    @Operation(summary = "取消拉黑")
    public BaseResponse<?> unblockUser(@AuthenticationPrincipal SecurityUser securityUser, @RequestParam Long blockedWxId) {
        boolean result = userBlacklistService.unblockUser(securityUser.getWxUser().getWxId(), blockedWxId);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "取消拉黑成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "取消拉黑失败");
    }

    @GetMapping("/blacklist/check")
    @Operation(summary = "检查是否已拉黑")
    public BaseResponse<Boolean> isBlocked(@AuthenticationPrincipal SecurityUser securityUser, @RequestParam Long blockedWxId) {
        boolean isBlocked = userBlacklistService.isBlocked(securityUser.getWxUser().getWxId(), blockedWxId);
        return ResultUtils.success(Code.SUCCESS, isBlocked, "查询成功");
    }

    // ==================== 统计相关接口 ====================

    @GetMapping("/statistics/current")
    @Operation(summary = "获取当前用户的关注统计")
    public BaseResponse<UserFollowStatisticsVo> getCurrentUserStatistics(@AuthenticationPrincipal SecurityUser securityUser) {
        UserFollowStatisticsVo statistics = followStatisticsService.getCurrentUserStatistics(securityUser.getWxUser().getWxId());
        return ResultUtils.success(Code.SUCCESS, statistics, "查询成功");
    }
}
