package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动-门店关联表
 */
@Data
@TableName(value = "activity_shop")
public class ActivityShop implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID
     */
    @TableField(value = "activity_id")
    private Long activityId;

    /**
     * 门店ID
     */
    @TableField(value = "shop_id")
    private Long shopId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
