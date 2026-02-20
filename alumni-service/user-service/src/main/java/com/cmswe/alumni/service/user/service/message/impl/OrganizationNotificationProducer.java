package com.cmswe.alumni.service.user.service.message.impl;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.producer.AbstractMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 组织通知生产者
 *
 * <p>负责处理组织发布的通知消息，包括：
 * <ul>
 *   <li>活动发布通知</li>
 *   <li>组织公告</li>
 *   <li>组织新闻</li>
 *   <li>活动报名成功通知</li>
 *   <li>活动即将开始提醒</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
public class OrganizationNotificationProducer extends AbstractMessageProducer<UnifiedMessage> {

    @Override
    public String getProducerName() {
        return "OrganizationNotificationProducer";
    }

    @Override
    protected void validateMessage(UnifiedMessage message) {
        // 组织通知必须有发送方（组织ID）
        if (message.getFromId() == null || message.getFromId() <= 0) {
            throw new IllegalArgumentException("组织通知发送方（组织ID）不能为空");
        }

        // 组织通知必须有接收方（可以是批量用户）
        if (message.getToIds() == null || message.getToIds().isEmpty()) {
            throw new IllegalArgumentException("组织通知接收方列表不能为空");
        }

        if (message.getTitle() == null || message.getTitle().isEmpty()) {
            throw new IllegalArgumentException("组织通知标题不能为空");
        }

        if (message.getContent() == null || message.getContent().isEmpty()) {
            throw new IllegalArgumentException("组织通知内容不能为空");
        }

        log.debug("[OrganizationNotificationProducer] 消息验证通过 - OrgId: {}, RecipientCount: {}",
                message.getFromId(), message.getToIds().size());
    }

    @Override
    public String getTargetTopic(UnifiedMessage message) {
        return KafkaTopicConstants.ORGANIZATION_NOTIFICATION;
    }

    @Override
    protected void preprocessMessage(UnifiedMessage message) {
        super.preprocessMessage(message);

        // 组织通知默认设置发送方类型为组织
        if (message.getFromType() == null) {
            message.setFromType("ORGANIZATION");
        }

        // 接收方类型默认为用户批量
        if (message.getToType() == null) {
            message.setToType("USER_BATCH");
        }
    }
}
