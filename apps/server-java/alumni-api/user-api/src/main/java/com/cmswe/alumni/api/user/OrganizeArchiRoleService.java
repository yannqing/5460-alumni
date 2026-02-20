package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.AddOrganizeArchiRoleDto;
import com.cmswe.alumni.common.dto.UpdateOrganizeArchiRoleDto;
import com.cmswe.alumni.common.entity.OrganizeArchiRole;
import com.cmswe.alumni.common.vo.OrganizeArchiRoleVo;

import java.util.List;

/**
 * 组织架构角色服务接口
 * @author yanqing
 * @description 组织架构角色相关业务操作接口定义，可用于 Feign 调用
 */
public interface OrganizeArchiRoleService extends IService<OrganizeArchiRole> {

    /**
     * 新增组织架构角色
     *
     * @param addDto 新增请求参数
     * @return 是否成功
     */
    boolean addOrganizeArchiRole(AddOrganizeArchiRoleDto addDto);

    /**
     * 更新组织架构角色
     *
     * @param updateDto 更新请求参数
     * @return 是否成功
     */
    boolean updateOrganizeArchiRole(UpdateOrganizeArchiRoleDto updateDto);

    /**
     * 删除组织架构角色
     *
     * @param roleOrId 角色ID
     * @param organizeId 组织ID
     * @return 是否成功
     */
    boolean deleteOrganizeArchiRole(Long roleOrId, Long organizeId);

    /**
     * 查询某组织的所有角色列表
     *
     * @param organizeId 组织ID
     * @param organizeType 组织类型
     * @param roleOrName 角色名（模糊查询）
     * @param status 状态
     * @return 角色列表
     */
    List<OrganizeArchiRole> getOrganizeArchiRoleList(Long organizeId, Integer organizeType, String roleOrName, Integer status);

    /**
     * 查询某组织的角色树形结构
     *
     * @param organizeId 组织ID
     * @param organizeType 组织类型
     * @param roleOrName 角色名（模糊查询）
     * @param status 状态
     * @return 角色树形结构
     */
    List<OrganizeArchiRoleVo> getOrganizeArchiRoleTree(Long organizeId, Integer organizeType, String roleOrName, Integer status);
}
