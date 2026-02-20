package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.Coupon;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CouponVo", description = "优惠券信息返回VO")
public class CouponVo implements Serializable {

    /**
     * 优惠券ID
     */
    @Schema(description = "优惠券ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponId;

    /**
     * 所属商户ID
     */
    @Schema(description = "所属商户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long merchantId;

    /**
     * 所属店铺ID
     */
    @Schema(description = "所属店铺ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shopId;

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
     * 最高优惠金额
     */
    @Schema(description = "最高优惠金额")
    private BigDecimal maxDiscount;

    /**
     * 发行总量
     */
    @Schema(description = "发行总量")
    private Integer totalQuantity;

    /**
     * 剩余数量
     */
    @Schema(description = "剩余数量")
    private Integer remainQuantity;

    /**
     * 每人限领数量
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
     * 使用时段限制
     */
    @Schema(description = "使用时段限制")
    private String useTimeLimit;

    /**
     * 发布方式：1-立即发布 2-定时发布
     */
    @Schema(description = "发布方式：1-立即发布 2-定时发布")
    private Integer publishType;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    /**
     * 状态：0-未发布 1-已发布 2-已结束 3-已下架
     */
    @Schema(description = "状态：0-未发布 1-已发布 2-已结束 3-已下架")
    private Integer status;

    /**
     * 已领取数量
     */
    @Schema(description = "已领取数量")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receivedCount;

    /**
     * 已使用数量
     */
    @Schema(description = "已使用数量")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long usedCount;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long viewCount;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 将 Coupon 实体转换为 CouponVo
     *
     * @param coupon 优惠券实体
     * @return CouponVo
     */
    public static CouponVo objToVo(Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        CouponVo vo = new CouponVo();
        BeanUtils.copyProperties(coupon, vo);
        return vo;
    }
}
