package com.cmswe.alumni.service.user.service.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.model.UserOnlineStatusKaf;
import com.cmswe.alumni.redis.utils.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 在线状态统计消费者
 *
 * 职责：
 * 1. 统计实时在线用户数
 * 2. 记录用户上线/下线日志（用于后续数据分析）
 * 3. 统计每日活跃用户数（DAU）
 * 4. 统计每小时在线峰值
 *
 * Consumer Group: online-status-statistics-group
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class OnlineStatusStatisticsConsumer {

    @Resource
    private RedisCache redisCache;

    /**
     * 在线用户数统计 Key
     */
    private static final String ONLINE_COUNT_KEY = "statistics:online:count";

    /**
     * 每日活跃用户集合 Key 前缀
     */
    private static final String DAU_KEY_PREFIX = "statistics:dau:";

    /**
     * 每小时峰值 Key 前缀
     */
    private static final String HOURLY_PEAK_KEY_PREFIX = "statistics:hourly:peak:";

    /**
     * 用户上线日志 Key 前缀
     */
    private static final String USER_ONLINE_LOG_KEY_PREFIX = "statistics:log:online:";

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");

    /**
     * 消费用户在线状态事件 - 更新统计数据
     *
     * @param message 消息体（JSON 格式的 UserOnlineStatusKaf）
     * @param partition 分区号
     * @param offset 偏移量
     */
    @KafkaListener(
            topics = KafkaTopicConstants.USER_ONLINE_STATUS_TOPIC,
            groupId = "online-status-statistics-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void updateStatistics(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            log.info("[Statistics Consumer] 收到在线状态事件 - Partition: {}, Offset: {}, Message: {}",
                    partition, offset, message);

            // 1. 反序列化消息
            UserOnlineStatusKaf event = parseMessage(message);
            if (event == null) {
                log.warn("[Statistics Consumer] 消息解析失败，跳过处理 - Partition: {}, Offset: {}",
                        partition, offset);
                return;
            }

            Long userId = event.getWxId();
            UserOnlineStatusKaf.StatusAction action = event.getAction();

            // 2. 根据动作类型更新统计
            switch (action) {
                case ONLINE:
                    updateOnlineStatistics(userId, event);
                    break;
                case OFFLINE:
                    updateOfflineStatistics(userId, event);
                    break;
                case HEARTBEAT:
                    // 心跳事件只记录日志，不更新统计
                    log.debug("[Statistics Consumer] 心跳事件，跳过统计 - UserId: {}", userId);
                    break;
                default:
                    log.warn("[Statistics Consumer] 未知动作类型: {} - UserId: {}", action, userId);
            }

            log.info("[Statistics Consumer] 统计更新成功 - UserId: {}, Action: {}, Partition: {}, Offset: {}",
                    userId, action, partition, offset);

        } catch (Exception e) {
            log.error("[Statistics Consumer] 处理失败 - Partition: {}, Offset: {}, Message: {}, Error: {}",
                    partition, offset, message, e.getMessage(), e);

            // 注意：这里不抛异常，避免消息重复消费
        }
    }

    /**
     * 更新用户上线统计
     *
     * @param userId 用户ID
     * @param event 事件对象
     */
    private void updateOnlineStatistics(Long userId, UserOnlineStatusKaf event) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String today = now.format(DATE_FORMATTER);
            String currentHour = now.format(HOUR_FORMATTER);

            // 1. 增加实时在线用户数
            Long currentOnline = incrementOnlineCount();

            // 2. 记录到每日活跃用户集合（DAU）
            addToDAU(userId, today);

            // 3. 更新每小时峰值
            updateHourlyPeak(currentHour, currentOnline);

            // 4. 记录用户上线日志（用于后续分析）
            logUserOnline(userId, event, now);

            log.info("[Statistics Consumer] 用户上线统计已更新 - UserId: {}, CurrentOnline: {}, DAU: {}, Hour: {}",
                    userId, currentOnline, today, currentHour);

        } catch (Exception e) {
            log.error("[Statistics Consumer] 更新上线统计失败 - UserId: {}, Error: {}",
                    userId, e.getMessage(), e);
        }
    }

    /**
     * 更新用户下线统计
     *
     * @param userId 用户ID
     * @param event 事件对象
     */
    private void updateOfflineStatistics(Long userId, UserOnlineStatusKaf event) {
        try {
            // 1. 减少实时在线用户数
            Long currentOnline = decrementOnlineCount();

            log.info("[Statistics Consumer] 用户下线统计已更新 - UserId: {}, CurrentOnline: {}",
                    userId, currentOnline);

        } catch (Exception e) {
            log.error("[Statistics Consumer] 更新下线统计失败 - UserId: {}, Error: {}",
                    userId, e.getMessage(), e);
        }
    }

    /**
     * 增加在线用户数
     *
     * @return 当前在线用户数
     */
    private Long incrementOnlineCount() {
        // 使用 Redis 原子操作增加计数
        return redisCache.increment(ONLINE_COUNT_KEY);
    }

    /**
     * 减少在线用户数
     *
     * @return 当前在线用户数
     */
    private Long decrementOnlineCount() {
        // 使用 Redis 原子操作减少计数
        Long count = redisCache.decrement(ONLINE_COUNT_KEY);
        // 确保不会小于 0
        if (count != null && count < 0) {
            redisCache.setCacheObject(ONLINE_COUNT_KEY, 0);
            return 0L;
        }
        return count;
    }

    /**
     * 添加到每日活跃用户集合
     *
     * @param userId 用户ID
     * @param date 日期（yyyy-MM-dd）
     */
    private void addToDAU(Long userId, String date) {
        String dauKey = DAU_KEY_PREFIX + date;
        // 使用 Set 存储，自动去重
        redisCache.addToSet(dauKey, userId);

        // 设置过期时间：保留 30 天
        redisCache.expire(dauKey, 30, TimeUnit.DAYS);
    }

    /**
     * 更新每小时峰值
     *
     * @param hour 小时（yyyy-MM-dd:HH）
     * @param currentOnline 当前在线数
     */
    private void updateHourlyPeak(String hour, Long currentOnline) {
        String peakKey = HOURLY_PEAK_KEY_PREFIX + hour;

        // 获取当前小时的峰值
        Long peak = redisCache.getCacheObject(peakKey);
        peak = peak != null ? peak : 0L;

        // 如果当前在线数大于峰值，则更新
        if (currentOnline > peak) {
            redisCache.setCacheObject(peakKey, currentOnline);
            // 设置过期时间：保留 7 天
            redisCache.expire(peakKey, 7, TimeUnit.DAYS);

            log.info("[Statistics Consumer] 每小时峰值已更新 - Hour: {}, Peak: {} -> {}",
                    hour, peak, currentOnline);
        }
    }

    /**
     * 记录用户上线日志
     *
     * @param userId 用户ID
     * @param event 事件对象
     * @param now 当前时间
     */
    private void logUserOnline(Long userId, UserOnlineStatusKaf event, LocalDateTime now) {
        String logKey = USER_ONLINE_LOG_KEY_PREFIX + now.format(DATE_FORMATTER);

        // 构建日志记录
        String logEntry = String.format("%s|%d|%s|%s|%s",
                now.toString(),
                userId,
                event.getServerId(),
                event.getClientIp(),
                event.getDeviceId());

        // 追加到日志列表
        redisCache.rightPushToList(logKey, logEntry);

        // 设置过期时间：保留 3 天
        redisCache.expire(logKey, 3, TimeUnit.DAYS);
    }

    /**
     * 解析消息
     *
     * @param message 消息字符串
     * @return 解析后的事件对象
     */
    private UserOnlineStatusKaf parseMessage(String message) {
        try {
            return JSON.parseObject(message, UserOnlineStatusKaf.class);
        } catch (Exception e1) {
            try {
                // 兼容旧格式：只有 userId
                Long userId = JSON.parseObject(message, Long.class);
                UserOnlineStatusKaf event = new UserOnlineStatusKaf();
                event.setWxId(userId);
                event.setAction(UserOnlineStatusKaf.StatusAction.ONLINE);
                event.setTimestamp(System.currentTimeMillis());

                log.warn("[Statistics Consumer] 使用简单格式兼容模式 - UserId: {}", userId);
                return event;
            } catch (Exception e2) {
                log.error("[Statistics Consumer] 消息解析失败 - Message: {}, Error: {}",
                        message, e2.getMessage());
                return null;
            }
        }
    }

    /**
     * 获取当前在线用户数（供外部调用）
     *
     * @return 当前在线用户数
     */
    public Long getCurrentOnlineCount() {
        Long count = redisCache.getCacheObject(ONLINE_COUNT_KEY);
        return count != null ? count : 0L;
    }

    /**
     * 获取指定日期的 DAU（供外部调用）
     *
     * @param date 日期（yyyy-MM-dd）
     * @return DAU 数量
     */
    public Long getDAU(String date) {
        String dauKey = DAU_KEY_PREFIX + date;
        return redisCache.getSetSize(dauKey);
    }

    /**
     * 获取指定小时的峰值（供外部调用）
     *
     * @param hour 小时（yyyy-MM-dd:HH）
     * @return 峰值在线数
     */
    public Long getHourlyPeak(String hour) {
        String peakKey = HOURLY_PEAK_KEY_PREFIX + hour;
        Long peak = redisCache.getCacheObject(peakKey);
        return peak != null ? peak : 0L;
    }
}