package com.cmswe.alumni.common.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 申请创建校友会 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "申请创建校友会请求")
public class ApplyCreateAlumniAssociationDto implements Serializable {

    /**
     * 申请创建的校友会名称
     */
    @Schema(description = "申请创建的校友会名称")
    @NotBlank(message = "校友会名称不能为空")
    private String associationName;

    /**
     * 所属母校 ID
     */
    @Schema(description = "所属母校 ID")
    @NotNull(message = "所属母校 ID不能为空")
    private Long schoolId;

    /**
     * 所属校处会ID（可选）
     */
    @Schema(description = "所属校处会ID（可选）")
    private Long platformId;

    /**
     * 负责人微信用户 ID
     */
    @Schema(description = "负责人微信用户 ID")
    @NotNull(message = "负责人微信用户 ID不能为空")
    private Long chargeWxId;

    /**
     * 负责人姓名
     */
    @Schema(description = "负责人姓名")
    @NotBlank(message = "负责人姓名不能为空")
    private String chargeName;

    /**
     * 负责人架构角色
     */
    @Schema(description = "负责人架构角色")
    @NotBlank(message = "负责人角色不能为空")
    private String chargeRole;

    /**
     * 联系信息（负责人联系方式）
     */
    @Schema(description = "联系信息（负责人联系方式）")
    private String contactInfo;

    /**
     * 主要负责人社会职务
     */
    @Schema(description = "主要负责人社会职务")
    @NotBlank(message = "主要负责人社会职务不能为空")
    private String msocialAffiliation;

    /**
     * 驻会代表姓名
     */
    @Schema(description = "驻会代表姓名")
    @NotBlank(message = "驻会代表姓名不能为空")
    private String zhName;

    /**
     * 驻会代表联系电话
     */
    @Schema(description = "驻会代表联系电话")
    @NotBlank(message = "驻会代表联系电话不能为空")
    private String zhPhone;

    /**
     * 驻会代表社会职务
     */
    @Schema(description = "驻会代表社会职务")
    @NotBlank(message = "驻会代表社会职务不能为空")
    private String zhSocialAffiliation;

    /**
     * 背景图（URL 列表）
     */
    @Schema(description = "背景图（URL 列表）")
    private List<String> bgImg;

    /**
     * 常驻地点
     */
    @Schema(description = "常驻地点")
    private String location;

    /**
     * 校友会 logo
     */
    @Schema(description = "校友会 logo")
    private String logo;

    /**
     * 申请理由
     */
    @Schema(description = "申请理由")
    @NotBlank(message = "申请理由不能为空")
    private String applicationReason;

    /**
     * 校友会简介（选填）
     */
    @Schema(description = "校友会简介（选填）")
    private String associationProfile;

    /**
     * 初始成员列表
     */
    @Schema(description = "初始成员列表（除会长外的其他成员）")
    private List<InitialMemberDto> initialMembers;

    /**
     * 申请材料附件 ID数组
     */
    @Schema(description = "申请材料附件 ID数组")
    private List<Long> attachmentIds;

    @Serial
    private static final long serialVersionUID = 1L;
}
