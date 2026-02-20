package com.cmswe.alumni.service.user.service;

import com.cmswe.alumni.common.model.NotificationMessage;

/**
 * 消息通知生产者服务接口
 *
 * @author CMSWE
 * @since 2025-12-05
 */
public interface NotificationProducerService {

    /**
     * 发送用户通知消息
     *
     * @param message 通知消息
     */
    void sendUserNotification(NotificationMessage message);

    /**
     * 发送用户关注事件消息
     *
     * @param message 关注事件消息
     */
    void sendFollowEvent(NotificationMessage message);

    /**
     * 发送系统通知消息
     *
     * @param message 系统通知消息
     */
    void sendSystemNotification(NotificationMessage message);
}
