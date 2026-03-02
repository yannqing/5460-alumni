package com.cmswe.alumni.web;

import com.cmswe.alumni.api.association.AlumniHeadquartersService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ApplyActivateHeadquartersRequest;
import com.cmswe.alumni.common.dto.AuditHeadquartersRequest;
import com.cmswe.alumni.common.dto.QueryAlumniHeadquartersListDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniHeadquartersDetailVo;
import com.cmswe.alumni.common.vo.AlumniHeadquartersListVo;
import com.cmswe.alumni.common.vo.InactiveAlumniHeadquartersVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "校友总会")
@RestController
@RequestMapping("/AlumniHeadquarters")
public class AlumniHeadquartersController {

    @Resource
    private AlumniHeadquartersService alumniHeadquartersService;

    @PostMapping("/page")
    @Operation(summary = "分页查询校友总会列表")
    public BaseResponse<PageVo<AlumniHeadquartersListVo>> selectPage(
            @RequestBody QueryAlumniHeadquartersListDto alumniHeadquartersListDto) {
        PageVo<AlumniHeadquartersListVo> pageVo = alumniHeadquartersService.selectByPage(alumniHeadquartersListDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "分页查询成功");
    }

    @PostMapping("/pending/page")
    @Operation(summary = "分页查询待审核校友总会列表")
    public BaseResponse<PageVo<AlumniHeadquartersListVo>> selectPendingPage(
            @RequestBody QueryAlumniHeadquartersListDto alumniHeadquartersListDto) {
        PageVo<AlumniHeadquartersListVo> pageVo = alumniHeadquartersService
                .selectPendingByPage(alumniHeadquartersListDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    @PostMapping("/applyActivate")
    @Operation(summary = "申请激活校友总会（输入邀请码）")
    public BaseResponse<Boolean> applyActivateHeadquarters(
            @RequestBody ApplyActivateHeadquartersRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        Long wxId = securityUser != null && securityUser.getWxUser() != null
                ? securityUser.getWxUser().getWxId()
                : null;
        boolean result = alumniHeadquartersService.applyActivateHeadquarters(request, wxId);
        return ResultUtils.success(Code.SUCCESS, result, "激活成功");
    }

    @PostMapping("/inactive/page")
    @Operation(summary = "分页查询未激活校友总会列表（仅ID和名称）")
    public BaseResponse<PageVo<InactiveAlumniHeadquartersVo>> selectInactivePage(
            @RequestBody QueryAlumniHeadquartersListDto pageRequest) {
        PageVo<InactiveAlumniHeadquartersVo> pageVo = alumniHeadquartersService.selectInactiveByPage(pageRequest);
        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    @PostMapping("/audit")
    @Operation(summary = "审核校友总会申请")
    public BaseResponse<Boolean> auditHeadquarters(
            @RequestBody AuditHeadquartersRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        Long wxId = securityUser != null && securityUser.getWxUser() != null
                ? securityUser.getWxUser().getWxId()
                : null;
        boolean result = alumniHeadquartersService.auditHeadquarters(request, wxId);
        return ResultUtils.success(Code.SUCCESS, result, "审核操作成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询校友总会详情")
    public BaseResponse<AlumniHeadquartersDetailVo> getAlumniHeadquartersDetailById(@PathVariable Long id) {
        AlumniHeadquartersDetailVo detailVo = alumniHeadquartersService.getAlumniHeadquartersDetailById(id);
        return ResultUtils.success(Code.SUCCESS, detailVo, "查询成功");
    }
}
