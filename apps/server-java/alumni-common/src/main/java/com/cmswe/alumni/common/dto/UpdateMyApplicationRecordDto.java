package com.cmswe.alumni.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新「我的申请」记录参数（聚合入口，按 recordType 分发）
 */
@Data
@Schema(description = "更新我的申请记录参数")
public class UpdateMyApplicationRecordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "recordType不能为空")
    @Schema(description = "业务类型，见 MyApplicationRecordType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recordType;

    @NotBlank(message = "recordId不能为空")
    @Schema(description = "申请记录ID（字符串，避免Long精度问题）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recordId;

    @NotNull(message = "payload不能为空")
    @Schema(description = "按 recordType 对应的更新参数对象", requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonNode payload;
}

