package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 地方校处会成员关系表
 * 
 * @TableName local_platform_member
 */
@TableName(value = "local_platform_member")
@Data
public class LocalPlatformMember implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 校友ID
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 是否是架构成员：0-否，1-是
     */
    @TableField(value = "is_nu")
    private Integer isNu;

    /**
     * 校处会ID
     */
    @TableField(value = "local_platform_id")
    private Long localPlatformId;

    /**
     * 成员角色
     */
    @TableField(value = "role_or_id")
    private Long roleOrId;

    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    private String roleName;

    /**
     * 加入时间
     */
    @TableField(value = "join_time")
    private LocalDateTime joinTime;

    /**
     * 状态：0-退出 1-正常
     */
    @TableField(value = "status")
    private Integer status;

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
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
