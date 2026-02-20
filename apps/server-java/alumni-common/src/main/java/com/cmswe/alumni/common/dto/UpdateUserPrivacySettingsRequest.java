package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "UpdateUserPrivacySettingsRequest", description = "更新用户隐私设置请求")
public class UpdateUserPrivacySettingsRequest implements Serializable {

    @Schema(description = "用户隐私设置的id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long userPrivacySettingId;

    @Schema(description = "要更新的字段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String fieldCode;

    @Schema(description = "是否可见 0-不可见，1-可见", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer visibility;

    @Schema(description = "是否可被搜索 0-不可，1-可", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer searchable;

    @Serial
    private static final long serialVersionUID = 1L;
}
