package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户黑名单表
 * @TableName user_blacklist
 */
@TableName(value = "user_blacklist")
@Data
public class UserBlacklist implements Serializable {
    /**
     * 黑名单记录ID
     */
    @TableId(value = "black_id", type = IdType.AUTO)
    private Long blackId;

    /**
     * 操作用户ID（拉黑方）
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 被拉黑用户ID
     */
    @TableField(value = "blocked_wx_id")
    private Long blockedWxId;

    /**
     * 拉黑类型：1-单向拉黑 2-双向拉黑
     */
    @TableField(value = "block_type")
    private Integer blockType;

    /**
     * 限制范围（JSON数组）：1-消息 2-动态 3-评论 4-@提及 5-查找 6-推荐
     */
    @TableField(value = "block_scopes")
    private String blockScopes;

    /**
     * 拉黑原因
     */
    @TableField(value = "block_reason")
    private String blockReason;

    /**
     * 操作人ID（管理员操作时）
     */
    @TableField(value = "operator_id")
    private Long operatorId;

    /**
     * 拉黑到期时间（NULL为永久）
     */
    @TableField(value = "expire_time")
    private LocalDateTime expireTime;

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
     * 是否删除：0-正常 1-已解除
     */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
