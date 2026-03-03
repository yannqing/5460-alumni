package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.LocalPlatformPrivacySetting;

import java.util.List;

/**
 * 校促会隐私设置Service接口
 */
public interface LocalPlatformPrivacySettingService extends IService<LocalPlatformPrivacySetting> {
    /**
     * 获取校促会隐私设置（自动初始化）
     * 
     * @param platformId 校促会ID
     * @return 隐私设置列表
     */
    List<LocalPlatformPrivacySetting> getPlatformPrivacy(Long platformId);

    /**
     * 更新校促会隐私设置
     * 
     * @param platformId 校促会ID
     * @param fieldCode  字段代码
     * @param visibility 可见性
     * @return 是否更新成功
     */
    boolean updatePrivacy(Long platformId, String fieldCode, Integer visibility);
}
