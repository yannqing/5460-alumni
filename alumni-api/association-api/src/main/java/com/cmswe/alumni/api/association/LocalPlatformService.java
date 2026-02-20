package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.AddLocalPlatformDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationByPlatformDto;
import com.cmswe.alumni.common.dto.QueryLocalPlatformListDto;
import com.cmswe.alumni.common.dto.QueryLocalPlatformMemberListDto;
import com.cmswe.alumni.common.entity.LocalPlatform;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;
import com.cmswe.alumni.common.vo.LocalPlatformDetailVo;
import com.cmswe.alumni.common.vo.LocalPlatformListVo;
import com.cmswe.alumni.common.vo.OrganizationTreeVo;
import com.cmswe.alumni.common.vo.OrganizationTreeV2Vo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.OrganizationMemberResponse;
import com.cmswe.alumni.common.vo.UserListResponse;

import java.util.List;

public interface LocalPlatformService extends IService<LocalPlatform> {
    /**
     * 根据用户ID查询校处会详情
     *
     * @param id 用户ID
     * @return 详情列表
     */
    LocalPlatformDetailVo getLocalPlatformById(Long id);

    boolean insertLocalPlatform(AddLocalPlatformDto addLocalPlatformDto);

    /**
     * 分页查询校处会列表
     *
     * @param queryLocalPlatformListDto 查询条件
     * @return 分页结果
     */
    PageVo<LocalPlatformListVo> selectByPage(QueryLocalPlatformListDto queryLocalPlatformListDto);

    /**
     * 获取校处会组织架构树
     *
     * @param localPlatformId 校处会ID
     * @return 组织架构树列表
     */
    List<OrganizationTreeVo> getOrganizationTree(Long localPlatformId);

    /**
     * 获取校处会组织架构树 V2版本（基于username，支持wxId为空的情况）
     *
     * @param localPlatformId 校处会ID
     * @return 组织架构树列表
     */
    List<OrganizationTreeV2Vo> getOrganizationTreeV2(Long localPlatformId);

    /**
     * 删除校处会成员
     *
     * @param localPlatformId 校处会ID
     * @param wxId            成员用户ID
     * @return 删除结果
     */
    boolean deleteMember(Long localPlatformId, Long wxId);

    /**
     * 邀请成员加入校处会
     *
     * @param localPlatformId 校处会ID
     * @param wxId            成员用户ID
     * @param roleOrId        组织架构角色ID
     * @return 邀请结果
     */
    boolean inviteMember(Long localPlatformId, Long wxId, Long roleOrId);

    /**
     * 根据校处会ID分页查询校友会列表
     *
     * @param queryDto 查询条件
     * @param currentUserId 当前登录用户ID（可为null）
     * @return 分页结果
     */
    PageVo<AlumniAssociationListVo> getAlumniAssociationsByPlatformId(QueryAlumniAssociationByPlatformDto queryDto, Long currentUserId);

    /**
     * 根据校处会ID分页查询成员列表
     *
     * @param queryDto 查询条件
     * @return 分页结果
     */
    Page<OrganizationMemberResponse> getLocalPlatformMemberPage(QueryLocalPlatformMemberListDto queryDto);

    /**
     * 更新校处会成员的组织架构角色
     *
     * @param operatorWxId    操作人（管理员）的用户ID
     * @param localPlatformId 校处会ID
     * @param wxId            成员用户ID
     * @param roleOrId        新的组织架构角色ID
     * @return 更新是否成功
     */
    boolean updateMemberRole(Long operatorWxId, Long localPlatformId, Long wxId, Long roleOrId);

    /**
     * 更新校处会成员的组织架构角色 V2版本（基于username，支持wxId为空的情况）
     *
     * @param operatorWxId    操作人（管理员）的用户ID
     * @param localPlatformId 校处会ID
     * @param id              成员主键ID（新增时为空）
     * @param username        成员用户名
     * @param roleOrId        新的组织架构角色ID
     * @param roleName        角色名称
     * @return 更新是否成功
     */
    boolean updateMemberRoleV2(Long operatorWxId, Long localPlatformId, Long id, String username, Long roleOrId, String roleName);

    /**
     * 根据校处会ID查询校处会管理员列表
     *
     * @param localPlatformId 校处会ID
     * @return 管理员用户列表
     */
    List<UserListResponse> getAdminsByLocalPlatformId(Long localPlatformId);

    /**
     * 为校处会添加管理员
     *
     * @param localPlatformId 校处会ID
     * @param wxId 用户ID（被添加为管理员的用户）
     * @return 添加是否成功
     */
    boolean addAdminToLocalPlatform(Long localPlatformId, Long wxId);

    /**
     * 移除校处会管理员
     *
     * @param localPlatformId 校处会ID
     * @param wxId 用户ID（被移除管理员权限的用户）
     * @return 移除是否成功
     */
    boolean removeAdminFromLocalPlatform(Long localPlatformId, Long wxId);

}