package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 申请创建校友企业/场所 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ApplyAlumniPlaceDto", description = "申请创建校友企业/场所请求DTO")
public class ApplyAlumniPlaceDto implements Serializable {

    /**
     * 场所/企业名称
     */
    @Schema(description = "场所/企业名称", required = true)
    @NotBlank(message = "场所/企业名称不能为空")
    private String placeName;

    /**
     * 类型：1-企业 2-场所
     */
    @Schema(description = "类型：1-企业 2-场所", required = true)
    @NotNull(message = "类型不能为空")
    private Integer placeType;

    /**
     * 所属校友会ID
     */
    @Schema(description = "所属校友会ID", required = true)
    @NotNull(message = "所属校友会ID不能为空")
    private Long alumniAssociationId;

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
     * 纬度（-90 ~ 90）
     */
    @Schema(description = "纬度（-90 ~ 90）")
    private BigDecimal latitude;

    /**
     * 经度（-180 ~ 180）
     */
    @Schema(description = "经度（-180 ~ 180）")
    private BigDecimal longitude;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱")
    private String contactEmail;

    /**
     * 营业/办公时间
     */
    @Schema(description = "营业/办公时间")
    private String businessHours;

    /**
     * 图片URL数组（JSON格式：["url1","url2"]）
     */
    @Schema(description = "图片URL数组（JSON格式）")
    private String images;

    /**
     * logo URL
     */
    @Schema(description = "logo URL")
    private String logo;

    /**
     * 简介
     */
    @Schema(description = "简介")
    private String description;

    /**
     * 成立时间
     */
    @Schema(description = "成立时间")
    private LocalDate establishedTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
