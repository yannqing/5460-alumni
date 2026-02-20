package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.UserBlacklistService;
import com.cmswe.alumni.common.dto.BlacklistDto;
import com.cmswe.alumni.common.entity.UserBlacklist;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.JwtUtils;
import com.cmswe.alumni.service.user.mapper.UserBlacklistMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户黑名单服务实现类
 */
@Slf4j
@Service
public class UserBlacklistServiceImpl extends ServiceImpl<UserBlacklistMapper, UserBlacklist>
        implements UserBlacklistService {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean blockUser(Long wxId, BlacklistDto blacklistDto) {
        // 检查是否已拉黑
        LambdaQueryWrapper<UserBlacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBlacklist::getWxId, wxId)
                .eq(UserBlacklist::getBlockedWxId, blacklistDto.getBlockedWxId());

        UserBlacklist existingBlacklist = this.getOne(queryWrapper);

        if (existingBlacklist != null) {
            // 如果已经拉黑，更新拉黑信息
            existingBlacklist.setBlockType(blacklistDto.getBlockType() != null ? blacklistDto.getBlockType() : 1);
            existingBlacklist.setBlockReason(blacklistDto.getBlockReason());
            existingBlacklist.setUpdatedTime(LocalDateTime.now());
            existingBlacklist.setIsDeleted(0);
            return this.updateById(existingBlacklist);
        }

        // 创建新的拉黑记录
        UserBlacklist userBlacklist = new UserBlacklist();
        userBlacklist.setWxId(wxId);
        userBlacklist.setBlockedWxId(blacklistDto.getBlockedWxId());
        userBlacklist.setBlockType(blacklistDto.getBlockType() != null ? blacklistDto.getBlockType() : 1);
        userBlacklist.setBlockReason(blacklistDto.getBlockReason());
        userBlacklist.setCreatedTime(LocalDateTime.now());
        userBlacklist.setUpdatedTime(LocalDateTime.now());
        userBlacklist.setIsDeleted(0);

        return this.save(userBlacklist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unblockUser(Long wxId, Long blockedWxId) {
        LambdaQueryWrapper<UserBlacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBlacklist::getWxId, wxId)
                .eq(UserBlacklist::getBlockedWxId, blockedWxId);

        UserBlacklist userBlacklist = this.getOne(queryWrapper);

        if (userBlacklist == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "未找到拉黑记录");
        }

        return this.removeById(userBlacklist.getBlackId());
    }

    @Override
    public boolean isBlocked(Long wxId, Long blockedWxId) {
        LambdaQueryWrapper<UserBlacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBlacklist::getWxId, wxId)
                .eq(UserBlacklist::getBlockedWxId, blockedWxId);

        return this.count(queryWrapper) > 0;
    }
}
