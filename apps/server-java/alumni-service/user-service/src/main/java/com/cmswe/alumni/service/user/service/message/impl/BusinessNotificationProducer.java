package com.cmswe.alumni.service.user.service.message.impl;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.producer.AbstractMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 业务通知生产者
 *
 * <p>负责处理业务相关的通知消息，包括：
 * <ul>
 *   <li>用户关注通知</li>
 *   <li>评论通知</li>
 *   <li>点赞通知</li>
 *   <li>@提及通知</li>
 *   <li>回复通知</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
public class BusinessNotificationProducer extends AbstractMessageProducer<UnifiedMessage> {

    @Override
    public String getProducerName() {
        return "BusinessNotificationProducer";
    }

    @Override
    protected void validateMessage(UnifiedMessage message) {
        // 业务通知必须有发送方和接收方
        if (message.getFromId() == null || message.getFromId() < 0) {
            throw new IllegalArgumentException("业务通知发送方ID不能为空");
        }

        if (message.getToId() == null || message.getToId() <= 0) {
            throw new IllegalArgumentException("业务通知接收方ID不能为空");
        }

        // 业务通知通常需要关联业务ID
        if (message.getRelatedId() == null || message.getRelatedId() <= 0) {
            log.warn("[BusinessNotificationProducer] 业务通知缺少关联业务ID - Type: {}, MessageId: {}",
                    message.getMessageType(), message.getMessageId());
        }

        log.debug("[BusinessNotificationProducer] 消息验证通过 - Type: {}, From: {}, To: {}, RelatedId: {}",
                message.getMessageType(), message.getFromId(), message.getToId(), message.getRelatedId());
    }

    @Override
    public String getTargetTopic(UnifiedMessage message) {
        return KafkaTopicConstants.BUSINESS_NOTIFICATION;
    }

    @Override
    protected void preprocessMessage(UnifiedMessage message) {
        super.preprocessMessage(message);

        // 业务通知默认设置发送方类型为用户
        if (message.getFromType() == null) {
            message.setFromType("USER");
        }

        // 接收方类型默认为用户
        if (message.getToType() == null) {
            message.setToType("USER");
        }
    }
}
