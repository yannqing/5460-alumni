package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券表（用户领取的优惠券）
 * 
 * @TableName user_coupon
 */
@TableName(value = "user_coupon")
@Data
public class UserCoupon implements Serializable {
    /**
     * 用户优惠券ID（雪花ID）
     */
    @TableId(value = "user_coupon_id", type = IdType.ASSIGN_ID)
    private Long userCouponId;

    /**
     * 优惠券ID
     */
    @TableField(value = "coupon_id")
    private Long couponId;

    /**
     * 用户ID（关联 wx_users.wx_id）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 商户ID
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 可用店铺ID（NULL表示全部门店）
     */
    @TableField(value = "shop_id")
    private Long shopId;

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
     * 优惠值
     */
    @TableField(value = "discount_value")
    private BigDecimal discountValue;

    /**
     * 最低消费金额
     */
    @TableField(value = "min_spend")
    private BigDecimal minSpend;

    /**
     * 领取时间
     */
    @TableField(value = "receive_time")
    private LocalDateTime receiveTime;

    /**
     * 领取渠道（APP/小程序/H5/活动页）
     */
    @TableField(value = "receive_channel")
    private String receiveChannel;

    /**
     * 领取来源（首页/店铺详情/活动）
     */
    @TableField(value = "receive_source")
    private String receiveSource;

    /**
     * 状态：1-未使用 2-已使用 3-已过期 4-已作废
     */
    @TableField(value = "status")
    private Integer status;

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
     * 使用时间
     */
    @TableField(value = "use_time")
    private LocalDateTime useTime;

    /**
     * 核销码（使用时生成）
     */
    @TableField(value = "verification_code")
    private String verificationCode;

    /**
     * 核销记录ID（关联 coupon_verification）
     */
    @TableField(value = "verification_id")
    private Long verificationId;

    /**
     * 核销码过期时间
     */
    @TableField(value = "verification_expire_time")
    private LocalDateTime verificationExpireTime;

    /**
     * 是否已发送过期提醒：0-否 1-是
     */
    @TableField(value = "expire_reminded")
    private Integer expireReminded;

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

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
