package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.Coupon;
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
 * 优惠券审批记录返回 VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CouponApprovalVo", description = "优惠券审批记录查询返回 VO")
public class CouponApprovalVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券ID")
    private String couponId;

    @Schema(description = "所属商户ID")
    private String merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "所属店铺ID")
    private String shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "优惠券编码")
    private String couponCode;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "优惠券类型：1-折扣券 2-满减券 3-礼品券")
    private Integer couponType;

    @Schema(description = "优惠券描述")
    private String couponDesc;

    @Schema(description = "优惠券图片URL")
    private String couponImage;

    @Schema(description = "折扣类型：1-固定金额 2-折扣比例")
    private Integer discountType;

    @Schema(description = "优惠值")
    private BigDecimal discountValue;

    @Schema(description = "最低消费金额")
    private BigDecimal minSpend;

    @Schema(description = "最高优惠金额")
    private BigDecimal maxDiscount;

    @Schema(description = "发行总量（-1表示不限量）")
    private Integer totalQuantity;

    @Schema(description = "剩余数量")
    private Integer remainQuantity;

    @Schema(description = "每人限领数量")
    private Integer perUserLimit;

    @Schema(description = "是否仅校友可领：0-否 1-是")
    private Integer isAlumniOnly;

    @Schema(description = "有效期开始时间")
    private LocalDateTime validStartTime;

    @Schema(description = "有效期结束时间")
    private LocalDateTime validEndTime;

    @Schema(description = "发布方式：1-立即发布 2-定时发布")
    private Integer publishType;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "状态：0-未发布 1-已发布 2-已结束 3-已下架")
    private Integer status;

    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败")
    private Integer reviewStatus;

    @Schema(description = "审核原因")
    private String reviewReason;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "已领取数量")
    private String receivedCount;

    @Schema(description = "已使用数量")
    private String usedCount;

    @Schema(description = "创建人ID")
    private String createdBy;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 对象转VO
     *
     * @param coupon 优惠券实体
     * @return CouponApprovalVo
     */
    public static CouponApprovalVo objToVo(Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        CouponApprovalVo vo = new CouponApprovalVo();
        BeanUtils.copyProperties(coupon, vo);
        // ID 转换为 String 避免精度丢失
        vo.setCouponId(String.valueOf(coupon.getCouponId()));
        vo.setMerchantId(String.valueOf(coupon.getMerchantId()));
        if (coupon.getShopId() != null) {
            vo.setShopId(String.valueOf(coupon.getShopId()));
        }
        if (coupon.getCreatedBy() != null) {
            vo.setCreatedBy(String.valueOf(coupon.getCreatedBy()));
        }
        if (coupon.getReviewerId() != null) {
            vo.setReviewerId(String.valueOf(coupon.getReviewerId()));
        }
        if (coupon.getReceivedCount() != null) {
            vo.setReceivedCount(String.valueOf(coupon.getReceivedCount()));
        }
        if (coupon.getUsedCount() != null) {
            vo.setUsedCount(String.valueOf(coupon.getUsedCount()));
        }
        return vo;
    }
}
