package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校友信息表
 * @TableName alumni_info
 */
@TableName(value = "alumni_info")
@Data
public class AlumniInfo implements Serializable {
    /**
     * 校友ID
     */
    @TableId(value = "alumni_id", type = IdType.AUTO)
    private Long alumniId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 真实姓名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 身份证号
     */
    @TableField(value = "id_card")
    private String idCard;

    /**
     * 认证状态：0-未认证 1-已认证 2-认证中 3-认证失败
     */
    @TableField(value = "certification_status")
    private Integer certificationStatus;

    /**
     * 邀请码
     */
    @TableField(value = "invite_code")
    private String inviteCode;

    /**
     * 隐私设置
     */
    @TableField(value = "privacy_settings")
    private String privacySettings;

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
     * 逻辑删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}