package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 商户查询活动列表请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商户查询活动列表请求DTO")
public class QueryMerchantActivityDto extends PageRequest {

    /**
     * 商户ID
     */
    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long merchantId;

    /**
     * 活动类型：1-优惠活动 2-话题活动（可选筛选）
     */
    @Schema(description = "活动类型：1-优惠活动 2-话题活动")
    private Integer activityType;

    /**
     * 审核状态（可选筛选）
     */
    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败")
    private Integer reviewStatus;

    /**
     * 活动状态（可选筛选）
     */
    @Schema(description = "活动状态：0-草稿 1-报名中 2-报名结束 3-进行中 4-已结束 5-已取消")
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;
}
