package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 地方校处会表
 * @TableName local_platform
 */
@TableName(value = "local_platform")
@Data
public class LocalPlatform implements Serializable {
    /**
     * 校处会ID
     */
    @TableId(value = "platform_id", type = IdType.ASSIGN_ID)
    private Long platformId;

    /**
     * 校处会名称
     */
    @TableField(value = "platform_name")
    private String platformName;

    /**
     * 校处会头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 所在城市
     */
    @TableField(value = "city")
    private String city;

    /**
     * 管辖范围
     */
    @TableField(value = "scope")
    private String scope;

    /**
     * 联系信息
     */
    @TableField(value = "contact_info")
    private String contactInfo;

    /**
     * 简介
     */
    @TableField(value = "description")
    private String description;

    /**
     * 背景图片
     */
    @TableField(value = "bg_img")
    private String bgImg;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField(value = "status")
    private Integer status;

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