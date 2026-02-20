package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户会员等级配置表（会员等级权益配置）
 * @TableName merchant_tier_config
 */
@TableName(value = "merchant_tier_config")
@Data
public class MerchantTierConfig implements Serializable {
    /**
     * 配置ID（雪花ID）
     */
    @TableId(value = "config_id", type = IdType.ASSIGN_ID)
    private Long configId;

    /**
     * 等级：1-基础版 2-标准版 3-专业版 4-旗舰版
     */
    @TableField(value = "tier_level")
    private Integer tierLevel;

    /**
     * 等级名称
     */
    @TableField(value = "tier_name")
    private String tierName;

    /**
     * 年费价格
     */
    @TableField(value = "tier_price")
    private BigDecimal tierPrice;

    /**
     * 最大门店数量（-1表示不限）
     */
    @TableField(value = "max_shop_count")
    private Integer maxShopCount;

    /**
     * 每月可发优惠券数量（-1表示不限）
     */
    @TableField(value = "max_coupon_count_per_month")
    private Integer maxCouponCountPerMonth;

    /**
     * 单张优惠券最大发行量（-1表示不限）
     */
    @TableField(value = "max_coupon_quantity")
    private Integer maxCouponQuantity;

    /**
     * 是否可查看数据分析：0-否 1-是
     */
    @TableField(value = "can_view_analytics")
    private Integer canViewAnalytics;

    /**
     * 是否可查看竞品分析：0-否 1-是（专业版+）
     */
    @TableField(value = "can_view_competitor")
    private Integer canViewCompetitor;

    /**
     * 每日曝光次数上限
     */
    @TableField(value = "daily_exposure_limit")
    private Integer dailyExposureLimit;

    /**
     * 搜索权重（影响排名，值越大越靠前）
     */
    @TableField(value = "search_weight")
    private Integer searchWeight;

    /**
     * 其他特权列表（JSON数组）
     */
    @TableField(value = "features")
    private String features;

    /**
     * 状态：0-停用 1-启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 排序（越小越靠前）
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

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
