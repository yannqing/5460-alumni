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

    @Override
    public void batchWarmupCache(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            log.debug("批量预热缓存：用户ID列表为空");
            return;
        }

        log.debug("开始批量预热隐私设置缓存 - 用户数: {}", userIds.size());

        try {
            // 1. 批量查询所有用户的隐私设置
            java.util.Map<Long, List<UserPrivacySetting>> settingsMap = batchGetByUserIds(userIds);

            // 2. 批量写入 Redis 缓存
            int cachedCount = 0;
            for (Long userId : userIds) {
                try {
                    String cacheKey = PRIVACY_CACHE_KEY_PREFIX + userId;
                    List<UserPrivacySetting> settings = settingsMap.getOrDefault(userId, new java.util.ArrayList<>());

                    // 转换为需要隐藏的字段代码集合
                    java.util.Set<String> hiddenFieldCodes = new java.util.HashSet<>();
                    for (UserPrivacySetting setting : settings) {
                        // visibility=0 表示不可见
                        if (setting.getVisibility() != null && setting.getVisibility() == 0) {
                            hiddenFieldCodes.add(setting.getFieldCode());
                        }
                    }

                    // 写入缓存（即使是空集合也缓存，避免缓存穿透）
                    redisCache.setCacheSet(cacheKey, hiddenFieldCodes);
                    redisCache.expire(cacheKey, 30, java.util.concurrent.TimeUnit.MINUTES);
                    cachedCount++;

                } catch (Exception e) {
                    log.error("预热用户 {} 的隐私设置缓存失败", userId, e);
                }
            }

            log.info("批量预热隐私设置缓存完成 - 总用户数: {}, 成功缓存: {}", userIds.size(), cachedCount);

        } catch (Exception e) {
            log.error("批量预热隐私设置缓存失败", e);
        }
    }

    @Override
    public java.util.Map<Long, List<UserPrivacySetting>> batchGetByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("批量查询隐私设置：用户ID列表为空");
            return new java.util.HashMap<>();
        }

        try {
            // 批量查询所有用户的隐私设置
            LambdaQueryWrapper<UserPrivacySetting> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(UserPrivacySetting::getWxId, userIds);

            List<UserPrivacySetting> allSettings = this.list(queryWrapper);

            // 按用户ID分组
            java.util.Map<Long, List<UserPrivacySetting>> settingsMap = allSettings.stream()
                    .collect(java.util.stream.Collectors.groupingBy(UserPrivacySetting::getWxId));

            log.debug("批量查询隐私设置完成 - 查询用户数: {}, 有设置的用户数: {}",
                    userIds.size(), settingsMap.size());

            return settingsMap;

        } catch (Exception e) {
            log.error("批量查询隐私设置失败", e);
            return new java.util.HashMap<>();
        }
    }
}