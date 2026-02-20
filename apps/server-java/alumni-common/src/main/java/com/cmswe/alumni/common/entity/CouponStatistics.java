package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 优惠券统计表（按天统计优惠券效果）
 * @TableName coupon_statistics
 */
@TableName(value = "coupon_statistics")
@Data
public class CouponStatistics implements Serializable {
    /**
     * 统计ID（雪花ID）
     */
    @TableId(value = "stat_id", type = IdType.ASSIGN_ID)
    private Long statId;

    /**
     * 优惠券ID
     */
    @TableField(value = "coupon_id")
    private Long couponId;

    /**
     * 商户ID
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 店铺ID
     */
    @TableField(value = "shop_id")
    private Long shopId;

    /**
     * 统计日期（YYYY-MM-DD）
     */
    @TableField(value = "stat_date")
    private LocalDate statDate;

    /**
     * 浏览次数
     */
    @TableField(value = "view_count")
    private Long viewCount;

    /**
     * 领取次数
     */
    @TableField(value = "received_count")
    private Long receivedCount;

    /**
     * 领取率（received / view）
     */
    @TableField(value = "receive_rate")
    private BigDecimal receiveRate;

    /**
     * 使用次数
     */
    @TableField(value = "used_count")
    private Long usedCount;

    /**
     * 使用率（used / received）
     */
    @TableField(value = "use_rate")
    private BigDecimal useRate;

    /**
     * 独立领取用户数
     */
    @TableField(value = "unique_receiver_count")
    private Integer uniqueReceiverCount;

    /**
     * 校友领取用户数
     */
    @TableField(value = "alumni_receiver_count")
    private Integer alumniReceiverCount;

    /**
     * 总优惠金额
     */
    @TableField(value = "total_discount_amount")
    private BigDecimal totalDiscountAmount;

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
