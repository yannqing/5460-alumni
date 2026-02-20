  package com.cmswe.alumni.common.websocket;

  import io.netty.channel.Channel;

  /**
   * WebSocket 业务处理接口
   * 由具体业务模块实现
   */
  public interface IWebSocketHandler {

      /**
       * 用户上线处理
       * @param channel 通道
       * @param token 认证令牌
       */
      void online(Channel channel, String token);

      /**
       * 用户下线处理
       * @param channel 通道
       */
      void offline(Channel channel);
  }
