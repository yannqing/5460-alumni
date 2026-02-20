package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天群组实体类
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Data
@TableName("chat_group")
public class ChatGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 群名称
     */
    private String groupName;

    /**
     * 群头像URL
     */
    private String groupAvatar;

    /**
     * 群主用户ID
     */
    private Long groupOwnerId;

    /**
     * 群类型：NORMAL-普通群, ORGANIZATION-组织群, DEPARTMENT-部门群
     */
    private String groupType;

    /**
     * 当前成员数量
     */
    private Integer memberCount;

    /**
     * 最大成员数量
     */
    private Integer maxMemberCount;

    /**
     * 群公告
     */
    private String groupNotice;

    /**
     * 群简介
     */
    private String groupDescription;

    /**
     * 加群方式：FREE-自由加入, APPROVE-需要审核, INVITE-仅邀请
     */
    private String joinMode;

    /**
     * 是否全员禁言：0-否, 1-是
     */
    private Integer muteAll;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除, 1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
