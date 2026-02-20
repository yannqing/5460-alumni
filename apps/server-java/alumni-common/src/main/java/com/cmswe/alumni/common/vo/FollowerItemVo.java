package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的粉丝列表项VO
 */
@Data
@Schema(description = "我的粉丝列表项VO")
public class FollowerItemVo implements Serializable {

    @Schema(description = "关注ID")
    private String followId;

    @Schema(description = "粉丝用户ID")
    private String wxId;

    @Schema(description = "粉丝用户名称")
    private String userName;

    @Schema(description = "粉丝头像")
    private String avatar;

    @Schema(description = "粉丝简介")
    private String description;

    @Schema(description = "关注状态：1-正常关注 2-特别关注 3-免打扰 4-已取消")
    private Integer followStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否互相关注（是否为好友）")
    private Boolean isMutualFollow;

    @Schema(description = "关注时间")
    private LocalDateTime createdTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
