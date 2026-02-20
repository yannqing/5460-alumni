package com.cmswe.alumni.api.user;

import com.cmswe.alumni.common.enums.NotificationType;

/**
 * 统一消息API服务接口
 * 供其他模块调用消息发送功能
 */
public interface UnifiedMessageApiService {

    /**
     * 发送系统通知给单个用户
     *
     * @param userId           接收方用户ID
     * @param notificationType 通知类型
     * @param title            通知标题
     * @param content          通知内容
     * @return 是否发送成功
     */
    boolean sendSystemNotification(Long userId, NotificationType notificationType,
                                    String title, String content);

    /**
     * 发送系统通知给单个用户（带关联业务信息）
     *
     * @param userId           接收方用户ID
     * @param notificationType 通知类型
     * @param title            通知标题
     * @param content          通知内容
     * @param relatedId        关联业务ID
     * @param relatedType      关联业务类型
     * @return 是否发送成功
     */
    boolean sendSystemNotification(Long userId, NotificationType notificationType,
                                    String title, String content,
                                    Long relatedId, String relatedType);

    /**
     * 发送业务通知给单个用户
     *
     * @param userId      接收方用户ID
     * @param messageType 业务消息类型
     * @param title       通知标题
     * @param content     通知内容
     * @param relatedId   关联业务ID
     * @param relatedType 关联业务类型
     * @return 是否发送成功
     */
    boolean sendBusinessNotification(Long userId, String messageType,
                                      String title, String content,
                                      Long relatedId, String relatedType);
}
