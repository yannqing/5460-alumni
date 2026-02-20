package com.cmswe.alumni.common.websocket;

import cn.hutool.extra.spring.SpringUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable  // 表示这个处理器可以被多个 Channel 共享
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private IWebSocketHandler webSocketService;  // 用于处理 WebSocket 相关的服务

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 当处理器被添加时，获取 WebSocketService 的实例
        if (this.webSocketService == null) {
            this.webSocketService = SpringUtil.getBean(IWebSocketHandler.class);
        }
    }

    // 处理下线逻辑
    private void offLine(ChannelHandlerContext ctx) {
        log.info("关闭连接");
        webSocketService.offline(ctx.channel());  // 标记用户为离线
        ctx.channel().close();  // 关闭 Channel
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerRemoved");
        offLine(ctx);  // 处理器被移除时，下线用户
    }

    /**
     * 通道就绪后 调用，一般用户来初始化
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入");
    }

    /**
     * 当 Channel 不再活动时触发
     *
     * @param ctx ChannelHandlerContext
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelInactive");
        offLine(ctx);  // 处理不活动状态
    }

    /**
     * 处理用户事件（如心跳Heartbeat）一般是处理刚刚握手之后，以及心跳超时事件（但是由于我自定义了心跳处理器，里面超时处理没有调用这个，所以不会进入）
     *
     * @param ctx ChannelHandlerContext
     * @param evt 事件对象
     * @throws Exception 异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            // 获取 WebSocket 握手完成后的 token
            String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            try{
                webSocketService.online(ctx.channel(), token);  // 标记用户为在线
            }catch (Exception e){
                offLine(ctx);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught");
        // 处理异常，关闭 Channel
        ctx.channel().close();
    }

    /**
     * 这里是接受websocket的信息， 我们用http接收信息，websocket推送消息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
        String ip = NettyUtil.getAttr(ctx.channel(), NettyUtil.IP);
        log.info("token:{}",token);
        log.info("ip:{}",ip);
        log.info("channelRead0");
    }
}
