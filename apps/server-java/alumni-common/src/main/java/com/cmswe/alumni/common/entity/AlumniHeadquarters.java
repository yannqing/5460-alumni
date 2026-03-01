package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 校友总会表
 * 
 * @TableName alumni_headquarters
 */
@TableName(value = "alumni_headquarters")
@Data
public class AlumniHeadquarters implements Serializable {

    /**
     * 校友总会ID
     */
    @TableId(value = "headquarters_id", type = IdType.ASSIGN_ID)
    private Long headquartersId;

    /**
     * 校友总会名称
     */
    @TableField(value = "headquarters_name")
    private String headquartersName;

    /**
     * 所属母校ID
     */
    @TableField(value = "school_id")
    private Long schoolId;

    /**
     * logo
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 校友总会描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 联系信息
     */
    @TableField(value = "contact_info")
    private String contactInfo;

    /**
     * 办公地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * 官方网站
     */
    @TableField(value = "website")
    private String website;

    /**
     * 微信公众号
     */
    @TableField(value = "wechat_public_account")
    private String wechatPublicAccount;

    /**
     * 联系邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 联系电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 成立日期
     */
    @TableField(value = "established_date")
    private LocalDate establishedDate;

    /**
     * 下属校友会总数
     */
    @TableField(value = "member_count")
    private Integer memberCount;

    /**
     * 活跃状态：0-不活跃 1-活跃
     */
    @TableField(value = "active_status")
    private Integer activeStatus;

    /**
     * 审核状态：0-待审核 1-已通过 2-已驳回
     */
    @TableField(value = "approval_status")
    private Integer approvalStatus;

    /**
     * 级别：1-校级 2-省级 3-国家级 4-国际级
     */
    @TableField(value = "level")
    private Integer level;

    /**
     * 创建码
     */
    @TableField(value = "create_code")
    private Integer createCode;

    /**
     * 创建人ID
     */
    @TableField(value = "created_user_id")
    private Long createdUserId;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新人ID
     */
    @TableField(value = "updated_user_id")
    private Long updatedUserId;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 获取排序字段方法
     * 
     * @param sortField 排序字段名
     * @return 排序方法引用
     */
    public static SFunction<AlumniHeadquarters, ?> getSortMethod(String sortField) {
        if (sortField == null) {
            return AlumniHeadquarters::getCreatedTime;
        }
        return switch (sortField) {
            case "memberCount" -> AlumniHeadquarters::getMemberCount;
            case "createdTime" -> AlumniHeadquarters::getCreatedTime;
            case "updatedTime" -> AlumniHeadquarters::getUpdatedTime;
            case "establishedDate" -> AlumniHeadquarters::getEstablishedDate;
            case "level" -> AlumniHeadquarters::getLevel;
            default -> AlumniHeadquarters::getCreatedTime;
        };
    }
}
