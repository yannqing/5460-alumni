package com.cmswe.alumni.common.websocket;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Netty Channel 适配器
 * 将 Jakarta WebSocket Session 适配为 Netty Channel
 * 使得现有的业务逻辑（基于 Netty Channel）可以无缝工作
 */
@Slf4j
public class NettyChannelAdapter implements Channel {

    private final Session session;

    public NettyChannelAdapter(Session session) {
        this.session = session;
    }

    /**
     * 判断 Channel 是否打开
     */
    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    /**
     * 关闭 Channel
     */
    @Override
    public ChannelFuture close() {
        try {
            session.close();
        } catch (IOException e) {
            log.error("关闭WebSocket会话失败", e);
        }
        return new DefaultChannelPromise(this);
    }

    /**
     * 写入并刷新数据
     * 将 Netty 的 TextWebSocketFrame 转换为 Jakarta WebSocket 的消息
     */
    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        try {
            if (msg instanceof TextWebSocketFrame) {
                TextWebSocketFrame frame = (TextWebSocketFrame) msg;
                String text = frame.text();
                session.getBasicRemote().sendText(text);
            } else if (msg instanceof String) {
                session.getBasicRemote().sendText((String) msg);
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
        return new DefaultChannelPromise(this);
    }

    /**
     * 获取会话 ID（用作 Channel ID）
     */
    @Override
    public ChannelId id() {
        return new ChannelId() {
            @Override
            public String asShortText() {
                return session.getId();
            }

            @Override
            public String asLongText() {
                return session.getId();
            }

            @Override
            public int compareTo(ChannelId o) {
                return session.getId().compareTo(o.asShortText());
            }
        };
    }

    // ==================== 以下是未实现的方法，使用默认实现 ====================

    @Override
    public EventLoop eventLoop() {
        return null;
    }

    @Override
    public Channel parent() {
        return null;
    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isRegistered() {
        return true;
    }

    @Override
    public boolean isActive() {
        return session.isOpen();
    }

    @Override
    public ChannelMetadata metadata() {
        return null;
    }

    @Override
    public SocketAddress localAddress() {
        return null;
    }

    @Override
    public SocketAddress remoteAddress() {
        return null;
    }

    @Override
    public ChannelFuture closeFuture() {
        return null;
    }

    @Override
    public boolean isWritable() {
        return session.isOpen();
    }

    @Override
    public long bytesBeforeUnwritable() {
        return 0;
    }

    @Override
    public long bytesBeforeWritable() {
        return 0;
    }

    @Override
    public Unsafe unsafe() {
        return null;
    }

    @Override
    public ChannelPipeline pipeline() {
        return null;
    }

    @Override
    public ByteBufAllocator alloc() {
        return null;
    }

    @Override
    public Channel read() {
        return this;
    }

    @Override
    public Channel flush() {
        return this;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return null;
    }

    @Override
    public ChannelFuture disconnect() {
        return null;
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return close();
    }

    @Override
    public ChannelFuture deregister() {
        return null;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture write(Object msg) {
        return writeAndFlush(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return writeAndFlush(msg);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return writeAndFlush(msg);
    }

    @Override
    public ChannelPromise newPromise() {
        return new DefaultChannelPromise(this);
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return null;
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return null;
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return null;
    }

    @Override
    public ChannelPromise voidPromise() {
        return new DefaultChannelPromise(this);
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return null;
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return false;
    }

    @Override
    public int compareTo(Channel o) {
        return id().asShortText().compareTo(o.id().asShortText());
    }
}
