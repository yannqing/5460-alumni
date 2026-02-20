package com.cmswe.alumni.service.user.example;

import com.cmswe.alumni.common.enums.NotificationTypeEnum;
import com.cmswe.alumni.common.model.NotificationMessage;
import com.cmswe.alumni.service.user.service.NotificationProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户关注服务 + Kafka 集成示例
 *
 * 本示例展示如何在用户关注功能中集成 Kafka 消息队列
 *
 * 使用方式：
 * 1. 在现有的 UserFollowServiceImpl 中注入 NotificationProducerService
 * 2. 在关注/取消关注方法中调用发送消息的方法
 *
 * @author CMSWE
 * @since 2025-12-05
 */
@Slf4j
@Service
public class UserFollowServiceWithKafkaExample {

    private final NotificationProducerService notificationProducerService;

    public UserFollowServiceWithKafkaExample(NotificationProducerService notificationProducerService) {
        this.notificationProducerService = notificationProducerService;
    }

    /**
     * 关注用户时发送消息示例
     *
     * 在 UserFollowServiceImpl.follow() 方法中，当关注成功后调用此方法
     *
     * @param followerId 关注者ID
     * @param followerName 关注者姓名
     * @param targetUserId 被关注者ID
     */
    public void handleFollowSuccess(Long followerId, String followerName, Long targetUserId) {
        try {
            // 1. 发送关注事件到 Kafka（用于统计、数据分析等）
            NotificationMessage followEvent = NotificationMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType(NotificationTypeEnum.USER_FOLLOW.getCode())
                    .fromUserId(followerId)
                    .fromUsername(followerName)
                    .toUserId(targetUserId)
                    .title("关注事件")
                    .content("用户关注")
                    .relatedId(followerId)
                    .relatedType("USER_FOLLOW")
                    .createTime(LocalDateTime.now())
                    .build();

            notificationProducerService.sendFollowEvent(followEvent);

            // 2. 发送通知消息给被关注者
            NotificationMessage notification = NotificationMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType(NotificationTypeEnum.USER_FOLLOW.getCode())
                    .fromUserId(followerId)
                    .fromUsername(followerName)
                    .toUserId(targetUserId)
                    .title("新的关注")
                    .content(followerName + " 关注了你")
                    .relatedId(followerId)
                    .relatedType("USER")
                    .createTime(LocalDateTime.now())
                    .build();

            notificationProducerService.sendUserNotification(notification);

            log.info("关注成功，已发送 Kafka 消息 - 关注者: {}, 被关注者: {}", followerId, targetUserId);

        } catch (Exception e) {
            log.error("发送关注消息失败 - 关注者: {}, 被关注者: {}, Error: {}",
                    followerId, targetUserId, e.getMessage(), e);
            // 注意：消息发送失败不应影响主业务流程，这里只记录日志
        }
    }

    /**
     * 取消关注时发送消息示例
     *
     * 在 UserFollowServiceImpl.unfollow() 方法中，当取消关注成功后调用此方法
     *
     * @param followerId 关注者ID
     * @param targetUserId 被关注者ID
     */
    public void handleUnfollowSuccess(Long followerId, Long targetUserId) {
        try {
            // 发送取消关注事件（主要用于数据统计）
            NotificationMessage unfollowEvent = NotificationMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType("USER_UNFOLLOW")
                    .fromUserId(followerId)
                    .toUserId(targetUserId)
                    .title("取消关注事件")
                    .content("用户取消关注")
                    .relatedId(followerId)
                    .relatedType("USER_UNFOLLOW")
                    .createTime(LocalDateTime.now())
                    .build();

            notificationProducerService.sendFollowEvent(unfollowEvent);

            log.info("取消关注成功，已发送 Kafka 消息 - 关注者: {}, 被关注者: {}", followerId, targetUserId);

        } catch (Exception e) {
            log.error("发送取消关注消息失败 - 关注者: {}, 被关注者: {}, Error: {}",
                    followerId, targetUserId, e.getMessage(), e);
        }
    }
}
