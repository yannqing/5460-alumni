package com.cmswe.alumni.web.LocalPlatform;

import com.cmswe.alumni.api.association.AlumniAssociationApplicationService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.*;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniAssociationApplicationListVo;
import com.cmswe.alumni.common.vo.OrganizeArchiRoleVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.UserListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校处会管理控制器
 */
@Tag(name = "校处会管理")
@Slf4j
@RestController
@RequestMapping("/localPlatformManagement")
public class LocalPlatformManagementController {

        @Resource
        private AlumniAssociationApplicationService alumniAssociationApplicationService;

        @Resource
        private OrganizeArchiRoleService organizeArchiRoleService;

        @Resource
        private LocalPlatformService localPlatformService;

        /**
         * 分页查询校友会创建申请列表
         *
         * @param queryDto 查询条件（包含校处会ID、审核状态等）
         * @return 校友会创建申请列表
         */
        @Deprecated
        @PostMapping("/queryAssociationApplicationPage")
        @Operation(summary = "分页查询校友会创建申请列表")
        public BaseResponse<PageVo<AlumniAssociationApplicationListVo>> queryAssociationApplicationPage(
                        @Valid @RequestBody QueryAlumniAssociationApplicationListDto queryDto) {

                PageVo<AlumniAssociationApplicationListVo> pageVo = alumniAssociationApplicationService
                                .queryApplicationPage(queryDto);

                return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
        }

        /**
         * 审核校友会创建申请
         *
         * @param securityUser 当前登录用户（审核人）
         * @param reviewDto    审核信息
         * @return 审核结果
         */
        @Deprecated
        @PostMapping("/reviewAssociationApplication")
        @Operation(summary = "审核校友会创建申请")
        public BaseResponse<Boolean> reviewAssociationApplication(
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
         * 新增组织架构角色
         *
         * @param addDto 新增请求参数
         * @return 返回新增结果
         */
        @PostMapping("/role/add")
        @Operation(summary = "新增校处会组织架构角色")
        public BaseResponse<Boolean> addOrganizeArchiRole(@Valid @RequestBody AddOrganizeArchiRoleDto addDto) {
                log.info("新增校处会组织架构角色，组织 ID: {}, 角色名: {}", addDto.getOrganizeId(), addDto.getRoleOrName());

                // 后端指定组织类型为校处会（1）
                addDto.setOrganizeType(1);

                boolean result = organizeArchiRoleService.addOrganizeArchiRole(addDto);

                log.info("新增校处会组织架构角色成功，组织 ID: {}, 角色名: {}", addDto.getOrganizeId(), addDto.getRoleOrName());
                return ResultUtils.success(Code.SUCCESS, result, "新增成功");
        }

        /**
         * 更新组织架构角色
         *
         * @param updateDto 更新请求参数
         * @return 返回更新结果
         */
        @PutMapping("/role/update")
        @Operation(summary = "更新校处会组织架构角色")
        public BaseResponse<Boolean> updateOrganizeArchiRole(@Valid @RequestBody UpdateOrganizeArchiRoleDto updateDto) {
                log.info("更新校处会组织架构角色，角色 ID: {}, 组织 ID: {}", updateDto.getRoleOrId(), updateDto.getOrganizeId());

                boolean result = organizeArchiRoleService.updateOrganizeArchiRole(updateDto);

                log.info("更新校处会组织架构角色成功，角色 ID: {}, 组织 ID: {}", updateDto.getRoleOrId(), updateDto.getOrganizeId());
                return ResultUtils.success(Code.SUCCESS, result, "更新成功");
        }

        /**
         * 删除组织架构角色
         *
         * @param deleteDto 删除请求参数
         * @return 返回删除结果
         */
        @DeleteMapping("/role/delete")
        @Operation(summary = "删除校处会组织架构角色")
        public BaseResponse<Boolean> deleteOrganizeArchiRole(@Valid @RequestBody DeleteOrganizeArchiRoleDto deleteDto) {
                log.info("删除校处会组织架构角色，角色 ID: {}, 组织 ID: {}", deleteDto.getRoleOrId(), deleteDto.getOrganizeId());

                boolean result = organizeArchiRoleService.deleteOrganizeArchiRole(
                                deleteDto.getRoleOrId(),
                                deleteDto.getOrganizeId());

                log.info("删除校处会组织架构角色成功，角色 ID: {}, 组织 ID: {}", deleteDto.getRoleOrId(), deleteDto.getOrganizeId());
                return ResultUtils.success(Code.SUCCESS, result, "删除成功");
        }

        /**
         * 查询组织架构角色列表（树形结构）
         *
         * @param queryDto 查询请求参数
         * @return 返回角色树形列表
         */
        @PostMapping("/role/list")
        @Operation(summary = "查询校处会组织架构角色列表（树形结构）")
        public BaseResponse<List<OrganizeArchiRoleVo>> getOrganizeArchiRoleList(
                        @Valid @RequestBody QueryOrganizeArchiRoleListDto queryDto) {
                log.info("查询校处会组织架构角色树，组织 ID: {}", queryDto.getOrganizeId());

                // 后端指定组织类型为校处会（1）
                queryDto.setOrganizeType(1);

                List<OrganizeArchiRoleVo> roleTree = organizeArchiRoleService.getOrganizeArchiRoleTree(
                                queryDto.getOrganizeId(),
                                queryDto.getOrganizeType(),
                                queryDto.getRoleOrName(),
                                queryDto.getStatus());

                log.info("查询校处会组织架构角色树成功，组织 ID: {}, 根节点数: {}", queryDto.getOrganizeId(), roleTree.size());
                return ResultUtils.success(Code.SUCCESS, roleTree, "查询成功");
        }

        /**
         * 删除校处会成员
         *
         * @param deleteDto 删除请求参数
         * @return 返回删除结果
         */
        @DeleteMapping("/deleteMember")
        @Operation(summary = "删除校处会成员")
        public BaseResponse<Boolean> deleteMember(@Valid @RequestBody DeleteLocalPlatformMemberDto deleteDto) {
                log.info("删除校处会成员，校处会 ID: {}, 成员用户 ID: {}",
                                deleteDto.getLocalPlatformId(), deleteDto.getWxId());

                boolean result = localPlatformService.deleteMember(
                                deleteDto.getLocalPlatformId(),
                                deleteDto.getWxId());

                if (result) {
                        log.info("删除校处会成员成功，校处会 ID: {}, 成员用户 ID: {}",
                                        deleteDto.getLocalPlatformId(), deleteDto.getWxId());
                        return ResultUtils.success(Code.SUCCESS, true, "删除成功");
                } else {
                        log.error("删除校处会成员失败，校处会 ID: {}, 成员用户 ID: {}",
                                        deleteDto.getLocalPlatformId(), deleteDto.getWxId());
                        return ResultUtils.failure(Code.FAILURE, false, "删除失败");
                }
        }

        /**
         * 邀请成员加入校处会
         *
         * @param inviteDto 邀请请求参数
         * @return 返回邀请结果
         */
        @PostMapping("/inviteMember")
        @Operation(summary = "邀请成员加入校处会")
        public BaseResponse<Boolean> inviteMember(@Valid @RequestBody InviteLocalPlatformMemberDto inviteDto) {
                log.info("邀请成员加入校处会，校处会 ID: {}, 成员用户 ID: {}, 角色 ID: {}",
                                inviteDto.getLocalPlatformId(), inviteDto.getWxId(), inviteDto.getRoleOrId());

                boolean result = localPlatformService.inviteMember(
                                inviteDto.getLocalPlatformId(),
                                inviteDto.getWxId(),
                                inviteDto.getRoleOrId(),
                                inviteDto.getUsername(),
                                inviteDto.getRoleName(),
                                inviteDto.getContactInformation(),
                                inviteDto.getSocialDuties());

                if (result) {
                        log.info("邀请成员加入校处会成功，校处会 ID: {}, 成员用户 ID: {}, 角色 ID: {}",
                                        inviteDto.getLocalPlatformId(), inviteDto.getWxId(), inviteDto.getRoleOrId());
                        return ResultUtils.success(Code.SUCCESS, true, "邀请成功");
                } else {
                        log.error("邀请成员加入校处会失败，校处会 ID: {}, 成员用户 ID: {}, 角色 ID: {}",
                                        inviteDto.getLocalPlatformId(), inviteDto.getWxId(), inviteDto.getRoleOrId());
                        return ResultUtils.failure(Code.FAILURE, false, "邀请失败");
                }
        }

        /**
         * 更新校处会成员的组织架构角色
         *
         * @param securityUser 当前登录用户（操作人）
         * @param updateDto    更新请求参数
         * @return 返回更新结果
         */
        @PutMapping("/updateMemberRole")
        @Operation(summary = "更新校处会成员的组织架构角色")
        public BaseResponse<Boolean> updateMemberRole(
                        @AuthenticationPrincipal SecurityUser securityUser,
                        @Valid @RequestBody UpdateLocalPlatformMemberRoleDto updateDto) {

                Long operatorWxId = securityUser.getWxUser().getWxId();

                log.info("更新校处会成员角色，操作人 ID: {}, 校处会 ID: {}, 成员用户 ID: {}, 新角色 ID: {}",
                                operatorWxId, updateDto.getLocalPlatformId(), updateDto.getWxId(),
                                updateDto.getRoleOrId());

                boolean result = localPlatformService.updateMemberRole(
                                operatorWxId,
                                updateDto.getLocalPlatformId(),
                                updateDto.getWxId(),
                                updateDto.getRoleOrId());

                if (result) {
                        log.info("更新校处会成员角色成功，校处会 ID: {}, 成员用户 ID: {}, 新角色 ID: {}",
                                        updateDto.getLocalPlatformId(), updateDto.getWxId(), updateDto.getRoleOrId());
                        return ResultUtils.success(Code.SUCCESS, true, "更新成功");
                } else {
                        log.error("更新校处会成员角色失败，校处会 ID: {}, 成员用户 ID: {}, 新角色 ID: {}",
                                        updateDto.getLocalPlatformId(), updateDto.getWxId(), updateDto.getRoleOrId());
                        return ResultUtils.failure(Code.FAILURE, false, "更新失败");
                }
        }

        /**
         * 更新校处会成员的组织架构角色 V2版本（基于username，支持wxId为空）
         *
         * @param securityUser 当前登录用户（操作人）
         * @param updateDto    更新请求参数
         * @return 返回更新结果
         */
        @PutMapping("/updateMemberRole/v2")
        @Operation(summary = "更新校处会成员的组织架构角色V2（基于username）")
        public BaseResponse<Boolean> updateMemberRoleV2(
                        @AuthenticationPrincipal SecurityUser securityUser,
                        @Valid @RequestBody UpdateLocalPlatformMemberRoleV2Dto updateDto) {

                Long operatorWxId = securityUser.getWxUser().getWxId();

                log.info("更新校处会成员角色V2，操作人 ID: {}, 校处会 ID: {}, 成员用户名: {}, 新角色 ID: {}",
                                operatorWxId, updateDto.getLocalPlatformId(), updateDto.getUsername(),
                                updateDto.getRoleOrId());

                boolean result = localPlatformService.updateMemberRoleV2(
                                operatorWxId,
                                updateDto.getLocalPlatformId(),
                                updateDto.getId(),
                                updateDto.getUsername(),
                                updateDto.getRoleOrId(),
                                updateDto.getRoleName());

                if (result) {
                        log.info("更新校处会成员角色V2成功，校处会 ID: {}, 成员用户名: {}, 新角色 ID: {}",
                                        updateDto.getLocalPlatformId(), updateDto.getUsername(),
                                        updateDto.getRoleOrId());
                        return ResultUtils.success(Code.SUCCESS, true, "更新成功");
                } else {
                        log.error("更新校处会成员角色V2失败，校处会 ID: {}, 成员用户名: {}, 新角色 ID: {}",
                                        updateDto.getLocalPlatformId(), updateDto.getUsername(),
                                        updateDto.getRoleOrId());
                        return ResultUtils.failure(Code.FAILURE, false, "更新失败");
                }
        }

        /**
         * 根据校处会 id 查询校处会管理员列表
         *
         * @param localPlatformId 校处会ID
         * @return 返回管理员用户列表
         */
        @GetMapping("/admins/{localPlatformId}")
        @Operation(summary = "根据校处会 id 查询校处会管理员列表")
        public BaseResponse<List<UserListResponse>> getAdminsByLocalPlatformId(@PathVariable Long localPlatformId) {
                log.info("查询校处会管理员列表，校处会 ID: {}", localPlatformId);

                List<UserListResponse> adminList = localPlatformService.getAdminsByLocalPlatformId(localPlatformId);

                log.info("查询校处会管理员列表成功，校处会 ID: {}, 管理员数量: {}", localPlatformId, adminList.size());
                return ResultUtils.success(Code.SUCCESS, adminList, "查询成功");
        }

        /**
         * 为校处会添加管理员
         *
         * @param addDto 添加管理员请求参数
         * @return 返回添加结果
         */
        @PostMapping("/admin/add")
        @Operation(summary = "为校处会添加管理员")
        public BaseResponse<Boolean> addAdminToLocalPlatform(@Valid @RequestBody AddLocalPlatformAdminDto addDto) {
                log.info("添加校处会管理员，校处会 ID: {}, 用户 ID: {}",
                                addDto.getLocalPlatformId(), addDto.getWxId());

                boolean result = localPlatformService.addAdminToLocalPlatform(
                                addDto.getLocalPlatformId(),
                                addDto.getWxId());

                if (result) {
                        log.info("添加校处会管理员成功，校处会 ID: {}, 用户 ID: {}",
                                        addDto.getLocalPlatformId(), addDto.getWxId());
                        return ResultUtils.success(Code.SUCCESS, true, "添加成功");
                } else {
                        log.error("添加校处会管理员失败，校处会 ID: {}, 用户 ID: {}",
                                        addDto.getLocalPlatformId(), addDto.getWxId());
                        return ResultUtils.failure(Code.FAILURE, false, "添加失败");
                }
        }

        /**
         * 移除校处会管理员
         *
         * @param removeDto 移除管理员请求参数
         * @return 返回移除结果
         */
        @DeleteMapping("/admin/remove")
        @Operation(summary = "移除校处会管理员")
        public BaseResponse<Boolean> removeAdminFromLocalPlatform(
                        @Valid @RequestBody RemoveLocalPlatformAdminDto removeDto) {
                log.info("移除校处会管理员，校处会 ID: {}, 用户 ID: {}",
                                removeDto.getLocalPlatformId(), removeDto.getWxId());

                boolean result = localPlatformService.removeAdminFromLocalPlatform(
                                removeDto.getLocalPlatformId(),
                                removeDto.getWxId());

                if (result) {
                        log.info("移除校处会管理员成功，校处会 ID: {}, 用户 ID: {}",
                                        removeDto.getLocalPlatformId(), removeDto.getWxId());
                        return ResultUtils.success(Code.SUCCESS, true, "移除成功");
                } else {
                        log.error("移除校处会管理员失败，校处会 ID: {}, 用户 ID: {}",
                                        removeDto.getLocalPlatformId(), removeDto.getWxId());
                        return ResultUtils.failure(Code.FAILURE, false, "移除失败");
                }
        }

        /**
         * 绑定校处会组织架构成员与系统用户
         *
         * @param bindDto 绑定请求参数
         * @return 返回绑定结果
         */
        @PutMapping("/bindMemberToUser")
        @Operation(summary = "绑定校处会组织架构成员与系统用户")
        public BaseResponse<Boolean> bindMemberToUser(@Valid @RequestBody BindLocalPlatformMemberToUserDto bindDto) {
                log.info("绑定校处会成员与系统用户，成员 ID: {}, 用户 ID: {}",
                                bindDto.getMemberId(), bindDto.getWxId());

                boolean result = localPlatformService.bindMemberToUser(
                                bindDto.getMemberId(),
                                bindDto.getWxId());

                if (result) {
                        log.info("绑定校处会成员与系统用户成功，成员 ID: {}, 用户 ID: {}",
                                        bindDto.getMemberId(), bindDto.getWxId());
                        return ResultUtils.success(Code.SUCCESS, true, "绑定成功");
                } else {
                        log.error("绑定校处会成员与系统用户失败，成员 ID: {}, 用户 ID: {}",
                                        bindDto.getMemberId(), bindDto.getWxId());
                        return ResultUtils.failure(Code.FAILURE, false, "绑定失败");
                }
        }

        /**
         * 获取校促会隐私设置
         * 
         * @param platformId 校促会ID
         * @return 隐私设置列表
         */
        @GetMapping("/privacy/{platformId}")
        @Operation(summary = "获取校促会隐私设置")
        public BaseResponse<List<com.cmswe.alumni.common.entity.LocalPlatformPrivacySetting>> getPrivacySettings(
                        @PathVariable Long platformId) {
                log.info("获取校促会隐私设置，校促会 ID: {}", platformId);
                List<com.cmswe.alumni.common.entity.LocalPlatformPrivacySetting> settings = localPlatformService
                                .getPrivacySettings(platformId);
                return ResultUtils.success(Code.SUCCESS, settings, "查询成功");
        }

        /**
         * 更新校促会隐私设置
         * 
         * @param updateDto 更新请求参数
         * @return 是否更新成功
         */
        @PostMapping("/privacy/update")
        @Operation(summary = "更新校促会隐私设置")
        public BaseResponse<Boolean> updatePrivacySetting(@Valid @RequestBody UpdateLocalPlatformPrivacyDto updateDto) {
                log.info("更新校促会隐私设置，校促会 ID: {}, 字段: {}, 可见性: {}",
                                updateDto.getPlatformId(), updateDto.getFieldCode(), updateDto.getVisibility());
                boolean result = localPlatformService.updatePrivacySetting(updateDto.getPlatformId(),
                                updateDto.getFieldCode(), updateDto.getVisibility());
                return ResultUtils.success(Code.SUCCESS, result, "更新成功");
        }

        /**
         * 管理端获取校处会详情
         * 
         * @param platformId 校处会ID
         * @return 校处会管理端详情VO
         */
        @GetMapping("/detail/{platformId}")
        @Operation(summary = "管理端获取校处会详情")
        public BaseResponse<com.cmswe.alumni.common.vo.LocalPlatformAdminVo> getAdminLocalPlatformById(
                        @PathVariable Long platformId) {
                log.info("管理端获取校处会详情，校处会 ID: {}", platformId);
                com.cmswe.alumni.common.vo.LocalPlatformAdminVo detail = localPlatformService
                                .getAdminLocalPlatformById(platformId);
                return ResultUtils.success(Code.SUCCESS, detail, "查询成功");
        }

        /**
         * 管理端修改校处会信息
         * 
         * @param updateDto 修改载体
         * @return 是否成功
         */
        @PutMapping("/update")
        @Operation(summary = "管理端修改校处会信息")
        public BaseResponse<Boolean> updateLocalPlatform(
                        @Valid @RequestBody com.cmswe.alumni.common.dto.UpdateLocalPlatformDto updateDto) {
                log.info("管理端修改校处会信息，校处会 ID: {}", updateDto.getPlatformId());
                boolean result = localPlatformService.updateLocalPlatform(updateDto);
                return ResultUtils.success(Code.SUCCESS, result, "修改成功");
        }

        /**
         * 添加校促会预设成员（假人）
         * 
         * @param addDto 添加预设成员请求参数
         * @return 添加是否成功
         */
        @PostMapping("/addPresetMember")
        @Operation(summary = "添加校促会预设成员（假人）")
        public BaseResponse<Boolean> addPresetMember(@Valid @RequestBody AddLocalPlatformPresetMemberDto addDto) {
                log.info("添加校促会预设成员，校促会 ID: {}, 用户名: {}, 角色名称: {}, 角色 ID: {}",
                                addDto.getLocalPlatformId(), addDto.getUsername(), addDto.getRoleName(), addDto.getRoleOrId());

                boolean result = localPlatformService.addPresetMember(
                                addDto.getLocalPlatformId(),
                                addDto.getUsername(),
                                addDto.getRoleName(),
                                addDto.getRoleOrId(),
                                addDto.getContactInformation(),
                                addDto.getSocialDuties());

                if (result) {
                        log.info("添加校促会预设成员成功，校促会 ID: {}, 用户名: {}",
                                        addDto.getLocalPlatformId(), addDto.getUsername());
                        return ResultUtils.success(Code.SUCCESS, true, "添加成功");
                } else {
                        log.error("添加校促会预设成员失败，校促会 ID: {}, 用户名: {}",
                                        addDto.getLocalPlatformId(), addDto.getUsername());
                        return ResultUtils.failure(Code.FAILURE, false, "添加失败");
                }
        }

        /**
         * 更新校促会预设成员（关联用户ID）
         * 
         * @param updateDto 更新预设成员请求参数
         * @return 更新是否成功
         */
        @PutMapping("/updatePresetMember")
        @Operation(summary = "更新校促会预设成员（关联用户ID）")
        public BaseResponse<Boolean> updatePresetMember(@Valid @RequestBody UpdateLocalPlatformPresetMemberDto updateDto) {
                log.info("更新校促会预设成员，成员 ID: {}, 用户 ID: {}",
                                updateDto.getMemberId(), updateDto.getWxId());

                boolean result = localPlatformService.updatePresetMember(
                                updateDto.getMemberId(),
                                updateDto.getWxId());

                if (result) {
                        log.info("更新校促会预设成员成功，成员 ID: {}, 用户 ID: {}",
                                        updateDto.getMemberId(), updateDto.getWxId());
                        return ResultUtils.success(Code.SUCCESS, true, "更新成功");
                } else {
                        log.error("更新校促会预设成员失败，成员 ID: {}, 用户 ID: {}",
                                        updateDto.getMemberId(), updateDto.getWxId());
                        return ResultUtils.failure(Code.FAILURE, false, "更新失败");
                }
        }

        /**
         * 更新校促会预设成员信息
         * 
         * @param updateDto 更新预设成员信息请求参数
         * @return 更新是否成功
         */
        @PutMapping("/updatePresetMemberInfo")
        @Operation(summary = "更新校促会预设成员信息")
        public BaseResponse<Boolean> updatePresetMemberInfo(@Valid @RequestBody UpdateLocalPlatformPresetMemberInfoDto updateDto) {
                log.info("更新校促会预设成员信息，成员 ID: {}, 用户名: {}, 角色名称: {}, 联系方式: {}, 社会职务: {}",
                                updateDto.getMemberId(), updateDto.getUsername(), updateDto.getRoleName(), 
                                updateDto.getContactInformation(), updateDto.getSocialDuties());

                boolean result = localPlatformService.updatePresetMemberInfo(
                                updateDto.getMemberId(),
                                updateDto.getUsername(),
                                updateDto.getRoleName(),
                                updateDto.getContactInformation(),
                                updateDto.getSocialDuties());

                if (result) {
                        log.info("更新校促会预设成员信息成功，成员 ID: {}",
                                        updateDto.getMemberId());
                        return ResultUtils.success(Code.SUCCESS, true, "更新成功");
                } else {
                        log.error("更新校促会预设成员信息失败，成员 ID: {}",
                                        updateDto.getMemberId());
                        return ResultUtils.failure(Code.FAILURE, false, "更新失败");
                }
        }

        /**
         * 删除校促会预设成员
         * 
         * @param deleteDto 删除预设成员请求参数
         * @return 删除是否成功
         */
        @DeleteMapping("/deletePresetMember")
        @Operation(summary = "删除校促会预设成员")
        public BaseResponse<Boolean> deletePresetMember(@Valid @RequestBody DeleteLocalPlatformPresetMemberDto deleteDto) {
                log.info("删除校促会预设成员，成员 ID: {}", deleteDto.getMemberId());

                boolean result = localPlatformService.deletePresetMember(deleteDto.getMemberId());

                if (result) {
                        log.info("删除校促会预设成员成功，成员 ID: {}", deleteDto.getMemberId());
                        return ResultUtils.success(Code.SUCCESS, true, "删除成功");
                } else {
                        log.error("删除校促会预设成员失败，成员 ID: {}", deleteDto.getMemberId());
                        return ResultUtils.failure(Code.FAILURE, false, "删除失败");
                }
        }
}
