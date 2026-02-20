package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建店铺请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "创建店铺请求DTO")
public class CreateShopDto implements Serializable {

    /**
     * 所属商户ID
     */
    @NotNull(message = "商户ID不能为空")
    @Schema(description = "所属商户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long merchantId;

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称长度不能超过100个字符")
    @Schema(description = "店铺名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String shopName;

    /**
     * 店铺类型：1-总店 2-分店
     */
    @NotNull(message = "店铺类型不能为空")
    @Schema(description = "店铺类型：1-总店 2-分店", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer shopType;

    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    @Schema(description = "省份", requiredMode = Schema.RequiredMode.REQUIRED)
    private String province;

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    /**
     * 区县
     */
    @NotBlank(message = "区县不能为空")
    @Schema(description = "区县", requiredMode = Schema.RequiredMode.REQUIRED)
    private String district;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal latitude;

    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal longitude;

    /**
     * 店铺电话
     */
    @Schema(description = "店铺电话")
    private String phone;

    /**
     * 营业时间
     */
    @Schema(description = "营业时间")
    private String businessHours;

    /**
     * 店铺图片URL数组（JSON格式）
     */
    @Schema(description = "店铺图片URL数组（JSON格式）")
    private String shopImages;

    /**
     * 店铺简介
     */
    @Schema(description = "店铺简介")
    private String description;

    @Serial
    private static final long serialVersionUID = 1L;
}
