package com.cmswe.alumni.service.user.service.message.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.api.association.AlumniAssociationJoinApplicationService;
import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.user.AlumniEducationService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.user.RoleUserService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.api.user.WxUserInfoService;
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
    private final WxUserInfoService wxUserInfoService;
    private final AlumniEducationService alumniEducationService;
    private final AlumniAssociationJoinApplicationService alumniAssociationJoinApplicationService;
    private final AlumniAssociationService alumniAssociationService;

    public AlumniJoinApprovalHandler(
            RoleService roleService,
            RoleUserService roleUserService,
            OrganizeArchiRoleService organizeArchiRoleService,
            AlumniAssociationMemberService alumniAssociationMemberService,
            UserService userService,
            WxUserInfoService wxUserInfoService,
            AlumniEducationService alumniEducationService,
            AlumniAssociationJoinApplicationService alumniAssociationJoinApplicationService,
            AlumniAssociationService alumniAssociationService) {
        this.roleService = roleService;
        this.roleUserService = roleUserService;
        this.organizeArchiRoleService = organizeArchiRoleService;
        this.alumniAssociationMemberService = alumniAssociationMemberService;
        this.userService = userService;
        this.wxUserInfoService = wxUserInfoService;
        this.alumniEducationService = alumniEducationService;
        this.alumniAssociationJoinApplicationService = alumniAssociationJoinApplicationService;
        this.alumniAssociationService = alumniAssociationService;
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

            // 5. 查询申请记录获取申请时填写的信息
            AlumniAssociationJoinApplication application = getApplicationByMessageId(message);
            if (application == null) {
                log.error("[AlumniJoinApprovalHandler] 无法获取申请记录 - MessageId: {}", message.getMessageId());
                return false;
            }

            // 6. 同步基本信息到用户表（以申请时填写的信息为准）
            boolean userInfoSynced = syncUserBasicInfo(wxId, application);
            if (!userInfoSynced) {
                log.warn("[AlumniJoinApprovalHandler] 同步用户基本信息失败 - 用户ID: {}", wxId);
                // 不返回 false，继续处理其他步骤
            }

            // 7. 同步教育经历到教育经历表（以申请时填写的信息为准）
            boolean educationSynced = syncEducationInfo(wxId, application);
            if (!educationSynced) {
                log.warn("[AlumniJoinApprovalHandler] 同步教育经历失败 - 用户ID: {}", wxId);
                // 不返回 false，继续处理其他步骤
            }

            // 8. 添加用户到 role_user 表（分配校友会成员角色）
            boolean roleAssigned = assignMemberRole(wxId, alumniAssociationId);
            if (!roleAssigned) {
                log.error("[AlumniJoinApprovalHandler] 分配校友会成员角色失败 - 用户ID: {}, 校友会ID: {}",
                        wxId, alumniAssociationId);
                return false;
            }

            // 9. 创建或获取普通成员的架构角色
            Long roleOrId = getOrCreateRegularMemberArchiRole(alumniAssociationId);
            if (roleOrId == null) {
                log.error("[AlumniJoinApprovalHandler] 创建或获取普通成员架构角色失败 - 校友会ID: {}",
                        alumniAssociationId);
                return false;
            }

            // 10. 添加用户到校友会成员表
            boolean memberAdded = addToAssociationMemberTable(wxId, alumniAssociationId, roleOrId);
            if (!memberAdded) {
                log.error("[AlumniJoinApprovalHandler] 添加用户到校友会成员表失败 - 用户ID: {}, 校友会ID: {}",
                        wxId, alumniAssociationId);
                return false;
            }

            // 11. 更新用户的 isAlumni 字段为 1
            boolean alumniStatusUpdated = updateUserAlumniStatus(wxId);
            if (!alumniStatusUpdated) {
                log.warn("[AlumniJoinApprovalHandler] 更新用户校友状态失败 - 用户ID: {}", wxId);
                // 不返回 false，因为这不是关键操作
            }

            // 12. 更新校友会成员数量（+1）
            alumniAssociationService.updateMemberCount(alumniAssociationId, 1);

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

            // 2. 如果已经有认证，无需更新
            if (wxUser.getCertificationFlag() != null && wxUser.getCertificationFlag() > 0) {
                log.debug("[AlumniJoinApprovalHandler] 用户已有认证，无需更新 - 用户ID: {}, 认证标识: {}", wxId, wxUser.getCertificationFlag());
                return true;
            }

            // 3. 更新 certificationFlag 为 3（校友会认证）
            wxUser.setCertificationFlag(3);
            boolean updateResult = userService.updateById(wxUser);

            if (updateResult) {
                log.info("[AlumniJoinApprovalHandler] 校友认证状态更新成功 - 用户ID: {}, certificationFlag: 0 -> 3", wxId);
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

    /**
     * 从消息中获取申请记录
     *
     * @param message 统一消息对象
     * @return 申请记录，未找到返回 null
     */
    private AlumniAssociationJoinApplication getApplicationByMessageId(UnifiedMessage message) {
        try {
            Long wxId = message.getToId();
            Long alumniAssociationId = message.getRelatedId();

            if (wxId == null || alumniAssociationId == null) {
                log.warn("[AlumniJoinApprovalHandler] 消息参数不完整，无法查询申请记录 - MessageId: {}", message.getMessageId());
                return null;
            }

            // 查询最新的已通过的申请记录
            LambdaQueryWrapper<AlumniAssociationJoinApplication> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AlumniAssociationJoinApplication::getTargetId, wxId)
                    .eq(AlumniAssociationJoinApplication::getAlumniAssociationId, alumniAssociationId)
                    .eq(AlumniAssociationJoinApplication::getApplicantType, 1) // 1-用户
                    .eq(AlumniAssociationJoinApplication::getApplicationStatus, 1) // 1-已通过
                    .orderByDesc(AlumniAssociationJoinApplication::getReviewTime)
                    .last("LIMIT 1");

            AlumniAssociationJoinApplication application = alumniAssociationJoinApplicationService.getOne(queryWrapper);

            if (application == null) {
                log.warn("[AlumniJoinApprovalHandler] 未找到申请记录 - 用户ID: {}, 校友会ID: {}", wxId, alumniAssociationId);
            }

            return application;

        } catch (Exception e) {
            log.error("[AlumniJoinApprovalHandler] 查询申请记录异常 - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 同步基本信息到用户信息表（以申请时填写的信息为准）
     *
     * @param wxId        用户ID
     * @param application 申请记录
     * @return 是否成功
     */
    private boolean syncUserBasicInfo(Long wxId, AlumniAssociationJoinApplication application) {
        try {
            // 1. 检查申请记录中是否有基本信息
            if (application.getName() == null && application.getIdentifyCode() == null && application.getPhone() == null) {
                log.debug("[AlumniJoinApprovalHandler] 申请记录中无基本信息，跳过同步 - 用户ID: {}", wxId);
                return true;
            }

            // 2. 查询用户信息
            LambdaQueryWrapper<WxUserInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WxUserInfo::getWxId, wxId);
            WxUserInfo userInfo = wxUserInfoService.getOne(queryWrapper);

            if (userInfo == null) {
                // 创建新的用户信息
                userInfo = new WxUserInfo();
                userInfo.setWxId(wxId);
                userInfo.setName(application.getName());
                userInfo.setIdentifyType(0); // 默认为身份证
                userInfo.setIdentifyCode(application.getIdentifyCode());
                userInfo.setPhone(application.getPhone());

                boolean saved = wxUserInfoService.save(userInfo);
                if (saved) {
                    log.info("[AlumniJoinApprovalHandler] 创建用户基本信息成功 - 用户ID: {}, 姓名: {}", wxId, application.getName());
                    return true;
                } else {
                    log.error("[AlumniJoinApprovalHandler] 创建用户基本信息失败 - 用户ID: {}", wxId);
                    return false;
                }
            } else {
                // 更新用户信息（以申请时填写的为准）
                boolean updated = false;

                if (application.getName() != null) {
                    userInfo.setName(application.getName());
                    updated = true;
                }

                if (application.getIdentifyCode() != null) {
                    userInfo.setIdentifyCode(application.getIdentifyCode());
                    updated = true;
                }

                if (application.getPhone() != null) {
                    userInfo.setPhone(application.getPhone());
                    updated = true;
                }

                if (updated) {
                    boolean updateResult = wxUserInfoService.updateById(userInfo);
                    if (updateResult) {
                        log.info("[AlumniJoinApprovalHandler] 同步用户基本信息成功 - 用户ID: {}, 姓名: {}", wxId, application.getName());
                        return true;
                    } else {
                        log.error("[AlumniJoinApprovalHandler] 同步用户基本信息失败 - 用户ID: {}", wxId);
                        return false;
                    }
                } else {
                    log.debug("[AlumniJoinApprovalHandler] 无需更新用户基本信息 - 用户ID: {}", wxId);
                    return true;
                }
            }

        } catch (Exception e) {
            log.error("[AlumniJoinApprovalHandler] 同步用户基本信息异常 - 用户ID: {}, Error: {}", wxId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 同步教育经历到教育经历表（以申请时填写的信息为准）
     *
     * @param wxId        用户ID
     * @param application 申请记录
     * @return 是否成功
     */
    private boolean syncEducationInfo(Long wxId, AlumniAssociationJoinApplication application) {
        try {
            // 1. 检查申请记录中是否有教育经历信息
            if (application.getSchoolId() == null) {
                log.debug("[AlumniJoinApprovalHandler] 申请记录中无教育经历信息，跳过同步 - 用户ID: {}", wxId);
                return true;
            }

            // 2. 构建教育经历对象
            AlumniEducation education = new AlumniEducation();
            education.setWxId(wxId);
            education.setSchoolId(application.getSchoolId());
            education.setEnrollmentYear(application.getEnrollmentYear());
            education.setGraduationYear(application.getGraduationYear());
            education.setDepartment(application.getDepartment());
            education.setMajor(application.getMajor());
            education.setClassName(application.getClassName());
            education.setEducationLevel(application.getEducationLevel());
            education.setCertificationStatus(0); // 默认未认证

            // 3. 保存或更新教育经历（如果同一个学校已存在则更新，否则新增）
            boolean result = alumniEducationService.saveOrUpdateByWxIdAndSchoolId(education);

            if (result) {
                log.info("[AlumniJoinApprovalHandler] 同步教育经历成功 - 用户ID: {}, 学校ID: {}", wxId, application.getSchoolId());
                return true;
            } else {
                log.error("[AlumniJoinApprovalHandler] 同步教育经历失败 - 用户ID: {}, 学校ID: {}", wxId, application.getSchoolId());
                return false;
            }

        } catch (Exception e) {
            log.error("[AlumniJoinApprovalHandler] 同步教育经历异常 - 用户ID: {}, Error: {}", wxId, e.getMessage(), e);
            return false;
        }
    }
}
