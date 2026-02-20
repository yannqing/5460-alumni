package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关注关系表
 * @TableName user_follow
 */
@TableName(value = "user_follow")
@Data
public class UserFollow implements Serializable {
    /**
     * 关注ID
     */
    @TableId(value = "follow_id", type = IdType.AUTO)
    private Long followId;

    /**
     * 关注者用户ID
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 关注目标类型：1-用户，2-校友会，3-母校，4-商户
     */
    @TableField(value = "target_type")
    private Integer targetType;

    /**
     * 关注目标ID
     */
    @TableField(value = "target_id")
    private Long targetId;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 关注状态：1-正常关注 2-特别关注 3-免打扰 4-已取消
     */
    @TableField(value = "follow_status")
    private Integer followStatus;

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