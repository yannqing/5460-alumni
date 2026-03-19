package com.cmswe.alumni.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 校友会申请加入校促会详情 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AlumniAssociationJoinApplyDetailVo", description = "校友会申请加入校促会详情")
public class AlumniAssociationJoinApplyDetailVo implements Serializable {

    @Schema(description = "申请ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "审核状态(0待审核,1已通过,2已拒绝)")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "申请人wx_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicantWxId;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "申请人昵称")
    private String applicantNickname;

    @Schema(description = "申请人手机号")
    private String applicantPhone;

    @Schema(description = "申请人头像")
    private String applicantAvatarUrl;

    @Schema(description = "校友会ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long alumniAssociationId;

    @Schema(description = "校友会名称")
    private String associationName;

    @Schema(description = "母校ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long schoolId;

    @Schema(description = "校促会ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long platformId;

    @Schema(description = "校友会logo")
    private String logo;

    @Schema(description = "校友会背景图")
    private String bgImg;

    @Schema(description = "申请时填写的校友会logo")
    private String applyLogo;

    @Schema(description = "申请时填写的校友会背景图")
    private String applyBgImg;

    @Schema(description = "常驻地点")
    private String location;

    @Schema(description = "联系方式")
    private String contactInfo;

    @Schema(description = "申请时填写的负责人微信用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyChargeWxId;

    @Schema(description = "申请时填写的负责人姓名")
    private String applyChargeName;

    @Schema(description = "申请时填写的负责人架构角色")
    private String applyChargeRole;

    @Schema(description = "申请时填写的负责人联系方式")
    private String applyContactInfo;

    @Schema(description = "申请时填写的联系人(驻会代表)微信用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyZhWxId;

    @Schema(description = "申请时填写的联系人(驻会代表)姓名")
    private String applyZhName;

    @Schema(description = "申请时填写的联系人(驻会代表)架构角色")
    private String applyZhRole;

    @Schema(description = "申请时填写的联系人(驻会代表)联系电话")
    private String applyZhPhone;

    @Schema(description = "校友会简介")
    private String associationProfile;

    @Schema(description = "创建校友会申请ID(若有)")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createApplicationId;

    @Schema(description = "创建校友会申请中的附件ID JSON")
    private String attachmentIds;

    @Schema(description = "创建校友会申请中的附件文件信息")
    private List<FilesVo> attachmentFiles;

    @Serial
    private static final long serialVersionUID = 1L;
}
