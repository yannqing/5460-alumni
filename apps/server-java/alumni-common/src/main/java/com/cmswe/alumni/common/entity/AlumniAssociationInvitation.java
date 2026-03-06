package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校友会邀请记录表
 *
 * @TableName alumni_association_invitation
 */
@TableName(value = "alumni_association_invitation")
@Data
public class AlumniAssociationInvitation implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 校友会ID
     */
    @TableField(value = "alumni_association_id")
    private Long alumniAssociationId;

    /**
     * 邀请人ID（管理员）
     */
    @TableField(value = "inviter_id")
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    @TableField(value = "invitee_id")
    private Long inviteeId;

    /**
     * 组织架构角色ID（邀请时指定的角色，可为空）
     */
    @TableField(value = "role_or_id")
    private Long roleOrId;

    /**
     * 通知ID（关联notification表）
     */
    @TableField(value = "notification_id")
    private Long notificationId;

    /**
     * 邀请状态：0-待处理, 1-已同意, 2-已拒绝, 3-已过期
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 处理时间
     */
    @TableField(value = "process_time")
    private LocalDateTime processTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
