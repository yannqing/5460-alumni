package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.api.user.PermissionService;
import com.cmswe.alumni.api.user.RoleUserService;
import com.cmswe.alumni.common.dto.CreateRoleDto;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.*;
import com.cmswe.alumni.service.user.mapper.RoleMapper;
import com.cmswe.alumni.api.user.RoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * @author yanqing
 * @description 角色相关业务操作实现
 */
@Slf4j
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Resource
    private RoleUserService roleUserService;

    @Resource
    private PermissionService permissionService;

    @Lazy
    @Resource
    private LocalPlatformService localPlatformService;

    @Lazy
    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Lazy
    @Resource
    private MerchantService merchantService;

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        try {
            return baseMapper.selectRolesByUserId(userId);
        } catch (Exception e) {
            log.error("根据用户ID获取角色列表失败，userId: {}", userId, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public Role getByRoleName(String roleName) {
        try {
            return baseMapper.selectByRoleName(roleName);
        } catch (Exception e) {
            log.error("根据角色名称获取角色失败，roleName: {}", roleName, e);
            return null;
        }
    }

    @Override
    public Role getByRoleUuid(String roleUuid) {
        try {
            return baseMapper.selectByRoleUuid(roleUuid);
        } catch (Exception e) {
            log.error("根据角色UUID获取角色失败，roleUuid: {}", roleUuid, e);
            return null;
        }
    }

    @Override
    public boolean hasRole(Long userId, String roleName) {
        try {
            List<Role> roles = getRolesByUserId(userId);
            return roles.stream()
                    .anyMatch(role -> roleName.equals(role.getRoleName()));
        } catch (Exception e) {
            log.error("检查用户角色失败，userId: {}, roleName: {}", userId, roleName, e);
            return false;
        }
    }

    @Override
    public List<String> getUserRoleNames(Long userId) {
        try {
            List<Role> roles = getRolesByUserId(userId);
            return roles.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户角色名称列表失败，userId: {}", userId, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public boolean createRole(CreateRoleDto createRoleDto) {
        if (createRoleDto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        Role existRole = this.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, createRoleDto.getRoleCode()));
        if (existRole != null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        Role role = CreateRoleDto.dtoToObj(createRoleDto);

        boolean save = this.save(role);
        log.info("新增角色: {}", role);

        return save;
    }

    @Override
    public Role getRoleByCodeInner(String roleCode) {
        if (roleCode == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        Role role = this.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, roleCode));
        if (role == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        return role;
    }

    @Override
    public List<RoleListVo> getRoleListVoByWxId(Long wxId) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        // 获取用户的所有角色关联（不仅限于系统角色）
        List<RoleUser> roleUserList = roleUserService.getSystemRoleUserListByWxIdInner(wxId);

        return roleUserList.stream()
                .map(roleUser -> {
                    // 查询角色信息
                    Role role = this.getBaseMapper().selectOne(
                            new LambdaQueryWrapper<Role>()
                                    .eq(Role::getRoleId, roleUser.getRoleId())
                                    .eq(Role::getStatus, 1)  // 只查询启用状态的角色
                    );

                    if (role != null) {
                        RoleListVo roleListVo = RoleListVo.objToVo(role);
                        // 处理 JS 精度问题
                        roleListVo.setRoleId(String.valueOf(role.getRoleId()));

                        // 查询并封装用户组织信息（每个 roleUser 对应一个组织）
                        UserOrganizationVo organization = getOrganizationByRoleUser(roleUser);
                        roleListVo.setOrganization(organization);

                        // 查询角色对应的权限树（包含父级菜单权限）
                        try {
                            List<PermissionsVo> permissions = permissionService.getPermissionTreeByRoleId(role.getRoleId());
                            roleListVo.setPermissions(permissions);
                            log.debug("角色权限查询成功 - roleId: {}, 权限数量: {}", role.getRoleId(), permissions != null ? permissions.size() : 0);
                        } catch (Exception e) {
                            log.error("查询角色权限失败 - roleId: {}, Error: {}", role.getRoleId(), e.getMessage(), e);
                            // 即使权限查询失败，也返回角色信息，只是权限列表为空
                        }

                        return roleListVo;
                    }
                    return null;
                })
                .filter(vo -> vo != null)  // 过滤掉 null 值
                .collect(Collectors.toList());
    }

    /**
     * 根据 RoleUser 查询用户所属组织信息
     *
     * @param roleUser 角色用户关联
     * @return 组织信息（如果 type 或 organizeId 为空则返回 null）
     */
    private UserOrganizationVo getOrganizationByRoleUser(RoleUser roleUser) {
        // 如果 type 或 organizeId 为空，返回 null
        if (roleUser.getType() == null || roleUser.getOrganizeId() == null) {
            return null;
        }

        Integer type = roleUser.getType();
        Long organizeId = roleUser.getOrganizeId();

        try {
            UserOrganizationVo organizationVo = new UserOrganizationVo();
            organizationVo.setType(type);
            organizationVo.setOrganizeId(String.valueOf(organizeId));

            // 根据 type 查询不同的组织信息
            switch (type) {
                case 1: // 校处会
                    LocalPlatform localPlatform = localPlatformService.getById(organizeId);
                    if (localPlatform != null) {
                        LocalPlatformListVo localPlatformVo = LocalPlatformListVo.objToVo(localPlatform);
                        // 处理 JS 精度问题
                        if (localPlatformVo != null) {
                            localPlatformVo.setPlatformId(String.valueOf(localPlatform.getPlatformId()));
                        }
                        organizationVo.setOrganizationInfo(localPlatformVo);
                        return organizationVo;
                    }
                    break;

                case 2: // 校友会
                    AlumniAssociation alumniAssociation = alumniAssociationService.getById(organizeId);
                    if (alumniAssociation != null) {
                        AlumniAssociationListVo alumniAssociationVo = AlumniAssociationListVo.objToVo(alumniAssociation);
                        organizationVo.setOrganizationInfo(alumniAssociationVo);
                        return organizationVo;
                    }
                    break;

                case 3: // 商户
                    Merchant merchant = merchantService.getById(organizeId);
                    if (merchant != null) {
                        MerchantListVo merchantVo = MerchantListVo.objToVo(merchant);
                        // 处理 JS 精度问题
                        if (merchantVo != null) {
                            merchantVo.setMerchantId(String.valueOf(merchant.getMerchantId()));
                            merchantVo.setUserId(String.valueOf(merchant.getUserId()));
                            if (merchant.getAlumniAssociationId() != null) {
                                merchantVo.setAlumniAssociationId(String.valueOf(merchant.getAlumniAssociationId()));
                            }
                        }
                        organizationVo.setOrganizationInfo(merchantVo);
                        return organizationVo;
                    }
                    break;

                default:
                    log.warn("未知的组织类型: type={}, organizeId={}", type, organizeId);
                    break;
            }
        } catch (Exception e) {
            log.error("查询组织信息失败: type={}, organizeId={}", type, organizeId, e);
        }

        return null;
    }
}