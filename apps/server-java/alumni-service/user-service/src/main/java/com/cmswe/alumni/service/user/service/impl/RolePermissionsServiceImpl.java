package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.service.user.mapper.RolePermissionsMapper;
import com.cmswe.alumni.api.user.RolePermissionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色权限关联服务实现类
 * @author yanqing
 * @description 角色权限关联相关业务操作实现
 */
@Slf4j
@Service
public class RolePermissionsServiceImpl extends ServiceImpl<RolePermissionsMapper, RolePermissions> implements RolePermissionsService {

    @Override
    @Transactional
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        try {
            // 先移除角色原有的权限
            removeAllPermissionsByRoleId(roleId);
            
            // 创建新的权限关联
            List<RolePermissions> rolePermissionsList = permissionIds.stream()
                    .map(permissionId -> {
                        RolePermissions rolePermissions = new RolePermissions();
                        rolePermissions.setRoleId(roleId);
                        rolePermissions.setPerId(permissionId);
                        rolePermissions.setCreateTime(LocalDateTime.now());
                        rolePermissions.setUpdateTime(LocalDateTime.now());
                        rolePermissions.setIsDelete(0);
                        return rolePermissions;
                    })
                    .collect(Collectors.toList());
            
            boolean result = batchInsertRolePermissions(rolePermissionsList);
            log.info("为角色分配权限{}，roleId: {}, permissionIds: {}", result ? "成功" : "失败", roleId, permissionIds);
            return result;
        } catch (Exception e) {
            log.error("为角色分配权限失败，roleId: {}, permissionIds: {}", roleId, permissionIds, e);
            return false;
        }
    }

    @Override
    public boolean removeAllPermissionsByRoleId(Long roleId) {
        try {
            boolean result = baseMapper.deleteByRoleId(roleId) >= 0;
            log.info("移除角色权限{}，roleId: {}", result ? "成功" : "失败", roleId);
            return result;
        } catch (Exception e) {
            log.error("移除角色权限失败，roleId: {}", roleId, e);
            return false;
        }
    }

    @Override
    public boolean removeAllRolesByPermissionId(Long permissionId) {
        try {
            boolean result = baseMapper.deleteByPermissionId(permissionId) >= 0;
            log.info("移除权限关联角色{}，permissionId: {}", result ? "成功" : "失败", permissionId);
            return result;
        } catch (Exception e) {
            log.error("移除权限关联角色失败，permissionId: {}", permissionId, e);
            return false;
        }
    }

    @Override
    public boolean batchInsertRolePermissions(List<RolePermissions> rolePermissionsList) {
        try {
            if (rolePermissionsList == null || rolePermissionsList.isEmpty()) {
                return true;
            }
            boolean result = baseMapper.insertBatch(rolePermissionsList) > 0;
            log.info("批量插入角色权限关联{}，数量: {}", result ? "成功" : "失败", rolePermissionsList.size());
            return result;
        } catch (Exception e) {
            log.error("批量插入角色权限关联失败", e);
            return false;
        }
    }
}