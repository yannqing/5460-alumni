package com.cmswe.alumni.common.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.Future;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


@Slf4j
//@Configuration  // 已禁用，改用 NettyWebSocketServletConfig
public class NettyWebSocketServer {

    @Deprecated
    public static final int Web_Socket_Port = 9100;  // WebSocket 服务器端口（已废弃，现在与主应用共用端口）
    public static final NettyWebSocketServerHandler Netty_Web_Socket_Server_Handler = new NettyWebSocketServerHandler();

    // 事件循环组，用于处理接入的连接
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);//处理连接
    private EventLoopGroup workerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors());//处理消息

    /**
     * 启动 WebSocket 服务器
     * @deprecated 已废弃，现在使用 NettyWebSocketServletConfig 启动
     */
    @Deprecated
    //@PostConstruct  // 已禁用
    public void start() throws InterruptedException {
        run();
    }

    /**
     * 销毁 WebSocket 服务器
     */
    @PreDestroy
    public void destroy() {
        // 优雅关闭事件循环组
        Future<?> future = bossGroup.shutdownGracefully();
        Future<?> future1 = workerGroup.shutdownGracefully();
        future.syncUninterruptibly();
        future1.syncUninterruptibly();
        log.info("销毁成功");
    }

    /**
     * 运行 WebSocket 服务器
     * @deprecated 已废弃，现在使用 NettyWebSocketServletConfig 启动
     */
    @Deprecated
    public void run() throws InterruptedException {
        // 服务器启动引导对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)  // 指定 NIO 传输通道
                .option(ChannelOption.SO_BACKLOG, 128)  // 设置等待连接的队列长度
                .option(ChannelOption.SO_KEEPALIVE, true) // 启用保持活动
                .handler(new LoggingHandler(LogLevel.INFO)) // 为 bossGroup 添加日志处理器
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //设置必须的处理器
                        pipeline.addLast(new HttpServerCodec());  // HTTP 编解码器
                        pipeline.addLast(new ChunkedWriteHandler()); // 处理大数据流
                        pipeline.addLast(new HttpObjectAggregator(8192)); // 聚合 HTTP 消息
                        pipeline.addLast(new HttpHeadersHandler()); // 自定义头处理器
//                        pipeline.addLast(new IdleStateHandler(30, 0, 0));  // 心跳检测
                        pipeline.addLast(new IdleStateHandler(3000, 0, 0));  // 心跳检测
                        pipeline.addLast(new HeartBeatHandler());//心跳处理
                        pipeline.addLast(new WebSocketServerProtocolHandler("/ws")); // 升级为 WebSocket 协议处理
                        pipeline.addLast(Netty_Web_Socket_Server_Handler); // 自定义的 WebSocket 处理器 (如心跳规则/用户登入）
                    }
                });
        // 绑定端口并启动服务
        serverBootstrap.bind(Web_Socket_Port).sync();
    }
}
