package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友列表项VO（互相关注）
 */
@Data
@Schema(description = "好友列表项VO")
public class FriendItemVo implements Serializable {

    @Schema(description = "好友关系ID")
    private String friendshipId;

    @Schema(description = "好友用户ID")
    private String friendWxId;

    @Schema(description = "好友用户名称")
    private String friendName;

    @Schema(description = "好友头像")
    private String friendAvatar;

    @Schema(description = "好友简介")
    private String friendDescription;

    @Schema(description = "关系类型：1-好友 2-同事 3-同学 4-校友 5-师生")
    private Integer relationship;

    @Schema(description = "状态：1-正常 2-仅聊天 3-消息免打扰 4-已隐藏 5-已拉黑")
    private Integer status;

    @Schema(description = "亲密度评分")
    private Integer intimacyScore;

    @Schema(description = "最后互动时间")
    private LocalDateTime lastInteract;

    @Schema(description = "我给对方的备注")
    private String myRemark;

    @Schema(description = "对方给我的备注")
    private String friendRemark;

    @Schema(description = "是否星标")
    private Boolean isStar;

    @Schema(description = "添加时间")
    private LocalDateTime addTime;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Serial
    private static final long serialVersionUID = 1L;
}
