package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.UserPrivacySettingService;
import com.cmswe.alumni.common.entity.UserPrivacySetting;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.service.user.mapper.UserPrivacySettingMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户隐私设置表 Service 实现类
 * @author system
 */
@Slf4j
@Service
public class UserPrivacySettingServiceImpl extends ServiceImpl<UserPrivacySettingMapper, UserPrivacySetting> implements UserPrivacySettingService {

    @Resource
    private RedisCache redisCache;

    /**
     * Redis缓存键前缀（需与 PrivacyFilterAspect 保持一致）
     */
    private static final String PRIVACY_CACHE_KEY_PREFIX = "privacy:user:";

    @Override
    public List<UserPrivacySetting> getByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        LambdaQueryWrapper<UserPrivacySetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPrivacySetting::getWxId, userId);

        return this.list(queryWrapper);
    }

    @Override
    public boolean updateSetting(Long userId, Long settingId, Integer visibility, Integer searchable) {
        if (userId == null || settingId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        UserPrivacySetting existing = this.getById(settingId);
        boolean result;

        if (existing != null) {
            // 更新现有记录
            existing.setVisibility(visibility);
            existing.setSearchable(searchable);
            existing.setUpdateTime(LocalDateTime.now());
            result = this.updateById(existing);
        } else {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        // 清除 Redis 缓存
        if (result) {
            clearUserPrivacyCache(userId);
        }

        return result;
    }

    /**
     * 清除用户的隐私设置缓存
     *
     * @param userId 用户ID
     */
    private void clearUserPrivacyCache(Long userId) {
        try {
            String cacheKey = PRIVACY_CACHE_KEY_PREFIX + userId;
            redisCache.deleteObject(cacheKey);
            log.info("已清除用户 {} 的隐私设置缓存", userId);
        } catch (Exception e) {
            log.error("清除用户 {} 的隐私设置缓存失败", userId, e);
        }
    }
}