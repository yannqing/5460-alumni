package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我关注的列表项VO（泛型版本）
 *
 * 支持的目标类型：
 * - 用户：UserInfoVo
 * - 校友会：AlumniAssociationListVo
 * - 母校：SchoolListVo
 * - 商户：MerchantListVo
 *
 * @param <T> 目标详情类型
 * @author CMSWE
 * @since 2025-12-09
 */
@Data
@Schema(description = "我关注的列表项VO")
public class FollowingItemVo<T> implements Serializable {

    @Schema(description = "关注ID")
    private Long followId;

    @Schema(description = "目标类型：1-用户，2-校友会，3-母校，4-商户")
    private Integer targetType;

    @Schema(description = "目标ID")
    private String targetId;

    @Schema(description = "关注状态：1-正常关注 2-特别关注 3-免打扰 4-已取消")
    private Integer followStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "目标详细信息（根据 targetType 返回不同类型）")
    private T targetInfo;

    @Serial
    private static final long serialVersionUID = 1L;
}
