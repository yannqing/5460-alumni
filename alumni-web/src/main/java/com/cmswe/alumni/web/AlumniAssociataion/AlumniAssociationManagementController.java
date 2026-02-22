package com.cmswe.alumni.web.AlumniAssociataion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.*;
import com.cmswe.alumni.common.dto.AddAlumniAssociationAdminDto;
import com.cmswe.alumni.common.dto.BindMemberToUserDto;
import com.cmswe.alumni.common.dto.RemoveAlumniAssociationAdminDto;
import com.cmswe.alumni.common.entity.Activity;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniAssociationDetailVo;
import com.cmswe.alumni.common.vo.ActivityListVo;
import com.cmswe.alumni.common.vo.ActivityDetailVo;
import com.cmswe.alumni.common.vo.OrganizeArchiRoleVo;
import com.cmswe.alumni.common.vo.OrganizationMemberResponse;
import com.cmswe.alumni.common.vo.UserListResponse;
import com.cmswe.alumni.service.system.mapper.ActivityMapper;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 校友会管理控制器
 */
@Tag(name = "校友会管理")
@Slf4j
@RestController
@RequestMapping("/alumniAssociationManagement")
public class AlumniAssociationManagementController {

        @Resource
        private AlumniAssociationService alumniAssociationService;

        @Resource
        private OrganizeArchiRoleService organizeArchiRoleService;

        @Resource
        private ActivityMapper activityMapper;

        /**
         * 管理员根据校友会 id 获取校友会详情（不做权限校验）
         *
         * @param id 校友会 ID
         * @return 返回校友会详情
         */
        @GetMapping("/detail/{id}")
        @Operation(summary = "管理员根据校友会 id 获取校友会详情")
        public BaseResponse<AlumniAssociationDetailVo> getAlumniAssociationDetail(@PathVariable Long id) {
                log.info("管理员查询校友会详情，校友会 ID: {}", id);

                // 不需要传入用户ID，不做权限校验
                AlumniAssociationDetailVo detailVo = alumniAssociationService.getAlumniAssociationDetailVoById(id, null);

                log.info("管理员查询校友会详情成功，校友会 ID: {}", id);
                return ResultUtils.success(Code.SUCCESS, detailVo, "查询成功");
        }

        /**
         * 管理员根据 id 更新校友会信息
         *
         * @param updateDto 更新请求参数
         * @return 返回更新结果
         */
        @PutMapping("/update")
        @Operation(summary = "管理员根据 id 更新校友会信息")
        public BaseResponse<Boolean> updateAlumniAssociation(@Valid @RequestBody UpdateAlumniAssociationDto updateDto) {
                log.info("管理员更新校友会信息，校友会 ID: {}", updateDto.getAlumniAssociationId());

                boolean result = alumniAssociationService.updateAlumniAssociation(updateDto);

                if (result) {
                        log.info("管理员更新校友会信息成功，校友会 ID: {}", updateDto.getAlumniAssociationId());
                        return ResultUtils.success(Code.SUCCESS, true, "更新成功");
                } else {
                        log.error("管理员更新校友会信息失败，校友会 ID: {}", updateDto.getAlumniAssociationId());
                        return ResultUtils.failure(Code.FAILURE, false, "更新失败");
                }
        }

        /**
         * 根据校友会 id 获取活动列表
         *
         * @param id 校友会 ID
         * @return 返回活动列表
         */
        @GetMapping("/activities/{id}")
        @Operation(summary = "根据校友会 id 获取活动列表")
        public BaseResponse<List<ActivityListVo>> getActivitiesByAssociationId(@PathVariable Long id) {
                log.info("查询校友会的活动列表，校友会 ID: {}", id);

                List<ActivityListVo> activityList = alumniAssociationService.getActivitiesByAssociationId(id);

                log.info("查询校友会的活动列表成功，校友会 ID: {}, 活动数量: {}", id, activityList.size());
                return ResultUtils.success(Code.SUCCESS, activityList, "查询成功");
        }

        /**
         * 管理员根据活动 id 查看活动详情
         *
         * @param activityId 活动 ID
         * @return 返回活动详情
         */
        @GetMapping("/activity/detail/{activityId}")
        @Operation(summary = "管理员根据活动 id 查看活动详情")
        public BaseResponse<ActivityDetailVo> getActivityDetail(@PathVariable Long activityId) {
                log.info("管理员查询活动详情，活动 ID: {}", activityId);

                ActivityDetailVo activityDetailVo = alumniAssociationService.getActivityDetail(activityId);

                log.info("管理员查询活动详情成功，活动 ID: {}", activityId);
                return ResultUtils.success(Code.SUCCESS, activityDetailVo, "查询成功");
        }

        /**
         * 管理员根据校友会 id 发布活动
         *
         * @param securityUser 当前登录用户
         * @param publishDto 发布活动请求参数
         * @return 返回发布结果
         */
        @PostMapping("/activity/publish")
        @Operation(summary = "管理员根据校友会 id 发布活动")
        public BaseResponse<Boolean> publishActivity(
                @AuthenticationPrincipal SecurityUser securityUser,
                @Valid @RequestBody PublishActivityDto publishDto) {
                Long createdBy = securityUser.getWxUser().getWxId();
                log.info("管理员为校友会发布活动，创建人 ID: {}, 校友会 ID: {}, 活动标题: {}",
                        createdBy, publishDto.getAlumniAssociationId(), publishDto.getActivityTitle());

                boolean result = alumniAssociationService.publishActivity(createdBy, publishDto);

                log.info("管理员为校友会发布活动成功，创建人 ID: {}, 校友会 ID: {}, 活动标题: {}",
                        createdBy, publishDto.getAlumniAssociationId(), publishDto.getActivityTitle());
                return ResultUtils.success(Code.SUCCESS, result, "发布成功");
        }

        /**
         * 管理员根据 id 编辑活动
         *
         * @param updateDto 编辑活动请求参数
         * @return 返回编辑结果
         */
        @PutMapping("/activity/update")
        @Operation(summary = "管理员根据 id 编辑活动")
        public BaseResponse<Boolean> updateActivity(@Valid @RequestBody UpdateActivityDto updateDto) {
                log.info("管理员编辑活动，活动 ID: {}", updateDto.getActivityId());

                boolean result = alumniAssociationService.updateActivity(updateDto);

                log.info("管理员编辑活动成功，活动 ID: {}, 活动标题: {}",
                        updateDto.getActivityId(), updateDto.getActivityTitle());
                return ResultUtils.success(Code.SUCCESS, result, "编辑成功");
        }

        /**
         * 管理员根据 id 删除活动
         *
         * @param activityId 活动ID
         * @return 返回删除结果
         */
        @DeleteMapping("/activity/delete/{activityId}")
        @Operation(summary = "管理员根据 id 删除活动")
        @Transactional(rollbackFor = Exception.class)
        public BaseResponse<Boolean> deleteActivity(@PathVariable Long activityId) {
                log.info("管理员删除活动，活动 ID: {}", activityId);

                // 1. 参数校验
                if (activityId == null) {
                        throw new BusinessException(ErrorType.ARGS_NOT_NULL, "活动ID不能为空");
                }

                // 2. 查询活动是否存在
                Activity activity = activityMapper.selectById(activityId);
                if (activity == null) {
                        log.error("活动不存在，活动 ID: {}", activityId);
                        throw new BusinessException(Code.FAILURE, "活动不存在");
                }

                // 3. 执行删除（物理删除）
                int deleteResult = activityMapper.deleteById(activityId);

                if (deleteResult > 0) {
                        log.info("管理员删除活动成功，活动 ID: {}, 活动标题: {}",
                                activityId, activity.getActivityTitle());
                        return ResultUtils.success(Code.SUCCESS, true, "删除成功");
                } else {
                        log.error("删除活动失败，活动 ID: {}", activityId);
                        throw new BusinessException("删除活动失败");
                }
        }

        /**
         * 根据校友会 id 查询校友会成员列表（分页查询+条件查询）
         *
         * @param request 查询请求参数
         * @param securityUser 当前登录用户
         * @return 返回分页成员列表
         */
        @PostMapping("/queryMemberList")
        @Operation(summary = "根据校友会 id 查询校友会成员列表")
        public BaseResponse<Page<OrganizationMemberResponse>> queryMemberList(
                        @Valid @RequestBody QueryAlumniAssociationMemberListRequest request,
                        @AuthenticationPrincipal SecurityUser securityUser) {
                log.info("查询校友会成员列表，校友会 ID: {}, 查询条件: {}", request.getAlumniAssociationId(), request);

                // 获取当前用户ID（如果未登录则为null）
                Long currentUserId = securityUser != null && securityUser.getWxUser() != null
                        ? securityUser.getWxUser().getWxId()
                        : null;

                Page<OrganizationMemberResponse> memberPage = alumniAssociationService
                                .getAlumniAssociationMemberPage(request, currentUserId);

                log.info("查询校友会成员列表成功，校友会 ID: {}, 总记录数: {}",
                                request.getAlumniAssociationId(), memberPage.getTotal());

                return ResultUtils.success(Code.SUCCESS, memberPage, "查询成功");
        }

        /**
         * 删除校友会成员
         *
         * @param deleteDto 删除请求参数
         * @return 返回删除结果
         */
        @DeleteMapping("/deleteMember")
        @Operation(summary = "删除校友会成员")
        public BaseResponse<Boolean> deleteMember(@Valid @RequestBody DeleteAlumniAssociationMemberDto deleteDto) {
                log.info("删除校友会成员，校友会 ID: {}, 成员用户 ID: {}",
                                deleteDto.getAlumniAssociationId(), deleteDto.getWxId());

                boolean result = alumniAssociationService.deleteMember(
                                deleteDto.getAlumniAssociationId(),
                                deleteDto.getWxId());

                if (result) {
                        log.info("删除校友会成员成功，校友会 ID: {}, 成员用户 ID: {}",
                                        deleteDto.getAlumniAssociationId(), deleteDto.getWxId());
                        return ResultUtils.success(Code.SUCCESS, true, "删除成功");
                } else {
                        log.error("删除校友会成员失败，校友会 ID: {}, 成员用户 ID: {}",
                                        deleteDto.getAlumniAssociationId(), deleteDto.getWxId());
                        return ResultUtils.failure(Code.FAILURE, false, "删除失败");
                }
        }

        /**
         * 邀请校友加入校友会
         *
         * @param inviteDto 邀请请求参数
         * @return 返回邀请结果
         */
        @PostMapping("/inviteMember")
        @Operation(summary = "邀请校友加入校友会")
        public BaseResponse<Boolean> inviteMember(@Valid @RequestBody InviteAlumniAssociationMemberDto inviteDto) {
                log.info("邀请校友加入校友会，校友会 ID: {}, 校友用户 ID: {}, 角色 ID: {}",
                                inviteDto.getAlumniAssociationId(), inviteDto.getWxId(), inviteDto.getRoleOrId());

                boolean result = alumniAssociationService.inviteMember(
                                inviteDto.getAlumniAssociationId(),
                                inviteDto.getWxId(),
                                inviteDto.getRoleOrId());

                if (result) {
                        log.info("邀请校友加入校友会成功，校友会 ID: {}, 校友用户 ID: {}, 角色 ID: {}",
                                        inviteDto.getAlumniAssociationId(), inviteDto.getWxId(),
                                        inviteDto.getRoleOrId());
                        return ResultUtils.success(Code.SUCCESS, true, "邀请成功");
                } else {
                        log.error("邀请校友加入校友会失败，校友会 ID: {}, 校友用户 ID: {}, 角色 ID: {}",
                                        inviteDto.getAlumniAssociationId(), inviteDto.getWxId(),
                                        inviteDto.getRoleOrId());
                        return ResultUtils.failure(Code.FAILURE, false, "邀请失败");
                }
        }

        /**
         * 新增组织架构角色
         *
         * @param addDto 新增请求参数
         * @return 返回新增结果
         */
        @PostMapping("/role/add")
        @Operation(summary = "新增组织架构角色")
        public BaseResponse<Boolean> addOrganizeArchiRole(@Valid @RequestBody AddOrganizeArchiRoleDto addDto) {
                log.info("新增校友会组织架构角色，组织 ID: {}, 角色名: {}", addDto.getOrganizeId(), addDto.getRoleOrName());

                // 后端指定组织类型为校友会（0）
                addDto.setOrganizeType(0);

                boolean result = organizeArchiRoleService.addOrganizeArchiRole(addDto);

                log.info("新增校友会组织架构角色成功，组织 ID: {}, 角色名: {}", addDto.getOrganizeId(), addDto.getRoleOrName());
                return ResultUtils.success(Code.SUCCESS, result, "新增成功");
        }

        /**
         * 更新组织架构角色
         *
         * @param updateDto 更新请求参数
         * @return 返回更新结果
         */
        @PutMapping("/role/update")
        @Operation(summary = "更新组织架构角色")
        public BaseResponse<Boolean> updateOrganizeArchiRole(@Valid @RequestBody UpdateOrganizeArchiRoleDto updateDto) {
                log.info("更新组织架构角色，角色 ID: {}, 组织 ID: {}", updateDto.getRoleOrId(), updateDto.getOrganizeId());

                boolean result = organizeArchiRoleService.updateOrganizeArchiRole(updateDto);

                log.info("更新组织架构角色成功，角色 ID: {}, 组织 ID: {}", updateDto.getRoleOrId(), updateDto.getOrganizeId());
                return ResultUtils.success(Code.SUCCESS, result, "更新成功");
        }

        /**
         * 删除组织架构角色
         *
         * @param deleteDto 删除请求参数
         * @return 返回删除结果
         */
        @DeleteMapping("/role/delete")
        @Operation(summary = "删除组织架构角色")
        public BaseResponse<Boolean> deleteOrganizeArchiRole(@Valid @RequestBody DeleteOrganizeArchiRoleDto deleteDto) {
                log.info("删除组织架构角色，角色 ID: {}, 组织 ID: {}", deleteDto.getRoleOrId(), deleteDto.getOrganizeId());

                boolean result = organizeArchiRoleService.deleteOrganizeArchiRole(
                                deleteDto.getRoleOrId(),
                                deleteDto.getOrganizeId());

                log.info("删除组织架构角色成功，角色 ID: {}, 组织 ID: {}", deleteDto.getRoleOrId(), deleteDto.getOrganizeId());
                return ResultUtils.success(Code.SUCCESS, result, "删除成功");
        }

        /**
         * 查询组织架构角色列表（树形结构）
         *
         * @param queryDto 查询请求参数
         * @return 返回角色树形列表
         */
        @PostMapping("/role/list")
        @Operation(summary = "查询组织架构角色列表（树形结构）")
        public BaseResponse<List<OrganizeArchiRoleVo>> getOrganizeArchiRoleList(
                        @Valid @RequestBody QueryOrganizeArchiRoleListDto queryDto) {
                log.info("查询校友会组织架构角色树，组织 ID: {}", queryDto.getOrganizeId());

                // 后端指定组织类型为校友会（0）
                queryDto.setOrganizeType(0);

                List<OrganizeArchiRoleVo> roleTree = organizeArchiRoleService.getOrganizeArchiRoleTree(
                                queryDto.getOrganizeId(),
                                queryDto.getOrganizeType(),
                                queryDto.getRoleOrName(),
                                queryDto.getStatus());

                log.info("查询校友会组织架构角色树成功，组织 ID: {}, 根节点数: {}", queryDto.getOrganizeId(), roleTree.size());
                return ResultUtils.success(Code.SUCCESS, roleTree, "查询成功");
        }

        /**
         * 更新校友会成员的组织架构角色
         *
         * @param securityUser 当前登录用户（操作人）
         * @param updateDto    更新请求参数
         * @return 返回更新结果
         */
        @PutMapping("/updateMemberRole")
        @Operation(summary = "更新校友会成员的组织架构角色")
        public BaseResponse<Boolean> updateMemberRole(
                        @AuthenticationPrincipal SecurityUser securityUser,
                        @Valid @RequestBody UpdateMemberRoleDto updateDto) {

                Long operatorWxId = securityUser.getWxUser().getWxId();

                log.info("更新校友会成员角色，操作人 ID: {}, 校友会 ID: {}, 成员用户 ID: {}, 新角色 ID: {}",
                                operatorWxId, updateDto.getAlumniAssociationId(), updateDto.getWxId(),
                                updateDto.getRoleOrId());

                boolean result = alumniAssociationService.updateMemberRole(
                                operatorWxId,
                                updateDto.getAlumniAssociationId(),
                                updateDto.getWxId(),
                                updateDto.getRoleOrId());

                if (result) {
                        log.info("更新校友会成员角色成功，校友会 ID: {}, 成员用户 ID: {}, 新角色 ID: {}",
                                        updateDto.getAlumniAssociationId(), updateDto.getWxId(),
                                        updateDto.getRoleOrId());
                        return ResultUtils.success(Code.SUCCESS, true, "更新成功");
                } else {
                        log.error("更新校友开成员角色失败，校友会 ID: {}, 成员用户 ID: {}, 新角色 ID: {}",
                                        updateDto.getAlumniAssociationId(), updateDto.getWxId(),
                                        updateDto.getRoleOrId());
                        return ResultUtils.failure(Code.FAILURE, false, "更新失败");
                }
        }

        /**
         * 更新校友会成员的组织架构角色 V2版本（基于username，支持wxId为空）
         *
         * @param securityUser 当前登录用户（操作人）
         * @param updateDto    更新请求参数
         * @return 返回更新结果
         */
        @PutMapping("/updateMemberRole/v2")
        @Operation(summary = "更新校友会成员的组织架构角色V2（基于username）")
        public BaseResponse<Boolean> updateMemberRoleV2(
                        @AuthenticationPrincipal SecurityUser securityUser,
                        @Valid @RequestBody UpdateAlumniAssociationMemberRoleV2Dto updateDto) {

                Long operatorWxId = securityUser.getWxUser().getWxId();

                log.info("更新校友会成员角色V2，操作人 ID: {}, 校友会 ID: {}, 成员 ID: {}, 成员用户名: {}, 新角色 ID: {}",
                                operatorWxId, updateDto.getAlumniAssociationId(), updateDto.getId(),
                                updateDto.getUsername(),
                                updateDto.getRoleOrId());

                boolean result = alumniAssociationService.updateMemberRoleV2(
                                operatorWxId,
                                updateDto.getAlumniAssociationId(),
                                updateDto.getId(),
                                updateDto.getUsername(),
                                updateDto.getRoleOrId(),
                                updateDto.getRoleName());

                if (result) {
                        log.info("更新校友会成员角色V2成功，校友会 ID: {}, 成员记录 ID: {}, 成员用户名: {}, 新角色 ID: {}",
                                        updateDto.getAlumniAssociationId(), updateDto.getId(), updateDto.getUsername(),
                                        updateDto.getRoleOrId());
                        return ResultUtils.success(Code.SUCCESS, true, "更新成功");
                } else {
                        log.error("更新校友会成员角色V2失败，校友会 ID: {}, 成员记录 ID: {}, 成员用户名: {}, 新角色 ID: {}",
                                        updateDto.getAlumniAssociationId(), updateDto.getId(), updateDto.getUsername(),
                                        updateDto.getRoleOrId());
                        return ResultUtils.failure(Code.FAILURE, false, "更新失败");
                }
        }

        /**
         * 根据校友会 id 查询校友会管理员列表
         *
         * @param alumniAssociationId 校友会ID
         * @return 返回管理员用户列表
         */
        @GetMapping("/admins/{alumniAssociationId}")
        @Operation(summary = "根据校友会 id 查询校友会管理员列表")
        public BaseResponse<List<UserListResponse>> getAdminsByAssociationId(@PathVariable Long alumniAssociationId) {
                log.info("查询校友会管理员列表，校友会 ID: {}", alumniAssociationId);

                List<UserListResponse> adminList = alumniAssociationService.getAdminsByAssociationId(alumniAssociationId);

                log.info("查询校友会管理员列表成功，校友会 ID: {}, 管理员数量: {}", alumniAssociationId, adminList.size());
                return ResultUtils.success(Code.SUCCESS, adminList, "查询成功");
        }

        /**
         * 为校友会添加管理员
         *
         * @param addDto 添加管理员请求参数
         * @return 返回添加结果
         */
        @PostMapping("/admin/add")
        @Operation(summary = "为校友会添加管理员")
        public BaseResponse<Boolean> addAdminToAssociation(@Valid @RequestBody AddAlumniAssociationAdminDto addDto) {
                log.info("添加校友会管理员，校友会 ID: {}, 用户 ID: {}",
                        addDto.getAlumniAssociationId(), addDto.getWxId());

                boolean result = alumniAssociationService.addAdminToAssociation(
                        addDto.getAlumniAssociationId(),
                        addDto.getWxId());

                if (result) {
                        log.info("添加校友会管理员成功，校友会 ID: {}, 用户 ID: {}",
                                addDto.getAlumniAssociationId(), addDto.getWxId());
                        return ResultUtils.success(Code.SUCCESS, true, "添加成功");
                } else {
                        log.error("添加校友会管理员失败，校友会 ID: {}, 用户 ID: {}",
                                addDto.getAlumniAssociationId(), addDto.getWxId());
                        return ResultUtils.failure(Code.FAILURE, false, "添加失败");
                }
        }

        /**
         * 移除校友会管理员
         *
         * @param removeDto 移除管理员请求参数
         * @return 返回移除结果
         */
        @DeleteMapping("/admin/remove")
        @Operation(summary = "移除校友会管理员")
        public BaseResponse<Boolean> removeAdminFromAssociation(@Valid @RequestBody RemoveAlumniAssociationAdminDto removeDto) {
                log.info("移除校友会管理员，校友会 ID: {}, 用户 ID: {}",
                        removeDto.getAlumniAssociationId(), removeDto.getWxId());

                boolean result = alumniAssociationService.removeAdminFromAssociation(
                        removeDto.getAlumniAssociationId(),
                        removeDto.getWxId());

                if (result) {
                        log.info("移除校友会管理员成功，校友会 ID: {}, 用户 ID: {}",
                                removeDto.getAlumniAssociationId(), removeDto.getWxId());
                        return ResultUtils.success(Code.SUCCESS, true, "移除成功");
                } else {
                        log.error("移除校友会管理员失败，校友会 ID: {}, 用户 ID: {}",
                                removeDto.getAlumniAssociationId(), removeDto.getWxId());
                        return ResultUtils.failure(Code.FAILURE, false, "移除失败");
                }
        }

        /**
         * 绑定校友会组织架构成员与系统用户
         *
         * @param bindDto 绑定请求参数
         * @return 返回绑定结果
         */
        @PutMapping("/bindMemberToUser")
        @Operation(summary = "绑定校友会组织架构成员与系统用户")
        public BaseResponse<Boolean> bindMemberToUser(@Valid @RequestBody BindMemberToUserDto bindDto) {
                log.info("绑定校友会成员与系统用户，成员 ID: {}, 用户 ID: {}",
                        bindDto.getMemberId(), bindDto.getWxId());

                boolean result = alumniAssociationService.bindMemberToUser(
                        bindDto.getMemberId(),
                        bindDto.getWxId());

                if (result) {
                        log.info("绑定校友会成员与系统用户成功，成员 ID: {}, 用户 ID: {}",
                                bindDto.getMemberId(), bindDto.getWxId());
                        return ResultUtils.success(Code.SUCCESS, true, "绑定成功");
                } else {
                        log.error("绑定校友会成员与系统用户失败，成员 ID: {}, 用户 ID: {}",
                                bindDto.getMemberId(), bindDto.getWxId());
                        return ResultUtils.failure(Code.FAILURE, false, "绑定失败");
                }
        }
}
