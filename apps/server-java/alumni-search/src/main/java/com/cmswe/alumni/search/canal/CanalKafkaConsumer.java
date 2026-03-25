package com.cmswe.alumni.search.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.DataChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Canal Kafka 消费者（从 Kafka 消费 Canal 发送的 Binlog 数据）
 *
 * <p>功能：
 * <ul>
 *   <li>从 Kafka 消费 Canal Server 发送的 MySQL Binlog 变更数据</li>
 *   <li>将数据变更事件转换为 DataChangeEvent</li>
 *   <li>发送到下游 Kafka Topic 供搜索服务消费</li>
 *   <li>支持手动 ACK 保证消息不丢失</li>
 * </ul>
 *
 * <p>Canal Flat Message 格式（canal.mq.flatMessage=true）：
 * <pre>
 * {
 *   "id": 0,
 *   "database": "cni_alumni",
 *   "table": "wx_users",
 *   "type": "INSERT",
 *   "es": 1641370800000,
 *   "ts": 1641370800123,
 *   "sql": "",
 *   "sqlType": {"wx_id": -5, "nickname": 12},
 *   "mysqlType": {"wx_id": "bigint", "nickname": "varchar(100)"},
 *   "data": [{"wx_id": "123", "nickname": "张三"}],
 *   "old": null
 * }
 * </pre>
 *
 * @author CNI Alumni System
 * @since 2025-03-18
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "canal.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class CanalKafkaConsumer {

    private final CanalDataProcessor dataProcessor;

    public CanalKafkaConsumer(CanalDataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("========================================");
        log.info("[CanalKafkaConsumer] Bean 已创建！");
        log.info("[CanalKafkaConsumer] 开始监听 Canal Kafka Topic");
        log.info("========================================");
    }

    /**
     * 消费 Canal 发送到 Kafka 的 Binlog 数据
     *
     * @param message Canal Flat Message JSON 字符串
     * @param partition Kafka 分区号
     * @param offset Kafka 偏移量
     * @param acknowledgment 手动确认对象
     */
    @KafkaListener(
            topics = "${canal.kafka.topic:alumni_binlog}",
            groupId = "${canal.kafka.group-id:alumni-search-canal-consumer}",
            concurrency = "${canal.kafka.concurrency:3}",
            containerFactory = "canalKafkaListenerContainerFactory"
    )
    public void consumeCanalMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.debug("收到 Canal Kafka 消息 - Partition: {}, Offset: {}", partition, offset);

            // 解析 Canal Flat Message
            JSONObject canalMessage = JSON.parseObject(message);

            // 提取核心字段
            String database = canalMessage.getString("database");
            String table = canalMessage.getString("table");
            String type = canalMessage.getString("type"); // INSERT/UPDATE/DELETE
            Long es = canalMessage.getLong("es"); // 执行时间（毫秒）
            Long ts = canalMessage.getLong("ts"); // Canal 处理时间（毫秒）

            // 获取数据变更内容
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) canalMessage.get("data");
            List<Map<String, Object>> oldList = (List<Map<String, Object>>) canalMessage.get("old");

            // 过滤不需要的表
            if (!shouldProcess(table)) {
                log.debug("跳过不需要处理的表: {}", table);
                acknowledgment.acknowledge();
                return;
            }

            log.info("处理 Canal 数据变更 - Database: {}, Table: {}, Type: {}, Records: {}",
                    database, table, type, dataList != null ? dataList.size() : 0);

            // 处理每一行数据变更
            if (dataList != null) {
                for (int i = 0; i < dataList.size(); i++) {
                    Map<String, Object> data = dataList.get(i);
                    Map<String, Object> old = (oldList != null && i < oldList.size()) ? oldList.get(i) : null;

                    // 转换为 DataChangeEvent
                    DataChangeEvent event = convertToDataChangeEvent(
                            database, table, type, data, old, es, ts
                    );

                    // 交给数据处理器处理
                    dataProcessor.process(event);
                }
            }

            // 手动确认消息
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("处理 Canal Kafka 消息失败 - Partition: {}, Offset: {}, Message: {}",
                    partition, offset, message, e);
            // 不确认消息，Kafka 会重新投递
            throw new RuntimeException("Canal 消息处理失败", e);
        }
    }

    /**
     * 判断是否需要处理该表
     */
    private boolean shouldProcess(String table) {
        return "wx_users".equals(table) ||
                "wx_user_info".equals(table) ||
                "alumni_education".equals(table) ||
                "alumni_association".equals(table) ||
                "merchant".equals(table);
    }

    /**
     * 转换为 DataChangeEvent
     */
    private DataChangeEvent convertToDataChangeEvent(
            String database,
            String table,
            String type,
            Map<String, Object> data,
            Map<String, Object> old,
            Long executeTime,
            Long canalProcessTime) {

        // 确定主键
        String primaryKeyName = getPrimaryKeyName(table);
        Long primaryKey = extractPrimaryKey(data, primaryKeyName);

        // 转换事件类型
        DataChangeEvent.EventType eventType = convertEventType(type);

        return DataChangeEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .database(database)
                .table(table)
                .eventType(eventType)
                .beforeData(old)
                .afterData(data)
                .primaryKey(primaryKey)
                .primaryKeyName(primaryKeyName)
                .executeTime(executeTime != null ? executeTime : System.currentTimeMillis())
                .createTime(LocalDateTime.now())
                .isDdl(false)
                .build();
    }

    /**
     * 提取主键值
     */
    private Long extractPrimaryKey(Map<String, Object> data, String primaryKeyName) {
        if (data == null || primaryKeyName == null) {
            return null;
        }

        Object pkValue = data.get(primaryKeyName);
        if (pkValue == null) {
            return null;
        }

        try {
            if (pkValue instanceof Long) {
                return (Long) pkValue;
            } else if (pkValue instanceof Integer) {
                return ((Integer) pkValue).longValue();
            } else if (pkValue instanceof String) {
                return Long.parseLong((String) pkValue);
            }
        } catch (Exception e) {
            log.warn("提取主键失败 - Table: {}, PrimaryKeyName: {}, Value: {}",
                    data.get("table"), primaryKeyName, pkValue);
        }

        return null;
    }

    /**
     * 转换事件类型
     */
    private DataChangeEvent.EventType convertEventType(String type) {
        if (type == null) {
            return DataChangeEvent.EventType.OTHER;
        }

        return switch (type.toUpperCase()) {
            case "INSERT" -> DataChangeEvent.EventType.INSERT;
            case "UPDATE" -> DataChangeEvent.EventType.UPDATE;
            case "DELETE" -> DataChangeEvent.EventType.DELETE;
            default -> DataChangeEvent.EventType.OTHER;
        };
    }

    /**
     * 获取表的主键字段名
     */
    private String getPrimaryKeyName(String table) {
        return switch (table) {
            case "wx_users" -> "wx_id";
            case "wx_user_info" -> "wx_id";
            case "alumni_education" -> "edu_id";
            case "alumni_association" -> "association_id";
            case "merchant" -> "merchant_id";
            default -> "id";
        };
    }
}
