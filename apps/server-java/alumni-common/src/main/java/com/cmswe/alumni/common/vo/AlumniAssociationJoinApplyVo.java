package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApply;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlumniAssociationJoinApplyVo", description = "校友会申请加入校促会申请VO")
public class AlumniAssociationJoinApplyVo implements Serializable {

    /**
     * 校友会申请加入校促会住主键id
     */
    @Schema(description = "校友会申请加入校促会住主键id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 校友会ID
     */
    @Schema(description = "校友会ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long alumniAssociationId;

    /**
     * 校促会ID
     */
    @Schema(description = "校促会ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long platformId;

    /**
     * 审核状态(0待审核,1已通过,2已拒绝)
     */
    @Schema(description = "审核状态(0待审核,1已通过,2已拒绝)")
    private Integer applyStatus;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    // 校友会信息
    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称")
    private String associationName;

    /**
     * 所属母校ID
     */
    @Schema(description = "所属母校ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long schoolId;

    /**
     * 认证标识（0-未认证，1-校友总会，2-校促会，3-校友总会）
     */
    @Schema(description = "认证标识（0-未认证，1-校友总会，2-校促会，3-校友总会）")
    private Integer certificationFlag;

    /**
     * 成员身份（0-会员单位 1-理事单位）
     */
    @Schema(description = "成员身份（0-会员单位 1-理事单位）")
    private Integer role;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息")
    private String contactInfo;

    /**
     * 常驻地点
     */
    @Schema(description = "常驻地点")
    private String location;

    /**
     * 会员数量
     */
    @Schema(description = "会员数量")
    private Integer memberCount;

    /**
     * 当月可发布到首页的文章数量（配额）
     */
    @Schema(description = "当月可发布到首页的文章数量（配额）")
    private Integer monthlyHomepageArticleQuota;

    /**
     * logo
     */
    @Schema(description = "logo")
    private String logo;

    /**
     * 校友会简介
     */
    @Schema(description = "校友会简介")
    private String associationProfile;

    /**
     * 主要负责人微信用户ID
     */
    @Schema(description = "主要负责人微信用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
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
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 驻会代表社会职务
     */
    @Schema(description = "驻会代表社会职务")
    private String zhSocialAffiliation;

    /**
     * 背景图（json 数组）
     */
    @Schema(description = "背景图（json 数组）")
    private String bgImg;

    /**
     * 状态：0-禁用 1-启用
     */
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;

    public static AlumniAssociationJoinApplyVo objToVo(AlumniAssociationJoinApply apply,
            AlumniAssociation association) {
        if (apply == null) {
            return null;
        }
        AlumniAssociationJoinApplyVo vo = new AlumniAssociationJoinApplyVo();
        // Copy apply fields
        vo.setId(apply.getId());
        vo.setAlumniAssociationId(apply.getAlumniAssociationId());
        vo.setPlatformId(apply.getPlatformId());
        vo.setApplyStatus(apply.getStatus());
        vo.setCreateTime(apply.getCreateTime());
        vo.setUpdateTime(apply.getUpdateTime());
        // Copy association fields
        if (association != null) {
            BeanUtils.copyProperties(association, vo);
        }
        return vo;
    }
}
