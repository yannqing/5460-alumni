package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniAssociation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlumniAssociationDetailVo", description = "校友会信息列表返回VO")
public class AlumniAssociationDetailVo implements Serializable {

    /**
     * 校友会ID
     */
    @Schema(description = "校友会ID")
    private Long alumniAssociationId;

    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称")
    private String associationName;

    /**
     * 所属母校ID
     */
    @Schema(description = "所属母校信息")
    private SchoolListVo schoolInfo;

    /**
     * 所属校处会ID
     */
    @Schema(description = "所属校处会")
    private LocalPlatformDetailVo platform;

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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private java.time.LocalDateTime updateTime;

    /**
     * 当前用户加入状态：1-已加入（成员表中存在且状态正常） null-未加入
     */
    @Schema(description = "当前用户加入状态：1-已加入 null-未加入")
    private Integer applicationStatus;

    /**
     * 活动列表
     */
    @Schema(description = "活动列表")
    private List<ActivityListVo> activityList;

    /**
     * 企业列表
     */
    @Schema(description = "企业列表")
    private List<AlumniPlaceListVo> enterpriseList;

    /**
     * 文章列表
     */
    @Schema(description = "文章列表（已发布的文章）")
    private List<HomePageArticleVo> articleList;


    @Serial
    private static final long serialVersionUID = 1L;

    public static AlumniAssociationDetailVo objToVo(AlumniAssociation alumniAssociation) {
        if (alumniAssociation == null) {
            return null;
        }
        AlumniAssociationDetailVo alumniAssociationListVo = new AlumniAssociationDetailVo();
        BeanUtils.copyProperties(alumniAssociation, alumniAssociationListVo);
        return alumniAssociationListVo;
    }
}