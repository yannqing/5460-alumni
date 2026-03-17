package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.api.user.UserPrivacyBatchInitService;
import com.cmswe.alumni.common.entity.SysConfig;
import com.cmswe.alumni.common.entity.UserPrivacySetting;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.vo.BatchInitResultVo;
import com.cmswe.alumni.service.system.mapper.SysConfigMapper;
import com.cmswe.alumni.service.user.mapper.UserPrivacySettingMapper;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户隐私设置批量初始化服务实现
 *
 * @author CMSWE
 * @since 2026-03-17
 */
@Slf4j
@Service
public class UserPrivacyBatchInitServiceImpl implements UserPrivacyBatchInitService {

    @Resource
    private WxUserMapper wxUserMapper;

    @Resource
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Resource
    private SysConfigMapper sysConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchInitResultVo batchInitAllUsers() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("开始批量初始化用户隐私设置");

        try {
            // 1. 查询所有用户
            List<WxUser> allUsers = wxUserMapper.selectList(null);
            int totalUsers = allUsers.size();
            log.info("查询到总用户数: {}", totalUsers);

            // 2. 查询已有隐私设置的用户ID
            List<UserPrivacySetting> existingSettings = userPrivacySettingMapper.selectList(null);
            Set<Long> usersWithSettings = existingSettings.stream()
                    .map(UserPrivacySetting::getWxId)
                    .collect(Collectors.toSet());
            log.info("已有隐私设置的用户数: {}", usersWithSettings.size());

            // 3. 筛选出需要初始化的用户
            List<WxUser> usersNeedInit = allUsers.stream()
                    .filter(user -> !usersWithSettings.contains(user.getWxId()))
                    .toList();
            int needInitUsers = usersNeedInit.size();
            log.info("需要初始化的用户数: {}", needInitUsers);

            if (needInitUsers == 0) {
                LocalDateTime endTime = LocalDateTime.now();
                return BatchInitResultVo.builder()
                        .totalUsers(totalUsers)
                        .needInitUsers(0)
                        .successCount(0)
                        .failedCount(0)
                        .startTime(startTime)
                        .endTime(endTime)
                        .durationMs(java.time.Duration.between(startTime, endTime).toMillis())
                        .failedUserIds(List.of())
                        .build();
            }

            // 4. 查询系统配置的隐私字段
            SysConfig parentConfig = sysConfigMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>()
                            .eq(SysConfig::getConfigKey, "user_privacy_setting")
            );

            if (parentConfig == null) {
                log.error("未找到系统隐私配置 - configKey: user_privacy_setting");
                throw new RuntimeException("系统隐私配置不存在");
            }

            List<SysConfig> privacyConfigs = sysConfigMapper.selectList(
                    new LambdaQueryWrapper<SysConfig>()
                            .eq(SysConfig::getParentId, parentConfig.getConfigId())
                            .eq(SysConfig::getStatus, 1)
            );

            if (privacyConfigs == null || privacyConfigs.isEmpty()) {
                log.error("未找到可用的隐私配置项");
                throw new RuntimeException("隐私配置项不存在");
            }

            log.info("查询到隐私配置项数: {}", privacyConfigs.size());

            // 5. 批量初始化用户隐私设置
            int successCount = 0;
            int failedCount = 0;
            List<Long> failedUserIds = new ArrayList<>();

            for (WxUser user : usersNeedInit) {
                try {
                    // 为每个用户创建隐私设置
                    for (SysConfig config : privacyConfigs) {
                        UserPrivacySetting setting = new UserPrivacySetting();
                        setting.setWxId(user.getWxId());
                        setting.setFieldName(config.getConfigName());
                        setting.setFieldCode(config.getConfigKey());
                        setting.setType(1); // 1-用户配置
                        setting.setVisibility(0); // 默认可见
                        setting.setSearchable(0); // 默认可搜索

                        userPrivacySettingMapper.insert(setting);
                    }

                    successCount++;
                    log.debug("用户隐私设置初始化成功 - wxId: {}", user.getWxId());

                } catch (Exception e) {
                    failedCount++;
                    failedUserIds.add(user.getWxId());
                    log.error("用户隐私设置初始化失败 - wxId: {}, error: {}",
                            user.getWxId(), e.getMessage(), e);
                }

                // 每处理100个用户打印一次进度
                if ((successCount + failedCount) % 100 == 0) {
                    log.info("批量初始化进度: {}/{}, 成功: {}, 失败: {}",
                            successCount + failedCount, needInitUsers, successCount, failedCount);
                }
            }

            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

            log.info("批量初始化用户隐私设置完成 - 总用户数: {}, 需要初始化: {}, 成功: {}, 失败: {}, 耗时: {}ms",
                    totalUsers, needInitUsers, successCount, failedCount, durationMs);

            return BatchInitResultVo.builder()
                    .totalUsers(totalUsers)
                    .needInitUsers(needInitUsers)
                    .successCount(successCount)
                    .failedCount(failedCount)
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationMs(durationMs)
                    .failedUserIds(failedUserIds)
                    .build();

        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

            log.error("批量初始化用户隐私设置异常", e);

            return BatchInitResultVo.builder()
                    .totalUsers(0)
                    .needInitUsers(0)
                    .successCount(0)
                    .failedCount(0)
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationMs(durationMs)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
