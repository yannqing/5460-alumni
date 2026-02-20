package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@TableName(value ="chat_group_member")
@Data
public class ChatGroupMember {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 聊天群id
     */
    private Long chatGroupId;

    /**
     * 成员id
     */
    private Long userId;

    /**
     * 群备注
     */
    private String groupRemark;

    /**
     * 群昵称
     */
    private String groupName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
