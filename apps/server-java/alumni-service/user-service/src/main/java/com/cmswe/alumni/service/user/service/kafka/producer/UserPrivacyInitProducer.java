package com.cmswe.alumni.service.user.service.kafka.producer;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.UserPrivacyInitEvent;
import com.cmswe.alumni.kafka.producer.AbstractMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户隐私设置初始化事件生产者
 * <p>
 * 用于用户注册后异步发送隐私设置初始化事件
 *
 * @author CMSWE
 * @since 2026-03-17
 */
@Slf4j
@Component
public class UserPrivacyInitProducer extends AbstractMessageProducer<UserPrivacyInitEvent> {

    @Override
    protected void preprocessMessage(UserPrivacyInitEvent message) {
        // 如果没有事件ID，自动生成
        if (message.getEventId() == null || message.getEventId().trim().isEmpty()) {
            message.setEventId(UUID.randomUUID().toString());
        }

        // 如果没有创建时间，设置当前时间
        if (message.getCreateTime() == null) {
            message.setCreateTime(LocalDateTime.now());
        }

        // 如果没有来源，设置默认来源
        if (message.getSource() == null || message.getSource().trim().isEmpty()) {
            message.setSource("user-register");
        }
    }

    @Override
    protected void validateMessage(UserPrivacyInitEvent message) {
        if (message == null) {
            throw new IllegalArgumentException("UserPrivacyInitEvent 不能为空");
        }

        if (message.getWxId() == null) {
            throw new IllegalArgumentException("wxId 不能为空");
        }

        if (message.getEventId() == null || message.getEventId().trim().isEmpty()) {
            throw new IllegalArgumentException("eventId 不能为空");
        }
    }

    @Override
    protected String extractMessageKey(UserPrivacyInitEvent message) {
        // 使用用户ID作为消息Key，保证同一用户的消息进入同一分区
        return String.valueOf(message.getWxId());
    }

    @Override
    public String getTargetTopic(UserPrivacyInitEvent message) {
        return KafkaTopicConstants.USER_PRIVACY_INIT_TOPIC;
    }

    @Override
    public String getProducerName() {
        return "UserPrivacyInitProducer";
    }

    /**
     * 发送用户隐私初始化事件
     *
     * @param wxId 微信用户ID
     * @return 是否发送成功
     */
    public boolean sendPrivacyInitEvent(Long wxId) {
        UserPrivacyInitEvent event = UserPrivacyInitEvent.builder()
                .wxId(wxId)
                .build();

        return sendAsync(event);
    }
}
