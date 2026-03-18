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
            // 1. 先检查哪些用户的缓存不存在（性能优化：避免重复预热）
            java.util.List<Long> uncachedUserIds = new java.util.ArrayList<>();
            int alreadyCachedCount = 0;

            for (Long userId : userIds) {
                String cacheKey = PRIVACY_CACHE_KEY_PREFIX + userId;
                // 检查缓存是否存在
                if (!redisCache.hasKey(cacheKey)) {
                    uncachedUserIds.add(userId);
                } else {
                    alreadyCachedCount++;
                }
            }

            log.debug("缓存检查完成 - 总用户数: {}, 已缓存: {}, 需预热: {}",
                    userIds.size(), alreadyCachedCount, uncachedUserIds.size());

            // 2. 如果所有用户都已缓存，直接返回
            if (uncachedUserIds.isEmpty()) {
                log.debug("所有用户的隐私设置已缓存，无需预热");
                return;
            }

            // 3. 批量查询缺失的用户的隐私设置
            java.util.Map<Long, List<UserPrivacySetting>> settingsMap = batchGetByUserIds(uncachedUserIds);

            // 4. 批量写入 Redis 缓存（只写入缺失的）
            int cachedCount = 0;
            for (Long userId : uncachedUserIds) {
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

                    // 写入缓存并设置过期时间（即使是空集合也缓存，避免缓存穿透）
                    // 使用优化版本的 setCacheSet，一次操作完成写入和设置过期时间
                    redisCache.setCacheSet(cacheKey, hiddenFieldCodes, 30, java.util.concurrent.TimeUnit.MINUTES);
                    cachedCount++;

                } catch (Exception e) {
                    log.error("预热用户 {} 的隐私设置缓存失败", userId, e);
                }
            }

            log.info("批量预热隐私设置缓存完成 - 总用户数: {}, 已缓存: {}, 新预热: {}",
                    userIds.size(), alreadyCachedCount, cachedCount);

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