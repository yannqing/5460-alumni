package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.service.user.mapper.RoleUserMapper;
import com.cmswe.alumni.api.user.RoleUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户角色关联服务实现类
 * @author yanqing
 * @description 用户角色关联相关业务操作实现
 */
@Slf4j
@Service
public class RoleUserServiceImpl extends ServiceImpl<RoleUserMapper, RoleUser> implements RoleUserService {

    @Override
    public boolean assignRoleToUser(Long userId, Long roleId) {
        try {
            RoleUser roleUser = new RoleUser();
            roleUser.setWxId(userId);
            roleUser.setRoleId(roleId);
            roleUser.setCreateTime(LocalDateTime.now());
            roleUser.setUpdateTime(LocalDateTime.now());
            roleUser.setIsDelete(0);
            
            boolean result = this.save(roleUser);
            log.info("为用户分配角色{}，userId: {}, roleId: {}", result ? "成功" : "失败", userId, roleId);
            return result;
        } catch (Exception e) {
            log.error("为用户分配角色失败，userId: {}, roleId: {}", userId, roleId, e);
            return false;
        }
    }

    @Override
    public boolean removeRoleFromUser(Long userId, Long roleId) {
        try {
            boolean result = this.lambdaUpdate()
                    .eq(RoleUser::getWxId, userId)
                    .eq(RoleUser::getRoleId, roleId)
                    .remove();
            log.info("移除用户角色{}，userId: {}, roleId: {}", result ? "成功" : "失败", userId, roleId);
            return result;
        } catch (Exception e) {
            log.error("移除用户角色失败，userId: {}, roleId: {}", userId, roleId, e);
            return false;
        }
    }

    @Override
    public boolean removeAllRolesByUserId(Long userId) {
        try {
            boolean result = this.lambdaUpdate()
                    .eq(RoleUser::getWxId, userId)
                    .remove();
            log.info("移除用户所有角色{}，userId: {}", result ? "成功" : "失败", userId);
            return result;
        } catch (Exception e) {
            log.error("移除用户所有角色失败，userId: {}", userId, e);
            return false;
        }
    }

    @Override
    public List<RoleUser> getSystemRoleUserListByWxIdInner(Long wxId) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        List<RoleUser> roleUsers = this.getBaseMapper().selectList(new LambdaQueryWrapper<RoleUser>().eq(RoleUser::getWxId, wxId));

        if (CollectionUtils.isEmpty(roleUsers)) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        return roleUsers;
    }
}