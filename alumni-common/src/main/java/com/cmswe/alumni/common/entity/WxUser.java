package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 微信用户表
 * @TableName wx_users
 */
@TableName(value = "wx_users")
@Data
public class WxUser implements Serializable {
    /**
     * 用户雪花id
     */
    @TableId(value = "wx_id", type = IdType.ASSIGN_ID)
    private Long wxId;

    /**
     * 微信openid
     */
    @TableField(value = "openid")
    private String openid;

    /**
     * 微信unionid
     */
    @TableField(value = "union_id")
    private String unionId;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 纬度
     */
    @TableField(value = "latitude")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @TableField(value = "longitude")
    private BigDecimal longitude;

    /**
     * 上一次登录时间
     */
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 上一次登录ip
     */
    @TableField(value = "last_login_ip")
    private String lastLoginIp;

    /**
     * 账户是否可用 TODO 测试如果用户被禁用，返回结果是什么
     */
    @TableField(value = "is_enabled")
    private Integer isEnabled;

    /**
     * 是否成为校友
     */
    @TableField(value = "is_alumni")
    private Integer isAlumni;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private LocalDateTime updatedTime;

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
