package com.cmswe.alumni.web;

import com.cmswe.alumni.api.search.AlumniPlaceApplicationService;
import com.cmswe.alumni.api.search.AlumniPlaceService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ApplyAlumniPlaceDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniPlaceListVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校友企业/场所 Controller
 *
 * @author CNI Alumni System
 */
@Tag(name = "校友企业/场所")
@Slf4j
@RestController
@RequestMapping("/alumni-place")
public class AlumniPlaceController {

    @Resource
    private AlumniPlaceApplicationService alumniPlaceApplicationService;

    @Resource
    private AlumniPlaceService alumniPlaceService;

    @PostMapping("/apply")
    @Operation(summary = "用户申请创建校友企业/场所")
    public BaseResponse<Boolean> applyAlumniPlace(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ApplyAlumniPlaceDto applyDto) {
        Long wxId = securityUser.getWxUser().getWxId();
        boolean result = alumniPlaceApplicationService.applyAlumniPlace(wxId, applyDto);
        return ResultUtils.success(Code.SUCCESS, result, "申请提交成功，等待审核");
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询企业/场所详情")
    public BaseResponse<AlumniPlaceListVo> getPlaceDetail(@PathVariable Long id) {
        AlumniPlaceListVo vo = alumniPlaceService.getPlaceDetail(id);
        return ResultUtils.success(Code.SUCCESS, vo, "查询成功");
    }

    @GetMapping("/my-list")
    @Operation(summary = "获取用户个人的企业/场所列表")
    public BaseResponse<List<AlumniPlaceListVo>> getMyPlaceList(
            @AuthenticationPrincipal SecurityUser securityUser) {
        Long wxId = securityUser.getWxUser().getWxId();
        List<AlumniPlaceListVo> placeList = alumniPlaceService.getMyPlaceList(wxId);
        return ResultUtils.success(Code.SUCCESS, placeList, "查询成功");
    }
}
