package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 查询附近商铺DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "查询附近商铺请求DTO")
public class QueryNearbyShopDto extends PageRequest implements Serializable {

    /**
     * 纬度（-90 ~ 90）
     */
    @Schema(description = "纬度", example = "39.9042", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度必须在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度必须在-90到90之间")
    private BigDecimal latitude;

    /**
     * 经度（-180 ~ 180）
     */
    @Schema(description = "经度", example = "116.4074", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度必须在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度必须在-180到180之间")
    private BigDecimal longitude;

    /**
     * 查询半径（公里）默认30km
     */
    @Schema(description = "查询半径（公里）", example = "30", defaultValue = "30", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Max(value = 100, message = "查询半径不能超过100公里")
    private Integer radius = 30;

    /**
     * 商铺名称（模糊查询）
     */
    @Schema(description = "商铺名称", example = "星巴克", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shopName;

    /**
     * 是否推荐：0-否 1-是
     */
    @Schema(description = "是否推荐：0-否 1-是", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer isRecommended;

    @Serial
    private static final long serialVersionUID = 1L;
}
