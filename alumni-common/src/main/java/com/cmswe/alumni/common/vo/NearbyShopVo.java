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
 * 附近商铺VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "NearbyShopVo", description = "附近商铺信息返回VO")
public class NearbyShopVo implements Serializable {

    /**
     * 店铺ID
     */
    @Schema(description = "店铺ID")
    private String shopId;

    /**
     * 所属商户ID
     */
    @Schema(description = "所属商户ID")
    private String merchantId;

    /**
     * 店铺名称
     */
    @Schema(description = "店铺名称")
    private String shopName;

    /**
     * 店铺类型：1-总店 2-分店
     */
    @Schema(description = "店铺类型：1-总店 2-分店")
    private Integer shopType;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 区县
     */
    @Schema(description = "区县")
    private String district;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String address;

    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @Schema(description = "经度")
    private BigDecimal longitude;

    /**
     * 店铺电话
     */
    @Schema(description = "店铺电话")
    private String phone;

    /**
     * 营业时间
     */
    @Schema(description = "营业时间")
    private String businessHours;

    /**
     * 店铺图片URL数组
     */
    @Schema(description = "店铺图片URL数组")
    private String shopImages;

    /**
     * 店铺简介
     */
    @Schema(description = "店铺简介")
    private String description;

    /**
     * 状态：0-停业 1-营业中 2-装修中
     */
    @Schema(description = "状态：0-停业 1-营业中 2-装修中")
    private Integer status;

    /**
     * 校友会推荐：0-否 1-是
     */
    @Schema(description = "校友会推荐：0-否 1-是")
    private Integer isRecommended;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数")
    private String viewCount;

    /**
     * 点击次数
     */
    @Schema(description = "点击次数")
    private String clickCount;

    /**
     * 优惠券领取次数
     */
    @Schema(description = "优惠券领取次数")
    private String couponReceivedCount;

    /**
     * 优惠券核销次数
     */
    @Schema(description = "优惠券核销次数")
    private String couponVerifiedCount;

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
     * 距离（公里）
     */
    @Schema(description = "距离（公里）")
    private BigDecimal distance;

    /**
     * 优惠券列表
     */
    @Schema(description = "优惠券列表")
    private List<ShopCouponVo> coupons;

    @Serial
    private static final long serialVersionUID = 1L;
}
