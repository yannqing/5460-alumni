package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.Activity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动列表VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ActivityListVo", description = "活动列表信息返回VO")
public class ActivityListVo implements Serializable {

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
     * 是否需要报名：0-否 1-是
     */
    @Schema(description = "是否需要报名：0-否 1-是")
    private Integer isSignup;

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
     * 浏览次数
     */
    @Schema(description = "浏览次数")
    private String viewCount;

    /**
     * 是否推荐：0-否 1-是
     */
    @Schema(description = "是否推荐：0-否 1-是")
    private Integer isRecommended;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Serial
    private static final long serialVersionUID = 1L;

    public static ActivityListVo objToVo(Activity activity) {
        if (activity == null) {
            return null;
        }
        ActivityListVo vo = new ActivityListVo();
        BeanUtils.copyProperties(activity, vo);

        // 将 Long 转换为 String，避免前端精度丢失
        vo.setActivityId(String.valueOf(activity.getActivityId()));
        vo.setOrganizerId(String.valueOf(activity.getOrganizerId()));
        vo.setViewCount(String.valueOf(activity.getViewCount()));

        return vo;
    }
}
