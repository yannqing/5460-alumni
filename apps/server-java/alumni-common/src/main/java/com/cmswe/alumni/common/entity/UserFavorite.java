package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏关系表
 */
@Data
@TableName("user_favorite")
public class UserFavorite implements Serializable {

    /**
     * 收藏ID（雪花ID）
     */
    @TableId(value = "favorite_id", type = IdType.INPUT)
    private Long favoriteId;

    /**
     * 用户ID（wx_id）
     */
    @TableField("wx_id")
    private Long wxId;

    /**
     * 收藏目标类型：1-商户
     */
    @TableField("target_type")
    private Integer targetType;

    /**
     * 收藏目标ID（本次使用 merchant_id）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 收藏备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除：0-否，1-是
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    @Serial
    private static final long serialVersionUID = 1L;
}
