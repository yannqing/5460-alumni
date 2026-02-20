package com.cmswe.alumni.common.websocket;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * NettyUtil 类提供了用于操作 Netty Channel 属性的工具方法。
 * 它允许存储和获取与通道相关的自定义属性，如 IP 地址和 Token。
 */
public class NettyUtil {

    // 定义用于存储 IP 地址的 AttributeKey
    public static AttributeKey<String> IP = AttributeKey.valueOf("x-ip");
    // 定义用于存储 Token 的 AttributeKey
    public static AttributeKey<String> TOKEN = AttributeKey.valueOf("x-token");

    /**
     * 设置指定通道的属性。
     *
     * @param channel       要设置属性的通道
     * @param attributeKey  属性键
     * @param data          要存储的属性值
     * @param <T>          属性值的类型
     */
    public static <T> void setAttr(Channel channel, AttributeKey<T> attributeKey, T data) {
        // 获取指定属性键的 Attribute 对象
        Attribute<T> attr = channel.attr(attributeKey);
        // 设置属性值
        attr.set(data);
    }

    /**
     * 获取指定通道的属性值。
     *
     * @param channel       要获取属性的通道
     * @param attributeKey  属性键
     * @param <T>          属性值的类型
     * @return             属性值，如果不存在则返回 null
     */
    public static <T> T getAttr(Channel channel, AttributeKey<T> attributeKey) {
        // 获取指定属性键的值
        return channel.attr(attributeKey).get();
    }
}
