package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询「我的申请」详情参数
 */
@Data
@Schema(description = "查询我的申请详情参数")
public class QueryMyApplicationRecordDetailDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "recordType不能为空")
    @Schema(description = "业务类型，见 MyApplicationRecordType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recordType;

    @NotBlank(message = "recordId不能为空")
    @Schema(description = "申请记录ID（字符串，避免Long精度问题）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recordId;
}

