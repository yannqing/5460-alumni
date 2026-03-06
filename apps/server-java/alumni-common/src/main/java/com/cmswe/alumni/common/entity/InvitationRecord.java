package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邀请记录表
 *
 * @TableName invitation_record
 */
@TableName(value = "invitation_record")
@Data
public class InvitationRecord implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "inviter_wx_id")
    private Long inviterWxId;

    @TableField(value = "invitee_wx_id")
    private Long inviteeWxId;

    @TableField(value = "is_verified")
    private Integer isVerified;

    @TableField(value = "is_register")
    private Integer isRegister;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
