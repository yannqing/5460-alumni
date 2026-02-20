package com.cmswe.alumni.common.model;
import lombok.Data;

@Data
public class ChatMessageContent {
    /**
     * 发送方用户id
     */
    private String formUserId;
    /**
     * 发送方用户名称
     */
    private String formUserName;
    /**
     * 发送方用户头像
     */
    private String formUserPortrait;
    /**
     * 消息内容类型 check in MessageContentType
     */
    private String type;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 扩展
     */
    private String ext;
}
