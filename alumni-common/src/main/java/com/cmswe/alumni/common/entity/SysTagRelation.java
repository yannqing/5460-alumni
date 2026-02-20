package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签关联关系表
 * @TableName sys_tag_relations
 */
@TableName(value = "sys_tag_relations")
@Data
public class SysTagRelation implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "tag_rel_id", type = IdType.ASSIGN_ID)
    private Long tagRelId;

    /**
     * 标签ID
     */
    @TableField(value = "tag_id")
    private Long tagId;

    /**
     * 目标对象ID (用户ID/商户ID/内容ID)
     */
    @TableField(value = "target_id")
    private Long targetId;

    /**
     * 目标类型: 1-校友(User), 2-商户(Shop), 3-活动(Event)
     */
    @TableField(value = "target_type")
    private Integer targetType;

    /**
     * 创建时间 (打标签的时间)
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 操作人 (谁打的标签)
     */
    @TableField(value = "create_by")
    private String createBy;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
