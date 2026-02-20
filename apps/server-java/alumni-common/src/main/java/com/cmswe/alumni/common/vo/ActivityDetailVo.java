package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.Activity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动详情VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ActivityDetailVo", description = "活动详情信息返回VO")
public class ActivityDetailVo implements Serializable {

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private String activityId;

    /**
     * 活动标题
     */
    @Schema(description = "活动标题")
    private String activityTitle;

    /**
     * 主办方类型：1-校友会 2-校处会 3-商铺 4-母校 5-门店
     */
    @Schema(description = "主办方类型：1-校友会 2-校处会 3-商铺 4-母校 5-门店")
    private Integer organizerType;

    /**
     * 主办方ID
     */
    @Schema(description = "主办方ID")
    private String organizerId;

    /**
     * 主办方名称
     */
    @Schema(description = "主办方名称")
    private String organizerName;

    /**
     * 主办方头像
     */
    @Schema(description = "主办方头像")
    private String organizerAvatar;

    /**
     * 活动封面图URL
     */
    @Schema(description = "活动封面图URL")
    private String coverImage;

    /**
     * 活动图片URL数组（JSON格式）
     */
    @Schema(description = "活动图片URL数组")
    private String activityImages;

    /**
     * 活动详情描述
     */
    @Schema(description = "活动详情描述")
    private String description;

    /**
     * 活动分类（如：年会、聚会、讲座、公益、话题等）
     */
    @Schema(description = "活动分类")
    private String activityCategory;

    /**
     * 活动开始时间
     */
    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    /**
     * 报名开始时间
     */
    @Schema(description = "报名开始时间")
    private LocalDateTime registrationStartTime;

    /**
     * 报名截止时间
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
     * GeoHash编码
     */
    @Schema(description = "GeoHash编码")
    private String geohash;

    /**
     * 最大参与人数（NULL表示不限）
     */
    @Schema(description = "最大参与人数")
    private Integer maxParticipants;

    /**
     * 当前报名人数
     */
    @Schema(description = "当前报名人数")
    private Integer currentParticipants;

    /**
     * 是否需要审核：0-无需审核 1-需要审核
     */
    @Schema(description = "是否需要审核：0-无需审核 1-需要审核")
    private Integer isNeedReview;

    /**
     * 是否需要报名：0-否 1-是
     */
    @Schema(description = "是否需要报名：0-否 1-是")
    private Integer isSignup;

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
     * 状态：0-草稿 1-报名中 2-报名结束 3-进行中 4-已结束 5-已取消
     */
    @Schema(description = "状态：0-草稿 1-报名中 2-报名结束 3-进行中 4-已结束 5-已取消")
    private Integer status;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败")
    private Integer reviewStatus;

    /**
     * 审核原因（驳回时填写）
     */
    @Schema(description = "审核原因")
    private String reviewReason;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private String reviewerId;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数")
    private String viewCount;

    /**
     * 是否公开：0-不公开 1-公开
     */
    @Schema(description = "是否公开：0-不公开 1-公开")
    private Integer isPublic;

    /**
     * 是否推荐：0-否 1-是
     */
    @Schema(description = "是否推荐：0-否 1-是")
    private Integer isRecommended;

    /**
     * 活动标签（JSON数组）
     */
    @Schema(description = "活动标签")
    private String tagsId;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息")
    private String remark;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private String createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    public static ActivityDetailVo objToVo(Activity activity) {
        if (activity == null) {
            return null;
        }
        ActivityDetailVo vo = new ActivityDetailVo();
        BeanUtils.copyProperties(activity, vo);

        // 将 Long 转换为 String，避免前端精度丢失
        vo.setActivityId(String.valueOf(activity.getActivityId()));
        vo.setOrganizerId(String.valueOf(activity.getOrganizerId()));
        vo.setViewCount(String.valueOf(activity.getViewCount()));
        if (activity.getReviewerId() != null) {
            vo.setReviewerId(String.valueOf(activity.getReviewerId()));
        }
        if (activity.getCreatedBy() != null) {
            vo.setCreatedBy(String.valueOf(activity.getCreatedBy()));
        }

        return vo;
    }
}
