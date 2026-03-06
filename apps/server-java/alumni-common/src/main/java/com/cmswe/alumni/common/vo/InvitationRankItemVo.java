package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邀请排行榜单项 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邀请排行榜单项")
public class InvitationRankItemVo {

    @Schema(description = "用户wxid")
    private Long wxId;

    @Schema(description = "排行（从1开始）")
    private Integer rank;

    @Schema(description = "邀请人数")
    private Integer inviteCount;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "学校名称")
    private String school;
}
