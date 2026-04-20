package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户入驻校友会申请表
 * @TableName merchant_alumni_association_apply
 */
@TableName(value = "merchant_alumni_association_apply")
@Data
public class MerchantAlumniAssociationApply implements Serializable {
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 申请的商户 ID
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 目标校友会 ID
     */
    @TableField(value = "alumni_association_id")
    private Long alumniAssociationId;

    /**
     * 提交申请的操作人 ID
     */
    @TableField(value = "applicant_wx_id")
    private Long applicantWxId;

    /**
     * 审核状态（0-待审核, 1-已通过, 2-已拒绝, 3-已撤销）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 审核人
     */
    @TableField(value = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField(value = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    @TableField(value = "review_comment")
    private String reviewComment;

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
