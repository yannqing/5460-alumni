package com.cmswe.alumni.service.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.NotificationStatistic;
import com.cmswe.alumni.common.dto.QueryNotificationListDto;
import com.cmswe.alumni.common.entity.Notification;
import com.cmswe.alumni.common.vo.NotificationVo;
import com.cmswe.alumni.common.vo.PageVo;

import java.util.List;

/**
 * 通知消息服务接口
 *
 * @author CMSWE
 * @since 2025-12-05
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 保存通知（带去重）
     *
     * @param notification 通知
     * @return 是否保存成功
     */
    boolean saveWithDeduplication(Notification notification);

    /**
     * 查询用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int getUnreadCount(Long userId);

    /**
     * 查询用户通知列表（分页）
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 通知列表
     */
    List<Notification> getUserNotifications(Long userId, Integer page, Integer size);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 更新数量
     */
    int markAllAsRead(Long userId);

    /**
     * 标记单个通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 批量删除通知
     *
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 删除数量
     */
    int batchDeleteNotifications(List<Long> notificationIds, Long userId);

    /**
     * 查询用户按类型分组的通知统计
     *
     * @param userId 用户ID
     * @return 统计列表
     */
    List<NotificationStatistic> getNotificationStatistics(Long userId);

    /**
     * 分页查询用户通知列表
     *
     * @param userId 用户ID
     * @param queryDto 查询条件
     * @return 通知列表
     */
    PageVo<NotificationVo> getNotificationListPage(Long userId, QueryNotificationListDto queryDto);
}
