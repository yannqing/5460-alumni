package com.cmswe.alumni.service.user.service.message.impl;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.producer.AbstractMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * P2P消息生产者（点对点聊天）
 *
 * <p>负责处理用户之间的私聊消息发送
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
public class P2PMessageProducer extends AbstractMessageProducer<UnifiedMessage> {

    @Override
    public String getProducerName() {
        return "P2PMessageProducer";
    }

    @Override
    public String getTargetTopic(UnifiedMessage message) {
        return KafkaTopicConstants.USER_MESSAGE_P2P;
    }

    @Override
    protected String extractMessageKey(UnifiedMessage message) {
        // 使用用户 ID 作为 Key，确保同一用户的消息进入同一分区
        return message.getFromId() != null ? String.valueOf(message.getFromId()) : null;
    }

    @Override
    protected void preprocessMessage(UnifiedMessage message) {
        // 设置消息类别
        if (message.getCategory() == null) {
            message.setCategory(MessageCategory.P2P);
        }

        // 设置创建时间
        if (message.getCreateTime() == null) {
            message.setCreateTime(java.time.LocalDateTime.now());
        }

        // 生成消息 ID（如果没有）
        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
            message.setMessageId(generateMessageId(message));
        }
    }

    @Override
    protected void validateMessage(UnifiedMessage message) {
        // P2P消息必须有发送方和接收方
        if (message.getFromId() == null || message.getFromId() <= 0) {
            throw new IllegalArgumentException("P2P消息发送方ID不能为空");
        }

        if (message.getToId() == null || message.getToId() <= 0) {
            throw new IllegalArgumentException("P2P消息接收方ID不能为空");
        }

        if (message.getContent() == null || message.getContent().isEmpty()) {
            throw new IllegalArgumentException("P2P消息内容不能为空");
        }

        // 发送方和接收方不能相同
        if (message.getFromId().equals(message.getToId())) {
            throw new IllegalArgumentException("P2P消息发送方和接收方不能相同");
        }

        log.debug("[P2PMessageProducer] 消息验证通过 - From: {}, To: {}",
                message.getFromId(), message.getToId());
    }

    /**
     * 生成消息 ID
     */
    private String generateMessageId(UnifiedMessage message) {
        return String.format("P2P_%d_%d_%s",
                message.getFromId(),
                System.currentTimeMillis(),
                java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8));
    }

    // 保留原有的方法签名以兼容旧代码（已废弃）
    @Deprecated
    public MessageCategory getSupportedCategory() {
        return MessageCategory.P2P;
    }
}
