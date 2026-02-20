package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cmswe.alumni.common.enums.MessageFormat;
import com.cmswe.alumni.common.enums.MessageStatus;
import com.cmswe.alumni.common.enums.MessageType;
import com.cmswe.alumni.common.enums.SourceType;
import com.cmswe.alumni.common.model.ChatMessageContent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = false)
@Data
@TableName(value = "chat_message", autoResultMap = true)
public class ChatMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long messageId;

    /**
     * Kafka 消息id
     */
    @TableField("kf_msg_id")
    private String kfMsgId;

    /**
     * 消息发送方 id
     */
    @TableField("from_id")
    private Long fromId;

    /**
     * 消息接受方 id
     */
    @TableField("to_id")
    private Long toId;

    /**
     * 消息格式 (text,image,video,audio.file) check in MessageFormat
     */
    @TableField("`message_format`")
    private MessageFormat messageFormat;

    /**
     * 消息类型 (message,notify,media)
     */
    @TableField("`message_type`")
    private MessageType messageType;

    /**
     * 消息内容
     */
    @TableField(value = "msg_content", typeHandler = JacksonTypeHandler.class)
    private ChatMessageContent ChatMessageContent;

    /**
     * 是否显示时间
     */
    @TableField("is_show_time")
    private Boolean isShowTime;

    /**
     * 消息状态：0-发送中, 1-已送达, 2-已读, 3-失败, 4-撤回
     */
    @TableField("status")
    private MessageStatus status;

    /**
     * 消息源 (user,system,group)
     */
    @TableField("`source_type`")
    private SourceType sourceType;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
