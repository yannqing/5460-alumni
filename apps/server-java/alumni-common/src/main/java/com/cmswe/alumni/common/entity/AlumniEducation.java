package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校友教育经历表
 * @TableName alumni_education
 */
@TableName(value = "alumni_education")
@Data
public class AlumniEducation implements Serializable {
    /**
     * 教育经历ID
     */
    @TableId(value = "alumni_education_id", type = IdType.ASSIGN_ID)
    private Long alumniEducationId;

    /**
     * 微信用户ID
     */
    @TableField(value = "wx_id")
    private Long wxId;

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
     * 学位
     */
    @TableField(value = "degree")
    private String degree;

    /**
     * 认证状态
     */
    @TableField(value = "certification_status")
    private Integer certificationStatus;

    /**
     * 类型（1 主要经历 0 次要经历）
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private LocalDateTime createdTime;

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
}