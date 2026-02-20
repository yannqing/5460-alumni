package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询权限列表
     */
    List<Permission> selectPermissionsByUserId(Long userId);

    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> selectPermissionsByRoleId(Long roleId);

    /**
     * 根据权限编码查询权限
     */
    Permission selectByCode(String code);
}