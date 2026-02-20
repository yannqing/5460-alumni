package com.cmswe.alumni.web.LocalPlatform;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationByPlatformDto;
import com.cmswe.alumni.common.dto.QueryLocalPlatformListDto;
import com.cmswe.alumni.common.dto.QueryLocalPlatformMemberListDto;
import com.cmswe.alumni.common.dto.QueryLocalPlatformTreeDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;
import com.cmswe.alumni.common.vo.LocalPlatformDetailVo;
import com.cmswe.alumni.common.vo.LocalPlatformListVo;
import com.cmswe.alumni.common.vo.OrganizationTreeVo;
import com.cmswe.alumni.common.vo.OrganizationTreeV2Vo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.OrganizationMemberResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "校处会")
@Slf4j
@RestController
@RequestMapping("/localPlatform")
public class LocalPlatformController {

    @Resource
    private LocalPlatformService localPlatformService;

    @PostMapping("/page")
    @Operation(summary = "分页查询校处会列表")
    public BaseResponse<PageVo<LocalPlatformListVo>> selectPage(@RequestBody QueryLocalPlatformListDto queryLocalPlatformListDto) {
        log.info("分页查询校处会列表，查询条件：{}", queryLocalPlatformListDto);
        PageVo<LocalPlatformListVo> pageVo = localPlatformService.selectByPage(queryLocalPlatformListDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "分页查询成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 id 查询校处会信息")
    public BaseResponse<LocalPlatformDetailVo> getSchoolById(@PathVariable Long id) {
        LocalPlatformDetailVo localPlatformDetailVo = localPlatformService.getLocalPlatformById(id);
        return ResultUtils.success(Code.SUCCESS, localPlatformDetailVo);
    }

    @PostMapping("/organizationTree")
    @Operation(summary = "获取校处会组织架构树")
    public BaseResponse<List<OrganizationTreeVo>> getOrganizationTree(
            @Valid @RequestBody QueryLocalPlatformTreeDto request) {
        log.info("查询校处会组织架构树，校处会 ID: {}", request.getLocalPlatformId());

        List<OrganizationTreeVo> organizationTree =
                localPlatformService.getOrganizationTree(request.getLocalPlatformId());

        log.info("查询校处会组织架构树成功，校处会 ID: {}, 根节点数: {}",
                request.getLocalPlatformId(), organizationTree.size());

        return ResultUtils.success(Code.SUCCESS, organizationTree, "查询成功");
    }

    @PostMapping("/organizationTree/v2")
    @Operation(summary = "获取校处会组织架构树V2（基于username，支持wxId为空）")
    public BaseResponse<List<OrganizationTreeV2Vo>> getOrganizationTreeV2(
            @Valid @RequestBody QueryLocalPlatformTreeDto request) {
        log.info("查询校处会组织架构树V2，校处会 ID: {}", request.getLocalPlatformId());

        List<OrganizationTreeV2Vo> organizationTree =
                localPlatformService.getOrganizationTreeV2(request.getLocalPlatformId());

        log.info("查询校处会组织架构树V2成功，校处会 ID: {}, 根节点数: {}",
                request.getLocalPlatformId(), organizationTree.size());

        return ResultUtils.success(Code.SUCCESS, organizationTree, "查询成功");
    }

    @PostMapping("/alumniAssociations/page")
    @Operation(summary = "根据校处会ID分页查询校友会列表")
    public BaseResponse<PageVo<AlumniAssociationListVo>> getAlumniAssociationsByPlatformId(
            @Valid @RequestBody QueryAlumniAssociationByPlatformDto queryDto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("根据校处会ID分页查询校友会列表，查询条件：{}", queryDto);

        // 获取当前用户ID（如果未登录则为null）
        Long currentUserId = securityUser != null && securityUser.getWxUser() != null
                ? securityUser.getWxUser().getWxId()
                : null;

        PageVo<AlumniAssociationListVo> pageVo = localPlatformService.getAlumniAssociationsByPlatformId(queryDto, currentUserId);
        return ResultUtils.success(Code.SUCCESS, pageVo, "分页查询成功");
    }

    @PostMapping("/members/page")
    @Operation(summary = "根据校处会ID分页查询成员列表")
    public BaseResponse<Page<OrganizationMemberResponse>> getLocalPlatformMemberPage(
            @Valid @RequestBody QueryLocalPlatformMemberListDto queryDto) {
        log.info("根据校处会ID分页查询成员列表，查询条件：{}", queryDto);
        Page<OrganizationMemberResponse> memberPage = localPlatformService.getLocalPlatformMemberPage(queryDto);
        return ResultUtils.success(Code.SUCCESS, memberPage, "查询成功");
    }
}