package com.cmswe.alumni.service.user.example;

import com.cmswe.alumni.common.enums.NotificationTypeEnum;
import com.cmswe.alumni.common.model.NotificationMessage;
import com.cmswe.alumni.service.user.service.NotificationProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka 使用示例
 *
 * 本示例展示了如何在用户关注功能中使用 Kafka 发送和消费消息
 *
 * @author CMSWE
 * @since 2025-12-05
 */
@Slf4j
@Component
public class KafkaUsageExample {

    private final NotificationProducerService notificationProducerService;

    public KafkaUsageExample(NotificationProducerService notificationProducerService) {
        this.notificationProducerService = notificationProducerService;
    }

    /**
     * 示例1: 发送用户关注通知
     *
     * 场景：当用户A关注了用户B时，发送通知给用户B
     *
     * @param fromUserId 关注者ID
     * @param fromUsername 关注者用户名
     * @param toUserId 被关注者ID
     */
    public void sendFollowNotification(Long fromUserId, String fromUsername, Long toUserId) {
        // 构建通知消息
        NotificationMessage message = NotificationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageType(NotificationTypeEnum.USER_FOLLOW.getCode())
                .fromUserId(fromUserId)
                .fromUsername(fromUsername)
                .toUserId(toUserId)
                .title("新的关注")
                .content(fromUsername + " 关注了你")
                .relatedId(fromUserId)
                .relatedType("USER")
                .createTime(LocalDateTime.now())
                .build();

        // 发送到 Kafka
        notificationProducerService.sendFollowEvent(message);

        log.info("已发送关注通知 - FromUser: {}, ToUser: {}", fromUserId, toUserId);
    }

    /**
     * 示例2: 发送系统通知
     *
     * 场景：系统管理员发送全站通知
     *
     * @param title 通知标题
     * @param content 通知内容
     * @param targetUserIds 目标用户ID列表（可选，null表示全站通知）
     */
    public void sendSystemNotification(String title, String content, Long[] targetUserIds) {
        if (targetUserIds == null || targetUserIds.length == 0) {
            // 全站通知 - 发送一条消息，消费者负责广播
            NotificationMessage message = NotificationMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType(NotificationTypeEnum.SYSTEM_NOTICE.getCode())
                    .fromUserId(0L) // 0 表示系统
                    .fromUsername("系统通知")
                    .toUserId(0L) // 0 表示全体用户
                    .title(title)
                    .content(content)
                    .createTime(LocalDateTime.now())
                    .build();

            notificationProducerService.sendSystemNotification(message);
            log.info("已发送系统全站通知 - Title: {}", title);
        } else {
            // 定向通知 - 为每个用户发送一条消息
            for (Long userId : targetUserIds) {
                NotificationMessage message = NotificationMessage.builder()
                        .messageId(UUID.randomUUID().toString())
                        .messageType(NotificationTypeEnum.SYSTEM_NOTICE.getCode())
                        .fromUserId(0L)
                        .fromUsername("系统通知")
                        .toUserId(userId)
                        .title(title)
                        .content(content)
                        .createTime(LocalDateTime.now())
                        .build();

                notificationProducerService.sendSystemNotification(message);
            }
            log.info("已发送系统定向通知 - Title: {}, 用户数: {}", title, targetUserIds.length);
        }
    }

    /**
     * 示例3: 发送评论通知
     *
     * 场景：用户A评论了用户B的文章，通知用户B
     *
     * @param fromUserId 评论者ID
     * @param fromUsername 评论者用户名
     * @param toUserId 文章作者ID
     * @param articleId 文章ID
     * @param commentContent 评论内容
     */
    public void sendCommentNotification(Long fromUserId, String fromUsername,
                                       Long toUserId, Long articleId, String commentContent) {
        NotificationMessage message = NotificationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageType(NotificationTypeEnum.COMMENT.getCode())
                .fromUserId(fromUserId)
                .fromUsername(fromUsername)
                .toUserId(toUserId)
                .title("新评论")
                .content(fromUsername + " 评论了你的文章：" + commentContent)
                .relatedId(articleId)
                .relatedType("ARTICLE")
                .createTime(LocalDateTime.now())
                .build();

        notificationProducerService.sendUserNotification(message);
        log.info("已发送评论通知 - FromUser: {}, ToUser: {}, Article: {}",
                fromUserId, toUserId, articleId);
    }

    /**
     * 示例4: 发送点赞通知
     *
     * 场景：用户A点赞了用户B的文章，通知用户B
     *
     * @param fromUserId 点赞者ID
     * @param fromUsername 点赞者用户名
     * @param toUserId 文章作者ID
     * @param articleId 文章ID
     */
    public void sendLikeNotification(Long fromUserId, String fromUsername,
                                    Long toUserId, Long articleId) {
        NotificationMessage message = NotificationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageType(NotificationTypeEnum.LIKE.getCode())
                .fromUserId(fromUserId)
                .fromUsername(fromUsername)
                .toUserId(toUserId)
                .title("新点赞")
                .content(fromUsername + " 赞了你的文章")
                .relatedId(articleId)
                .relatedType("ARTICLE")
                .createTime(LocalDateTime.now())
                .build();

        notificationProducerService.sendUserNotification(message);
        log.info("已发送点赞通知 - FromUser: {}, ToUser: {}, Article: {}",
                fromUserId, toUserId, articleId);
    }

    /**
     * 示例5: 发送校友会通知
     *
     * 场景：校友会发布新公告
     *
     * @param associationId 校友会ID
     * @param associationName 校友会名称
     * @param title 公告标题
     * @param content 公告内容
     */
    public void sendAssociationNotification(Long associationId, String associationName,
                                           String title, String content) {
        NotificationMessage message = NotificationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageType(NotificationTypeEnum.ASSOCIATION_NOTICE.getCode())
                .fromUserId(associationId)
                .fromUsername(associationName)
                .toUserId(0L) // 0 表示校友会全体成员
                .title(title)
                .content(content)
                .relatedId(associationId)
                .relatedType("ASSOCIATION")
                .createTime(LocalDateTime.now())
                .build();

        notificationProducerService.sendUserNotification(message);
        log.info("已发送校友会通知 - Association: {}, Title: {}", associationName, title);
    }
}
