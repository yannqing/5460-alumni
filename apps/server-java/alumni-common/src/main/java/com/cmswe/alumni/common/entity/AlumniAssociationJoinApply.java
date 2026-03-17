package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校友会申请加入校促会申请表
 */
@TableName(value = "alumni_association_join_apply")
@Data
public class AlumniAssociationJoinApply implements Serializable {
    /**
     * 校友会申请加入校促会住主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 校友会ID
     */
    @TableField(value = "alumni_association_id")
    private Long alumniAssociationId;

    /**
     * 校促会ID
     */
    @TableField(value = "platform_id")
    private Long platformId;

    /**
     * 申请人wx_id
     */
    @TableField(value = "applicant_wx_id")
    private Long applicantWxId;

    /**
     * 审核状态(0待审核,1已通过,2已拒绝)
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
     * 逻辑删除：0-未删除 1-已删除
     */
    @JsonIgnore
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
