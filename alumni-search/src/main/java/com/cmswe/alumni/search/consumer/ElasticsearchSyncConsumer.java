package com.cmswe.alumni.search.consumer;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.DataChangeEvent;
import com.cmswe.alumni.kafka.reliability.DeadLetterQueueService;
import com.cmswe.alumni.kafka.reliability.MessageIdempotentService;
import com.cmswe.alumni.search.document.AlumniDocument;
import com.cmswe.alumni.search.repository.AlumniDocumentRepository;
import com.cmswe.alumni.search.service.sync.DataMergeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch 同步消费者（发布订阅模式）
 *
 * <p>架构设计：
 * <ul>
 *   <li>独立消费者组：es-sync-group</li>
 *   <li>职责单一：只负责 ES 索引的增删改</li>
 *   <li>水平扩展：可以启动多个实例并行消费</li>
 *   <li>故障隔离：本服务挂了不影响缓存清除等其他消费者</li>
 * </ul>
 *
 * <p>数据流：
 * MySQL变更 → Canal → Kafka Topic → 本消费者 → ES索引更新
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "canal.enabled", havingValue = "true")
public class ElasticsearchSyncConsumer {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private DataMergeService dataMergeService;

    @Resource
    private AlumniDocumentRepository alumniDocumentRepository;

    @Resource
    private MessageIdempotentService messageIdempotentService;

    @Resource
    private DeadLetterQueueService deadLetterQueueService;

    /**
     * 消费校友数据变更事件，同步到 Elasticsearch
     *
     * <p>企业级可靠性保障：
     * <ul>
     *   <li>幂等性检查：防止消息重复消费</li>
     *   <li>死信队列：失败消息自动发送到 DLQ，支持人工介入</li>
     *   <li>异常隔离：异常不抛出，避免阻塞 Kafka 消费进度</li>
     * </ul>
     *
     * @param message   消息内容（JSON格式的 DataChangeEvent）
     * @param partition Kafka 分区
     * @param offset    消息偏移量
     */
    @KafkaListener(
            topics = KafkaTopicConstants.DATA_SYNC_ALUMNI,
            groupId = KafkaTopicConstants.ConsumerGroup.ES_SYNC,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void syncAlumniToElasticsearch(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[ES同步] 接收到数据变更事件 - Partition: {}, Offset: {}", partition, offset);

        DataChangeEvent event = null;
        String eventId = null;

        try {
            // 1. 反序列化事件
            event = objectMapper.readValue(message, DataChangeEvent.class);
            eventId = event.getEventId();

            // 2. 【幂等性检查】检查消息是否已处理过
            if (messageIdempotentService.isMessageProcessed(eventId)) {
                log.warn("[ES同步] 检测到重复消息，跳过处理 - EventId: {}", eventId);
                return;
            }

            // 3. 验证事件
            if (!dataMergeService.validateEvent(event)) {
                log.warn("[ES同步] 事件验证失败，跳过处理 - EventId: {}", eventId);
                return;
            }

            log.info("[ES同步] 数据变更详情 - EventId: {}, Table: {}, EventType: {}",
                    eventId, event.getTable(), event.getEventType());

            // 4. 提取 wxId
            Long wxId = dataMergeService.extractWxId(event);
            if (wxId == null) {
                log.warn("[ES同步] 无法提取wxId - EventId: {}", eventId);
                return;
            }

            long startTime = System.currentTimeMillis();

            // 5. 根据事件类型处理
            if (event.getEventType() == DataChangeEvent.EventType.DELETE) {
                // 删除ES索引
                handleDelete(wxId, eventId);
            } else {
                // INSERT/UPDATE：更新ES索引
                handleInsertOrUpdate(wxId, eventId);
            }

            // 6. 【标记消息为已处理】保证幂等性
            messageIdempotentService.markMessageAsProcessed(eventId);

            long endTime = System.currentTimeMillis();
            log.info("[ES同步] 同步成功 - wxId: {}, EventId: {}, Time: {}ms",
                    wxId, eventId, (endTime - startTime));

        } catch (Exception e) {
            log.error("[ES同步] 同步异常 - Partition: {}, Offset: {}, EventId: {}",
                    partition, offset, eventId, e);

            // 7. 【发送到死信队列】支持人工介入处理
            if (event != null && eventId != null) {
                try {
                    deadLetterQueueService.sendToDeadLetterQueue(
                            KafkaTopicConstants.DATA_SYNC_ALUMNI,
                            eventId,
                            event,
                            "ES同步失败: " + e.getMessage()
                    );
                    log.warn("[ES同步] 消息已发送到死信队列 - EventId: {}, DLQ Topic: {}",
                            eventId, KafkaTopicConstants.DATA_SYNC_ALUMNI + ".dlq");
                } catch (Exception dlqException) {
                    log.error("[ES同步] 发送到死信队列失败 - EventId: {}", eventId, dlqException);
                }
            }

            // 企业级标准：异常不抛出，避免阻塞 Kafka 消费进度
        }
    }

    /**
     * 处理删除事件
     */
    private void handleDelete(Long wxId, String eventId) {
        try {
            alumniDocumentRepository.deleteById(Long.valueOf(wxId.toString()));
            log.info("[ES同步] 索引已删除 - wxId: {}, EventId: {}", wxId, eventId);
        } catch (Exception e) {
            log.error("[ES同步] 删除索引失败 - wxId: {}, EventId: {}", wxId, eventId, e);
            throw e;
        }
    }

    /**
     * 处理插入或更新事件
     */
    private void handleInsertOrUpdate(Long wxId, String eventId) {
        try {
            // 合并多表数据
            AlumniDocument document = dataMergeService.mergeToDocument(wxId);
            if (document == null) {
                log.warn("[ES同步] 数据合并失败 - wxId: {}, EventId: {}", wxId, eventId);
                return;
            }

            // 保存到ES
            alumniDocumentRepository.save(document);
            log.info("[ES同步] 索引已更新 - wxId: {}, EventId: {}", wxId, eventId);

        } catch (Exception e) {
            log.error("[ES同步] 更新索引失败 - wxId: {}, EventId: {}", wxId, eventId, e);
            throw e;
        }
    }
}
