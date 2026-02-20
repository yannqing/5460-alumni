package com.cmswe.alumni.service.user.service.kafka.producer;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.NotificationMessage;
import com.cmswe.alumni.kafka.utils.KafkaUtils;
import com.cmswe.alumni.service.user.service.NotificationProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息通知生产者服务实现类
 *
 * @author CMSWE
 * @since 2025-12-05
 */
@Slf4j
@Service
public class NotificationProducerServiceImpl implements NotificationProducerService {

    private final KafkaUtils kafkaUtils;

    public NotificationProducerServiceImpl(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }

    @Override
    public void sendUserNotification(NotificationMessage message) {
        kafkaUtils.sendAsync(
            KafkaTopicConstants.USER_NOTIFICATION_TOPIC,
            message.getMessageId(),
            message
        );
    }

    @Override
    public void sendFollowEvent(NotificationMessage message) {
        kafkaUtils.sendAsync(
            KafkaTopicConstants.USER_FOLLOW_EVENT_TOPIC,
            message.getMessageId(),
            message
        );
    }

    @Override
    public void sendSystemNotification(NotificationMessage message) {
        kafkaUtils.sendAsync(
            KafkaTopicConstants.SYSTEM_NOTIFICATION_TOPIC,
            message.getMessageId(),
            message
        );
    }
}
