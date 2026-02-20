package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送消息DTO
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Data
@Schema(description = "发送消息请求")
public class SendMessageDto {

    @NotNull(message = "接收方ID不能为空")
    @Schema(description = "接收方ID（用户ID或群组ID）", example = "123456")
    private Long toId;

    @NotNull(message = "消息内容不能为空")
    @Schema(description = "消息内容", example = "你好，在吗？")
    private String content;

    @Schema(description = "消息格式：text-文本, image-图片, video-视频, audio-音频, file-文件", example = "text")
    private String messageFormat;

    @Schema(description = "消息类型：message-普通消息, notify-通知消息, media-媒体消息", example = "message")
    private String messageType;

    @Schema(description = "消息源类型：user-用户消息, group-群组消息", example = "user")
    private String sourceType;

    @Schema(description = "扩展数据（JSON格式）")
    private String ext;
}
