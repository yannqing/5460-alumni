package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.LocalPlatformPrivacySettingService;
import com.cmswe.alumni.common.entity.LocalPlatformPrivacySetting;
import com.cmswe.alumni.common.entity.SysConfig;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.service.association.mapper.LocalPlatformPrivacySettingMapper;
import com.cmswe.alumni.service.system.mapper.SysConfigMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 校促会隐私设置Service实现类
 */
@Slf4j
@Service
public class LocalPlatformPrivacySettingServiceImpl
        extends ServiceImpl<LocalPlatformPrivacySettingMapper, LocalPlatformPrivacySetting>
        implements LocalPlatformPrivacySettingService {

    @Resource
    private SysConfigMapper sysConfigMapper;

    @Override
    public List<LocalPlatformPrivacySetting> getPlatformPrivacy(Long platformId) {
        if (platformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 1. 获取系统关于校促会隐私的配置项 (适配数据库中的 Key: local_platform_privcay_sttting)
        SysConfig parentConfig = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, "local_platform_privcay_sttting"));

        if (parentConfig == null) {
            log.warn("未找到校促会隐私配置主节点：local_platform_privcay_sttting，请检查 sys_config 表中的 config_key 是否匹配");
            return List.of();
        }

        List<SysConfig> subConfigs = sysConfigMapper.selectList(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getParentId, parentConfig.getConfigId())
                        .eq(SysConfig::getStatus, "1"));

        if (subConfigs.isEmpty()) {
            log.warn("校促会隐私配置子项列表为空，请检查 parent_id 为 {} 的配置项状态是否为正常(1)", parentConfig.getConfigId());
            return List.of();
        }

        // 2. 检查并同步到校促会隐私设置表
        return subConfigs.stream().map(config -> {
            LocalPlatformPrivacySetting setting = this.getOne(
                    new LambdaQueryWrapper<LocalPlatformPrivacySetting>()
                            .eq(LocalPlatformPrivacySetting::getPlatformId, platformId)
                            .eq(LocalPlatformPrivacySetting::getFieldCode, config.getConfigKey()));

            if (setting == null) {
                // 初始化默认设置 (默认可见性设为 0-不可见，可根据实际需求调整)
                setting = new LocalPlatformPrivacySetting();
                setting.setPlatformId(platformId);
                setting.setFieldName(config.getConfigName());
                setting.setFieldCode(config.getConfigKey());
                setting.setVisibility(0); // 默认不可见
                setting.setCreateTime(LocalDateTime.now());
                setting.setUpdateTime(LocalDateTime.now());
                this.save(setting);
            }
            return setting;
        }).toList();
    }

    @Override
    public boolean updatePrivacy(Long platformId, String fieldCode, Integer visibility) {
        if (platformId == null || fieldCode == null || visibility == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LocalPlatformPrivacySetting setting = this.getOne(
                new LambdaQueryWrapper<LocalPlatformPrivacySetting>()
                        .eq(LocalPlatformPrivacySetting::getPlatformId, platformId)
                        .eq(LocalPlatformPrivacySetting::getFieldCode, fieldCode));

        if (setting == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "未找到对应的隐私配置项");
        }

        setting.setVisibility(visibility);
        setting.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(setting);

        if (result) {
            log.info("更新校促会隐私设置成功 - PlatformId: {}, Field: {}, Visibility: {}", platformId, fieldCode, visibility);
        }

        return result;
    }
}
