package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询活动报名列表DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "查询活动报名列表请求")
public class QueryActivityRegistrationListDto extends PageRequest implements Serializable {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long activityId;

    /**
     * 报名状态：0-待审核 1-审核通过 2-审核拒绝 3-已取消，不传则查全部
     */
    @Schema(description = "报名状态：0-待审核 1-审核通过 2-审核拒绝 3-已取消，不传则查全部")
    private Integer registrationStatus;

    /**
     * 关键词（按 user_name / user_phone 模糊查询）
     */
    @Schema(description = "关键词（姓名/手机号模糊查询）")
    private String keyword;

    @Serial
    private static final long serialVersionUID = 1L;
}
