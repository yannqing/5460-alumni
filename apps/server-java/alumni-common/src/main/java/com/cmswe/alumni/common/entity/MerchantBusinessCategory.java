package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户经营类目及范围表
 * @TableName merchant_business_category
 */
@TableName(value = "merchant_business_category")
@Data
public class MerchantBusinessCategory implements Serializable {
    /**
     * 主键id（雪花id）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 父级id（0表示一级类目，非0表示该类目下的经营范围）
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 分类名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 层级：1-经营类目 2-经营范围
     */
    @TableField(value = "level")
    private Integer level;

    /**
     * 排序权重
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
