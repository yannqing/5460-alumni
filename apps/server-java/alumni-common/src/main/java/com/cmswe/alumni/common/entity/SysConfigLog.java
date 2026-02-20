package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置变更日志表
 * @TableName sys_config_log
 */
@TableName(value ="sys_config_log")
@Data
public class SysConfigLog implements Serializable {
    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 关联的配置ID
     */
    @TableField(value = "config_id")
    private Long configId;

    /**
     * 冗余配置键 (方便直接查询)
     */
    @TableField(value = "config_key")
    private String configKey;

    /**
     * 冗余配置名称
     */
    @TableField(value = "config_name")
    private String configName;

    /**
     * 修改前的值 (变更前快照)
     */
    @TableField(value = "old_value")
    private String oldValue;

    /**
     * 修改后的值
     */
    @TableField(value = "new_value")
    private String newValue;

    /**
     * 操作者IP地址
     */
    @TableField(value = "operator_ip")
    private String operatorIp;

    /**
     * 操作者id
     */
    @TableField(value = "operator_id")
    private Long operatorId;

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