package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户工作经历表
 * @TableName wx_user_work
 */
@TableName(value = "wx_user_work")
@Data
public class WxUserWork implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "user_work_id", type = IdType.ASSIGN_ID)
    private Long userWorkId;

    /**
     * 关联用户表ID
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 公司名称
     */
    @TableField(value = "company_name")
    private String companyName;

    /**
     * 职位/角色名称
     */
    @TableField(value = "position")
    private String position;

    /**
     * 所属行业
     */
    @TableField(value = "industry")
    private String industry;

    /**
     * 入职日期
     */
    @TableField(value = "start_date")
    private LocalDate startDate;

    /**
     * 离职日期（NULL表示至今）
     */
    @TableField(value = "end_date")
    private LocalDate endDate;

    /**
     * 是否当前在职：0-否，1-是
     */
    @TableField(value = "is_current")
    private Integer isCurrent;

    /**
     * 工作内容/项目成就详情
     */
    @TableField(value = "work_description")
    private String workDescription;

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
     * 是否删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}