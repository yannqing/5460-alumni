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
import java.util.List;

/**
 * 商户发布活动请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商户发布活动请求DTO")
public class PublishMerchantActivityDto implements Serializable {

    /**
     * 商户ID
     */
    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long merchantId;

    /**
     * 活动类型：1-优惠活动 2-话题活动
     */
    @NotNull(message = "活动类型不能为空")
    @Schema(description = "活动类型：1-优惠活动 2-话题活动", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer activityType;

    /**
     * 适用门店ID列表（为空则表示所有门店适用）
     */
    @Schema(description = "适用门店ID列表（为空则所有门店适用）")
    private List<Long> shopIds;

    /**
     * 活动标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityTitle;

    /**
     * 活动封面图URL
     */
    @NotBlank(message = "活动封面图不能为空")
    @Schema(description = "活动封面图URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String coverImage;

    /**
     * 活动图片URL数组（JSON格式）
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
    @Schema(description = "是否需要审核：0-无需审核 1-需要审核")
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
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    @Serial
    private static final long serialVersionUID = 1L;
}
