package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.RoleUser;

import java.util.List;

/**
 * 用户角色关联服务接口
 * @author yanqing
 * @description 用户角色关联相关业务操作接口定义，可用于 Feign 调用
 */
public interface RoleUserService extends IService<RoleUser> {
    
    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否分配成功
     */
    boolean assignRoleToUser(Long userId, Long roleId);

    /**
     * 移除用户的指定角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否移除成功
     */
    boolean removeRoleFromUser(Long userId, Long roleId);

    /**
     * 移除用户的所有角色
     * @param userId 用户ID
     * @return 是否移除成功
     */
    boolean removeAllRolesByUserId(Long userId);

    /**
     * 根据用户的 wxId 来获取对应的关系列表
     * @param wxId 用户 wxId
     * @return 返回关系列表
     */
    List<RoleUser> getSystemRoleUserListByWxIdInner(Long wxId);

}