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
}