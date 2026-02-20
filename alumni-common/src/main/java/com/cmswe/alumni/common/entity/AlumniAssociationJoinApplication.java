package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校友会加入申请表
 * @TableName alumni_association_join_application
 */
@TableName(value = "alumni_association_join_application")
@Data
public class AlumniAssociationJoinApplication implements Serializable {
    /**
     * 申请ID
     */
    @TableId(value = "application_id", type = IdType.ASSIGN_ID)
    private Long applicationId;

    /**
     * 校友会ID
     */
    @TableField(value = "alumni_association_id")
    private Long alumniAssociationId;

    /**
     * 申请人类型：1-用户 2-商户
     */
    @TableField(value = "applicant_type")
    private Integer applicantType;

    /**
     * 申请人ID（用户ID或商户ID）
     */
    @TableField(value = "target_id")
    private Long targetId;

    /**
     * 申请理由
     */
    @TableField(value = "application_reason")
    private String applicationReason;

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
     * 学校ID
     */
    @TableField(value = "school_id")
    private Long schoolId;

    /**
     * 入学年份
     */
    @TableField(value = "enrollment_year")
    private Integer enrollmentYear;

    /**
     * 毕业年份
     */
    @TableField(value = "graduation_year")
    private Integer graduationYear;

    /**
     * 院系
     */
    @TableField(value = "department")
    private String department;

    /**
     * 专业
     */
    @TableField(value = "major")
    private String major;

    /**
     * 班级
     */
    @TableField(value = "class_name")
    private String className;

    /**
     * 学历层次
     */
    @TableField(value = "education_level")
    private String educationLevel;

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
