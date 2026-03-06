package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 我的邀请列表响应 VO（含邀请人数、排名、列表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的邀请列表响应")
public class InvitationMyListVo {

    @Schema(description = "自己邀了几个人")
    private Integer inviteCount;

    @Schema(description = "排名（从1开始，0表示未上榜）")
    private Integer myRank;

    @Schema(description = "邀请记录列表")
    private List<InvitationRecordItemVo> list;
}
