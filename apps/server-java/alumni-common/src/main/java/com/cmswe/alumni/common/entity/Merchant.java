package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户表
 * @TableName merchant
 */
@TableName(value = "merchant")
@Data
public class Merchant implements Serializable {
    /**
     * 商户ID（雪花ID）
     */
    @TableId(value = "merchant_id", type = IdType.ASSIGN_ID)
    private Long merchantId;

    /**
     * 关联用户ID（商户所有者的 wx_id）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 商户名称
     */
    @TableField(value = "merchant_name")
    private String merchantName;

    /**
     * 商户类型：1-校友商铺 2-普通商铺
     */
    @TableField(value = "merchant_type")
    private Integer merchantType;

    /**
     * 营业执照URL
     */
    @TableField(value = "business_license")
    private String businessLicense;

    /**
     * 统一社会信用代码
     */
    @TableField(value = "unified_social_credit_code")
    private String unifiedSocialCreditCode;

    /**
     * 法人姓名
     */
    @TableField(value = "legal_person")
    private String legalPerson;

    /**
     * 法人身份证号（AES加密存储）
     */
    @TableField(value = "legal_person_id")
    private String legalPersonId;

    /**
     * 联系电话
     */
    @TableField(value = "contact_phone")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @TableField(value = "contact_email")
    private String contactEmail;

    /**
     * 经营范围
     */
    @TableField(value = "business_scope")
    private String businessScope;

    /**
     * 经营类目（餐饮/酒店/零售/服务等）
     */
    @TableField(value = "business_category")
    private String businessCategory;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @TableField(value = "review_status")
    private Integer reviewStatus;

    /**
     * 审核原因（驳回时填写）
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
     * 会员等级：1-基础版 2-标准版 3-专业版 4-旗舰版
     */
    @TableField(value = "member_tier")
    private Integer memberTier;

    /**
     * 会员等级到期时间
     */
    @TableField(value = "tier_expire_time")
    private LocalDateTime tierExpireTime;

    /**
     * 累计缴费金额
     */
    @TableField(value = "total_paid_amount")
    private BigDecimal totalPaidAmount;

    /**
     * 状态：0-禁用 1-启用 2-已注销
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 门店数量（冗余统计）
     */
    @TableField(value = "shop_count")
    private Integer shopCount;

    /**
     * 累计发放优惠券数量
     */
    @TableField(value = "total_coupon_issued")
    private Long totalCouponIssued;

    /**
     * 累计核销优惠券数量
     */
    @TableField(value = "total_coupon_verified")
    private Long totalCouponVerified;

    /**
     * 商户评分（0-5分）
     */
    @TableField(value = "rating_score")
    private BigDecimal ratingScore;

    /**
     * 评价数量
     */
    @TableField(value = "rating_count")
    private Integer ratingCount;

    /**
     * 是否校友认证：0-否 1-是
     */
    @TableField(value = "is_alumni_certified")
    private Integer isAlumniCertified;

    /**
     * 关联校友会ID
     */
    @TableField(value = "alumni_association_id")
    private Long alumniAssociationId;

    /**
     * 认证通过时间
     */
    @TableField(value = "certified_time")
    private LocalDateTime certifiedTime;

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
