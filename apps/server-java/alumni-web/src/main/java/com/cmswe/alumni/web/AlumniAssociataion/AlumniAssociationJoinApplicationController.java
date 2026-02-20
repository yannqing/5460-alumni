package com.cmswe.alumni.web.AlumniAssociataion;

import com.cmswe.alumni.api.association.AlumniAssociationJoinApplicationService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ApplyAlumniAssociationDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationJoinApplicationListDto;
import com.cmswe.alumni.common.dto.QuitAlumniAssociationDto;
import com.cmswe.alumni.common.dto.ReviewAlumniAssociationJoinApplicationDto;
import com.cmswe.alumni.common.dto.UpdateAlumniAssociationJoinApplicationDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplicationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplicationListVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 校友会加入申请控制器
 */
@Tag(name = "校友会加入申请")
@RestController
@RequestMapping("/AlumniAssociationJoinApplication")
public class AlumniAssociationJoinApplicationController {

    @Resource
    private AlumniAssociationJoinApplicationService alumniAssociationJoinApplicationService;

    /**
     * 申请加入校友会（普通用户）
     *
     * @param securityUser 当前登录用户
     * @param applyDto     申请信息
     * @return 申请结果
     */
    @PostMapping("/apply")
    @Operation(summary = "申请加入校友会（普通用户）")
    public BaseResponse<Boolean> applyToJoinAssociation(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ApplyAlumniAssociationDto applyDto) {

        // 从当前登录用户中获取用户ID
        Long wxId = securityUser.getWxUser().getWxId();

        boolean result = alumniAssociationJoinApplicationService.applyToJoinAssociation(wxId, applyDto);

        if (result) {
            return ResultUtils.success(Code.SUCCESS, true, "申请提交成功，请等待审核");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "申请提交失败");
        }
    }

    /**
     * 审核校友会加入申请
     *
     * @param securityUser 当前登录用户（审核人）
     * @param reviewDto    审核信息
     * @return 审核结果
     */
    @PostMapping("/review")
    @Operation(summary = "审核校友会加入申请")
    public BaseResponse<Boolean> reviewApplication(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ReviewAlumniAssociationJoinApplicationDto reviewDto) {

        // 从当前登录用户中获取审核人ID
        Long reviewerId = securityUser.getWxUser().getWxId();

        boolean result = alumniAssociationJoinApplicationService.reviewApplication(reviewerId, reviewDto);

        if (result) {
            String message = reviewDto.getReviewResult() == 1 ? "审核通过" : "审核拒绝";
            return ResultUtils.success(Code.SUCCESS, true, message + "操作成功");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "审核操作失败");
        }
    }

    /**
     * 分页查询校友会加入申请列表
     *
     * @param queryDto 查询条件
     * @return 申请列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询校友会加入申请列表")
    public BaseResponse<PageVo<AlumniAssociationJoinApplicationListVo>> queryApplicationPage(
            @Valid @RequestBody QueryAlumniAssociationJoinApplicationListDto queryDto) {

        PageVo<AlumniAssociationJoinApplicationListVo> pageVo =
                alumniAssociationJoinApplicationService.queryApplicationPage(queryDto);

        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    /**
     * 查看用户自己的申请详情
     *
     * @param securityUser        当前登录用户
     * @param alumniAssociationId 校友会ID
     * @return 申请详情
     */
    @GetMapping("/detail/{alumniAssociationId}")
    @Operation(summary = "查看用户自己的申请详情")
    public BaseResponse<AlumniAssociationJoinApplicationDetailVo> getApplicationDetail(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long alumniAssociationId) {

        // 从当前登录用户中获取用户ID
        Long wxId = securityUser.getWxUser().getWxId();

        AlumniAssociationJoinApplicationDetailVo detailVo =
                alumniAssociationJoinApplicationService.getApplicationDetail(wxId, alumniAssociationId);

        return ResultUtils.success(Code.SUCCESS, detailVo, "查询成功");
    }

    /**
     * 编辑并重新提交待审核的校友会加入申请（普通用户）
     *
     * @param securityUser 当前登录用户
     * @param updateDto    更新信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @Operation(summary = "编辑并重新提交待审核的校友会加入申请（普通用户）")
    public BaseResponse<Boolean> updateAndResubmitApplication(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody UpdateAlumniAssociationJoinApplicationDto updateDto) {

        // 从当前登录用户中获取用户ID
        Long wxId = securityUser.getWxUser().getWxId();

        boolean result = alumniAssociationJoinApplicationService.updateAndResubmitApplication(wxId, updateDto);

        if (result) {
            return ResultUtils.success(Code.SUCCESS, true, "申请更新成功，请等待审核");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "申请更新失败");
        }
    }

    /**
     * 撤销校友会申请（普通用户）
     *
     * @param securityUser  当前登录用户
     * @param applicationId 申请ID
     * @return 撤销结果
     */
    @PutMapping("/cancel/{applicationId}")
    @Operation(summary = "撤销校友会申请（普通用户）")
    public BaseResponse<Boolean> cancelApplication(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long applicationId) {

        // 从当前登录用户中获取用户ID
        Long wxId = securityUser.getWxUser().getWxId();

        boolean result = alumniAssociationJoinApplicationService.cancelApplication(wxId, applicationId);

        if (result) {
            return ResultUtils.success(Code.SUCCESS, true, "申请撤销成功");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "申请撤销失败");
        }
    }

    /**
     * 退出校友会（普通用户）
     *
     * @param securityUser 当前登录用户
     * @param quitDto      退出请求参数
     * @return 退出结果
     */
    @PostMapping("/quit")
    @Operation(summary = "退出校友会（普通用户）")
    public BaseResponse<Boolean> quitAlumniAssociation(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody QuitAlumniAssociationDto quitDto) {

        // 从当前登录用户中获取用户ID
        Long wxId = securityUser.getWxUser().getWxId();

        boolean result = alumniAssociationJoinApplicationService.quitAlumniAssociation(wxId, quitDto.getAlumniAssociationId());

        if (result) {
            return ResultUtils.success(Code.SUCCESS, true, "退出校友会成功");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "退出校友会失败");
        }
    }
}
