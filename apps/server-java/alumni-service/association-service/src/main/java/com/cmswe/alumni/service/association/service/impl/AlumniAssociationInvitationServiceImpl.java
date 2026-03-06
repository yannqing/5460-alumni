package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationInvitationService;
import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.user.UnifiedMessageApiService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.AlumniAssociationInvitation;
import com.cmswe.alumni.common.entity.AlumniAssociationMember;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationInvitationMapper;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 校友会邀请记录Service实现类
 */
@Slf4j
@Service
public class AlumniAssociationInvitationServiceImpl
        extends ServiceImpl<AlumniAssociationInvitationMapper, AlumniAssociationInvitation>
        implements AlumniAssociationInvitationService {

    @Resource
    private AlumniAssociationMapper alumniAssociationMapper;

    @Resource
    private AlumniAssociationMemberService alumniAssociationMemberService;

    @Resource
    private UnifiedMessageApiService unifiedMessageApiService;

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Resource
    private com.cmswe.alumni.api.user.UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleInvitation(Long invitationId, Long notificationId, Long userId, Boolean agree) {
        // 1. 参数校验
        if (invitationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "邀请ID不能为空");
        }
        if (notificationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "通知ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }
        if (agree == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "是否同意不能为空");
        }

        log.info("开始处理校友会邀请 - 邀请ID: {}, 用户ID: {}, 是否同意: {}", invitationId, userId, agree);

        // 2. 查询邀请记录
        AlumniAssociationInvitation invitation = this.getById(invitationId);
        if (invitation == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "邀请记录不存在");
        }

        // 3. 校验是否是本人的邀请
        if (!invitation.getInviteeId().equals(userId)) {
            throw new BusinessException(ErrorType.NO_AUTH_ERROR, "无权处理该邀请");
        }

        // 4. 检查邀请状态
        if (invitation.getStatus() != 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该邀请已被处理");
        }

        // 5. 查询校友会信息
        AlumniAssociation association = alumniAssociationMapper.selectById(invitation.getAlumniAssociationId());
        if (association == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
        }

        // 6. 查询被邀请人信息
        WxUserInfo inviteeInfo = wxUserInfoService.getById(userId);
        if (inviteeInfo == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "用户信息不存在");
        }

        // 7. 更新邀请记录状态
        invitation.setStatus(agree ? 1 : 2); // 1-已同意, 2-已拒绝
        invitation.setProcessTime(LocalDateTime.now());
        this.updateById(invitation);

        // 8. 更新通知状态
        // TODO: 需要通过 NotificationService API 来更新通知的 action_status
        // 由于模块依赖问题，暂时跳过此步骤
        // 前端可以通过邀请记录的状态来判断是否已处理

        // 9. 如果同意，则添加成员记录
        if (agree) {
            // 创建成员记录
            AlumniAssociationMember member = new AlumniAssociationMember();
            member.setWxId(userId);
            member.setAlumniAssociationId(invitation.getAlumniAssociationId());
            member.setRoleOrId(invitation.getRoleOrId());
            member.setJoinTime(LocalDateTime.now());
            member.setStatus(1); // 状态：1-正常
            alumniAssociationMemberService.save(member);

            log.info("用户同意邀请，已加入校友会 - 用户ID: {}, 校友会ID: {}", userId, invitation.getAlumniAssociationId());

            // 更新校友会成员数量（+1）
            Integer currentMemberCount = association.getMemberCount();
            if (currentMemberCount == null) {
                currentMemberCount = 0;
            }
            association.setMemberCount(currentMemberCount + 1);
            alumniAssociationMapper.updateById(association);

            // 更新用户的 isAlumni 字段为 1（成为校友）
            com.cmswe.alumni.common.entity.WxUser wxUser = userService.getById(userId);
            if (wxUser != null && (wxUser.getIsAlumni() == null || wxUser.getIsAlumni() == 0)) {
                wxUser.setIsAlumni(1);
                userService.updateById(wxUser);
                log.info("用户校友状态已更新 - 用户ID: {}, isAlumni: 1", userId);
            }

            // 10. 发送通知给管理员：用户已同意
            String contentToInviter = String.format("您邀请的用户 %s 已成功加入 %s",
                    inviteeInfo.getName() != null ? inviteeInfo.getName() : inviteeInfo.getNickname(),
                    association.getAssociationName());

            unifiedMessageApiService.sendSystemNotification(
                    invitation.getInviterId(),
                    com.cmswe.alumni.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                    "邀请已接受",
                    contentToInviter,
                    invitation.getAlumniAssociationId(),
                    "ASSOCIATION"
            );

            // 11. 发送通知给用户：欢迎加入
            String contentToInvitee = String.format("您已成功加入 %s，欢迎！", association.getAssociationName());

            unifiedMessageApiService.sendSystemNotification(
                    userId,
                    com.cmswe.alumni.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                    "加入成功",
                    contentToInvitee,
                    invitation.getAlumniAssociationId(),
                    "ASSOCIATION"
            );

            log.info("已发送邀请接受通知给管理员和欢迎通知给用户");
        } else {
            log.info("用户拒绝邀请 - 用户ID: {}, 校友会ID: {}", userId, invitation.getAlumniAssociationId());

            // 发送通知给管理员：用户已拒绝
            String contentToInviter = String.format("用户 %s 拒绝了加入 %s 的邀请",
                    inviteeInfo.getName() != null ? inviteeInfo.getName() : inviteeInfo.getNickname(),
                    association.getAssociationName());

            unifiedMessageApiService.sendSystemNotification(
                    invitation.getInviterId(),
                    com.cmswe.alumni.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                    "邀请被拒绝",
                    contentToInviter,
                    invitation.getAlumniAssociationId(),
                    "ASSOCIATION"
            );
        }

        return true;
    }
}
