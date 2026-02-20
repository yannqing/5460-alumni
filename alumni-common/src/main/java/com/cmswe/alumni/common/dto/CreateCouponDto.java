package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建优惠券请求DTO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CreateCouponDto", description = "创建优惠券请求DTO")
public class CreateCouponDto implements Serializable {

    /**
     * 所属商户ID
     */
    @NotNull(message = "商户ID不能为空")
    @Schema(description = "所属商户ID", required = true)
    private Long merchantId;

    /**
     * 所属店铺ID（NULL表示全部门店可用）
     */
    @Schema(description = "所属店铺ID（NULL表示全部门店可用）")
    private Long shopId;

    /**
     * 优惠券名称
     */
    @NotBlank(message = "优惠券名称不能为空")
    @Schema(description = "优惠券名称", required = true)
    private String couponName;

    /**
     * 优惠券类型：1-折扣券 2-满减券 3-礼品券
     */
    @NotNull(message = "优惠券类型不能为空")
    @Schema(description = "优惠券类型：1-折扣券 2-满减券 3-礼品券", required = true)
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
     * 折扣类型：1-固定金额 2-折扣比例（coupon_type=1时使用）
     */
    @Schema(description = "折扣类型：1-固定金额 2-折扣比例")
    private Integer discountType;

    /**
     * 优惠值（折扣券=折扣如0.8表示8折，满减券=减免金额）
     */
    @NotNull(message = "优惠值不能为空")
    @Schema(description = "优惠值", required = true)
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
     * 发行总量（-1表示不限量）
     */
    @NotNull(message = "发行总量不能为空")
    @Schema(description = "发行总量（-1表示不限量）", required = true)
    private Integer totalQuantity;

    /**
     * 每人限领数量（0表示不限）
     */
    @Schema(description = "每人限领数量（0表示不限）", defaultValue = "1")
    private Integer perUserLimit;

    /**
     * 是否仅校友可领：0-否 1-是
     */
    @Schema(description = "是否仅校友可领：0-否 1-是", defaultValue = "0")
    private Integer isAlumniOnly;

    /**
     * 有效期开始时间
     */
    @NotNull(message = "有效期开始时间不能为空")
    @Schema(description = "有效期开始时间", required = true)
    private LocalDateTime validStartTime;

    /**
     * 有效期结束时间
     */
    @NotNull(message = "有效期结束时间不能为空")
    @Schema(description = "有效期结束时间", required = true)
    private LocalDateTime validEndTime;

    /**
     * 使用时段限制（如：仅工作日可用）
     */
    @Schema(description = "使用时段限制")
    private String useTimeLimit;

    /**
     * 发布方式：1-立即发布 2-定时发布
     */
    @NotNull(message = "发布方式不能为空")
    @Schema(description = "发布方式：1-立即发布 2-定时发布", required = true, defaultValue = "1")
    private Integer publishType;

    /**
     * 发布时间（定时发布时使用）
     */
    @Schema(description = "发布时间（定时发布时使用）")
    private LocalDateTime publishTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
