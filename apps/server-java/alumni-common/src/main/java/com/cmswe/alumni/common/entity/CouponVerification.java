package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券核销记录表（核销历史记录）
 * @TableName coupon_verification
 */
@TableName(value = "coupon_verification")
@Data
public class CouponVerification implements Serializable {
    /**
     * 核销记录ID（雪花ID）
     */
    @TableId(value = "verification_id", type = IdType.ASSIGN_ID)
    private Long verificationId;

    /**
     * 用户优惠券ID
     */
    @TableField(value = "user_coupon_id")
    private Long userCouponId;

    /**
     * 优惠券ID
     */
    @TableField(value = "coupon_id")
    private Long couponId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 商户ID
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 核销店铺ID
     */
    @TableField(value = "shop_id")
    private Long shopId;

    /**
     * 核销码
     */
    @TableField(value = "verification_code")
    private String verificationCode;

    /**
     * 核销时间
     */
    @TableField(value = "verification_time")
    private LocalDateTime verificationTime;

    /**
     * 核销人ID（店员的 user_id）
     */
    @TableField(value = "verifier_id")
    private Long verifierId;

    /**
     * 核销人姓名
     */
    @TableField(value = "verifier_name")
    private String verifierName;

    /**
     * 订单金额
     */
    @TableField(value = "order_amount")
    private BigDecimal orderAmount;

    /**
     * 优惠金额
     */
    @TableField(value = "discount_amount")
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    @TableField(value = "actual_amount")
    private BigDecimal actualAmount;

    /**
     * 核销方式：1-扫码 2-输入卡号
     */
    @TableField(value = "verification_method")
    private Integer verificationMethod;

    /**
     * 核销设备信息
     */
    @TableField(value = "device_info")
    private String deviceInfo;

    /**
     * IP地址
     */
    @TableField(value = "ip_address")
    private String ipAddress;

    /**
     * 风险等级：0-正常 1-低风险 2-中风险 3-高风险
     */
    @TableField(value = "risk_level")
    private Integer riskLevel;

    /**
     * 风险原因（如：异常时间、异常地点、短时间多次核销）
     */
    @TableField(value = "risk_reason")
    private String riskReason;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
