package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 更新用户标签DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "更新用户标签请求")
public class UpdateUserTagsDto implements Serializable {

    @Schema(description = "标签ID列表", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标签列表不能为空")
    private List<Long> tagIds;
}
