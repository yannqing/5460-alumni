package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织架构角色表
 * @TableName organize_archi_role
 */
@TableName(value = "organize_archi_role")
@Data
public class OrganizeArchiRole implements Serializable {
    /**
     * 架构角色id
     */
    @TableId(value = "role_or_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleOrId;

    /**
     * 父id
     */
    @TableField(value = "pid")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    /**
     * 组织类型（0校友会，1校处会，2商户）
     */
    @TableField(value = "organize_type")
    private Integer organizeType;

    /**
     * 组织id
     */
    @TableField(value = "organize_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long organizeId;

    /**
     * 角色名
     */
    @TableField(value = "role_or_name")
    private String roleOrName;

    /**
     * 角色唯一代码
     */
    @TableField(value = "role_or_code")
    private String roleOrCode;

    /**
     * 角色含义
     */
    @TableField(value = "remark")
    private String remark;

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
