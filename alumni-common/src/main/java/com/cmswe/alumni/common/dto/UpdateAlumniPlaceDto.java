package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 管理员更新校友企业/场所基本信息 DTO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "管理员更新校友企业/场所基本信息DTO")
public class UpdateAlumniPlaceDto implements Serializable {

    /**
     * 企业/场所ID
     */
    @NotNull(message = "企业/场所ID不能为空")
    @Schema(description = "企业/场所ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long placeId;

    /**
     * 场所/企业名称
     */
    @Schema(description = "场所/企业名称")
    private String placeName;

    /**
     * 类型：1-企业 2-场所
     */
    @Schema(description = "类型：1-企业 2-场所")
    private Integer placeType;

    /**
     * 所属校友会ID
     */
    @Schema(description = "所属校友会ID")
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
     * 营业时间（如：周一至周五 09:00-18:00）
     */
    @Schema(description = "营业时间")
    private String businessHours;

    /**
     * 企业/场所图片（JSON数组：["url1","url2"]）
     */
    @Schema(description = "企业/场所图片（JSON数组）")
    private String images;

    /**
     * Logo URL
     */
    @Schema(description = "Logo URL")
    private String logo;

    /**
     * 企业/场所描述
     */
    @Schema(description = "企业/场所描述")
    private String description;

    /**
     * 成立时间
     */
    @Schema(description = "成立时间")
    private LocalDate establishedTime;

    /**
     * 状态：0-停业 1-正常运营 2-装修中
     */
    @Schema(description = "状态：0-停业 1-正常运营 2-装修中")
    private Integer status;

    /**
     * 是否推荐：0-否 1-是
     */
    @Schema(description = "是否推荐：0-否 1-是")
    private Integer isRecommended;

    @Serial
    private static final long serialVersionUID = 1L;
}
