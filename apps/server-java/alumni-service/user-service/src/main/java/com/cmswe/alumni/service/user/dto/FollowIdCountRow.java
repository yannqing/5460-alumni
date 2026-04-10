package com.cmswe.alumni.service.user.dto;

import lombok.Data;

/**
 * user_follow 按用户维度聚合行（用于关注数校准）
 */
@Data
public class FollowIdCountRow {
    private Long id;
    private Long cnt;
}
