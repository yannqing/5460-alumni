package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.enums.MessageFormat;
import com.cmswe.alumni.common.enums.MessageType;
import com.cmswe.alumni.common.enums.SourceType;
import com.cmswe.alumni.common.model.ChatMessageContent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息VO
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Data
@Schema(description = "聊天消息")
public class ChatMessageVo {

    @Schema(description = "消息唯一ID")
    private String messageId;

    @Schema(description = "发送方ID")
    private String fromId;

    @Schema(description = "接收方ID")
    private String toId;

    @Schema(description = "消息格式")
    private MessageFormat messageFormat;

    @Schema(description = "消息类型")
    private MessageType messageType;

    @Schema(description = "消息内容")
    private ChatMessageContent msgContent;

    @Schema(description = "是否显示时间")
    private Boolean isShowTime;

    @Schema(description = "消息状态：0-发送中, 1-已送达, 2-已读, 3-失败, 4-撤回")
    private Integer status;

    @Schema(description = "消息源类型")
    private SourceType sourceType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否是我发送的")
    private Boolean isMine;
}
