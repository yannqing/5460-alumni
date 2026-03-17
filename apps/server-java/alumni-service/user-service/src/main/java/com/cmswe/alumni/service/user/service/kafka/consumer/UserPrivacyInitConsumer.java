package com.cmswe.alumni.service.user.service.kafka.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.entity.SysConfig;
import com.cmswe.alumni.common.entity.UserPrivacySetting;
import com.cmswe.alumni.common.model.UserPrivacyInitEvent;
import com.cmswe.alumni.kafka.consumer.AbstractMessageConsumer;
import com.cmswe.alumni.service.system.mapper.SysConfigMapper;
import com.cmswe.alumni.service.user.mapper.UserPrivacySettingMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户隐私设置初始化消费者
 * <p>
 * 监听用户注册事件，异步初始化用户隐私设置
 *
 * @author CMSWE
 * @since 2026-03-17
 */
@Slf4j
@Component
public class UserPrivacyInitConsumer extends AbstractMessageConsumer<UserPrivacyInitEvent> {

    @Resource
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Resource
    private SysConfigMapper sysConfigMapper;

    /**
     * 监听用户隐私初始化事件
     */
    @KafkaListener(
            topics = KafkaTopicConstants.USER_PRIVACY_INIT_TOPIC,
            groupId = KafkaTopicConstants.ConsumerGroup.USER_PRIVACY_INIT,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        consume(message, partition, offset);
    }

    @Override
    protected UserPrivacyInitEvent deserializeMessage(String message)
            throws com.fasterxml.jackson.core.JsonProcessingException {
        return objectMapper.readValue(message, UserPrivacyInitEvent.class);
    }

    @Override
    protected boolean validateMessage(UserPrivacyInitEvent message) {
        if (message == null) {
            log.error("消息对象为空");
            return false;
        }

        if (message.getWxId() == null) {
            log.error("wxId 为空");
            return false;
        }

        if (message.getEventId() == null || message.getEventId().trim().isEmpty()) {
            log.error("eventId 为空");
            return false;
        }

        return true;
    }

    @Override
    protected boolean processMessageWithChain(UserPrivacyInitEvent message) {
        Long wxId = message.getWxId();

        try {
            log.info("开始初始化用户隐私设置 - wxId: {}, eventId: {}", wxId, message.getEventId());

            // 1. 检查用户隐私设置是否已存在
            Long existingCount = userPrivacySettingMapper.selectCount(
                    new LambdaQueryWrapper<UserPrivacySetting>()
                            .eq(UserPrivacySetting::getWxId, wxId)
            );

            if (existingCount != null && existingCount > 0) {
                log.warn("用户隐私设置已存在，跳过初始化 - wxId: {}, 已有设置数: {}", wxId, existingCount);
                return true;
            }

            // 2. 查询系统配置的隐私字段
            SysConfig parentConfig = sysConfigMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>()
                            .eq(SysConfig::getConfigKey, "user_privacy_setting")
            );

            if (parentConfig == null) {
                log.error("未找到系统隐私配置 - configKey: user_privacy_setting");
                return false;
            }

            List<SysConfig> privacyConfigs = sysConfigMapper.selectList(
                    new LambdaQueryWrapper<SysConfig>()
                            .eq(SysConfig::getParentId, parentConfig.getConfigId())
                            .eq(SysConfig::getStatus, 1) // 只获取启用的配置
            );

            if (privacyConfigs == null || privacyConfigs.isEmpty()) {
                log.warn("未找到可用的隐私配置项 - wxId: {}", wxId);
                return true; // 没有配置也算成功
            }

            // 3. 为用户初始化隐私设置
            int successCount = 0;
            for (SysConfig config : privacyConfigs) {
                UserPrivacySetting setting = new UserPrivacySetting();
                setting.setWxId(wxId);
                setting.setFieldName(config.getConfigName());
                setting.setFieldCode(config.getConfigKey());
                setting.setType(1); // 1-用户配置
                setting.setVisibility(0); // 默认可见
                setting.setSearchable(0); // 默认可搜索

                int insertResult = userPrivacySettingMapper.insert(setting);
                if (insertResult > 0) {
                    successCount++;
                }
            }

            log.info("用户隐私设置初始化完成 - wxId: {}, 配置项数: {}, 成功初始化: {}",
                    wxId, privacyConfigs.size(), successCount);

            return successCount > 0;

        } catch (Exception e) {
            log.error("用户隐私设置初始化失败 - wxId: {}, error: {}", wxId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getConsumerName() {
        return "UserPrivacyInitConsumer";
    }

    @Override
    public String getConsumedTopic() {
        return KafkaTopicConstants.USER_PRIVACY_INIT_TOPIC;
    }

    @Override
    protected boolean enableIdempotent() {
        // 启用幂等性检查，防止重复初始化
        return true;
    }

    @Override
    protected boolean isDuplicateMessage(UserPrivacyInitEvent message) {
        // 检查该用户的隐私设置是否已存在
        Long existingCount = userPrivacySettingMapper.selectCount(
                new LambdaQueryWrapper<UserPrivacySetting>()
                        .eq(UserPrivacySetting::getWxId, message.getWxId())
        );

        return existingCount != null && existingCount > 0;
    }
}
