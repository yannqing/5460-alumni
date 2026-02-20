package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 编辑活动请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "编辑活动请求DTO")
public class UpdateActivityDto implements Serializable {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long activityId;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityTitle;

    /**
     * 主办方名称
     */
    @NotBlank(message = "主办方名称不能为空")
    @Size(max = 200, message = "主办方名称长度不能超过200个字符")
    @Schema(description = "主办方名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String organizerName;

    /**
     * 主办方头像
     */
    @Schema(description = "主办方头像")
    private String organizerAvatar;

    /**
     * 活动封面图URL
     */
    @NotBlank(message = "活动封面图不能为空")
    @Schema(description = "活动封面图URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String coverImage;

    /**
     * 活动图片URL数组（JSON格式：[1,2]）
     */
    @Schema(description = "活动图片URL数组（JSON格式）")
    private String activityImages;

    /**
     * 活动详情描述
     */
    @NotBlank(message = "活动描述不能为空")
    @Schema(description = "活动详情描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    /**
     * 活动分类（如：年会、聚会、讲座、公益等）
     */
    @Schema(description = "活动分类")
    private String activityCategory;

    /**
     * 活动开始时间
     */
    @NotNull(message = "活动开始时间不能为空")
    @Schema(description = "活动开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @NotNull(message = "活动结束时间不能为空")
    @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    /**
     * 是否需要报名：0-否 1-是
     */
    @NotNull(message = "是否需要报名不能为空")
    @Schema(description = "是否需要报名：0-否 1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isSignup;

    /**
     * 报名开始时间（如果需要报名）
     */
    @Schema(description = "报名开始时间")
    private LocalDateTime registrationStartTime;

    /**
     * 报名截止时间（如果需要报名）
     */
    @Schema(description = "报名截止时间")
    private LocalDateTime registrationEndTime;

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
     * 最大参与人数（NULL表示不限）
     */
    @Schema(description = "最大参与人数（NULL表示不限）")
    private Integer maxParticipants;

    /**
     * 是否需要审核：0-无需审核 1-需要审核
     */
    @NotNull(message = "是否需要审核不能为空")
    @Schema(description = "是否需要审核：0-无需审核 1-需要审核", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isNeedReview;

    /**
     * 联系人
     */
    @Schema(description = "联系人")
    private String contactPerson;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱")
    private String contactEmail;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息")
    private String remark;

    @Serial
    private static final long serialVersionUID = 1L;
}
