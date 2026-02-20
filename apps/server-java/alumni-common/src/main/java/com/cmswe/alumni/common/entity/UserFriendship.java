package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户好友关系表（双向关系）
 * @TableName user_friendship
 */
@TableName(value = "user_friendship")
@Data
public class UserFriendship implements Serializable {
    /**
     * 好友关系ID
     */
    @TableId(value = "friendship_id", type = IdType.AUTO)
    private Long friendshipId;

    /**
     * 用户A ID（较小的ID）
     */
    @TableField(value = "wx_id_a")
    private Long wxIdA;

    /**
     * 用户B ID（较大的ID）
     */
    @TableField(value = "wx_id_b")
    private Long wxIdB;

    /**
     * 关系类型：1-好友 2-同事 3-同学 4-校友 5-师生
     */
    @TableField(value = "relationship")
    private Integer relationship;

    /**
     * 状态：1-正常 2-仅聊天 3-消息免打扰 4-已隐藏 5-已拉黑
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 亲密度评分
     */
    @TableField(value = "intimacy_score")
    private Integer intimacyScore;

    /**
     * 最后互动时间
     */
    @TableField(value = "last_interact")
    private LocalDateTime lastInteract;

    /**
     * 互动次数
     */
    @TableField(value = "interact_count")
    private Integer interactCount;

    /**
     * A给B的备注
     */
    @TableField(value = "remark_a_to_b")
    private String remarkAToB;

    /**
     * B给A的备注
     */
    @TableField(value = "remark_b_to_a")
    private String remarkBToA;

    /**
     * 标签列表（JSON数组）
     */
    @TableField(value = "tags")
    private String tags;

    /**
     * A是否星标B
     */
    @TableField(value = "is_star_a")
    private Integer isStarA;

    /**
     * B是否星标A
     */
    @TableField(value = "is_star_b")
    private Integer isStarB;

    /**
     * 添加来源：0-未知 1-扫码 2-名片分享 3-群聊 4-搜索 5-推荐
     */
    @TableField(value = "source_type")
    private Integer sourceType;

    /**
     * 来源标识（如群ID、推荐ID等）
     */
    @TableField(value = "source_id")
    private String sourceId;

    /**
     * 添加时的备注
     */
    @TableField(value = "add_remarks")
    private String addRemarks;

    /**
     * 添加时间
     */
    @TableField(value = "add_time")
    private LocalDateTime addTime;

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
     * 是否删除：0-正常 1-删除
     */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
