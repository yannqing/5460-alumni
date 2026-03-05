package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询公开活动列表DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "查询公开活动列表请求")
public class QueryPublicActivityDto extends PageRequest implements Serializable {

    /**
     * 活动分类（可选）
     */
    @Schema(description = "活动分类（可选）")
    private String activityCategory;

    /**
     * 主办方类型（可选，1-校友会 2-校处会 3-商铺 4-母校）
     */
    @Schema(description = "主办方类型（可选，1-校友会 2-校处会 3-商铺 4-母校）")
    private Integer organizerType;

    /**
     * 主办方ID（可选）
     */
    @Schema(description = "主办方ID（可选）")
    private Long organizerId;

    @Serial
    private static final long serialVersionUID = 1L;
}
