package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券表（优惠券主表）
 * @TableName coupon
 */
@TableName(value = "coupon")
@Data
public class Coupon implements Serializable {
    /**
     * 优惠券ID（雪花ID）
     */
    @TableId(value = "coupon_id", type = IdType.ASSIGN_ID)
    private Long couponId;

    /**
     * 所属商户ID
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 所属店铺ID（NULL表示全部门店可用）
     */
    @TableField(value = "shop_id")
    private Long shopId;

    /**
     * 优惠券编码（唯一，用于核销）
     */
    @TableField(value = "coupon_code")
    private String couponCode;

    /**
     * 优惠券名称
     */
    @TableField(value = "coupon_name")
    private String couponName;

    /**
     * 优惠券类型：1-折扣券 2-满减券 3-礼品券
     */
    @TableField(value = "coupon_type")
    private Integer couponType;

    /**
     * 优惠券描述
     */
    @TableField(value = "coupon_desc")
    private String couponDesc;

    /**
     * 优惠券图片URL
     */
    @TableField(value = "coupon_image")
    private String couponImage;

    /**
     * 折扣类型：1-固定金额 2-折扣比例（coupon_type=1时使用）
     */
    @TableField(value = "discount_type")
    private Integer discountType;

    /**
     * 优惠值（折扣券=折扣如0.8表示8折，满减券=减免金额）
     */
    @TableField(value = "discount_value")
    private BigDecimal discountValue;

    /**
     * 最低消费金额（满减券使用）
     */
    @TableField(value = "min_spend")
    private BigDecimal minSpend;

    /**
     * 最高优惠金额（折扣券封顶金额）
     */
    @TableField(value = "max_discount")
    private BigDecimal maxDiscount;

    /**
     * 发行总量（-1表示不限量）
     */
    @TableField(value = "total_quantity")
    private Integer totalQuantity;

    /**
     * 剩余数量（实时扣减，Redis缓存）
     */
    @TableField(value = "remain_quantity")
    private Integer remainQuantity;

    /**
     * 每人限领数量（0表示不限）
     */
    @TableField(value = "per_user_limit")
    private Integer perUserLimit;

    /**
     * 是否仅校友可领：0-否 1-是
     */
    @TableField(value = "is_alumni_only")
    private Integer isAlumniOnly;

    /**
     * 有效期开始时间
     */
    @TableField(value = "valid_start_time")
    private LocalDateTime validStartTime;

    /**
     * 有效期结束时间
     */
    @TableField(value = "valid_end_time")
    private LocalDateTime validEndTime;

    /**
     * 使用时段限制（如：仅工作日可用）
     */
    @TableField(value = "use_time_limit")
    private String useTimeLimit;

    /**
     * 发布方式：1-立即发布 2-定时发布
     */
    @TableField(value = "publish_type")
    private Integer publishType;

    /**
     * 发布时间（定时发布时使用）
     */
    @TableField(value = "publish_time")
    private LocalDateTime publishTime;

    /**
     * 状态：0-未发布 1-已发布 2-已结束 3-已下架
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @TableField(value = "review_status")
    private Integer reviewStatus;

    /**
     * 审核原因（审核不通过时填写）
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
     * 已领取数量
     */
    @TableField(value = "received_count")
    private Long receivedCount;

    /**
     * 已使用数量
     */
    @TableField(value = "used_count")
    private Long usedCount;

    /**
     * 浏览次数
     */
    @TableField(value = "view_count")
    private Long viewCount;

    /**
     * 创建人（店长/管理员的 user_id）
     */
    @TableField(value = "created_by")
    private Long createdBy;

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
