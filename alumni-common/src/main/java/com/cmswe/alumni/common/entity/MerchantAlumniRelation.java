package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户与校友关联表
 * @TableName merchant_alumni_relation
 */
@TableName(value = "merchant_alumni_relation")
@Data
public class MerchantAlumniRelation implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户ID
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 校友ID
     */
    @TableField(value = "alumni_id")
    private Long alumniId;

    /**
     * 关系类型：owner-所有者, manager-管理者
     */
    @TableField(value = "relation_type")
    private String relationType;

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