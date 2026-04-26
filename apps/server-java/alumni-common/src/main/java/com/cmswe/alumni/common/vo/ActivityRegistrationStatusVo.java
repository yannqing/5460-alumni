package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 当前用户在某活动中的报名状态VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ActivityRegistrationStatusVo", description = "当前用户报名状态VO")
public class ActivityRegistrationStatusVo implements Serializable {

    /**
     * 是否已报名（包含所有状态：待审/通过/拒绝/已取消）
     */
    @Schema(description = "是否已报名")
    private Boolean hasRegistered;

    /**
     * 当前最新一条报名记录的状态：0-待审核 1-审核通过 2-审核拒绝 3-已取消
     */
    @Schema(description = "报名状态：0-待审核 1-审核通过 2-审核拒绝 3-已取消")
    private Integer registrationStatus;

    /**
     * 报名记录ID（用于取消报名时使用）
     */
    @Schema(description = "报名记录ID")
    private String registrationId;

    /**
     * 审核理由（被拒时展示）
     */
    @Schema(description = "审核理由")
    private String auditReason;

    @Serial
    private static final long serialVersionUID = 1L;
}
