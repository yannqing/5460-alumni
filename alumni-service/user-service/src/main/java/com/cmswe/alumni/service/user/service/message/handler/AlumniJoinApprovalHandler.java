package com.cmswe.alumni.service.user.service.message.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.user.RoleUserService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.handler.AbstractMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 校友会加入申请审核通过处理器（责任链模式）
 *
 * <p>功能：当校友会加入申请审核通过时，执行以下操作：
 * <ul>
 *   <li>1. 添加用户到 role_user 表（分配校友会成员角色 ORGANIZE_ALUMNI_MEMBER）</li>
 *   <li>2. 创建或获取普通成员的架构角色（在 organize_archi_role 表中）</li>
 *   <li>3. 添加用户到校友会成员表（alumni_association_member），设置架构角色</li>
 *   <li>4. 更新用户的 isAlumni 字段为 1</li>
 * </ul>
 *
 * <p>触发条件：
 * <ul>
 *   <li>消息类别为 BUSINESS（业务通知）</li>
 *   <li>消息类型为 ALUMNI_JOIN_APPROVED（校友会加入申请审核通过）</li>
 *   <li>relatedType 为 ASSOCIATION（校友会相关）</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-01-26
 */
@Slf4j
@Component
public class AlumniJoinApprovalHandler extends AbstractMessageHandler<UnifiedMessage> {

    private final RoleService roleService;
    private final RoleUserService roleUserService;
    private final OrganizeArchiRoleService organizeArchiRoleService;
    private final AlumniAssociationMemberService alumniAssociationMemberService;
    private final UserService userService;

    public AlumniJoinApprovalHandler(
            RoleService roleService,
            RoleUserService roleUserService,
            OrganizeArchiRoleService organizeArchiRoleService,
            AlumniAssociationMemberService alumniAssociationMemberService,
            UserService userService) {
        this.roleService = roleService;
        this.roleUserService = roleUserService;
        this.organizeArchiRoleService = organizeArchiRoleService;
        this.alumniAssociationMemberService = alumniAssociationMemberService;
        this.userService = userService;
    }

    @Override
    public String getHandlerName() {
        return "AlumniJoinApprovalHandler";
    }

    @Override
    public int getOrder() {
        // 在数据库持久化之后，校友状态更新之前执行
        return 24;
    }

    @Override
    protected boolean doHandle(UnifiedMessage message) {
        // 1. 检查是否为业务通知
        if (message.getCategory() != MessageCategory.BUSINESS) {
            log.debug("[AlumniJoinApprovalHandler] 非业务通知，跳过处理 - Category: {}", message.getCategory());
            return true;
        }

        // 2. 检查消息类型是否为校友会加入申请审核通过
        if (!"ALUMNI_JOIN_APPROVED".equals(message.getMessageType())) {
            log.debug("[AlumniJoinApprovalHandler] 非校友会加入申请审核通过消息，跳过处理 - MessageType: {}",
                    message.getMessageType());
            return true;
        }

        // 3. 检查关联类型是否为校友会
        if (!"ASSOCIATION".equals(message.getRelatedType())) {
            log.debug("[AlumniJoinApprovalHandler] 非校友会相关消息，跳过处理 - RelatedType: {}",
                    message.getRelatedType());
            return true;
        }

        // 4. 获取用户ID和校友会ID
        Long wxId = message.getToId();
        Long alumniAssociationId = message.getRelatedId();

        if (wxId == null || wxId == 0) {
            log.warn("[AlumniJoinApprovalHandler] 用户ID为空，无法处理 - MessageId: {}", message.getMessageId());
            return false;
        }

        if (alumniAssociationId == null || alumniAssociationId == 0) {
            log.warn("[AlumniJoinApprovalHandler] 校友会ID为空，无法处理 - MessageId: {}", message.getMessageId());
            return false;
        }

        try {
            log.info("[AlumniJoinApprovalHandler] 开始处理校友会加入申请审核通过 - 用户ID: {}, 校友会ID: {}",
                    wxId, alumniAssociationId);

            // 5. 添加用户到 role_user 表（分配校友会成员角色）
            boolean roleAssigned = assignMemberRole(wxId, alumniAssociationId);
            if (!roleAssigned) {
                log.error("[AlumniJoinApprovalHandler] 分配校友会成员角色失败 - 用户ID: {}, 校友会ID: {}",
                        wxId, alumniAssociationId);
                return false;
            }

            // 6. 创建或获取普通成员的架构角色
            Long roleOrId = getOrCreateRegularMemberArchiRole(alumniAssociationId);
            if (roleOrId == null) {
                log.error("[AlumniJoinApprovalHandler] 创建或获取普通成员架构角色失败 - 校友会ID: {}",
                        alumniAssociationId);
                return false;
            }

            // 7. 添加用户到校友会成员表
            boolean memberAdded = addToAssociationMemberTable(wxId, alumniAssociationId, roleOrId);
            if (!memberAdded) {
                log.error("[AlumniJoinApprovalHandler] 添加用户到校友会成员表失败 - 用户ID: {}, 校友会ID: {}",
                        wxId, alumniAssociationId);
                return false;
            }

            // 8. 更新用户的 isAlumni 字段为 1
            boolean alumniStatusUpdated = updateUserAlumniStatus(wxId);
            if (!alumniStatusUpdated) {
                log.warn("[AlumniJoinApprovalHandler] 更新用户校友状态失败 - 用户ID: {}", wxId);
                // 不返回 false，因为这不是关键操作
            }

            log.info("[AlumniJoinApprovalHandler] 校友会加入申请审核通过处理完成 - 用户ID: {}, 校友会ID: {}",
                    wxId, alumniAssociationId);
            return true;

        } catch (Exception e) {
            log.error("[AlumniJoinApprovalHandler] 处理校友会加入申请审核通过异常 - 用户ID: {}, 校友会ID: {}, Error: {}",
                    wxId, alumniAssociationId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 分配校友会成员角色（添加到 role_user 表）
     *
     * @param wxId               用户ID
     * @param alumniAssociationId 校友会ID
     * @return 是否成功
     */
    private boolean assignMemberRole(Long wxId, Long alumniAssociationId) {
        try {
            // 1. 查找校友会成员角色（code: ORGANIZE_ALUMNI_MEMBER）
            Role memberRole = roleService.getRoleByCodeInner("ORGANIZE_ALUMNI_MEMBER");
            if (memberRole == null) {
                log.error("[AlumniJoinApprovalHandler] 未找到校友会成员角色 - Code: ORGANIZE_ALUMNI_MEMBER");
                return false;
            }

            // 2. 检查用户是否已经有该角色
            LambdaQueryWrapper<RoleUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RoleUser::getWxId, wxId)
                    .eq(RoleUser::getRoleId, memberRole.getRoleId())
                    .eq(RoleUser::getType, 2) // 2-组织角色
                    .eq(RoleUser::getOrganizeId, alumniAssociationId);

            RoleUser existingRoleUser = roleUserService.getOne(queryWrapper);
            if (existingRoleUser != null) {
                log.debug("[AlumniJoinApprovalHandler] 用户已有该角色，无需重复添加 - 用户ID: {}, 角色ID: {}",
                        wxId, memberRole.getRoleId());
                return true;
            }

            // 3. 添加角色
            RoleUser roleUser = new RoleUser();
            roleUser.setWxId(wxId);
            roleUser.setRoleId(memberRole.getRoleId());
            roleUser.setType(2); // 2-组织角色
            roleUser.setOrganizeId(alumniAssociationId);

            boolean saved = roleUserService.save(roleUser);
            if (saved) {
                log.info("[AlumniJoinApprovalHandler] 校友会成员角色分配成功 - 用户ID: {}, 角色ID: {}, 校友会ID: {}",
                        wxId, memberRole.getRoleId(), alumniAssociationId);
            }
            return saved;

        } catch (Exception e) {
            log.error("[AlumniJoinApprovalHandler] 分配校友会成员角色异常 - 用户ID: {}, 校友会ID: {}, Error: {}",
                    wxId, alumniAssociationId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建或获取普通成员的架构角色
     *
     * <p>参考 LocalPlatformManagementController 中的 reviewAssociationApplication 接口
     * 使用相同的 code 和名称
     *
     * @param alumniAssociationId 校友会ID
     * @return 架构角色ID，失败返回 null
     */
    private Long getOrCreateRegularMemberArchiRole(Long alumniAssociationId) {
        try {
            final String REGULAR_MEMBER_CODE = "MEMBER";
            final String REGULAR_MEMBER_NAME = "成员";
            final String REGULAR_MEMBER_REMARK = "普通成员";

            // 1. 查询是否已存在普通成员的架构角色
            LambdaQueryWrapper<OrganizeArchiRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrganizeArchiRole::getOrganizeId, alumniAssociationId)
                    .eq(OrganizeArchiRole::getOrganizeType, 0) // 0-校友会
                    .eq(OrganizeArchiRole::getRoleOrCode, REGULAR_MEMBER_CODE)
                    .eq(OrganizeArchiRole::getStatus, 1); // 1-启用

            OrganizeArchiRole existingRole = organizeArchiRoleService.getOne(queryWrapper);
            if (existingRole != null) {
                log.debug("[AlumniJoinApprovalHandler] 普通成员架构角色已存在 - 校友会ID: {}, 角色ID: {}",
                        alumniAssociationId, existingRole.getRoleOrId());
                return existingRole.getRoleOrId();
            }

            // 2. 不存在则创建
            OrganizeArchiRole newRole = new OrganizeArchiRole();
            newRole.setOrganizeType(0); // 0-校友会
            newRole.setOrganizeId(alumniAssociationId);
            newRole.setRoleOrName(REGULAR_MEMBER_NAME);
            newRole.setRoleOrCode(REGULAR_MEMBER_CODE);
            newRole.setRemark(REGULAR_MEMBER_REMARK);
            newRole.setStatus(1); // 1-启用

            boolean saved = organizeArchiRoleService.save(newRole);
            if (saved) {
                log.info("[AlumniJoinApprovalHandler] 创建普通成员架构角色成功 - 校友会ID: {}, 角色ID: {}, 角色名: {}",
                        alumniAssociationId, newRole.getRoleOrId(), REGULAR_MEMBER_NAME);
                return newRole.getRoleOrId();
            } else {
                log.error("[AlumniJoinApprovalHandler] 创建普通成员架构角色失败 - 校友会ID: {}", alumniAssociationId);
                return null;
            }

        } catch (Exception e) {
            log.error("[AlumniJoinApprovalHandler] 创建或获取普通成员架构角色异常 - 校友会ID: {}, Error: {}",
                    alumniAssociationId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 添加用户到校友会成员表
     *
     * @param wxId                用户ID
     * @param alumniAssociationId 校友会ID
     * @param roleOrId            架构角色ID
     * @return 是否成功
     */
    private boolean addToAssociationMemberTable(Long wxId, Long alumniAssociationId, Long roleOrId) {
        try {
            // 1. 检查用户是否已经是该校友会成员
            LambdaQueryWrapper<AlumniAssociationMember> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AlumniAssociationMember::getWxId, wxId)
                    .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                    .eq(AlumniAssociationMember::getStatus, 1); // 1-正常

            AlumniAssociationMember existingMember = alumniAssociationMemberService.getOne(queryWrapper);
            if (existingMember != null) {
                log.debug("[AlumniJoinApprovalHandler] 用户已是该校友会成员，无需重复添加 - 用户ID: {}, 校友会ID: {}",
                        wxId, alumniAssociationId);
                return true;
            }

            // 2. 添加成员记录
            AlumniAssociationMember member = new AlumniAssociationMember();
            member.setWxId(wxId);
            member.setAlumniAssociationId(alumniAssociationId);
            member.setRoleOrId(roleOrId);
            member.setJoinTime(java.time.LocalDateTime.now());
            member.setStatus(1); // 1-正常

            boolean saved = alumniAssociationMemberService.save(member);
            if (saved) {
                log.info("[AlumniJoinApprovalHandler] 添加用户到校友会成员表成功 - 用户ID: {}, 校友会ID: {}, 架构角色ID: {}",
                        wxId, alumniAssociationId, roleOrId);
                return true;
            } else {
                log.error("[AlumniJoinApprovalHandler] 添加用户到校友会成员表失败 - 用户ID: {}, 校友会ID: {}",
                        wxId, alumniAssociationId);
                return false;
            }

        } catch (Exception e) {
            log.error("[AlumniJoinApprovalHandler] 添加用户到校友会成员表异常 - 用户ID: {}, 校友会ID: {}, Error: {}",
                    wxId, alumniAssociationId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新用户的校友状态（isAlumni 字段为 1）
     *
     * @param wxId 用户ID
     * @return 是否成功
     */
    private boolean updateUserAlumniStatus(Long wxId) {
        try {
            // 1. 查询用户当前的 isAlumni 状态
            WxUser wxUser = userService.getById(wxId);
            if (wxUser == null) {
                log.warn("[AlumniJoinApprovalHandler] 用户不存在，无法更新校友状态 - 用户ID: {}", wxId);
                return false;
            }

            // 2. 如果已经是校友，无需更新
            if (wxUser.getIsAlumni() != null && wxUser.getIsAlumni() == 1) {
                log.debug("[AlumniJoinApprovalHandler] 用户已是校友，无需更新 - 用户ID: {}", wxId);
                return true;
            }

            // 3. 更新 isAlumni 为 1
            wxUser.setIsAlumni(1);
            boolean updateResult = userService.updateById(wxUser);

            if (updateResult) {
                log.info("[AlumniJoinApprovalHandler] 校友状态更新成功 - 用户ID: {}, isAlumni: 0 -> 1", wxId);
                return true;
            } else {
                log.warn("[AlumniJoinApprovalHandler] 校友状态更新失败 - 用户ID: {}", wxId);
                return false;
            }

        } catch (Exception e) {
            log.error("[AlumniJoinApprovalHandler] 更新校友状态异常 - 用户ID: {}, Error: {}", wxId, e.getMessage(), e);
            return false;
        }
    }
}
