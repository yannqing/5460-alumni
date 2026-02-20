package com.cmswe.alumni.service.user.service.message.impl;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.enums.MessagePriority;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.producer.AbstractMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统通知生产者
 *
 * <p>负责处理系统级别的通知消息发送，包括：
 * <ul>
 *   <li>系统公告</li>
 *   <li>会员升级/到期提醒</li>
 *   <li>优惠券到期提醒</li>
 *   <li>账号安全通知</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
public class SystemNotificationProducer extends AbstractMessageProducer<UnifiedMessage> {

    @Override
    public String getProducerName() {
        return "SystemNotificationProducer";
    }

    @Override
    protected void validateMessage(UnifiedMessage message) {
        // 系统通知必须有接收方（可以是单个用户或全体用户）
        if (message.getToId() == null && (message.getToIds() == null || message.getToIds().isEmpty())) {
            throw new IllegalArgumentException("系统通知接收方不能为空");
        }

        if (message.getTitle() == null || message.getTitle().isEmpty()) {
            throw new IllegalArgumentException("系统通知标题不能为空");
        }

        if (message.getContent() == null || message.getContent().isEmpty()) {
            throw new IllegalArgumentException("系统通知内容不能为空");
        }

        log.debug("[SystemNotificationProducer] 消息验证通过 - Type: {}, ToId: {}, ToIds: {}",
                message.getMessageType(), message.getToId(),
                message.getToIds() != null ? message.getToIds().size() : 0);
    }

    @Override
    public String getTargetTopic(UnifiedMessage message) {
        // 根据优先级选择不同的Topic（可选优化）
        return KafkaTopicConstants.SYSTEM_NOTIFICATION;
    }

    @Override
    protected void preprocessMessage(UnifiedMessage message) {
        super.preprocessMessage(message);

        // 系统通知默认设置发送方为系统
        if (message.getFromId() == null) {
            message.setFromId(0L); // 0 表示系统
        }

        if (message.getFromType() == null) {
            message.setFromType("SYSTEM");
        }

        if (message.getFromName() == null) {
            message.setFromName("系统通知");
        }

        // 系统通知默认为高优先级
        if (message.getPriority() == null || message.getPriority() == MessagePriority.NORMAL) {
            message.setPriority(MessagePriority.HIGH);
        }
    }
}
