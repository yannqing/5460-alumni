package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校友会表
 * @TableName alumni_association
 */
@TableName(value = "alumni_association")
@Data
public class AlumniAssociation implements Serializable {
    /**
     * 校友会ID
     */
    @TableId(value = "alumni_association_id", type = IdType.ASSIGN_ID)
    private Long alumniAssociationId;

    /**
     * 校友会名称
     */
    @TableField(value = "association_name")
    private String associationName;

    /**
     * 所属母校ID
     */
    @TableField(value = "school_id")
    private Long schoolId;

    /**
     * 所属校处会ID
     */
    @TableField(value = "platform_id")
    private Long platformId;

    /**
     * 联系信息
     */
    @TableField(value = "contact_info")
    private String contactInfo;

    /**
     * 常驻地点
     */
    @TableField(value = "location")
    private String location;

    /**
     * 会员数量
     */
    @TableField(value = "member_count")
    private Integer memberCount;

    /**
     * 当月可发布到首页的文章数量（配额）
     */
    @TableField(value = "monthly_homepage_article_quota")
    private Integer monthlyHomepageArticleQuota;

    /**
     * 校友会logo
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 校友会简介
     */
    @TableField(value = "association_profile")
    private String associationProfile;

    /**
     * 主要负责人微信用户ID
     */
    @TableField(value = "charge_wx_id")
    private Long chargeWxId;

    /**
     * 主要负责人姓名
     */
    @TableField(value = "charge_name")
    private String chargeName;

    /**
     * 主要负责人架构角色
     */
    @TableField(value = "charge_role")
    private String chargeRole;

    /**
     * 主要负责人社会职务
     */
    @TableField(value = "charge_social_affiliation")
    private String chargeSocialAffiliation;

    /**
     * 驻会代表微信用户ID
     */
    @TableField(value = "zh_wx_id")
    private Long zhWxId;

    /**
     * 驻会代表姓名
     */
    @TableField(value = "zh_name")
    private String zhName;

    /**
     * 驻会代表联系电话
     */
    @TableField(value = "zh_phone")
    private String zhPhone;

    /**
     * 驻会代表社会职务
     */
    @TableField(value = "zh_social_affiliation")
    private String zhSocialAffiliation;

    /**
     * 背景图（json 数组）
     */
    @TableField(value = "bg_img")
    private String bgImg;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 成员身份：0-会员单位 1-理事单位
     */
    @TableField(value = "role")
    private Integer role;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public static SFunction<AlumniAssociation, ?> getSortMethod(String sortField) {
        if (sortField == null) {
            return AlumniAssociation::getCreateTime;
        }
        switch (sortField) {
            case "memberCount" -> {
                return AlumniAssociation::getMemberCount;
            }
            case "createTime" -> {
                return AlumniAssociation::getCreateTime;
            }
        }
        return AlumniAssociation::getCreateTime;
    }
}