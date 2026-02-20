package com.cmswe.alumni.service.user.service.message.impl;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.producer.AbstractMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 群聊消息生产者
 *
 * <p>负责处理群组内的聊天消息发送
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
public class GroupMessageProducer extends AbstractMessageProducer<UnifiedMessage> {

    @Override
    public String getProducerName() {
        return "GroupMessageProducer";
    }

    @Override
    public String getTargetTopic(UnifiedMessage message) {
        return KafkaTopicConstants.GROUP_MESSAGE_CHAT;
    }

    @Override
    protected void validateMessage(UnifiedMessage message) {
        // 群聊消息必须有发送方和群组ID
        if (message.getFromId() == null || message.getFromId() <= 0) {
            throw new IllegalArgumentException("群聊消息发送方ID不能为空");
        }

        if (message.getToId() == null || message.getToId() <= 0) {
            throw new IllegalArgumentException("群聊消息群组ID不能为空");
        }

        if (message.getContent() == null || message.getContent().isEmpty()) {
            throw new IllegalArgumentException("群聊消息内容不能为空");
        }

        log.debug("[GroupMessageProducer] 消息验证通过 - From: {}, GroupId: {}",
                message.getFromId(), message.getToId());
    }
}
