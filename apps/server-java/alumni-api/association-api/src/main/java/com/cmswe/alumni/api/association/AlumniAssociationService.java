package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.AddAlumniAssociationDto;
import com.cmswe.alumni.common.dto.PublishActivityDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationListDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationMemberListRequest;
import com.cmswe.alumni.common.dto.QueryAlumniListDto;
import com.cmswe.alumni.common.dto.UpdateActivityDto;
import com.cmswe.alumni.common.dto.UpdateAlumniAssociationDto;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.vo.ActivityDetailVo;
import com.cmswe.alumni.common.vo.ActivityListVo;
import com.cmswe.alumni.common.vo.AlumniAssociationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;
import com.cmswe.alumni.common.vo.OrganizationTreeVo;
import com.cmswe.alumni.common.vo.OrganizationTreeV2Vo;
import com.cmswe.alumni.common.vo.ManagedOrganizationVo;
import com.cmswe.alumni.common.vo.OrganizationMemberResponse;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.UserListResponse;

import java.util.List;

public interface AlumniAssociationService extends IService<AlumniAssociation> {

    /**
     * 获取校友会列表
     *
     * @param alumniAssociationListDto 校友会列表查询DTO
     * @param currentUserId 当前用户ID（可为null，表示未登录）
     * @return 返回结果
     */
    PageVo<AlumniAssociationListVo> selectByPage(QueryAlumniAssociationListDto alumniAssociationListDto, Long currentUserId);

    /**
     * 根据id获取校友会详情
     * 
     * @param id   校友会id
     * @param wxId 当前用户wxId（可为null，表示未登录）
     * @return 返回结果
     */
    AlumniAssociationDetailVo getAlumniAssociationDetailVoById(Long id, Long wxId);

    /**
     * 新增校友会（测试使用）
     *
     * @param addAlumniAssociationDto 新增校友会请求
     * @return 返回新增结果
     */
    boolean insertAlumniAssociation(AddAlumniAssociationDto addAlumniAssociationDto);

    /**
     * 更新校友会信息
     *
     * @param updateAlumniAssociationDto 更新校友会请求
     * @return 返回更新结果
     */
    boolean updateAlumniAssociation(UpdateAlumniAssociationDto updateAlumniAssociationDto);

    /**
     * 查询校友会成员列表
     *
     * @param queryAlumniAssociationMemberListRequest 查询请求
     * @param currentUserId 当前登录用户ID（可为null）
     * @return 返回校友成员列表
     */
    Page<OrganizationMemberResponse> getAlumniAssociationMemberPage(
            QueryAlumniAssociationMemberListRequest queryAlumniAssociationMemberListRequest,
            Long currentUserId);

    /**
     * 查询本人是会长的校友会列表（如果是超级管理员则返回所有）
     *
     * @param queryDto 查询参数
     * @param wxId     当前用户ID
     * @return 返回校友会列表
     */
    PageVo<AlumniAssociationListVo> getMyPresidentAssociationPage(QueryAlumniAssociationListDto queryDto, Long wxId);

    /**
     * 删除校友会成员
     *
     * @param alumniAssociationId 校友会ID
     * @param id                  成员记录ID（可选，用于删除未注册成员）
     * @param wxId                成员用户ID（可选，用于删除已注册成员）
     * @return 删除是否成功
     */
    boolean deleteMember(Long alumniAssociationId, Long id, Long wxId);

    /**
     * 邀请校友加入校友会
     *
     * @param alumniAssociationId 校友会ID
     * @param wxId                校友用户ID
     * @param roleOrId            组织架构角色ID
     * @return 邀请是否成功
     */
    boolean inviteMember(Long alumniAssociationId, Long wxId, Long roleOrId);

    /**
     * 获取校友会组织架构树
     *
     * @param alumniAssociationId 校友会ID
     * @return 组织架构树列表
     */
    List<OrganizationTreeVo> getOrganizationTree(Long alumniAssociationId);

    /**
     * 获取校友会组织架构树 V2（基于username）
     *
     * @param alumniAssociationId 校友会ID
     * @return 组织架构树 V2
     */
    List<OrganizationTreeV2Vo> getOrganizationTreeV2(Long alumniAssociationId);

    /**
     * 更新校友会成员的组织架构角色
     *
     * @param operatorWxId        操作人（管理员）的用户ID
     * @param alumniAssociationId 校友会ID
     * @param wxId                成员用户ID
     * @param roleOrId            新的组织架构角色ID
     * @param roleName            角色名称
     * @return 更新是否成功
     */
    boolean updateMemberRole(Long operatorWxId, Long alumniAssociationId, Long wxId, Long roleOrId, String roleName);

    /**
     * 更新校友会成员的组织架构角色 V2版本（基于username）
     *
     * @param operatorWxId        操作人（管理员）的用户ID
     * @param alumniAssociationId 校友会ID
     * @param id                  成员主键ID（新增时为空）
     * @param username            成员用户名
     * @param roleOrId            新的组织架构角色ID
     * @param roleName            角色名称
     * @return 更新是否成功
     */
    boolean updateMemberRoleV2(Long operatorWxId, Long alumniAssociationId, Long id, String username, Long roleOrId, String roleName);

    /**
     * 根据校友会ID获取活动列表
     *
     * @param alumniAssociationId 校友会ID
     * @return 活动列表
     */
    List<ActivityListVo> getActivitiesByAssociationId(Long alumniAssociationId);

    /**
     * 根据活动ID获取活动详情
     *
     * @param activityId 活动ID
     * @return 活动详情
     */
    ActivityDetailVo getActivityDetail(Long activityId);

    /**
     * 为校友会发布活动
     *
     * @param createdBy 创建人ID
     * @param publishDto 发布活动请求参数
     * @return 发布是否成功
     */
    boolean publishActivity(Long createdBy, PublishActivityDto publishDto);

    /**
     * 编辑活动
     *
     * @param updateDto 编辑活动请求参数
     * @return 编辑是否成功
     */
    boolean updateActivity(UpdateActivityDto updateDto);

    /**
     * 查询用户加入的校友会列表
     *
     * @param wxId 用户ID
     * @return 用户加入的校友会列表
     */
    List<AlumniAssociationListVo> getMyJoinedAssociations(Long wxId);

    /**
     * 根据校友会ID查询校友会管理员列表
     *
     * @param alumniAssociationId 校友会ID
     * @return 管理员用户列表
     */
    List<UserListResponse> getAdminsByAssociationId(Long alumniAssociationId);

    /**
     * 为校友会添加管理员
     *
     * @param alumniAssociationId 校友会ID
     * @param wxId 用户ID（被添加为管理员的用户）
     * @return 添加是否成功
     */
    boolean addAdminToAssociation(Long alumniAssociationId, Long wxId);

    /**
     * 移除校友会管理员
     *
     * @param alumniAssociationId 校友会ID
     * @param wxId 用户ID（被移除管理员权限的用户）
     * @return 移除是否成功
     */
    boolean removeAdminFromAssociation(Long alumniAssociationId, Long wxId);

    /**
     * 绑定校友会组织架构成员与系统用户
     *
     * @param memberId 校友会成员表ID
     * @param wxId 用户微信ID
     * @return 绑定是否成功
     */
    boolean bindMemberToUser(Long memberId, Long wxId);

    /**
     * 根据用户权限获取管理的校友会列表
     *
     * @param wxId 当前用户ID
     * @return 管理的校友会列表
     */
    List<ManagedOrganizationVo> getManagedAssociations(Long wxId);

    /**
     * 更新校友会成员信息
     *
     * @param id 成员ID
     * @param username 用户名
     * @param roleName 角色名称
     * @param userPhone 用户联系电话
     * @param userAffiliation 用户社会职务
     * @param isShowOnHome 是否展示在主页（0-否，1-是）
     * @return 更新是否成功
     */
    boolean updateMemberInfo(Long id, String username, String roleName, String userPhone, String userAffiliation, Integer isShowOnHome);

    /**
     * 添加校友会成员到分支（组织架构角色）
     *
     * @param alumniAssociationId 校友会ID
     * @param wxId 成员用户ID
     * @param roleOrId 分支（组织架构角色）ID
     * @return 添加是否成功
     */
    boolean addMemberToBranch(Long alumniAssociationId, Long wxId, Long roleOrId);

    /**
     * 从分支（组织架构角色）移除校友会成员
     *
     * @param alumniAssociationId 校友会ID
     * @param wxId 成员用户ID
     * @return 移除是否成功
     */
    boolean removeMemberFromBranch(Long alumniAssociationId, Long wxId);

    /**
     * 添加未注册成员到校友会
     *
     * @param alumniAssociationId 校友会ID
     * @param username 用户名字
     * @param roleName 角色名称
     * @param userPhone 用户的联系电话
     * @param userAffiliation 用户的社会职务
     * @return 添加是否成功
     */
    boolean addUnregisteredMember(Long alumniAssociationId, String username, String roleName, String userPhone, String userAffiliation);

    /**
     * 根据手机号和姓名绑定校友会预设成员（假人转真人）
     * 查找 wx_id 为空且 user_phone、username 分别与手机号、姓名匹配的预设成员，为其绑定 wxId
     *
     * @param phone 用户手机号
     * @param name  用户姓名（可为空，为空时仅按手机号匹配）
     * @param wxId  用户ID
     * @return 成功绑定的成员数量
     */
    int bindPresetMembersByPhone(String phone, String name, Long wxId);
}