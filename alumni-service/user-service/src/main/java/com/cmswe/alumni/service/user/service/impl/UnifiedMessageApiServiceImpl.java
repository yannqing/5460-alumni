package com.cmswe.alumni.service.user.service.impl;

import com.cmswe.alumni.api.user.UnifiedMessageApiService;
import com.cmswe.alumni.common.enums.NotificationType;
import com.cmswe.alumni.service.user.service.message.UnifiedMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 统一消息API服务实现类
 * 包装UnifiedMessageService供其他模块调用
 */
@Slf4j
@Service
public class UnifiedMessageApiServiceImpl implements UnifiedMessageApiService {

    @Resource
    private UnifiedMessageService unifiedMessageService;

    @Override
    public boolean sendSystemNotification(Long userId, NotificationType notificationType,
                                          String title, String content) {
        return unifiedMessageService.sendSystemNotification(userId, notificationType, title, content);
    }

    @Override
    public boolean sendSystemNotification(Long userId, NotificationType notificationType,
                                          String title, String content,
                                          Long relatedId, String relatedType) {
        return unifiedMessageService.sendSystemNotification(userId, notificationType, title, content, relatedId, relatedType);
    }

    @Override
    public boolean sendBusinessNotification(Long userId, String messageType,
                                            String title, String content,
                                            Long relatedId, String relatedType) {
        return unifiedMessageService.sendBusinessNotification(userId, messageType, title, content, relatedId, relatedType);
    }
}
