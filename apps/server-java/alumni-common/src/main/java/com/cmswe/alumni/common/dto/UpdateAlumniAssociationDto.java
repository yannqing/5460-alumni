package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新校友会信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UpdateAlumniAssociationDto", description = "更新校友会信息DTO")
public class UpdateAlumniAssociationDto implements Serializable {

    /**
     * 校友会ID
     */
    @Schema(description = "校友会ID")
    @NotNull(message = "校友会ID不能为空")
    private Long alumniAssociationId;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息（json 格式）")
    private String contactInfo;

    /**
     * 常驻地点
     */
    @Schema(description = "常驻地点")
    private String location;

    /**
     * 校友会logo
     */
    @Schema(description = "校友会logo")
    private String logo;

    /**
     * 背景图（json 数组）
     */
    @Schema(description = "背景图（json 数组）")
    private String bgImg;

    /**
     * 校友会简介
     */
    @Schema(description = "校友会简介")
    private String associationProfile;

    /**
     * 主要负责人微信用户ID
     */
    @Schema(description = "主要负责人微信用户ID")
    private Long chargeWxId;

    /**
     * 主要负责人姓名
     */
    @Schema(description = "主要负责人姓名")
    private String chargeName;

    /**
     * 主要负责人架构角色
     */
    @Schema(description = "主要负责人架构角色")
    private String chargeRole;

    /**
     * 主要负责人社会职务
     */
    @Schema(description = "主要负责人社会职务")
    private String chargeSocialAffiliation;

    /**
     * 驻会代表微信用户ID
     */
    @Schema(description = "驻会代表微信用户ID")
    private Long zhWxId;

    /**
     * 驻会代表姓名
     */
    @Schema(description = "驻会代表姓名")
    private String zhName;

    /**
     * 驻会代表联系电话
     */
    @Schema(description = "驻会代表联系电话")
    private String zhPhone;

    /**
     * 驻会代表架构角色
     */
    @Schema(description = "驻会代表架构角色")
    private String zhRole;

    /**
     * 驻会代表社会职务
     */
    @Schema(description = "驻会代表社会职务")
    private String zhSocialAffiliation;

    @Serial
    private static final long serialVersionUID = 1L;
}
