package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.RolePermissions;

import java.util.List;

/**
 * 角色权限关联服务接口
 * @author yanqing
 * @description 角色权限关联相关业务操作接口定义，可用于 Feign 调用
 */
public interface RolePermissionsService extends IService<RolePermissions> {

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否分配成功
     */
    boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 移除角色的所有权限
     * @param roleId 角色ID
     * @return 是否移除成功
     */
    boolean removeAllPermissionsByRoleId(Long roleId);

    /**
     * 移除指定权限的所有角色关联
     * @param permissionId 权限ID
     * @return 是否移除成功
     */
    boolean removeAllRolesByPermissionId(Long permissionId);

    /**
     * 批量插入角色权限关联
     * @param rolePermissionsList 角色权限关联列表
     * @return 是否插入成功
     */
    boolean batchInsertRolePermissions(List<RolePermissions> rolePermissionsList);
}