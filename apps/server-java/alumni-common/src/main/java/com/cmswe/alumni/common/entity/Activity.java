package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动表
 * @TableName activity
 */
@TableName(value = "activity")
@Data
public class Activity implements Serializable {
    /**
     * 活动ID（雪花ID）
     */
    @TableId(value = "activity_id", type = IdType.ASSIGN_ID)
    private Long activityId;

    /**
     * 活动标题
     */
    @TableField(value = "activity_title")
    private String activityTitle;

    /**
     * 主办方类型：1-校友会 2-校处会 3-商铺 4-母校
     */
    @TableField(value = "organizer_type")
    private Integer organizerType;

    /**
     * 主办方ID（根据类型关联不同表）
     */
    @TableField(value = "organizer_id")
    private Long organizerId;

    /**
     * 主办方名称（冗余字段，便于查询）
     */
    @TableField(value = "organizer_name")
    private String organizerName;

    /**
     * 主办方头像
     */
    @TableField(value = "organizer_avatar")
    private String organizerAvatar;

    /**
     * 活动封面图URL
     */
    @TableField(value = "cover_image")
    private String coverImage;

    /**
     * 活动图片URL数组（JSON格式：[1,2]）
     */
    @TableField(value = "activity_images")
    private String activityImages;

    /**
     * 活动详情描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 活动分类（如：年会、聚会、讲座、公益等）
     */
    @TableField(value = "activity_category")
    private String activityCategory;

    /**
     * 活动开始时间
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 报名开始时间
     */
    @TableField(value = "registration_start_time")
    private LocalDateTime registrationStartTime;

    /**
     * 报名截止时间
     */
    @TableField(value = "registration_end_time")
    private LocalDateTime registrationEndTime;

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
     * 最大参与人数（NULL表示不限）
     */
    @TableField(value = "max_participants")
    private Integer maxParticipants;

    /**
     * 当前报名人数
     */
    @TableField(value = "current_participants")
    private Integer currentParticipants;

    /**
     * 是否需要审核：0-无需审核 1-需要审核
     */
    @TableField(value = "is_need_review")
    private Integer isNeedReview;

    /**
     * 是否需要报名：0-否 1-是
     */
    @TableField(value = "is_signup")
    private Integer isSignup;

    /**
     * 联系人
     */
    @TableField(value = "contact_person")
    private String contactPerson;

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
     * 状态：0-草稿 1-报名中 2-报名结束 3-进行中 4-已结束 5-已取消
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @TableField(value = "review_status")
    private Integer reviewStatus;

    /**
     * 审核原因（驳回时填写）
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
     * 浏览次数
     */
    @TableField(value = "view_count")
    private Long viewCount;

    /**
     * 是否公开：0-不公开 1-公开
     */
    @TableField(value = "is_public")
    private Integer isPublic;

    /**
     * 是否推荐：0-否 1-是
     */
    @TableField(value = "is_recommended")
    private Integer isRecommended;

    /**
     * 活动标签（JSON数组：[1,2]）
     */
    @TableField(value = "tags_id")
    private String tagsId;

    /**
     * 备注信息
     */
    @TableField(value = "remark")
    private String remark;

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
