package com.cmswe.alumni.web;

import com.cmswe.alumni.api.search.AlumniPlaceApplicationService;
import com.cmswe.alumni.api.search.AlumniPlaceService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ApproveAlumniPlaceApplicationDto;
import com.cmswe.alumni.common.dto.QueryAlumniPlaceApplicationDto;
import com.cmswe.alumni.common.dto.UpdateAlumniPlaceDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniPlaceApplicationVo;
import com.cmswe.alumni.common.vo.AlumniPlaceListVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 校友企业/场所管理 Controller
 *
 * @author CNI Alumni System
 */
@Tag(name = "校友企业/场所管理")
@Slf4j
@RestController
@RequestMapping("/alumni-place/management")
public class AlumniPlaceManagementController {

    @Resource
    private AlumniPlaceApplicationService alumniPlaceApplicationService;

    @Resource
    private AlumniPlaceService alumniPlaceService;

    @PostMapping("/application/page")
    @Operation(summary = "管理员分页查询企业/场所申请列表")
    public BaseResponse<PageVo<AlumniPlaceApplicationVo>> getApplicationPage(
            @Valid @RequestBody QueryAlumniPlaceApplicationDto queryDto) {
        PageVo<AlumniPlaceApplicationVo> pageVo = alumniPlaceApplicationService.getApplicationPage(queryDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    @PostMapping("/application/approve")
    @Operation(summary = "管理员审核企业/场所申请")
    public BaseResponse<Boolean> approveApplication(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ApproveAlumniPlaceApplicationDto approveDto) {
        Long reviewUserId = securityUser.getWxUser().getWxId();
        boolean result = alumniPlaceApplicationService.approveApplication(reviewUserId, approveDto);
        String message = approveDto.getApplicationStatus() == 1 ? "审核通过" : "审核拒绝";
        return ResultUtils.success(Code.SUCCESS, result, message);
    }

    @GetMapping("/{id}")
    @Operation(summary = "管理员根据ID查询企业/场所详情")
    public BaseResponse<AlumniPlaceListVo> getPlaceDetail(@PathVariable Long id) {
        AlumniPlaceListVo vo = alumniPlaceService.getPlaceDetail(id);
        return ResultUtils.success(Code.SUCCESS, vo, "查询成功");
    }

    @PostMapping("/update")
    @Operation(summary = "管理员更新企业/场所基本信息")
    public BaseResponse<Boolean> updatePlaceInfo(@Valid @RequestBody UpdateAlumniPlaceDto updateDto) {
        boolean result = alumniPlaceService.updatePlaceInfo(updateDto);
        return ResultUtils.success(Code.SUCCESS, result, "更新成功");
    }
}
