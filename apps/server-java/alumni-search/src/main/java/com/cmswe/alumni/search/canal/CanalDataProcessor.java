package com.cmswe.alumni.search.canal;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.DataChangeEvent;
import com.cmswe.alumni.kafka.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Canal 数据处理器
 *
 * <p>负责将 Canal 接收到的数据变更事件发送到对应的 Kafka Topic
 *
 * @author CNI Alumni System
 * @since 2025-03-18
 */
@Slf4j
@Component
public class CanalDataProcessor {

    private final KafkaUtils kafkaUtils;

    public CanalDataProcessor(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }

    /**
     * 处理数据变更事件
     *
     * @param event 数据变更事件
     */
    public void process(DataChangeEvent event) {
        try {
            // 确定发送到哪个 Kafka Topic
            String topic = getTopicByTable(event.getTable());

            // 发送到 Kafka（使用主键作为Key，保证同一条数据的变更发送到同一分区，保证顺序性）
            String key = event.getPrimaryKey() != null
                    ? event.getPrimaryKey().toString()
                    : event.getEventId();

            // 使用统一的 KafkaUtils 工具类发送消息
            kafkaUtils.sendAsync(topic, key, event);

            log.debug("数据变更事件已发送到 Kafka - Topic: {}, Table: {}, EventType: {}, PrimaryKey: {}",
                    topic, event.getTable(), event.getEventType(), event.getPrimaryKey());

        } catch (Exception e) {
            log.error("处理数据变更事件失败 - Table: {}, EventType: {}, PrimaryKey: {}",
                    event.getTable(), event.getEventType(), event.getPrimaryKey(), e);
            throw new RuntimeException("数据变更事件处理失败", e);
        }
    }

    /**
     * 根据表名获取对应的 Kafka Topic
     */
    private String getTopicByTable(String table) {
        return switch (table) {
            case "wx_users", "wx_user_info", "alumni_education" -> KafkaTopicConstants.DATA_SYNC_ALUMNI;
            case "alumni_association" -> KafkaTopicConstants.DATA_SYNC_ASSOCIATION;
            case "merchant" -> KafkaTopicConstants.DATA_SYNC_MERCHANT;
            default -> KafkaTopicConstants.DATA_SYNC_ALUMNI;
        };
    }
}
