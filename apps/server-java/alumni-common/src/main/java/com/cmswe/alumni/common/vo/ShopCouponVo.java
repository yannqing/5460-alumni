package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 店铺优惠券VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ShopCouponVo", description = "店铺优惠券信息返回VO")
public class ShopCouponVo implements Serializable {

    /**
     * 优惠券ID
     */
    @Schema(description = "优惠券ID")
    private String couponId;

    /**
     * 优惠券编码
     */
    @Schema(description = "优惠券编码")
    private String couponCode;

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
     * 优惠券描述
     */
    @Schema(description = "优惠券描述")
    private String couponDesc;

    /**
     * 优惠券图片URL
     */
    @Schema(description = "优惠券图片URL")
    private String couponImage;

    /**
     * 折扣类型：1-固定金额 2-折扣比例
     */
    @Schema(description = "折扣类型：1-固定金额 2-折扣比例")
    private Integer discountType;

    /**
     * 优惠值（折扣券=折扣如0.8表示8折，满减券=减免金额）
     */
    @Schema(description = "优惠值")
    private BigDecimal discountValue;

    /**
     * 最低消费金额（满减券使用）
     */
    @Schema(description = "最低消费金额")
    private BigDecimal minSpend;

    /**
     * 最高优惠金额（折扣券封顶金额）
     */
    @Schema(description = "最高优惠金额")
    private BigDecimal maxDiscount;

    /**
     * 剩余数量
     */
    @Schema(description = "剩余数量")
    private Integer remainQuantity;

    /**
     * 每人限领数量（0表示不限）
     */
    @Schema(description = "每人限领数量")
    private Integer perUserLimit;

    /**
     * 是否仅校友可领：0-否 1-是
     */
    @Schema(description = "是否仅校友可领：0-否 1-是")
    private Integer isAlumniOnly;

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
     * 状态：0-未发布 1-已发布 2-已结束 3-已下架
     */
    @Schema(description = "状态：0-未发布 1-已发布 2-已结束 3-已下架")
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;
}
