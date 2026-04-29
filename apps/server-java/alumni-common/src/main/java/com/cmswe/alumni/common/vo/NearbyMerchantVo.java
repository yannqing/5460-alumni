package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 附近商户VO（用于发现页附近优惠模块，按商户维度展示）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "NearbyMerchantVo", description = "附近商户信息返回VO")
public class NearbyMerchantVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商户ID
     */
    @Schema(description = "商户ID")
    private String merchantId;

    /**
     * 商户名称
     */
    @Schema(description = "商户名称")
    private String merchantName;

    /**
     * 商户logo（图片URL）
     */
    @Schema(description = "商户logo")
    private String logo;

    /**
     * 店铺评分（0-5分）
     */
    @Schema(description = "店铺评分（0-5分）")
    private BigDecimal ratingScore;

    /**
     * 评价数量
     */
    @Schema(description = "评价数量")
    private Integer ratingCount;

    /**
     * 距离（公里）：用户到该商户最近门店的距离
     */
    @Schema(description = "距离（公里）")
    private BigDecimal distance;

    /**
     * 商户下门店数量
     */
    @Schema(description = "商户下门店数量")
    private Integer shopCount;

    /**
     * 收藏数量
     */
    @Schema(description = "收藏数量")
    private Long favoriteCount;

    /**
     * 优惠券列表（商户专属优惠券，不属于任何门店）
     */
    @Schema(description = "优惠券列表")
    private List<ShopCouponVo> coupons;

    /**
     * 活动列表（商户发布的活动）
     */
    @Schema(description = "活动列表")
    private List<ActivityItem> activities;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "ActivityItem", description = "活动简要信息")
    public static class ActivityItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "活动ID")
        private Long activityId;

        @Schema(description = "活动标题")
        private String activityTitle;

        @Schema(description = "活动类型：1-优惠活动 2-话题活动")
        private Integer activityType;
    }
}
