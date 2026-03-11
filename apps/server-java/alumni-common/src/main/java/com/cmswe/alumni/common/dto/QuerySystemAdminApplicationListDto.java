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
 * 系统管理员查询校友会创建申请列表请求DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuerySystemAdminApplicationListDto extends PageRequest implements Serializable {

    /**
     * 母校ID（可选）
     */
    @Schema(description = "母校 ID（可选）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long schoolId;

    /**
     * 校处会ID（可选）
     */
    @Schema(description = "校处会 ID（可选）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long platformId;

    /**
     * 校友会名称（模糊搜索）
     */
    @Schema(description = "校友会名称（模糊搜索）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String associationName;

    /**
     * 负责人姓名（模糊搜索）
     */
    @Schema(description = "负责人姓名（模糊搜索）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String chargeName;

    /**
     * 常驻地点（模糊搜索）
     */
    @Schema(description = "常驻地点（模糊搜索）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String location;

    /**
     * 申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销
     */
    @Schema(description = "申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer applicationStatus;

    @Serial
    private static final long serialVersionUID = 1L;
}
