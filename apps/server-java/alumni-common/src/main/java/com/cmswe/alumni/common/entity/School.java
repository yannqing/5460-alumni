package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 母校信息表
 * @TableName school
 */
@TableName(value = "school")
@Data
public class School implements Serializable {

    /**
     * 母校ID（雪花）
     */
    @TableId(value = "school_id", type = IdType.ASSIGN_ID)
    private Long schoolId;

    /**
     * 学校logo
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 学校名称
     */
    @TableField(value = "school_name")
    private String schoolName;

    /**
     * 学校编码
     */
    @TableField(value = "school_code")
    private String schoolCode;

    /**
     * 校友总会ID
     */
    @TableField(value = "headquarters_id")
    private Long headquartersId;

    /**
     * 所在省
     */
    @TableField(value = "province")
    private String province;

    /**
     * 所在市
     */
    @TableField(value = "city")
    private String city;

    /**
     * 办学层次
     */
    @TableField(value = "level")
    private String level;

    /**
     * 合并院校（json 数组）
     */
    @TableField(value = "merged_institutions")
    private String mergedInstitutions;

    /**
     * 曾用名（json 数组）
     */
    @TableField(value = "previous_name")
    private String previousName;

    /**
     * 其他内容
     */
    @TableField(value = "other_info")
    private String otherInfo;

    /**
     * 学校描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 建校日期
     */
    @TableField(value = "founding_date")
    private LocalDate foundingDate;

    /**
     * 学校地址
     */
    @TableField(value = "location")
    private String location;

    /**
     * 官方认证状态（0-未认证，1-已认证）
     */
    @TableField(value = "official_certification")
    private Integer officialCertification;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 获取排序字段方法
     * @param sortField 排序字段名
     * @return 排序方法引用
     */
    public static SFunction<School, ?> getSchoolSortMethod(String sortField) {
        return switch (sortField) {
            case "createTime" -> School::getCreateTime;
            case "updateTime" -> School::getUpdateTime;
            case "schoolName" -> School::getSchoolName;
            default -> School::getCreateTime;
        };
    }
}
