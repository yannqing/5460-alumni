package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.CreateRoleDto;
import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.vo.RoleListVo;

import java.util.List;

/**
 * 角色服务接口
 * @author yanqing
 * @description 角色相关业务操作接口定义，可用于 Feign 调用
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getRolesByUserId(Long userId);

    /**
     * 根据角色名称查询角色
     * @param roleName 角色名称
     * @return 角色信息
     */
    Role getByRoleName(String roleName);

    /**
     * 根据角色UUID查询角色
     * @param roleUuid 角色UUID
     * @return 角色信息
     */
    Role getByRoleUuid(String roleUuid);

    /**
     * 检查用户是否具有指定角色
     * @param userId 用户ID
     * @param roleName 角色名称
     * @return 是否具有该角色
     */
    boolean hasRole(Long userId, String roleName);

    /**
     * 获取用户所有角色名称
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getUserRoleNames(Long userId);

    /**
     * 创建角色
     * @param createRoleDto 创建角色请求
     * @return 返回创建结果
     */
    boolean createRole(CreateRoleDto createRoleDto);

    /**
     * 根据 roleCode 获取 role 对象
     * @param roleCode 查询参数
     * @return 返回 role 对象
     */
    Role getRoleByCodeInner(String roleCode);

    /**
     * 根据用户的 wxId 来获取角色列表（内部方法）
     * @param wxId 用户的 wxId
     * @return 返回角色列表 Vo
     */
    List<RoleListVo> getRoleListVoByWxId(Long wxId);


}