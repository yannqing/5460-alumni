package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校促会隐私设置
 * 
 * @TableName local_platform_privacy_setting
 */
@TableName(value = "local_platform_privacy_setting")
@Data
public class LocalPlatformPrivacySetting implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "local_platform_privacy_setting_id", type = IdType.ASSIGN_ID)
    private Long localPlatformPrivacySettingId;

    /**
     * 校促会ID
     */
    @TableField(value = "platform_id")
    private Long platformId;

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
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
