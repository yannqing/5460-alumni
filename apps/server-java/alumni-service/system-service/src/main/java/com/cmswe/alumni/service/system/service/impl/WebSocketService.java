package com.cmswe.alumni.service.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.cmswe.alumni.api.user.ChatGroupMemberService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.common.constant.WebSocketContentType;
import com.cmswe.alumni.common.entity.ChatGroupMember;
import com.cmswe.alumni.common.entity.ChatMessage;
import com.cmswe.alumni.common.enums.SourceType;
import com.cmswe.alumni.common.websocket.IWebSocketHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketService implements IWebSocketHandler {

    @Data
    public static class WsContent {
        private String type;
        private Object content;
    }

    @Resource
    private ChatGroupMemberService chatGroupMemberService;

    @Lazy
    @Resource
    private UserService userService;

    public static final ConcurrentHashMap<String, List<Channel>> Online_User = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Channel, String> Online_Channel = new ConcurrentHashMap<>();

    /**
     * 用户上线
     * @param channel
     * @param token
     */
    public void online(Channel channel, String token) {
        try {
            String userId = userService.onlineByToken(token);
            // 获取当前用户的通道列表
            List<Channel> channels = Online_User.getOrDefault(userId, new ArrayList<>());
            // 添加新通道
            channels.add(channel);
            // 检查通道数量，超过2个时下线最旧的通道
            if (channels.size() > 2) {
                // 下线最旧的通道
                Channel oldestChannel = channels.remove(0); // 移除最旧的通道
                oldestChannel.close(); // 关闭最旧通道
            }
            // 更新在线用户的通道列表
            Online_User.put(userId, channels);
            // 维护频道与用户 ID 的映射
            Online_Channel.put(channel, userId);
        } catch (Exception e) {
            sendMsg(channel, "连接错误", WebSocketContentType.Msg);
            channel.close();
        }
    }

    /**
     * 用户下线
     * @param channel
     */
    public void offline(Channel channel) {
        String userId = Online_Channel.get(channel);
        if (StringUtils.isNotBlank(userId)) {
            List<Channel> channels = Online_User.get(userId);
            if(channels != null){
                channels.remove(channel);
                if(channels.isEmpty()){
                    Online_User.remove(userId);
                    userService.offline(userId);
                }else {
                    Online_User.put(userId, channels);
                }
            }
            Online_Channel.remove(channel);
        }
    }

    /**
     * 发送消息给指定用户
     * @param channel
     * @param msg
     * @param type
     */
    private void sendMsg(Channel channel, Object msg, String type) {
        WsContent wsContent = new WsContent();
        wsContent.setType(type);
        wsContent.setContent(msg);
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(wsContent)));
    }

    /**
     * 发送消息给用户
     * @param msg
     * @param userId
     */
    public void sendMsgToUser(Object msg, String userId) {
        List<Channel> channels = Online_User.get(userId);
        if (channels != null) {
            channels.forEach(channel -> {
                sendMsg(channel, msg, WebSocketContentType.Msg);
            });
        }
    }

    /**
     * 发送消息给群组成员
     * @param chatMessage
     * @param groupId
     */
    public void sendMsgToGroup(ChatMessage chatMessage, String groupId) {
        //todo 待fix发送给自己的bug 可能需要修改查询代码
        List<ChatGroupMember> list = chatGroupMemberService.getGroupMemberByGroupId(groupId);
        for (ChatGroupMember member : list) {
            if (!chatMessage.getFromId().equals(member.getUserId()) || SourceType.GROUP.equals(chatMessage.getSourceType())) {
                sendMsgToUser(chatMessage, String.valueOf(member.getUserId()));
            }
        }
    }

    /**
     * 发送消息给所有在线用户
     * @param msg
     */
    public void sendMsgAll(Object msg) {
        Online_Channel.forEach((channel, ext) -> {
            sendMsg(channel, msg, WebSocketContentType.Msg);
        });
    }

    /**
     * 发送系统通知给指定用户
     * @param msg
     * @param userId
     */
    public void sendNotifyToUser(Object msg, String userId) {
        List<Channel> channels = Online_User.get(userId);
        if (channels != null) {
            channels.forEach(channel -> {
                sendMsg(channel, msg, WebSocketContentType.Notify);
            });
        }
    }

    /**
     * 发送系统消息给指定群组成员
     * @param chatMessage
     * @param groupId
     */
    public void sendNoticeToGroup(ChatMessage chatMessage, String groupId) {
        List<ChatGroupMember> list = chatGroupMemberService.getGroupMemberByGroupId(groupId);
        for (ChatGroupMember member : list) {
            if (!chatMessage.getFromId().equals(member.getUserId()) || SourceType.SYSTEM.equals(chatMessage.getSourceType())) {
                sendNotifyToUser(chatMessage, String.valueOf(member.getUserId()));
            }
        }
    }

    /**
     * 发送媒体消息给指定用户
     * @param msg
     * @param userId
     */
    public void sendVideoToUser(Object msg, String userId) {
        List<Channel> channels = Online_User.get(userId);
        if (channels != null) {
            channels.forEach(channel -> {
                sendMsg(channel, msg, WebSocketContentType.Media);
            });
        }
    }

    /**
     * 发送系统通知给所有在线用户
     * @param msg
     */
    public void sendNotifyAll(Object msg) {
        Online_Channel.forEach((channel, ext) -> {
            sendMsg(channel, msg, WebSocketContentType.Notify);
        });
    }

}
