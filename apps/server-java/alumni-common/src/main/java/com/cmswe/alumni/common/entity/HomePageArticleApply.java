package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 首页公众号文章审核表
 * @TableName home_page_article_apply
 */
@TableName(value = "home_page_article_apply")
@Data
public class HomePageArticleApply implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "home_article_apply_id", type = IdType.ASSIGN_ID)
    private Long homeArticleApplyId;

    /**
     * 首页文章id
     */
    @TableField(value = "home_article_id")
    private Long homeArticleId;

    /**
     * 审核状态 0-审核中，1-审核通过，2-审核拒绝
     */
    @TableField(value = "apply_status")
    private Integer applyStatus;

    /**
     * 审批人id
     */
    @TableField(value = "applied_wx_id")
    private Long appliedWxId;

    /**
     * 审批人名称
     */
    @TableField(value = "applied_name")
    private String appliedName;

    /**
     * 审批意见
     */
    @TableField(value = "applied_description")
    private String appliedDescription;

    /**
     * 审核完成时间
     */
    @TableField(value = "completed_time")
    private LocalDateTime completedTime;

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
