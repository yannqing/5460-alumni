package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.common.dto.NotificationStatistic;
import com.cmswe.alumni.common.dto.QueryNotificationListDto;
import com.cmswe.alumni.common.entity.Notification;
import com.cmswe.alumni.common.vo.NotificationVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.service.user.mapper.NotificationMapper;
import com.cmswe.alumni.service.user.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知消息服务实现类
 *
 * @author CMSWE
 * @since 2025-12-05
 */
@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithDeduplication(Notification notification) {
        // 检查消息是否已存在（根据 messageId 去重）
        Notification existing = baseMapper.selectByMessageId(notification.getMessageId());
        if (existing != null) {
            log.info("通知消息已存在，跳过保存 - MessageId: {}", notification.getMessageId());
            return false;
        }

        // 保存新通知
        boolean result = this.save(notification);
        if (result) {
            log.info("保存通知成功 - NotificationId: {}, MessageId: {}, ToUserId: {}",
                    notification.getNotificationId(),
                    notification.getMessageId(),
                    notification.getToUserId());
        }
        return result;
    }

    @Override
    public int getUnreadCount(Long userId) {
        return baseMapper.countUnreadByUserId(userId);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId, Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        return baseMapper.selectByUserId(userId, offset, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllAsRead(Long userId) {
        int count = baseMapper.markAllAsReadByUserId(userId);
        log.info("标记用户所有通知为已读 - UserId: {}, Count: {}", userId, count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long notificationId, Long userId) {
        int count = baseMapper.markAsRead(notificationId, userId);
        if (count > 0) {
            log.info("标记通知为已读 - NotificationId: {}, UserId: {}", notificationId, userId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotification(Long notificationId, Long userId) {
        int count = baseMapper.deleteByIdAndUserId(notificationId, userId);
        if (count > 0) {
            log.info("删除通知 - NotificationId: {}, UserId: {}", notificationId, userId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteNotifications(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }
        int count = baseMapper.batchDeleteByUserId(notificationIds, userId);
        log.info("批量删除通知 - UserId: {}, Count: {}", userId, count);
        return count;
    }

    @Override
    public List<NotificationStatistic> getNotificationStatistics(Long userId) {
        return baseMapper.countByMessageType(userId);
    }

    @Override
    public PageVo<NotificationVo> getNotificationListPage(Long userId, QueryNotificationListDto queryDto) {
        // 1. 构建分页对象
        Page<Notification> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getToUserId, userId);

        // 2.1 阅读状态筛选
        if (queryDto.getReadStatus() != null) {
            queryWrapper.eq(Notification::getReadStatus, queryDto.getReadStatus());
        }

        // 2.2 消息类型筛选
        if (StringUtils.isNotBlank(queryDto.getMessageType())) {
            queryWrapper.eq(Notification::getMessageType, queryDto.getMessageType());
        }

        // 2.3 按创建时间倒序排列
        queryWrapper.orderByDesc(Notification::getCreatedTime);

        // 3. 执行分页查询
        Page<Notification> notificationPage = this.page(page, queryWrapper);

        // 4. 如果查询结果为空，直接返回空列表
        if (notificationPage.getRecords().isEmpty()) {
            return new PageVo<>(new ArrayList<>(), 0L, (long) queryDto.getPageNum(), (long) queryDto.getPageSize());
        }

        // 5. 转换为 VO 列表
        List<NotificationVo> voList = notificationPage.getRecords().stream()
                .map(notification -> {
                    NotificationVo vo = new NotificationVo();
                    vo.setNotificationId(String.valueOf(notification.getNotificationId()));
                    vo.setMessageType(notification.getMessageType());
                    vo.setFromUserId(notification.getFromUserId() != null ? String.valueOf(notification.getFromUserId()) : null);
                    vo.setFromUsername(notification.getFromUsername());
                    vo.setTitle(notification.getTitle());
                    vo.setContent(notification.getContent());
                    vo.setRelatedId(notification.getRelatedId() != null ? String.valueOf(notification.getRelatedId()) : null);
                    vo.setRelatedType(notification.getRelatedType());
                    vo.setReadStatus(notification.getReadStatus());
                    vo.setReadTime(notification.getReadTime());
                    vo.setExtraData(notification.getExtraData());
                    vo.setCreatedTime(notification.getCreatedTime());
                    return vo;
                })
                .collect(Collectors.toList());

        // 6. 构建返回结果
        return new PageVo<>(voList, notificationPage.getTotal(), notificationPage.getCurrent(), notificationPage.getSize());
    }
}
