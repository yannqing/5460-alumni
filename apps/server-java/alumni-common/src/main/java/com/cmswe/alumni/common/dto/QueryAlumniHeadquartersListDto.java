package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 校友总会列表查询DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "校友总会列表查询请求DTO")
public class QueryAlumniHeadquartersListDto extends PageRequest implements Serializable {

    /**
     * 校友总会名称
     */
    @Schema(description = "校友总会名称", example = "清华大学校友总会", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 200, message = "校友总会名称长度不能超过200个字符")
    private String headquartersName;

    /**
     * 所属母校ID
     */
    @Schema(description = "所属母校ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long schoolId;

    /**
     * 办公地址
     */
    @Schema(description = "办公地址", example = "北京市朝阳区xxx", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String address;

    /**
     * 活跃状态：0-不活跃 1-活跃
     */
    @Schema(description = "活跃状态：0-不活跃 1-活跃", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer activeStatus;

    /**
     * 级别：1-校级 2-省级 3-国家级 4-国际级
     */
    @Schema(description = "级别：1-校级 2-省级 3-国家级 4-国际级", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer level;

    /**
     * 创建码
     */
    @Schema(description = "创建码", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer createCode;

    /**
     * 审核状态：0-待审核 1-已通过 2-已驳回
     */
    @Schema(description = "审核状态：0-待审核 1-已通过 2-已驳回", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer approvalStatus;

    @Serial
    private static final long serialVersionUID = 1L;

}
