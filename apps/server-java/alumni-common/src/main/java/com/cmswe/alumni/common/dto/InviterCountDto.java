package com.cmswe.alumni.common.dto;

import lombok.Data;

/**
 * 邀请人及邀请数量（用于排行榜统计）
 */
@Data
public class InviterCountDto {
    private Long inviterWxId;
    private Long inviteCount;
}
