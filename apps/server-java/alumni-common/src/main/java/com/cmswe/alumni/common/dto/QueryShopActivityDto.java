package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询门店活动列表 DTO
 *
 * @author CNI Alumni System
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "查询门店活动列表请求参数")
public class QueryShopActivityDto extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 门店ID
     */
    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long shopId;

    /**
     * 活动标题（模糊查询）
     */
    @Schema(description = "活动标题")
    private String activityTitle;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败")
    private Integer reviewStatus;

    /**
     * 活动状态：0-草稿 1-报名中 2-报名结束 3-进行中 4-已结束 5-已取消
     */
    @Schema(description = "活动状态：0-草稿 1-报名中 2-报名结束 3-进行中 4-已结束 5-已取消")
    private Integer status;
}
