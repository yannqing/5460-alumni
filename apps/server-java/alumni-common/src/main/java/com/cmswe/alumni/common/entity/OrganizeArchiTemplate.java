package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织架构模板表
 * @TableName organize_archi_template
 */
@TableName(value = "organize_archi_template")
@Data
public class OrganizeArchiTemplate implements Serializable {
    /**
     * 模板ID
     */
    @TableId(value = "template_id", type = IdType.ASSIGN_ID)
    private Long templateId;

    /**
     * 模板名称
     */
    @TableField(value = "template_name")
    private String templateName;

    /**
     * 模板唯一代码
     */
    @TableField(value = "template_code")
    private String templateCode;

    /**
     * 适用组织类型（0校友会，1校处会，2商户）
     */
    @TableField(value = "organize_type")
    private Integer organizeType;

    /**
     * 模板内容（JSON格式，包含树形结构）
     */
    @TableField(value = "template_json")
    private String templateJson;

    /**
     * 模板描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 是否默认模板：0-否，1-是
     */
    @TableField(value = "is_default")
    private Integer isDefault;

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
