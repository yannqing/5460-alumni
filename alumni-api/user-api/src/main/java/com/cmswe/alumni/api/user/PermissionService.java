package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.Permission;
import com.cmswe.alumni.common.vo.PermissionsVo;

import java.util.List;

/**
 * 权限服务接口
 * @author yanqing
 * @description 权限相关业务操作接口定义，可用于 Feign 调用
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 根据用户ID查询权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);

    /**
     * 根据角色ID查询权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);

    /**
     * 根据权限编码查询权限
     * @param code 权限编码
     * @return 权限信息
     */
    Permission getByCode(String code);

    /**
     * 检查用户是否具有指定权限
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否具有该权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 获取用户所有权限编码
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> getUserPermissionCodes(Long userId);

    /**
     * 根据角色ID查询权限树（包含父级菜单权限）
     *
     * <p>功能说明：
     * <ul>
     *   <li>查询角色拥有的所有权限</li>
     *   <li>如果用户只有子权限（如 id=2, pid=1），会自动补充父级菜单权限（id=1, pid=0）</li>
     *   <li>返回树形结构，父权限包含子权限列表</li>
     * </ul>
     *
     * @param roleId 角色ID
     * @return 权限树（树形结构的权限列表）
     */
    List<PermissionsVo> getPermissionTreeByRoleId(Long roleId);
}