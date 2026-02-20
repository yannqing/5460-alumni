package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询校友会加入申请列表请求DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryAlumniAssociationJoinApplicationListDto extends PageRequest implements Serializable {

    /**
     * 校友会ID（可选，用于查询特定校友会的申请）
     */
    @Schema(description = "校友会ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long alumniAssociationId;

    /**
     * 申请人姓名（模糊搜索）
     */
    @Schema(description = "申请人姓名（模糊搜索）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String applicantName;

    /**
     * 申请人手机号（模糊搜索）
     */
    @Schema(description = "申请人手机号（模糊搜索）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String applicantPhone;

    /**
     * 申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销
     */
    @Schema(description = "申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer applicationStatus;

    @Serial
    private static final long serialVersionUID = 1L;
}
