package com.cmswe.alumni.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.common.dto.QueryAlumniListDto;
import com.cmswe.alumni.common.dto.UpdateUserInfoDto;
import com.cmswe.alumni.common.dto.UpdateUserTagsDto;
import com.cmswe.alumni.common.entity.SysTag;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.common.dto.UpdateUserPrivacySettingsRequest;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;
import com.cmswe.alumni.common.vo.AlumniDetailVo;
import com.cmswe.alumni.common.vo.TagVo;
import com.cmswe.alumni.common.vo.UserDetailVo;
import com.cmswe.alumni.common.vo.UserListResponse;
import com.cmswe.alumni.common.vo.UserPrivacySettingListVo;
import com.cmswe.alumni.service.system.service.SysTagService;
import com.cmswe.alumni.web.aop.PrivacyFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户服务")
@RestController
@RequestMapping("/users")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private SysTagService sysTagService;

    @Resource
    private AlumniAssociationService alumniAssociationService;

    @GetMapping("/getInfo")
    @Operation(summary = "获取个人信息")
    public BaseResponse<UserDetailVo> getUserByToken(@AuthenticationPrincipal SecurityUser securityUser) throws JsonProcessingException {
        UserDetailVo userDetailVo = userService.getUserById(securityUser.getWxUser().getWxId());
        return ResultUtils.success(Code.SUCCESS, userDetailVo, "查询个人信息成功");
    }

    @PutMapping("/update")
    @Operation(summary = "修改个人信息")
    public BaseResponse<?> updateUserInfo(@AuthenticationPrincipal SecurityUser securityUser, @RequestBody UpdateUserInfoDto updateDto) throws JsonProcessingException {
        boolean result = userService.updateUserInfo(securityUser.getWxUser().getWxId(), updateDto);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "修改个人信息成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "修改个人信息失败");
    }

    @GetMapping("/getPrivacy")
    @Operation(summary = "获取个人隐私设置")
    public BaseResponse<List<UserPrivacySettingListVo>> getUserPrivacy(@AuthenticationPrincipal SecurityUser securityUser) throws JsonProcessingException {
        List<UserPrivacySettingListVo> userDetailVo = userService.getUserPrivacy(securityUser.getWxUser().getWxId());
        return ResultUtils.success(Code.SUCCESS, userDetailVo, "查询个人隐私成功");
    }

    @PutMapping("/update/privacy")
    @Operation(summary = "更新个人隐私设置")
    public BaseResponse<Boolean> getUserPrivacy(@AuthenticationPrincipal SecurityUser securityUser, @RequestBody UpdateUserPrivacySettingsRequest updateUserPrivacySettingsRequest) {
        boolean updateResult = userService.updateUserPrivacy(securityUser.getWxUser().getWxId(), updateUserPrivacySettingsRequest);
        return ResultUtils.success(Code.SUCCESS, updateResult, "更新个人隐私成功");
    }

    @PrivacyFilter
    @GetMapping("/getAlumniInfo/{id}")
    @Operation(summary = "获取校友信息（根据隐私设置，包含关注状态和校友认证状态）")
    public BaseResponse<AlumniDetailVo> getAlumniInfo(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        // 获取当前登录用户ID（如果未登录则为null）
        Long currentUserId = securityUser != null ? securityUser.getWxUser().getWxId() : null;
        AlumniDetailVo alumniDetailVo = userService.getAlumniInfoWithStatus(id, currentUserId);
        return ResultUtils.success(Code.SUCCESS, alumniDetailVo, "查询校友信息成功");
    }

    @PrivacyFilter
    @PostMapping("/query/alumni")
    @Operation(summary = "查询校友列表")
    public BaseResponse<Page<UserListResponse>> getAlumniList(@RequestBody QueryAlumniListDto queryAlumniListDto) {
        Page<UserListResponse> userListResponsePage = userService.queryAlumniList(queryAlumniListDto);
        return ResultUtils.success(Code.SUCCESS, userListResponsePage);
    }

    @PutMapping("/update/tags")
    @Operation(summary = "更新个人标签")
    public BaseResponse<Boolean> updateUserTags(@AuthenticationPrincipal SecurityUser securityUser, @RequestBody UpdateUserTagsDto updateUserTagsDto) {
        boolean updateResult = userService.updateUserTags(securityUser.getWxUser().getWxId(), updateUserTagsDto);
        return ResultUtils.success(Code.SUCCESS, updateResult, "更新个人标签成功");
    }

    @GetMapping("/tags")
    @Operation(summary = "获取标签列表（供用户选择）")
    public BaseResponse<List<TagVo>> getAvailableTags(@RequestParam(required = false) Integer category) {
        List<SysTag> tags;
        if (category != null) {
            // 按分类查询标签
            tags = sysTagService.getTagsByCategory(category);
        } else {
            // 查询所有标签
            tags = sysTagService.list();
        }

        // 转换为 VO
        List<TagVo> tagVoList = tags.stream()
                .map(TagVo::objToVo)
                .toList();

        return ResultUtils.success(Code.SUCCESS, tagVoList, "获取标签列表成功");
    }

    @GetMapping("/my-associations")
    @Operation(summary = "查询本人加入的校友会列表")
    public BaseResponse<List<AlumniAssociationListVo>> getMyJoinedAssociations(
            @AuthenticationPrincipal SecurityUser securityUser) {
        Long wxId = securityUser.getWxUser().getWxId();
        List<AlumniAssociationListVo> associations = alumniAssociationService.getMyJoinedAssociations(wxId);
        return ResultUtils.success(Code.SUCCESS, associations, "查询成功");
    }
}



