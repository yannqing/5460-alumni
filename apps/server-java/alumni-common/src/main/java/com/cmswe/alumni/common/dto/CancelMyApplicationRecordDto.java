package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 撤销「我的申请」记录参数
 */
@Data
@Schema(description = "撤销我的申请记录参数")
public class CancelMyApplicationRecordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "recordType不能为空")
    @Schema(description = "业务类型，见 MyApplicationRecordType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recordType;

    @NotBlank(message = "recordId不能为空")
    @Schema(description = "申请记录ID（字符串，避免Long精度问题）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recordId;
}
