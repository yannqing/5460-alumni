package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 校友企业/场所表
 * @TableName alumni_place
 */
@TableName(value = "alumni_place")
@Data
public class AlumniPlace implements Serializable {
    /**
     * 场所/企业ID（雪花ID）
     */
    @TableId(value = "place_id", type = IdType.ASSIGN_ID)
    private Long placeId;

    /**
     * 场所/企业名称
     */
    @TableField(value = "place_name")
    private String placeName;

    /**
     * 类型：1-企业 2-场所
     */
    @TableField(value = "place_type")
    private Integer placeType;

    /**
     * 所属校友ID
     */
    @TableField(value = "alumni_id")
    private Long alumniId;

    /**
     * 所属校友会ID
     */
    @TableField(value = "alumni_association_id")
    private Long alumniAssociationId;

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
     * 联系电话
     */
    @TableField(value = "contact_phone")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @TableField(value = "contact_email")
    private String contactEmail;

    /**
     * 营业/办公时间（如：周一至周五 09:00-18:00）
     */
    @TableField(value = "business_hours")
    private String businessHours;

    /**
     * 图片URL数组（JSON格式：["url1","url2"]）
     */
    @TableField(value = "images")
    private String images;

    /**
     * logo URL
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 简介
     */
    @TableField(value = "description")
    private String description;

    /**
     * 成立时间
     */
    @TableField(value = "established_time")
    private LocalDate establishedTime;

    /**
     * 状态：0-停业/关闭 1-正常运营 2-筹建中
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @TableField(value = "review_status")
    private Integer reviewStatus;

    /**
     * 审核备注
     */
    @TableField(value = "review_remark")
    private String reviewRemark;

    /**
     * 校友会推荐：0-否 1-是
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
     * 评分（0-5分）
     */
    @TableField(value = "rating_score")
    private BigDecimal ratingScore;

    /**
     * 评价数量
     */
    @TableField(value = "rating_count")
    private Integer ratingCount;

    /**
     * 创建人ID
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
