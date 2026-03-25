package com.cmswe.alumni.search.consumer;

import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.DataChangeEvent;
import com.cmswe.alumni.kafka.reliability.DeadLetterQueueService;
import com.cmswe.alumni.kafka.reliability.MessageIdempotentService;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.search.converter.AssociationDataConverter;
import com.cmswe.alumni.search.document.AlumniDocument;
import com.cmswe.alumni.search.document.AssociationDocument;
import com.cmswe.alumni.search.repository.AlumniDocumentRepository;
import com.cmswe.alumni.search.repository.AssociationDocumentRepository;
import com.cmswe.alumni.search.repository.MerchantDocumentRepository;
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
@ConditionalOnProperty(name = "canal.kafka.enabled", havingValue = "true")
public class ElasticsearchSyncConsumer {

    @Resource
    private ObjectMapper objectMapper;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("========================================");
        log.info("[ElasticsearchSyncConsumer] Bean 已创建！");
        log.info("[ElasticsearchSyncConsumer] 开始监听 Topic: {}", KafkaTopicConstants.DATA_SYNC_ALUMNI);
        log.info("[ElasticsearchSyncConsumer] 消费者组: {}", KafkaTopicConstants.ConsumerGroup.ES_SYNC);
        log.info("========================================");
    }

    @Resource
    private DataMergeService dataMergeService;

    @Resource
    private AlumniDocumentRepository alumniDocumentRepository;

    @Resource
    private AssociationDocumentRepository associationDocumentRepository;

    @Resource
    private MerchantDocumentRepository merchantDocumentRepository;

    @Resource
    private MessageIdempotentService messageIdempotentService;

    @Resource
    private DeadLetterQueueService deadLetterQueueService;

    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Resource
    private AssociationDataConverter associationDataConverter;

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
            topics = {
                    KafkaTopicConstants.DATA_SYNC_ALUMNI,
                    KafkaTopicConstants.DATA_SYNC_ASSOCIATION,
                    KafkaTopicConstants.DATA_SYNC_MERCHANT
            },
            groupId = KafkaTopicConstants.ConsumerGroup.ES_SYNC,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void syncAlumniToElasticsearch(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        System.out.println("!!!!!! ES同步方法被调用了！Partition: " + partition + ", Offset: " + offset);
        log.info("========================================");
        log.info("[ES同步] 接收到数据变更事件 - Partition: {}, Offset: {}", partition, offset);
        log.info("[ES同步] 消息内容: {}", message);
        log.info("========================================");

        DataChangeEvent event = null;
        String eventId = null;

        try {
            // 1. 反序列化事件
            event = objectMapper.readValue(message, DataChangeEvent.class);
            eventId = event.getEventId();

            // 2. 【幂等性检查】检查消息是否已处理过（使用"es-sync"命名空间隔离）
            if (messageIdempotentService.isMessageProcessed(eventId, "es-sync")) {
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

            long startTime = System.currentTimeMillis();

            // 4. 根据表名分发到不同的处理器
            String table = event.getTable();
            switch (table) {
                case "wx_users", "wx_user_info", "alumni_education" -> handleAlumniSync(event, eventId);
                case "alumni_association" -> handleAssociationSync(event, eventId);
                case "merchant" -> handleMerchantSync(event, eventId);
                default -> {
                    log.warn("[ES同步] 未知表类型，跳过处理 - Table: {}, EventId: {}", table, eventId);
                    return;
                }
            }

            // 5. 【标记消息为已处理】保证幂等性（使用"es-sync"命名空间隔离）
            messageIdempotentService.markMessageAsProcessed(eventId, "es-sync");

            long endTime = System.currentTimeMillis();
            log.info("[ES同步] 同步成功 - Table: {}, EventId: {}, Time: {}ms",
                    table, eventId, (endTime - startTime));

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
     * 处理校友数据同步
     */
    private void handleAlumniSync(DataChangeEvent event, String eventId) {
        // 提取 wxId
        Long wxId = dataMergeService.extractWxId(event);
        if (wxId == null) {
            log.warn("[ES同步-校友] 无法提取wxId - EventId: {}", eventId);
            return;
        }

        try {
            if (event.getEventType() == DataChangeEvent.EventType.DELETE) {
                // 删除ES索引
                alumniDocumentRepository.deleteById(wxId);
                log.info("[ES同步-校友] 索引已删除 - wxId: {}, EventId: {}", wxId, eventId);
            } else {
                // INSERT/UPDATE：合并多表数据并更新ES索引
                AlumniDocument document = dataMergeService.mergeToDocument(wxId);
                if (document == null) {
                    log.warn("[ES同步-校友] 数据合并失败 - wxId: {}, EventId: {}", wxId, eventId);
                    return;
                }
                alumniDocumentRepository.save(document);
                log.info("[ES同步-校友] 索引已更新 - wxId: {}, EventId: {}", wxId, eventId);
            }
        } catch (Exception e) {
            log.error("[ES同步-校友] 同步失败 - wxId: {}, EventId: {}", wxId, eventId, e);
            throw e;
        }
    }

    /**
     * 处理校友会数据同步
     */
    private void handleAssociationSync(DataChangeEvent event, String eventId) {
        // 提取 association_id
        Long associationId = extractPrimaryKey(event, "alumni_association_id");
        if (associationId == null) {
            log.warn("[ES同步-校友会] 无法提取associationId - EventId: {}", eventId);
            return;
        }

        try {
            if (event.getEventType() == DataChangeEvent.EventType.DELETE) {
                // 删除ES索引
                associationDocumentRepository.deleteById(associationId);
                log.info("[ES同步-校友会] 索引已删除 - associationId: {}, EventId: {}", associationId, eventId);
            } else {
                // INSERT/UPDATE：从数据库查询并同步到ES
                AlumniAssociation entity = alumniAssociationService.getById(associationId);
                if (entity == null) {
                    log.warn("[ES同步-校友会] 校友会不存在 - associationId: {}, EventId: {}", associationId, eventId);
                    return;
                }

                // 转换为ES文档
                AssociationDocument document = associationDataConverter.toDocument(entity);
                if (document == null) {
                    log.warn("[ES同步-校友会] 数据转换失败 - associationId: {}, EventId: {}", associationId, eventId);
                    return;
                }

                // 保存到ES
                associationDocumentRepository.save(document);
                log.info("[ES同步-校友会] 索引已更新 - associationId: {}, EventId: {}", associationId, eventId);
            }
        } catch (Exception e) {
            log.error("[ES同步-校友会] 同步失败 - associationId: {}, EventId: {}", associationId, eventId, e);
            throw e;
        }
    }

    /**
     * 处理商户数据同步
     */
    private void handleMerchantSync(DataChangeEvent event, String eventId) {
        // 提取 merchant_id
        Long merchantId = extractPrimaryKey(event, "merchant_id");
        if (merchantId == null) {
            log.warn("[ES同步-商户] 无法提取merchantId - EventId: {}", eventId);
            return;
        }

        try {
            if (event.getEventType() == DataChangeEvent.EventType.DELETE) {
                // 删除ES索引
                merchantDocumentRepository.deleteById(merchantId);
                log.info("[ES同步-商户] 索引已删除 - merchantId: {}, EventId: {}", merchantId, eventId);
            } else {
                // INSERT/UPDATE：从数据库查询并同步到ES
                // TODO: 调用 MerchantMergeService 或直接查询数据库
                log.warn("[ES同步-商户] 商户数据同步功能待实现 - merchantId: {}, EventId: {}", merchantId, eventId);
            }
        } catch (Exception e) {
            log.error("[ES同步-商户] 同步失败 - merchantId: {}, EventId: {}", merchantId, eventId, e);
            throw e;
        }
    }

    /**
     * 从事件中提取主键值
     */
    private Long extractPrimaryKey(DataChangeEvent event, String primaryKeyName) {
        Object value = event.getEventType() == DataChangeEvent.EventType.DELETE
                ? event.getBeforeValue(primaryKeyName)
                : event.getAfterValue(primaryKeyName);

        if (value == null) {
            return null;
        }

        try {
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            } else if (value instanceof String) {
                return Long.parseLong((String) value);
            }
        } catch (Exception e) {
            log.warn("[ES同步] 提取主键失败 - PrimaryKeyName: {}, Value: {}", primaryKeyName, value);
        }

        return null;
    }
}
