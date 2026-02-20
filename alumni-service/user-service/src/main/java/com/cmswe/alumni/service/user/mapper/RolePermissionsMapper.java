package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 */
@Mapper
public interface RolePermissionsMapper extends BaseMapper<RolePermissions> {

    /**
     * 批量插入角色权限关联
     */
    int insertBatch(@Param("rolePermissionsList") List<RolePermissions> rolePermissionsList);

    /**
     * 根据角色ID删除权限关联
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID删除角色关联
     */
    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}