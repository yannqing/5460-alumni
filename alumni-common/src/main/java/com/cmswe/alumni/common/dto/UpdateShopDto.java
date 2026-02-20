package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 更新店铺请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "更新店铺请求DTO")
public class UpdateShopDto implements Serializable {

    /**
     * 店铺ID
     */
    @NotNull(message = "店铺ID不能为空")
    @Schema(description = "店铺ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long shopId;

    /**
     * 店铺名称
     */
    @Schema(description = "店铺名称")
    private String shopName;

    /**
     * 店铺类型：1-总店 2-分店
     */
    @Schema(description = "店铺类型：1-总店 2-分店")
    private Integer shopType;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 区县
     */
    @Schema(description = "区县")
    private String district;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String address;

    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @Schema(description = "经度")
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

    /**
     * 状态：0-停业 1-营业中 2-装修中
     */
    @Schema(description = "状态：0-停业 1-营业中 2-装修中")
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;
}
