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

        long startTime = System.currentTimeMillis();
        log.debug("开始批量预热隐私设置缓存 - 用户数: {}", userIds.size());

        try {
            // 1. 批量检查哪些用户的缓存不存在（性能优化：使用 Pipeline 批量检查，减少网络往返）
            java.util.List<String> cacheKeys = new java.util.ArrayList<>();
            java.util.Map<String, Long> keyToUserIdMap = new java.util.HashMap<>();

            for (Long userId : userIds) {
                String cacheKey = PRIVACY_CACHE_KEY_PREFIX + userId;
                cacheKeys.add(cacheKey);
                keyToUserIdMap.put(cacheKey, userId);
            }

            // 批量检查缓存是否存在（一次 Pipeline 操作，替代 N 次单独查询）
            java.util.Map<String, Boolean> existsMap = redisCache.batchHasKey(cacheKeys);

            java.util.List<Long> uncachedUserIds = new java.util.ArrayList<>();
            int alreadyCachedCount = 0;

            for (java.util.Map.Entry<String, Boolean> entry : existsMap.entrySet()) {
                if (Boolean.FALSE.equals(entry.getValue())) {
                    uncachedUserIds.add(keyToUserIdMap.get(entry.getKey()));
                } else {
                    alreadyCachedCount++;
                }
            }

            long checkTime = System.currentTimeMillis() - startTime;
            log.debug("缓存检查完成 - 总用户数: {}, 已缓存: {}, 需预热: {}, 耗时: {}ms",
                    userIds.size(), alreadyCachedCount, uncachedUserIds.size(), checkTime);

            // 2. 如果所有用户都已缓存，直接返回
            if (uncachedUserIds.isEmpty()) {
                log.debug("所有用户的隐私设置已缓存，无需预热");
                return;
            }

            // 3. 批量查询缺失的用户的隐私设置（一次数据库查询，使用 IN 子句）
            long queryStartTime = System.currentTimeMillis();
            java.util.Map<Long, List<UserPrivacySetting>> settingsMap = batchGetByUserIds(uncachedUserIds);
            long queryTime = System.currentTimeMillis() - queryStartTime;
            log.debug("批量查询隐私设置完成 - 耗时: {}ms", queryTime);

            // 4. 批量写入 Redis 缓存（使用 setCacheSet 保证序列化一致性）
            long cacheStartTime = System.currentTimeMillis();
            int cachedCount = 0;

            // 批量写入缓存（使用 setCacheSet 方法，它会使用 FastJSON 序列化器）
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

                    // 使用 RedisCache 的 setCacheSet 方法（它会自动处理空集合并使用正确的序列化器）
                    redisCache.setCacheSet(cacheKey, hiddenFieldCodes, 30, java.util.concurrent.TimeUnit.MINUTES);
                    cachedCount++;

                } catch (Exception e) {
                    log.error("预热用户 {} 的隐私设置缓存失败", userId, e);
                }
            }
            long cacheTime = System.currentTimeMillis() - cacheStartTime;
            log.debug("批量写入缓存完成 - 耗时: {}ms", cacheTime);

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("批量预热隐私设置缓存完成 - 总用户数: {}, 已缓存: {}, 新预热: {}, 总耗时: {}ms (检查:{}ms, 查询:{}ms, 缓存:{}ms)",
                    userIds.size(), alreadyCachedCount, cachedCount, totalTime, checkTime, queryTime, cacheTime);

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
            long startTime = System.currentTimeMillis();
            log.info("开始批量查询隐私设置 - 用户数: {}, 用户ID列表: {}", userIds.size(), userIds);

            // 批量查询所有用户的隐私设置
            LambdaQueryWrapper<UserPrivacySetting> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(UserPrivacySetting::getWxId, userIds);

            long queryStartTime = System.currentTimeMillis();
            List<UserPrivacySetting> allSettings = this.list(queryWrapper);
            long queryDuration = System.currentTimeMillis() - queryStartTime;

            log.info("数据库查询完成 - 耗时: {}ms, 查询到记录数: {}", queryDuration, allSettings.size());

            // 按用户ID分组
            long groupStartTime = System.currentTimeMillis();
            java.util.Map<Long, List<UserPrivacySetting>> settingsMap = allSettings.stream()
                    .collect(java.util.stream.Collectors.groupingBy(UserPrivacySetting::getWxId));
            long groupDuration = System.currentTimeMillis() - groupStartTime;

            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("批量查询隐私设置完成 - 总耗时: {}ms (查询: {}ms, 分组: {}ms), 查询用户数: {}, 有设置的用户数: {}",
                    totalDuration, queryDuration, groupDuration, userIds.size(), settingsMap.size());

            return settingsMap;

        } catch (Exception e) {
            log.error("批量查询隐私设置失败 - 用户ID列表: {}", userIds, e);
            return new java.util.HashMap<>();
        }
    }
}