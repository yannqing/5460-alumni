package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.dto.NotificationStatistic;
import com.cmswe.alumni.common.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知消息 Mapper 接口
 *
 * @author CMSWE
 * @since 2025-12-05
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 查询用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 查询用户通知列表（分页）
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 通知列表
     */
    List<Notification> selectByUserId(@Param("userId") Long userId,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 更新数量
     */
    int markAllAsReadByUserId(@Param("userId") Long userId);

    /**
     * 标记单个通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 更新数量
     */
    int markAsRead(@Param("notificationId") Long notificationId,
                   @Param("userId") Long userId);

    /**
     * 根据消息ID查询通知（用于去重）
     *
     * @param messageId 消息ID
     * @return 通知
     */
    Notification selectByMessageId(@Param("messageId") String messageId);

    /**
     * 删除用户指定的通知（逻辑删除）
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByIdAndUserId(@Param("notificationId") Long notificationId,
                            @Param("userId") Long userId);

    /**
     * 批量删除用户的通知（逻辑删除）
     *
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 删除数量
     */
    int batchDeleteByUserId(@Param("notificationIds") List<Long> notificationIds,
                            @Param("userId") Long userId);

    /**
     * 查询用户按类型分组的通知统计
     *
     * @param userId 用户ID
     * @return 统计列表
     */
    List<NotificationStatistic> countByMessageType(@Param("userId") Long userId);
}
