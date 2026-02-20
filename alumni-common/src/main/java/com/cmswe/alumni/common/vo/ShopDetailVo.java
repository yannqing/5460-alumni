package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商铺详情VO
 *
 * @author CNI Alumni System
 * @since 2025-12-25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ShopDetailVo", description = "商铺详情信息返回VO")
public class ShopDetailVo implements Serializable {

    /**
     * 店铺ID
     */
    @Schema(description = "店铺ID")
    private String shopId;

    /**
     * 所属商户信息
     */
    @Schema(description = "所属商户信息")
    private MerchantListVo merchant;

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
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败")
    private Integer reviewStatus;

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
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 优惠券列表
     */
    @Schema(description = "优惠券列表")
    private List<ShopCouponVo> coupons;

    /**
     * 活动列表
     */
    @Schema(description = "活动列表")
    private List<ActivityListVo> activities;

    @Serial
    private static final long serialVersionUID = 1L;
}
