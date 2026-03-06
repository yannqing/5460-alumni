package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 邀请排行榜响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邀请排行榜响应")
public class InvitationRankVo {

    @Schema(description = "我的邀请人数（当前用户作为邀请人）")
    private Integer myInviteCount;

    @Schema(description = "我的排名（从1开始，未上榜返回0）")
    private Integer myRank;

    @Schema(description = "我的头像URL")
    private String myAvatar;

    @Schema(description = "我的姓名（若姓名为空则为昵称）")
    private String myName;

    @Schema(description = "我的学校名称")
    private String mySchool;

    @Schema(description = "全部邀请排行列表")
    private List<InvitationRankItemVo> rankList;
}
