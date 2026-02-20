package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询好友列表DTO（互相关注）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询好友列表DTO")
public class QueryFriendListDto extends PageRequest implements Serializable {

    @Schema(description = "关系类型：1-好友 2-同事 3-同学 4-校友 5-师生")
    private Integer relationship;

    @Schema(description = "状态：1-正常 2-仅聊天 3-消息免打扰 4-已隐藏 5-已拉黑")
    private Integer status;

    @Schema(description = "搜索关键词（好友名称或备注）")
    private String keyword;

    @Schema(description = "是否只查询星标好友")
    private Boolean onlyStar;

    @Serial
    private static final long serialVersionUID = 1L;
}
