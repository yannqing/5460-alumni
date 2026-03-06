package com.cmswe.alumni.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviterCountDto {
    private Long inviterWxId;
    private Integer inviteCount;
}