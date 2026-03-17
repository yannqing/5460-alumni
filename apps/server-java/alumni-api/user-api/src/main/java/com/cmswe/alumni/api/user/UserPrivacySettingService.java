package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.UserPrivacySetting;

import java.util.List;

/**
 * 用户隐私设置表 Service 接口
 * @author system
 */
public interface UserPrivacySettingService extends IService<UserPrivacySetting> {

    /**
     * 根据用户ID获取隐私设置
     * @param userId 用户ID
     * @return 隐私设置列表
     */
    List<UserPrivacySetting> getByUserId(Long userId);

    /**
     * 更新隐私设置
     * @param userId 用户ID
     * @param settingId 设置id
     * @param visibility 可见性
     * @param searchable 是否可搜索
     * @return 是否成功
     */
    boolean updateSetting(Long userId, Long settingId, Integer visibility, Integer searchable);

    /**
     * 批量预热用户隐私设置缓存
     * <p>将多个用户的隐私设置批量加载到 Redis 缓存，避免后续 AOP 处理时的 N+1 查询
     * <p>适用场景：在返回用户列表前，提前预热缓存
     *
     * @param userIds 用户ID列表
     */
    void batchWarmupCache(List<Long> userIds);

    /**
     * 批量获取多个用户的隐私设置
     * <p>性能优化方法，避免 N+1 查询问题
     *
     * @param userIds 用户ID列表
     * @return Map<userId, List<UserPrivacySetting>> 每个用户对应的隐私设置列表
     */
    java.util.Map<Long, List<UserPrivacySetting>> batchGetByUserIds(List<Long> userIds);
}