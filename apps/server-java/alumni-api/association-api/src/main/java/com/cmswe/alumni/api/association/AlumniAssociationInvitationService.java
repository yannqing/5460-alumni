package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.AlumniAssociationInvitation;

/**
 * 校友会邀请记录Service
 */
public interface AlumniAssociationInvitationService extends IService<AlumniAssociationInvitation> {

    /**
     * 处理邀请（同意或拒绝）
     *
     * @param invitationId 邀请ID
     * @param notificationId 通知ID
     * @param userId 当前用户ID
     * @param agree 是否同意
     * @return 处理是否成功
     */
    boolean handleInvitation(Long invitationId, Long notificationId, Long userId, Boolean agree);
}
