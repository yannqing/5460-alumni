package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校友会创建申请表
 * @TableName alumni_association_application
 */
@TableName(value = "alumni_association_application")
@Data
public class AlumniAssociationApplication implements Serializable {
    /**
     * 申请ID
     */
    @TableId(value = "application_id", type = IdType.ASSIGN_ID)
    private Long applicationId;

    /**
     * 申请创建的校友会名称
     */
    @TableField(value = "association_name")
    private String associationName;

    /**
     * 所属母校ID
     */
    @TableField(value = "school_id")
    private Long schoolId;

    /**
     * 所属校处会ID（可选）
     */
    @TableField(value = "platform_id")
    private Long platformId;

    /**
     * 负责人微信用户ID
     */
    @TableField(value = "charge_wx_id")
    private Long chargeWxId;

    /**
     * 负责人姓名
     */
    @TableField(value = "charge_name")
    private String chargeName;

    /**
     * 背景图（json 数组）
     */
    @TableField(value = "bg_img")
    private String bgImg;

    /**
     * 负责人架构角色
     */
    @TableField(value = "charge_role")
    private String chargeRole;

    /**
     * 联系信息（会长联系方式）
     */
    @TableField(value = "contact_info")
    private String contactInfo;

    /**
     * 主要负责人社会职务
     */
    @TableField(value = "msocial_affiliation")
    private String msocialAffiliation;

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
    @TableField(value = "zhsocial_affiliation")
    private String zhSocialAffiliation;

    /**
     * 常驻地点
     */
    @TableField(value = "location")
    private String location;

    /**
     * 校友会logo
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 申请理由
     */
    @TableField(value = "application_reason")
    private String applicationReason;

    /**
     * 校友会简介
     */
    @TableField(value = "association_profile")
    private String associationProfile;

    /**
     * 初始成员列表（JSON格式）
     * 格式：[{"wxId": 123, "name": "张三", "roleId": 1}, {"wxId": 456, "name": "李四", "roleId": 2}]
     */
    @TableField(value = "initial_members")
    private String initialMembers;

    /**
     * 申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销
     */
    @TableField(value = "application_status")
    private Integer applicationStatus;

    /**
     * 审核人ID
     */
    @TableField(value = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField(value = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    @TableField(value = "review_comment")
    private String reviewComment;

    /**
     * 申请时间
     */
    @TableField(value = "apply_time")
    private LocalDateTime applyTime;

    /**
     * 申请材料附件ID数组（JSON格式）
     */
    @TableField(value = "attachment_ids")
    private String attachmentIds;

    /**
     * 创建的校友会ID（审核通过后自动创建校友会时填写）
     */
    @TableField(value = "created_association_id")
    private Long createdAssociationId;

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
}
