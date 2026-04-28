package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户创建申请表
 * @TableName merchant_application
 */
@TableName(value = "merchant_application")
@Data
public class MerchantApplication implements Serializable {
    /**
     * 申请ID（雪花ID）
     */
    @TableId(value = "application_id", type = IdType.ASSIGN_ID)
    private Long applicationId;

    /**
     * 申请者wxid（关联用户ID）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 商户名称
     */
    @TableField(value = "merchant_name")
    private String merchantName;

    /**
     * 法人姓名
     */
    @TableField(value = "legal_person")
    private String legalPerson;

    /**
     * 法人电话号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 统一社会信用代码
     */
    @TableField(value = "unified_social_credit_code")
    private String unifiedSocialCreditCode;

    /**
     * 所在城市
     */
    @TableField(value = "city")
    private String city;

    /**
     * 申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销 4-待发布
     */
    @TableField(value = "review_status")
    private Integer reviewStatus;

    /**
     * 审核原因
     */
    @TableField(value = "review_reason")
    private String reviewReason;

    /**
     * 审核人ID
     */
    @TableField(value = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField(value = "review_time")
    private LocalDateTime reviewTime;

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
     * 逻辑删除（0-未删除 1-已删除）
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
