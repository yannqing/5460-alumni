package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 店铺表（门店基本信息及地理位置）
 * @TableName shop
 */
@TableName(value = "shop")
@Data
public class Shop implements Serializable {
    /**
     * 店铺ID（雪花ID）
     */
    @TableId(value = "shop_id", type = IdType.ASSIGN_ID)
    private Long shopId;

    /**
     * 所属商户ID
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 店铺名称
     */
    @TableField(value = "shop_name")
    private String shopName;

    /**
     * 店铺类型：1-总店 2-分店
     */
    @TableField(value = "shop_type")
    private Integer shopType;

    /**
     * 省份
     */
    @TableField(value = "province")
    private String province;

    /**
     * 城市
     */
    @TableField(value = "city")
    private String city;

    /**
     * 区县
     */
    @TableField(value = "district")
    private String district;

    /**
     * 详细地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * 纬度（-90 ~ 90）
     */
    @TableField(value = "latitude")
    private BigDecimal latitude;

    /**
     * 经度（-180 ~ 180）
     */
    @TableField(value = "longitude")
    private BigDecimal longitude;

    /**
     * GeoHash编码（用于快速地理位置查询）
     */
    @TableField(value = "geohash")
    private String geohash;

    /**
     * 店铺电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 营业时间（如：周一至周日 09:00-22:00）
     */
    @TableField(value = "business_hours")
    private String businessHours;

    /**
     * 店铺图片URL数组（JSON格式：["url1","url2"]）
     */
    @TableField(value = "shop_images")
    private String shopImages;

    /**
     * 店铺简介
     */
    @TableField(value = "description")
    private String description;

    /**
     * 状态：0-停业 1-营业中 2-装修中
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @TableField(value = "review_status")
    private Integer reviewStatus;

    /**
     * 审核原因（审核不通过时填写）
     */
    @TableField(value = "review_reason")
    private String reviewReason;

    /**
     * 审核人ID
     */
    @TableField(value = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField(value = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 校友会推荐：0-否 1-是（需要校处会审核）
     */
    @TableField(value = "is_recommended")
    private Integer isRecommended;

    /**
     * 浏览次数
     */
    @TableField(value = "view_count")
    private Long viewCount;

    /**
     * 点击次数
     */
    @TableField(value = "click_count")
    private Long clickCount;

    /**
     * 优惠券领取次数
     */
    @TableField(value = "coupon_received_count")
    private Long couponReceivedCount;

    /**
     * 优惠券核销次数
     */
    @TableField(value = "coupon_verified_count")
    private Long couponVerifiedCount;

    /**
     * 店铺评分（0-5分）
     */
    @TableField(value = "rating_score")
    private BigDecimal ratingScore;

    /**
     * 评价数量
     */
    @TableField(value = "rating_count")
    private Integer ratingCount;

    /**
     * 创建人（店长/总管理员的 user_id）
     */
    @TableField(value = "created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除（0-未删除 1-已删除）
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
