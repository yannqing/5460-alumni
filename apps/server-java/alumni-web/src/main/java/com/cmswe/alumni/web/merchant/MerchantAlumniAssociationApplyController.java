package com.cmswe.alumni.web.merchant;

import com.cmswe.alumni.api.system.MerchantAlumniAssociationApplyService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.dto.ApplyMerchantAssociationJoinDto;
import com.cmswe.alumni.common.dto.QueryMerchantAssociationJoinApplyDto;
import com.cmswe.alumni.common.dto.ReviewMerchantAssociationJoinApplyDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.MerchantAssociationJoinApplyVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 商户加入校友会申请控制器
 */
@Tag(name = "商户加入校友会申请管理")
@RestController
@RequestMapping("/merchant/association-apply")
public class MerchantAlumniAssociationApplyController {

    @Resource
    private MerchantAlumniAssociationApplyService merchantAlumniAssociationApplyService;

    @Operation(summary = "提交加入校友会申请")
    @PostMapping("/submit")
    public BaseResponse<Boolean> submitApply(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody ApplyMerchantAssociationJoinDto applyDto) {
        
        boolean result = merchantAlumniAssociationApplyService.applyJoinAssociation(user.getWxUser().getWxId(), applyDto);
        return ResultUtils.success(result);
    }

    @Operation(summary = "校友会管理员查询商户加入申请列表")
    @PostMapping("/list")
    public BaseResponse<PageVo<MerchantAssociationJoinApplyVo>> listApply(
            @Valid @RequestBody QueryMerchantAssociationJoinApplyDto queryDto) {
        
        PageVo<MerchantAssociationJoinApplyVo> result = merchantAlumniAssociationApplyService.queryJoinApplyPage(queryDto);
        return ResultUtils.success(result);
    }

    @Operation(summary = "审核商户加入校友会申请")
    @PostMapping("/review")
    public BaseResponse<Boolean> reviewApply(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody ReviewMerchantAssociationJoinApplyDto reviewDto) {
        
        boolean result = merchantAlumniAssociationApplyService.reviewJoinApply(user.getWxUser().getWxId(), reviewDto);
        return ResultUtils.success(result);
    }

    @Operation(summary = "获取商户加入校友会申请详情")
    @GetMapping("/detail")
    public BaseResponse<MerchantAssociationJoinApplyVo> getApplyDetail(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long merchantId) {
        if (id != null) {
            MerchantAssociationJoinApplyVo vo = merchantAlumniAssociationApplyService.getJoinApplyDetail(id);
            return ResultUtils.success(vo);
        } else if (merchantId != null) {
            MerchantAssociationJoinApplyVo vo = merchantAlumniAssociationApplyService.getLatestJoinApplyDetailByMerchantId(merchantId);
            return ResultUtils.success(vo);
        }
        return ResultUtils.failure("参数不能为空");
    }
}
