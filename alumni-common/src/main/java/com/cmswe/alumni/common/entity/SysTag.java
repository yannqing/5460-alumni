package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统标签定义表
 * @TableName sys_tags
 */
@TableName(value = "sys_tags")
@Data
public class SysTag implements Serializable {

    /**
     * 主键ID(雪花算法)
     */
    @TableId(value = "tag_id", type = IdType.ASSIGN_ID)
    private Long tagId;

    /**
     * 父标签ID (0为顶级)
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 唯一业务代码 (如: IND_IT, GEN_00)
     */
    @TableField(value = "code")
    private String code;

    /**
     * 标签名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 标签背景色 (HEX格式 #xxxxxx)
     */
    @TableField(value = "bg_color")
    private String bgColor;

    /**
     * 标签文字色 (HEX格式 #xxxxxx)
     */
    @TableField(value = "text_color")
    private String textColor;

    /**
     * 标签分类: 1-通用, 2-用户画像, 3-商户类型, 4-行业领域
     */
    @TableField(value = "category")
    private Integer category;

    /**
     * 标签图标URL(可选)
     */
    @TableField(value = "icon_url")
    private String iconUrl;

    /**
     * 显示排序(数值越小越靠前)
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 备注描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创建者
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @TableField(value = "update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
