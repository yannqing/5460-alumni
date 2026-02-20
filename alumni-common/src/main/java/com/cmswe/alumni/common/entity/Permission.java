package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限表
 * @TableName permission
 */
@TableName(value = "permission")
@Data
public class Permission implements Serializable {
    /**
     * 权限ID
     */
    @TableId(value = "per_id", type = IdType.ASSIGN_ID)
    private Long perId;

    /**
     * 权限UUID
     */
    @TableField(value = "per_uuid")
    private String perUuid;

    /**
     * 该权限的父ID
     */
    @TableField(value = "pid")
    private Long pid;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 权限编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 类型：0代表菜单，1权限
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
     * 状态：0-禁用 1-启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 排序字段
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 逻辑删除：0代表未删除，1代表已删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}