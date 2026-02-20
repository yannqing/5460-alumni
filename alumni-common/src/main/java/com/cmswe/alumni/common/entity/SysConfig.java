package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置表
 * @TableName sys_config
 */
@TableName(value ="sys_config")
@Data
public class SysConfig implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /**
     * 父级ID (0为顶级节点)
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 祖级列表 (如: 0,1,5, 方便层级查询)
     */
    @TableField(value = "ancestors")
    private String ancestors;

    /**
     * 配置名称 (中文显示, 如: 阿里云OSS配置)
     */
    @TableField(value = "config_name")
    private String configName;

    /**
     * 配置键 (全局唯一, 代码引用标识)
     */
    @TableField(value = "config_key")
    private String configKey;

    /**
     * 配置值 (仅叶子节点需要填值)
     */
    @TableField(value = "config_value")
    private String configValue;

    /**
     * 数据类型 (STRING, NUMBER, BOOL, JSON, GROUP)
     */
    @TableField(value = "data_type")
    private String dataType;

    /**
     * 是否内置系统配置 (1:是 0:否，系统级不可删除)
     */
    @TableField(value = "is_system")
    private Integer isSystem;

    /**
     * 状态 (1:正常 0:禁用)
     */
    @TableField(value = "status")
    private String status;

    /**
     * 备注/说明
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
     * 逻辑删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}