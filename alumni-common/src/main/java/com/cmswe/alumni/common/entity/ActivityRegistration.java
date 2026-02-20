package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动报名表
 * @TableName activity_registration
 */
@TableName(value = "activity_registration")
@Data
public class ActivityRegistration implements Serializable {
    /**
     * 报名ID（自增）
     */
    @TableId(value = "registration_id", type = IdType.AUTO)
    private Long registrationId;

    /**
     * 活动ID
     */
    @TableField(value = "activity_id")
    private Long activityId;

    /**
     * 报名用户ID（wx_id）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 用户姓名（冗余字段）
     */
    @TableField(value = "user_name")
    private String userName;

    /**
     * 联系电话
     */
    @TableField(value = "user_phone")
    private String userPhone;

    /**
     * 报名时间
     */
    @TableField(value = "registration_time")
    private LocalDateTime registrationTime;

    /**
     * 报名状态：0-待审核 1-审核通过 2-审核拒绝 3-已取消
     */
    @TableField(value = "registration_status")
    private Integer registrationStatus;

    /**
     * 审核时间
     */
    @TableField(value = "audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核原因（拒绝时填写）
     */
    @TableField(value = "audit_reason")
    private String auditReason;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除（0-未删除 1-已删除）
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
