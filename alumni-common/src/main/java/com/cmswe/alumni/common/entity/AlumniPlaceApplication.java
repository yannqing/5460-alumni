package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 校友企业/场所申请表
 * @TableName alumni_place_application
 */
@TableName(value = "alumni_place_application")
@Data
public class AlumniPlaceApplication implements Serializable {
    /**
     * 申请ID（雪花ID）
     */
    @TableId(value = "application_id", type = IdType.ASSIGN_ID)
    private Long applicationId;

    /**
     * 申请人ID（wx_id）
     */
    @TableField(value = "applicant_id")
    private Long applicantId;

    /**
     * 申请人姓名
     */
    @TableField(value = "applicant_name")
    private String applicantName;

    /**
     * 申请人联系电话
     */
    @TableField(value = "applicant_phone")
    private String applicantPhone;

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
     * 营业/办公时间
     */
    @TableField(value = "business_hours")
    private String businessHours;

    /**
     * 图片URL数组（JSON格式）
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
     * 申请状态：0-待审核 1-审核通过 2-审核拒绝 3-已撤销
     */
    @TableField(value = "application_status")
    private Integer applicationStatus;

    /**
     * 审核人ID
     */
    @TableField(value = "review_user_id")
    private Long reviewUserId;

    /**
     * 审核人姓名
     */
    @TableField(value = "review_user_name")
    private String reviewUserName;

    /**
     * 审核时间
     */
    @TableField(value = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核备注
     */
    @TableField(value = "review_remark")
    private String reviewRemark;

    /**
     * 审核通过后创建的场所/企业ID
     */
    @TableField(value = "place_id")
    private Long placeId;

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
