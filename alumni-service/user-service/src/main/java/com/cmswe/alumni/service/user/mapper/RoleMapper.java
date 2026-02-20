package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询角色列表
     */
    List<Role> selectRolesByUserId(Long userId);

    /**
     * 根据角色名称查询角色
     */
    Role selectByRoleName(String roleName);

    /**
     * 根据角色UUID查询角色
     */
    Role selectByRoleUuid(String roleUuid);

    /**
     * 根据角色代码查询角色
     */
    Role selectByRoleCode(String roleCode);
}




