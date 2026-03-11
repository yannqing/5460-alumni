package com.cmswe.alumni.web.AlumniAssociataion;

import com.cmswe.alumni.api.association.AlumniAssociationApplicationService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ApplyCreateAlumniAssociationDto;
import com.cmswe.alumni.common.dto.QuerySystemAdminApplicationListDto;
import com.cmswe.alumni.common.dto.ReviewAlumniAssociationApplicationDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniAssociationApplicationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationApplicationListVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 校友会创建申请控制器
 */
@Tag(name = "校友会创建申请")
@RestController
@RequestMapping("/AlumniAssociationApplication")
public class AlumniAssociationApplicationController {

    @Resource
    private AlumniAssociationApplicationService alumniAssociationApplicationService;

    /**
     * 申请创建校友会
     *
     * @param securityUser 当前登录用户
     * @param applyDto     申请信息
     * @return 申请结果
     */
    @PostMapping("/apply")
    @Operation(summary = "申请创建校友会")
    public BaseResponse<Boolean> applyToCreateAssociation(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ApplyCreateAlumniAssociationDto applyDto) {

        // 从当前登录用户中获取用户ID
        Long wxId = securityUser.getWxUser().getWxId();

        boolean result = alumniAssociationApplicationService.applyToCreateAssociation(wxId, applyDto);

        if (result) {
            return ResultUtils.success(Code.SUCCESS, true, "校友会创建申请提交成功，请等待审核");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "校友会创建申请提交失败");
        }
    }

    /**
     * 系统管理员分页查询所有校友会创建申请列表
     *
     * @param queryDto 查询条件（可选母校ID、校处会ID、审核状态等）
     * @return 校友会创建申请列表
     */
    @PostMapping("/querySystemAdminApplicationPage")
    @Operation(summary = "系统管理员分页查询所有校友会创建申请列表")
    public BaseResponse<PageVo<AlumniAssociationApplicationListVo>> querySystemAdminApplicationPage(
            @Valid @RequestBody QuerySystemAdminApplicationListDto queryDto) {

        PageVo<AlumniAssociationApplicationListVo> pageVo = alumniAssociationApplicationService
                .querySystemAdminApplicationPage(queryDto);

        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    /**
     * 系统管理员审核校友会创建申请
     *
     * @param securityUser 当前登录用户（系统管理员）
     * @param reviewDto    审核信息
     * @return 审核结果
     */
    @PostMapping("/reviewApplication")
    @Operation(summary = "系统管理员审核校友会创建申请")
    public BaseResponse<Boolean> reviewApplication(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ReviewAlumniAssociationApplicationDto reviewDto) {

        // 从当前登录用户中获取审核人ID
        Long reviewerId = securityUser.getWxUser().getWxId();

        boolean result = alumniAssociationApplicationService.reviewApplication(reviewerId, reviewDto);

        if (result) {
            String message = reviewDto.getReviewResult() == 1 ? "审核通过" : "审核拒绝";
            return ResultUtils.success(Code.SUCCESS, true, message + "操作成功");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "审核操作失败");
        }
    }

    /**
     * 根据申请ID查询申请详情
     *
     * @param applicationId 申请ID
     * @return 申请详情
     */
    @GetMapping("/detail/{applicationId}")
    @Operation(summary = "根据申请ID查询申请详情")
    public BaseResponse<AlumniAssociationApplicationDetailVo> getApplicationDetail(
            @PathVariable("applicationId") Long applicationId) {

        AlumniAssociationApplicationDetailVo detailVo = alumniAssociationApplicationService
                .getApplicationDetailById(applicationId);

        return ResultUtils.success(Code.SUCCESS, detailVo, "查询成功");
    }
}
