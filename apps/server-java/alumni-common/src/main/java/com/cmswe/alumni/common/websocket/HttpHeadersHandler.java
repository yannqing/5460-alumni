package com.cmswe.alumni.common.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * HttpHeadersHandler 类负责处理 HTTP 请求的头信息和查询参数。
 * 它在 WebSocket 握手期间提取 x-token 和客户端 IP 地址，并将其存储在通道属性中。
 * 更新请求的 URI，仅保留路径部分，并将请求传递给下一个处理器。
 */
public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 检查接收到的消息是否为 FullHttpRequest
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            // 使用 UrlBuilder 解析请求的 URI
            UrlBuilder urlBuilder = UrlBuilder.ofHttp(request.uri());

            // 提取查询参数中的 x-token 值
            String token = Optional.ofNullable(urlBuilder.getQuery())
                    .map(k -> k.get("x-token"))
                    .map(CharSequence::toString)
                    .orElse("");
            // 将 token 存储在 Channel 的属性中
            NettyUtil.setAttr(ctx.channel(), NettyUtil.TOKEN, token);

            // 更新请求的 URI，仅保留路径部分
            request.setUri(urlBuilder.getPath().toString());
            HttpHeaders headers = request.headers();

            // 从请求头中获取 x-ip 值
            String ip = headers.get("x-ip");
            // 如果 x-ip 为空，则获取客户端的 IP 地址
            if (StringUtils.isEmpty(ip)) {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
            }
            // 将 IP 存储在 Channel 的属性中
            NettyUtil.setAttr(ctx.channel(), NettyUtil.IP, ip);

            // 移除当前处理器，并将修改后的请求传递给下一个处理器
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(request);
        } else {
            // 如果接收到的消息不是 FullHttpRequest，直接传递消息给下一个处理器
            ctx.fireChannelRead(msg);
        }
    }
}
