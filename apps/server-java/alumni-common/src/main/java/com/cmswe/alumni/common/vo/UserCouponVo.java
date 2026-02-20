package com.cmswe.alumni.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UserCouponVo", description = "用户优惠券信息返回VO")
public class UserCouponVo implements Serializable {

    /**
     * 用户优惠券ID
     */
    @Schema(description = "用户优惠券ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userCouponId;

    /**
     * 优惠券详细信息
     */
    @Schema(description = "优惠券详细信息")
    private CouponVo coupon;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 商户ID
     */
    @Schema(description = "商户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long merchantId;

    /**
     * 可用店铺ID
     */
    @Schema(description = "可用店铺ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shopId;

    /**
     * 优惠券名称
     */
    @Schema(description = "优惠券名称")
    private String couponName;

    /**
     * 优惠券类型：1-折扣券 2-满减券 3-礼品券
     */
    @Schema(description = "优惠券类型：1-折扣券 2-满减券 3-礼品券")
    private Integer couponType;

    /**
     * 优惠值
     */
    @Schema(description = "优惠值")
    private BigDecimal discountValue;

    /**
     * 最低消费金额
     */
    @Schema(description = "最低消费金额")
    private BigDecimal minSpend;

    /**
     * 领取时间
     */
    @Schema(description = "领取时间")
    private LocalDateTime receiveTime;

    /**
     * 状态：1-未使用 2-已使用 3-已过期 4-已作废
     */
    @Schema(description = "状态：1-未使用 2-已使用 3-已过期 4-已作废")
    private Integer status;

    /**
     * 有效期开始时间
     */
    @Schema(description = "有效期开始时间")
    private LocalDateTime validStartTime;

    /**
     * 有效期结束时间
     */
    @Schema(description = "有效期结束时间")
    private LocalDateTime validEndTime;

    /**
     * 核销码
     */
    @Schema(description = "核销码")
    private String verificationCode;

    /**
     * 核销码过期时间
     */
    @Schema(description = "核销码过期时间")
    private LocalDateTime verificationExpireTime;

    /**
     * 核销码二维码（Base64）
     */
    @Schema(description = "核销码二维码（Base64）")
    private String base64CodeImg;

    @Serial
    private static final long serialVersionUID = 1L;
}
