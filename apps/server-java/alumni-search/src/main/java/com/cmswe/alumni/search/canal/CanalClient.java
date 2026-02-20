package com.cmswe.alumni.search.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.DataChangeEvent;
import com.cmswe.alumni.kafka.utils.KafkaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Canal 客户端（阿里巴巴企业级标准）
 *
 * <p>功能：
 * <ul>
 *   <li>监听 MySQL Binlog 变化</li>
 *   <li>将数据变更事件发送到 Kafka</li>
 *   <li>支持多表监听</li>
 *   <li>支持断点续传</li>
 * </ul>
 *
 * <p>监听表：
 * <ul>
 *   <li>wx_users - 用户表</li>
 *   <li>wx_user_info - 用户信息表</li>
 *   <li>alumni_education - 校友教育经历表</li>
 *   <li>alumni_association - 校友会表</li>
 *   <li>merchant - 商户表</li>
 * </ul>
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "canal.enabled", havingValue = "true")
public class CanalClient {

    @Value("${canal.server.host:127.0.0.1}")
    private String canalHost;

    @Value("${canal.server.port:11111}")
    private Integer canalPort;

    @Value("${canal.destination:example}")
    private String destination;

    @Value("${canal.username:}")
    private String username;

    @Value("${canal.password:}")
    private String password;

    @Value("${canal.batch.size:1000}")
    private Integer batchSize;

    private final KafkaUtils kafkaUtils;
    private final ObjectMapper objectMapper;

    private CanalConnector connector;
    private volatile boolean running = false;
    private Thread workerThread;

    public CanalClient(KafkaUtils kafkaUtils, ObjectMapper objectMapper) {
        this.kafkaUtils = kafkaUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * 启动 Canal 客户端
     */
    @PostConstruct
    public void start() {
        try {
            // 创建 Canal 连接器
            connector = CanalConnectors.newSingleConnector(
                    new InetSocketAddress(canalHost, canalPort),
                    destination,
                    username,
                    password
            );

            // 启动工作线程
            running = true;
            workerThread = new Thread(this::run, "canal-client-worker");
            workerThread.start();

            log.info("Canal客户端启动成功 - Host: {}, Port: {}, Destination: {}",
                    canalHost, canalPort, destination);

        } catch (Exception e) {
            log.error("Canal客户端启动失败", e);
            throw new RuntimeException("Canal客户端启动失败", e);
        }
    }

    /**
     * 停止 Canal 客户端
     */
    @PreDestroy
    public void stop() {
        running = false;

        if (workerThread != null) {
            try {
                workerThread.join(3000); // 等待3秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待Canal工作线程停止时被中断", e);
            }
        }

        if (connector != null) {
            connector.disconnect();
        }

        log.info("Canal客户端已停止");
    }

    /**
     * Canal 消息消费主循环
     */
    private void run() {
        try {
            // 连接 Canal Server
            connector.connect();

            // 订阅数据库表
            // 格式：数据库名.表名1,数据库名.表名2
            connector.subscribe(".*\\.wx_users,.*\\.wx_user_info,.*\\.alumni_education," +
                    ".*\\.alumni_association,.*\\.merchant");

            // 回滚到未确认的位置（断点续传）
            connector.rollback();

            log.info("Canal客户端开始监听数据变更...");

            while (running) {
                try {
                    // 获取指定数量的数据
                    Message message = connector.getWithoutAck(batchSize);
                    long batchId = message.getId();
                    int size = message.getEntries().size();

                    if (batchId == -1 || size == 0) {
                        // 没有数据，休眠1秒
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        continue;
                    }

                    log.debug("Canal接收到数据 - BatchId: {}, Size: {}", batchId, size);

                    // 处理数据变更事件
                    processEntries(message.getEntries());

                    // 确认消费（保证至少一次消费）
                    connector.ack(batchId);

                } catch (Exception e) {
                    log.error("Canal消息处理失败", e);
                    // 回滚到未确认的位置
                    connector.rollback();
                }
            }

        } catch (Exception e) {
            log.error("Canal客户端运行异常", e);
        } finally {
            connector.disconnect();
        }
    }

    /**
     * 处理 Binlog 条目列表
     */
    private void processEntries(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN ||
                    entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) {
                continue;
            }

            try {
                // 解析 RowChange
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

                // 获取表信息
                String database = entry.getHeader().getSchemaName();
                String table = entry.getHeader().getTableName();
                CanalEntry.EventType eventType = rowChange.getEventType();

                // 过滤不需要同步的事件
                if (!shouldSync(table, eventType)) {
                    continue;
                }

                log.debug("检测到数据变更 - Database: {}, Table: {}, EventType: {}",
                        database, table, eventType);

                // 处理每一行数据变更
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    processRowData(database, table, eventType, rowData, entry.getHeader());
                }

            } catch (Exception e) {
                log.error("处理Canal Entry失败", e);
            }
        }
    }

    /**
     * 处理单行数据变更
     */
    private void processRowData(String database, String table,
                                CanalEntry.EventType eventType,
                                CanalEntry.RowData rowData,
                                CanalEntry.Header header) {
        try {
            // 转换为 DataChangeEvent
            DataChangeEvent event = convertToDataChangeEvent(
                    database, table, eventType, rowData, header
            );

            // 确定发送到哪个 Kafka Topic
            String topic = getTopicByTable(table);

            // 发送到 Kafka（使用主键作为Key，保证同一条数据的变更发送到同一分区，保证顺序性）
            String key = event.getPrimaryKey() != null ? event.getPrimaryKey().toString() : event.getEventId();

            // 使用统一的 KafkaUtils 工具类发送消息（直接发送对象，让 JsonSerializer 自动序列化）
            kafkaUtils.sendAsync(topic, key, event);

        } catch (Exception e) {
            log.error("处理行数据失败 - Table: {}", table, e);
        }
    }

    /**
     * 转换为 DataChangeEvent
     */
    private DataChangeEvent convertToDataChangeEvent(String database, String table,
                                                      CanalEntry.EventType eventType,
                                                      CanalEntry.RowData rowData,
                                                      CanalEntry.Header header) {
        Map<String, Object> beforeData = null;
        Map<String, Object> afterData = null;
        Long primaryKey = null;
        String primaryKeyName = getPrimaryKeyName(table);

        // 解析变更前数据
        if (eventType == CanalEntry.EventType.UPDATE || eventType == CanalEntry.EventType.DELETE) {
            beforeData = convertColumnsToMap(rowData.getBeforeColumnsList());
            if (primaryKeyName != null && beforeData.containsKey(primaryKeyName)) {
                primaryKey = Long.parseLong(beforeData.get(primaryKeyName).toString());
            }
        }

        // 解析变更后数据
        if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE) {
            afterData = convertColumnsToMap(rowData.getAfterColumnsList());
            if (primaryKeyName != null && afterData.containsKey(primaryKeyName)) {
                primaryKey = Long.parseLong(afterData.get(primaryKeyName).toString());
            }
        }

        return DataChangeEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .database(database)
                .table(table)
                .eventType(convertEventType(eventType))
                .beforeData(beforeData)
                .afterData(afterData)
                .primaryKey(primaryKey)
                .primaryKeyName(primaryKeyName)
                .binlogFile(header.getLogfileName())
                .binlogPosition(header.getLogfileOffset())
                .executeTime(header.getExecuteTime())
                .createTime(LocalDateTime.now())
                .isDdl(false)
                .build();
    }

    /**
     * 将 Canal 列列表转换为 Map
     */
    private Map<String, Object> convertColumnsToMap(List<CanalEntry.Column> columns) {
        Map<String, Object> map = new HashMap<>();
        for (CanalEntry.Column column : columns) {
            map.put(column.getName(), column.getValue());
        }
        return map;
    }

    /**
     * 转换事件类型
     */
    private DataChangeEvent.EventType convertEventType(CanalEntry.EventType eventType) {
        return switch (eventType) {
            case INSERT -> DataChangeEvent.EventType.INSERT;
            case UPDATE -> DataChangeEvent.EventType.UPDATE;
            case DELETE -> DataChangeEvent.EventType.DELETE;
            default -> DataChangeEvent.EventType.OTHER;
        };
    }

    /**
     * 判断是否需要同步该表的该事件
     */
    private boolean shouldSync(String table, CanalEntry.EventType eventType) {
        // 只同步 INSERT/UPDATE/DELETE 事件
        if (eventType != CanalEntry.EventType.INSERT &&
                eventType != CanalEntry.EventType.UPDATE &&
                eventType != CanalEntry.EventType.DELETE) {
            return false;
        }

        // 只同步指定的表
        return "wx_users".equals(table) ||
                "wx_user_info".equals(table) ||
                "alumni_education".equals(table) ||
                "alumni_association".equals(table) ||
                "merchant".equals(table);
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
