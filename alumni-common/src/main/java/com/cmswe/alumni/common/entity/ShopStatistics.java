package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 店铺统计表（按天统计店铺运营数据）
 * @TableName shop_statistics
 */
@TableName(value = "shop_statistics")
@Data
public class ShopStatistics implements Serializable {
    /**
     * 统计ID（雪花ID）
     */
    @TableId(value = "stat_id", type = IdType.ASSIGN_ID)
    private Long statId;

    /**
     * 店铺ID
     */
    @TableField(value = "shop_id")
    private Long shopId;

    /**
     * 商户ID
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 统计日期（YYYY-MM-DD）
     */
    @TableField(value = "stat_date")
    private LocalDate statDate;

    /**
     * 曝光次数（展示在列表/地图中）
     */
    @TableField(value = "exposure_count")
    private Long exposureCount;

    /**
     * 点击次数（进入店铺详情）
     */
    @TableField(value = "click_count")
    private Long clickCount;

    /**
     * 点击率（click_count / exposure_count）
     */
    @TableField(value = "click_rate")
    private BigDecimal clickRate;

    /**
     * 优惠券浏览次数
     */
    @TableField(value = "coupon_view_count")
    private Long couponViewCount;

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
     * 优惠券转化率（verified / received）
     */
    @TableField(value = "coupon_conversion_rate")
    private BigDecimal couponConversionRate;

    /**
     * 独立访客数（UV）
     */
    @TableField(value = "unique_visitor_count")
    private Integer uniqueVisitorCount;

    /**
     * 新用户数（首次访问）
     */
    @TableField(value = "new_user_count")
    private Integer newUserCount;

    /**
     * 校友访客数
     */
    @TableField(value = "alumni_visitor_count")
    private Integer alumniVisitorCount;

    /**
     * 总订单金额（估算）
     */
    @TableField(value = "total_order_amount")
    private BigDecimal totalOrderAmount;

    /**
     * 总优惠金额
     */
    @TableField(value = "total_discount_amount")
    private BigDecimal totalDiscountAmount;

    /**
     * 总实付金额（估算）
     */
    @TableField(value = "total_actual_amount")
    private BigDecimal totalActualAmount;

    /**
     * 城市排名
     */
    @TableField(value = "city_ranking")
    private Integer cityRanking;

    /**
     * 同类目排名
     */
    @TableField(value = "category_ranking")
    private Integer categoryRanking;

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

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
