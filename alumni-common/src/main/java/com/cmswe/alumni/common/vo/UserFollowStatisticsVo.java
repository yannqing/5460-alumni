package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户关注统计VO（用户端简化版）
 */
@Data
@Schema(description = "用户关注统计VO")
public class UserFollowStatisticsVo {

    @Schema(description = "粉丝数量")
    private Integer followerCount;

    @Schema(description = "关注数量")
    private Integer followingCount;

    @Schema(description = "好友数量")
    private Integer friendCount;

    @Schema(description = "今日新增粉丝")
    private Integer dailyNewFollowers;
}
