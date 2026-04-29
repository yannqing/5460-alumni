package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校友商户列表返回 VO
 */
@Data
@Schema(name = "AlumniMerchantListVo", description = "校友商户列表返回对象")
public class AlumniMerchantListVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商户ID")
    private String merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户logo")
    private String logo;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "关联校友会信息")
    private AlumniAssociationSimpleVo alumniAssociation;

    @Schema(description = "最新优惠券（无则为null）")
    private LatestCouponVo latestCoupon;

    @Data
    @Schema(name = "AlumniAssociationSimpleVo", description = "简化校友会信息")
    public static class AlumniAssociationSimpleVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "校友会ID")
        private Long alumniAssociationId;

        @Schema(description = "校友会名称")
        private String associationName;
    }

    @Data
    @Schema(name = "LatestCouponVo", description = "最新优惠券信息")
    public static class LatestCouponVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "优惠券ID")
        private Long couponId;

        @Schema(description = "优惠券名称")
        private String couponName;

        @Schema(description = "优惠券类型：1-折扣券 2-满减券 3-礼品券")
        private Integer couponType;

        @Schema(description = "优惠券描述")
        private String couponDesc;

        @Schema(description = "优惠券图片")
        private String couponImage;

        @Schema(description = "有效开始时间")
        private LocalDateTime validStartTime;

        @Schema(description = "有效结束时间")
        private LocalDateTime validEndTime;

        @Schema(description = "状态：0-未发布 1-已发布 2-已结束 3-已下架")
        private Integer status;
    }
}
