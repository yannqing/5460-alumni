package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户隐私设置表
 * @TableName user_privacy_setting
 */
@TableName(value = "user_privacy_setting")
@Data
public class UserPrivacySetting implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "user_privacy_setting_id", type = IdType.ASSIGN_ID)
    private Long userPrivacySettingId;

    /**
     * 用户ID
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 字段名称
     */
    @TableField(value = "field_name")
    private String fieldName;

    /**
     * 字段代码
     */
    @TableField(value = "field_code")
    private String fieldCode;

    /**
     * 可见性: 0 不可见；1 可见
     */
    @TableField(value = "visibility")
    private Integer visibility;

    /**
     * 是否可被搜索：0-否，1-是
     */
    @TableField(value = "searchable")
    private Integer searchable;

    /**
     * 用户隐私的类型（1用户个人信息，2用户的企业信息，3用户的校友场所信息，4用户的校友会信息）
     */
    @TableField(value = "type")
    private Integer type;

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