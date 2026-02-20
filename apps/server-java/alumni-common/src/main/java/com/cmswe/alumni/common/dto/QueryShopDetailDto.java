package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询商铺详情DTO
 *
 * @author CNI Alumni System
 * @since 2025-12-25
 */
@Data
@Schema(description = "查询商铺详情DTO")
public class QueryShopDetailDto {

    @NotNull(message = "商铺ID不能为空")
    @Schema(description = "商铺ID")
    private Long shopId;
}
