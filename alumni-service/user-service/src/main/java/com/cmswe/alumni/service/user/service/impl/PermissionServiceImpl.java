package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.vo.PermissionsVo;
import com.cmswe.alumni.service.user.mapper.PermissionMapper;
import com.cmswe.alumni.api.user.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * @author yanqing
 * @description 权限相关业务操作实现
 */
@Slf4j
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        try {
            return baseMapper.selectPermissionsByUserId(userId);
        } catch (Exception e) {
            log.error("根据用户ID获取权限列表失败，userId: {}", userId, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        try {
            return baseMapper.selectPermissionsByRoleId(roleId);
        } catch (Exception e) {
            log.error("根据角色ID获取权限列表失败，roleId: {}", roleId, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public Permission getByCode(String code) {
        try {
            return baseMapper.selectByCode(code);
        } catch (Exception e) {
            log.error("根据权限编码获取权限失败，code: {}", code, e);
            return null;
        }
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        try {
            List<Permission> permissions = getPermissionsByUserId(userId);
            return permissions.stream()
                    .anyMatch(permission -> permissionCode.equals(permission.getCode()));
        } catch (Exception e) {
            log.error("检查用户权限失败，userId: {}, permissionCode: {}", userId, permissionCode, e);
            return false;
        }
    }

    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        try {
            List<Permission> permissions = getPermissionsByUserId(userId);
            return permissions.stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户权限编码列表失败，userId: {}", userId, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<PermissionsVo> getPermissionTreeByRoleId(Long roleId) {
        try {
            log.debug("开始查询角色权限树 - roleId: {}", roleId);

            // 1. 查询角色拥有的权限列表
            List<Permission> rolePermissions = getPermissionsByRoleId(roleId);
            if (rolePermissions == null || rolePermissions.isEmpty()) {
                log.debug("角色没有任何权限 - roleId: {}", roleId);
                return new ArrayList<>();
            }

            log.debug("角色拥有的权限数量 - roleId: {}, count: {}", roleId, rolePermissions.size());

            // 2. 收集所有权限ID
            Set<Long> permissionIds = rolePermissions.stream()
                    .map(Permission::getPerId)
                    .collect(Collectors.toSet());

            // 3. 查找需要补充的父级菜单权限
            Set<Long> allPermissionIds = new HashSet<>(permissionIds);
            for (Permission permission : rolePermissions) {
                if (permission.getPid() != null && permission.getPid() != 0) {
                    // 递归查找所有父级权限
                    addParentPermissions(permission.getPid(), allPermissionIds);
                }
            }

            log.debug("补充父级权限后的权限ID总数 - roleId: {}, count: {}", roleId, allPermissionIds.size());

            // 4. 查询所有需要的权限（包括补充的父级权限）
            List<Permission> allPermissions = baseMapper.selectList(
                    new LambdaQueryWrapper<Permission>()
                            .in(Permission::getPerId, allPermissionIds)
                            .eq(Permission::getStatus, 1) // 只查询启用的权限
                            .orderByAsc(Permission::getSortOrder)
            );

            // 5. 转换为VO并构建树形结构
            List<PermissionsVo> permissionsTree = buildPermissionTree(allPermissions);

            log.debug("权限树构建完成 - roleId: {}, 根节点数: {}", roleId, permissionsTree.size());
            return permissionsTree;

        } catch (Exception e) {
            log.error("查询角色权限树失败 - roleId: {}, Error: {}", roleId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 递归添加父级权限ID
     *
     * @param pid            父权限ID
     * @param permissionIds  权限ID集合
     */
    private void addParentPermissions(Long pid, Set<Long> permissionIds) {
        if (pid == null || pid == 0) {
            return;
        }

        // 如果已经包含，避免重复查询
        if (permissionIds.contains(pid)) {
            return;
        }

        // 添加父权限ID
        permissionIds.add(pid);

        // 查询父权限
        Permission parentPermission = baseMapper.selectById(pid);
        if (parentPermission != null && parentPermission.getPid() != null && parentPermission.getPid() != 0) {
            // 递归查找上级父权限
            addParentPermissions(parentPermission.getPid(), permissionIds);
        }
    }

    /**
     * 构建权限树形结构
     *
     * @param permissions 权限列表
     * @return 树形结构的权限列表
     */
    private List<PermissionsVo> buildPermissionTree(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 转换为VO
        List<PermissionsVo> permissionVos = permissions.stream()
                .map(PermissionsVo::objToVo)
                .collect(Collectors.toList());

        // 2. 创建ID到VO的映射
        Map<Long, PermissionsVo> permissionMap = permissionVos.stream()
                .collect(Collectors.toMap(PermissionsVo::getPerId, vo -> vo));

        // 3. 构建树形结构
        List<PermissionsVo> rootPermissions = new ArrayList<>();

        for (PermissionsVo vo : permissionVos) {
            Long pid = vo.getPid();

            if (pid == null || pid == 0) {
                // 顶级权限（pid为0或null）
                rootPermissions.add(vo);
            } else {
                // 子权限，添加到父权限的children列表
                PermissionsVo parent = permissionMap.get(pid);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                } else {
                    // 如果找不到父权限，当作顶级权限处理
                    rootPermissions.add(vo);
                }
            }
        }

        // 4. 对每个节点的子权限进行排序
        sortPermissions(rootPermissions);

        return rootPermissions;
    }

    /**
     * 递归对权限树进行排序
     *
     * @param permissions 权限列表
     */
    private void sortPermissions(List<PermissionsVo> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }

        // 根据sortOrder排序
        permissions.sort(Comparator.comparing(
                vo -> vo.getSortOrder() != null ? vo.getSortOrder() : Integer.MAX_VALUE));

        // 递归排序子权限
        for (PermissionsVo permission : permissions) {
            if (permission.getChildren() != null && !permission.getChildren().isEmpty()) {
                sortPermissions(permission.getChildren());
            }
        }
    }
}