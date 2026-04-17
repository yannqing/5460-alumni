package com.cmswe.alumni.web;

import com.cmswe.alumni.api.association.MyApplicationRecordService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.CancelMyApplicationRecordDto;
import com.cmswe.alumni.common.dto.QueryMyApplicationRecordDetailDto;
import com.cmswe.alumni.common.dto.QueryMyApplicationRecordListDto;
import com.cmswe.alumni.common.dto.UpdateMyApplicationRecordDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.MyApplicationRecordDetailVo;
import com.cmswe.alumni.common.vo.MyApplicationRecordListVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户「我的申请」聚合列表
 */
@Tag(name = "我的申请")
@RestController
@RequestMapping("/users/my-application-records")
public class MyApplicationRecordController {

    @Resource
    private MyApplicationRecordService myApplicationRecordService;

    @PostMapping("/page")
    @Operation(summary = "分页查询我的申请记录（校友会相关 / 商户入驻 / 门店申请）")
    public BaseResponse<PageVo<MyApplicationRecordListVo>> queryMyApplicationRecordPage(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody QueryMyApplicationRecordListDto queryDto) {
        if (securityUser == null || securityUser.getWxUser() == null) {
            return ResultUtils.failure(Code.TOKEN_ERROR, null, "用户未登录");
        }
        Long wxId = securityUser.getWxUser().getWxId();
        PageVo<MyApplicationRecordListVo> pageVo = myApplicationRecordService.queryMyApplicationRecordPage(wxId, queryDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    @PostMapping("/detail")
    @Operation(summary = "查询我的申请详情（按 recordType + recordId）")
    public BaseResponse<MyApplicationRecordDetailVo> queryMyApplicationRecordDetail(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody QueryMyApplicationRecordDetailDto queryDto) {
        if (securityUser == null || securityUser.getWxUser() == null) {
            return ResultUtils.failure(Code.TOKEN_ERROR, null, "用户未登录");
        }
        Long wxId = securityUser.getWxUser().getWxId();
        MyApplicationRecordDetailVo detailVo = myApplicationRecordService.queryMyApplicationRecordDetail(wxId, queryDto);
        return ResultUtils.success(Code.SUCCESS, detailVo, "查询成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新我的申请记录（按 recordType + recordId）")
    public BaseResponse<Boolean> updateMyApplicationRecord(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody UpdateMyApplicationRecordDto updateDto) {
        if (securityUser == null || securityUser.getWxUser() == null) {
            return ResultUtils.failure(Code.TOKEN_ERROR, null, "用户未登录");
        }
        Long wxId = securityUser.getWxUser().getWxId();
        boolean updated = myApplicationRecordService.updateMyApplicationRecord(wxId, updateDto);
        if (updated) {
            return ResultUtils.success(Code.SUCCESS, true, "更新成功");
        }
        return ResultUtils.failure(Code.FAILURE, false, "更新失败");
    }

    @PutMapping("/cancel")
    @Operation(summary = "撤销我的申请记录（按 recordType + recordId）")
    public BaseResponse<Boolean> cancelMyApplicationRecord(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody CancelMyApplicationRecordDto cancelDto) {
        if (securityUser == null || securityUser.getWxUser() == null) {
            return ResultUtils.failure(Code.TOKEN_ERROR, null, "用户未登录");
        }
        Long wxId = securityUser.getWxUser().getWxId();
        boolean cancelled = myApplicationRecordService.cancelMyApplicationRecord(wxId, cancelDto);
        if (cancelled) {
            return ResultUtils.success(Code.SUCCESS, true, "撤销成功");
        }
        return ResultUtils.failure(Code.FAILURE, false, "撤销失败");
    }
}
