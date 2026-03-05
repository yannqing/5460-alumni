package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.association.LocalPlatformMemberService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.common.dto.AddOrganizeArchiRoleDto;
import com.cmswe.alumni.common.dto.UpdateOrganizeArchiRoleDto;
import com.cmswe.alumni.common.entity.AlumniAssociationMember;
import com.cmswe.alumni.common.entity.LocalPlatformMember;
import com.cmswe.alumni.common.entity.OrganizeArchiRole;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.OrganizeArchiRoleVo;
import com.cmswe.alumni.common.vo.OrganizationMemberV2Vo;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.service.user.mapper.OrganizeArchiRoleMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 组织架构角色服务实现类
 */
@Slf4j
@Service
public class OrganizeArchiRoleServiceImpl extends ServiceImpl<OrganizeArchiRoleMapper, OrganizeArchiRole>
        implements OrganizeArchiRoleService {

    @Resource
    private OrganizeArchiRoleMapper organizeArchiRoleMapper;

    @Resource
    private AlumniAssociationMemberService alumniAssociationMemberService;

    @Resource
    private LocalPlatformMemberService localPlatformMemberService;

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addOrganizeArchiRole(AddOrganizeArchiRoleDto addDto) {
        // 1. 参数校验
        Optional.ofNullable(addDto).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        log.info("开始新增组织架构角色 - 组织ID: {}, 角色名: {}",
                addDto.getOrganizeId(), addDto.getRoleOrName());

        // 2. 自动生成角色代码
        // 格式: ORG_{组织类型}_{组织ID}_{时间戳}
        String roleCode = "ORG_" + addDto.getOrganizeType() + "_" + addDto.getOrganizeId() + "_" + System.currentTimeMillis();

        // 3. 检查生成的角色代码是否已存在（同一组织下不能有重复的角色代码）
        OrganizeArchiRole existingRole = this.getOne(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getOrganizeId, addDto.getOrganizeId())
                        .eq(OrganizeArchiRole::getRoleOrCode, roleCode)
        );

        if (existingRole != null) {
            // 如果重复，添加一个随机数
            roleCode = roleCode + "_" + (int)(Math.random() * 1000);
        }

        // 4. 构建实体对象
        OrganizeArchiRole role = new OrganizeArchiRole();
        BeanUtils.copyProperties(addDto, role);
        // 设置自动生成的角色代码
        role.setRoleOrCode(roleCode);

        // 设置父ID，如果为null则设置为0（根节点）
        if (role.getPid() == null) {
            role.setPid(0L);
        }

        // 设置默认状态为启用
        role.setStatus(1);

        // 5. 保存到数据库
        boolean result = this.save(role);
        if (!result) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "新增组织架构角色失败");
        }

        log.info("新增组织架构角色成功 - 组织ID: {}, 角色ID: {}, 角色名: {}, 角色代码: {}",
                addDto.getOrganizeId(), role.getRoleOrId(), addDto.getRoleOrName(), role.getRoleOrCode());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrganizeArchiRole(UpdateOrganizeArchiRoleDto updateDto) {
        // 1. 参数校验
        Optional.ofNullable(updateDto).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        log.info("开始更新组织架构角色 - 角色ID: {}, 组织ID: {}", updateDto.getRoleOrId(), updateDto.getOrganizeId());

        // 2. 查询角色是否存在
        OrganizeArchiRole existingRole = this.getById(updateDto.getRoleOrId());
        if (existingRole == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "组织架构角色不存在");
        }

        // 3. 校验角色是否属于该组织
        if (!existingRole.getOrganizeId().equals(updateDto.getOrganizeId())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权修改该组织架构角色");
        }

        // 4. 检查状态变更：从启用(1)变为禁用(0)时，需要检查是否有成员
        if (updateDto.getStatus() != null &&
            existingRole.getStatus() != null &&
            existingRole.getStatus() == 1 &&
            updateDto.getStatus() == 0) {

            log.info("检测到角色状态从启用变为禁用 - 角色ID: {}", updateDto.getRoleOrId());

            // 检查该角色下是否有成员
            long memberCount = alumniAssociationMemberService.count(
                    new LambdaQueryWrapper<AlumniAssociationMember>()
                            .eq(AlumniAssociationMember::getRoleOrId, updateDto.getRoleOrId())
                            .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
            );

            if (memberCount > 0) {
                throw new BusinessException(ErrorType.OPERATION_ERROR,
                        String.format("该角色下还有 %d 个成员，无法禁用", memberCount));
            }

            log.info("该角色下无成员，允许禁用 - 角色ID: {}", updateDto.getRoleOrId());
        }

        // 5. 更新角色信息
        OrganizeArchiRole role = new OrganizeArchiRole();
        BeanUtils.copyProperties(updateDto, role);
        
        // 保留原始角色代码，不允许更新
        role.setRoleOrCode(existingRole.getRoleOrCode());

        boolean result = this.updateById(role);
        if (!result) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新组织架构角色失败");
        }

        log.info("更新组织架构角色成功 - 角色ID: {}, 组织ID: {}, 角色名: {}",
                updateDto.getRoleOrId(), updateDto.getOrganizeId(), updateDto.getRoleOrName());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrganizeArchiRole(Long roleOrId, Long organizeId) {
        // 1. 参数校验
        if (roleOrId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "角色ID不能为空");
        }
        if (organizeId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "组织ID不能为空");
        }

        log.info("开始删除组织架构角色 - 角色ID: {}, 组织ID: {}", roleOrId, organizeId);

        // 2. 查询角色是否存在
        OrganizeArchiRole existingRole = this.getById(roleOrId);
        if (existingRole == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "组织架构角色不存在");
        }

        // 3. 校验角色是否属于该组织
        if (!existingRole.getOrganizeId().equals(organizeId)) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权删除该组织架构角色");
        }

        // 4. 检查是否有子角色
        long childCount = this.count(new LambdaQueryWrapper<OrganizeArchiRole>()
                .eq(OrganizeArchiRole::getPid, roleOrId));
        if (childCount > 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该角色下存在子角色，无法删除");
        }

        // 5. 检查是否有成员正在使用该角色
        long memberCount = alumniAssociationMemberService.count(new LambdaQueryWrapper<AlumniAssociationMember>()
                .eq(AlumniAssociationMember::getRoleOrId, roleOrId)
                .eq(AlumniAssociationMember::getStatus, 1)); // 状态：1-正常
        if (memberCount > 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR,
                    String.format("该角色下还有 %d 个成员，无法删除", memberCount));
        }

        // 6. 删除角色（逻辑删除）
        boolean result = this.removeById(roleOrId);
        if (!result) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "删除组织架构角色失败");
        }

        log.info("删除组织架构角色成功 - 角色ID: {}, 组织ID: {}", roleOrId, organizeId);

        return true;
    }

    @Override
    public List<OrganizeArchiRole> getOrganizeArchiRoleList(Long organizeId, Integer organizeType,
                                                             String roleOrName, Integer status) {
        // 1. 参数校验
        if (organizeId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "组织ID不能为空");
        }

        log.info("查询组织架构角色列表 - 组织ID: {}, 组织类型: {}, 角色名: {}, 状态: {}",
                organizeId, organizeType, roleOrName, status);

        // 2. 构建查询条件
        LambdaQueryWrapper<OrganizeArchiRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(OrganizeArchiRole::getOrganizeId, organizeId)
                .eq(organizeType != null, OrganizeArchiRole::getOrganizeType, organizeType)
                .like(StringUtils.isNotBlank(roleOrName), OrganizeArchiRole::getRoleOrName, roleOrName)
                .eq(status != null, OrganizeArchiRole::getStatus, status)
                .orderByAsc(OrganizeArchiRole::getRoleOrId);

        // 3. 查询列表
        List<OrganizeArchiRole> roleList = this.list(queryWrapper);

        log.info("查询组织架构角色列表成功 - 组织ID: {}, 结果数: {}", organizeId, roleList.size());

        return roleList;
    }

    @Override
    public List<OrganizeArchiRoleVo> getOrganizeArchiRoleTree(Long organizeId, Integer organizeType, String roleOrName, Integer status) {
        // 1. 参数校验
        if (organizeId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "组织ID不能为空");
        }

        log.info("查询组织架构角色树 - 组织ID: {}, 组织类型: {}, 角色名: {}, 状态: {}",
                organizeId, organizeType, roleOrName, status);

        // 2. 查询所有角色（使用已有的方法）
        List<OrganizeArchiRole> roleList = getOrganizeArchiRoleList(organizeId, organizeType, roleOrName, status);

        if (roleList.isEmpty()) {
            log.info("查询组织架构角色树结果为空 - 组织ID: {}", organizeId);
            return new ArrayList<>();
        }

        // 3. 转换为 VO 对象并构建 Map（key: roleOrId, value: VO）
        Map<Long, OrganizeArchiRoleVo> roleMap = roleList.stream()
                .map(OrganizeArchiRoleVo::objToVo)
                .collect(Collectors.toMap(
                        vo -> Long.valueOf(vo.getRoleOrId()),
                        vo -> vo,
                        (v1, v2) -> v1
                ));

        // 4. 查询成员信息并按角色分组
        Map<Long, List<OrganizationMemberV2Vo>> membersByRole = new HashMap<>();
        if (organizeType != null) {
            // 收集所有角色ID
            List<Long> roleIds = roleList.stream()
                    .map(OrganizeArchiRole::getRoleOrId)
                    .collect(Collectors.toList());

            if (organizeType == 1) { // 1-校处会：查询该校处会下所有正常状态成员（含未分配角色的）
                List<LocalPlatformMember> members = new ArrayList<>();
                if (!roleIds.isEmpty()) {
                    members.addAll(localPlatformMemberService.list(
                            new LambdaQueryWrapper<LocalPlatformMember>()
                                    .eq(LocalPlatformMember::getLocalPlatformId, organizeId)
                                    .in(LocalPlatformMember::getRoleOrId, roleIds)
                                    .eq(LocalPlatformMember::getStatus, 1) // 状态：1-正常
                    ));
                }
                members.addAll(localPlatformMemberService.list(
                        new LambdaQueryWrapper<LocalPlatformMember>()
                                .eq(LocalPlatformMember::getLocalPlatformId, organizeId)
                                .isNull(LocalPlatformMember::getRoleOrId)
                                .eq(LocalPlatformMember::getStatus, 1) // 状态：1-正常
                ));

                // 构建用户信息映射
                Map<Long, WxUserInfo> userInfoMap;
                if (!members.isEmpty()) {
                    List<Long> wxIds = members.stream()
                            .map(LocalPlatformMember::getWxId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    if (!wxIds.isEmpty()) {
                        List<WxUserInfo> userInfoList = wxUserInfoService.list(
                                new LambdaQueryWrapper<WxUserInfo>()
                                        .in(WxUserInfo::getWxId, wxIds)
                        );
                        userInfoMap = userInfoList.stream()
                                .collect(Collectors.toMap(WxUserInfo::getWxId, Function.identity(), (v1, v2) -> v1));
                    } else {
                        userInfoMap = new HashMap<>();
                    }
                } else {
                    userInfoMap = new HashMap<>();
                }
                // 创建一个最终变量供lambda表达式使用
                final Map<Long, WxUserInfo> finalUserInfoMap = userInfoMap;

                // 按角色ID分组成员（roleOrId 为 null 的归入 -1 表示未分配）
                Map<Long, List<LocalPlatformMember>> membersByRoleId = members.stream()
                        .collect(Collectors.groupingBy(m -> m.getRoleOrId() != null ? m.getRoleOrId() : -1L));

                // 转换为 OrganizationMemberV2Vo（校处会成员含联系方式、社会职务）
                for (Map.Entry<Long, List<LocalPlatformMember>> entry : membersByRoleId.entrySet()) {
                    Long roleId = entry.getKey();
                    List<LocalPlatformMember> roleMembers = entry.getValue();

                    List<OrganizationMemberV2Vo> memberVos = roleMembers.stream()
                            .map(member -> {
                                OrganizationMemberV2Vo.OrganizationMemberV2VoBuilder builder = OrganizationMemberV2Vo.builder()
                                        .id(member.getId())
                                        .username(member.getUsername())
                                        .wxId(member.getWxId())
                                        .roleName(member.getRoleName())
                                        .joinTime(member.getJoinTime())
                                        .contactInformation(organizeType == 1 ? member.getContactInformation() : null)
                                        .socialDuties(organizeType == 1 ? member.getSocialDuties() : null);

                                // 如果wxId不为空且能找到用户信息，则填充用户详细信息
                                if (member.getWxId() != null) {
                                    WxUserInfo userInfo = finalUserInfoMap.get(member.getWxId());
                                    if (userInfo != null) {
                                        builder.nickname(userInfo.getNickname())
                                                .name(userInfo.getName())
                                                .avatarUrl(userInfo.getAvatarUrl())
                                                .gender(userInfo.getGender());
                                    }
                                }

                                return builder.build();
                            })
                            .collect(Collectors.toList());

                    membersByRole.put(roleId, memberVos);
                }
            }
        }

        // 5. 为每个角色添加成员信息
        for (Map.Entry<Long, OrganizeArchiRoleVo> entry : roleMap.entrySet()) {
            Long roleId = entry.getKey();
            OrganizeArchiRoleVo roleVo = entry.getValue();
            roleVo.setMembers(membersByRole.getOrDefault(roleId, new ArrayList<>()));
        }

        // 5.1 若有未分配角色的成员，添加虚拟「未分配角色」节点
        List<OrganizationMemberV2Vo> unassignedMembers = membersByRole.getOrDefault(-1L, new ArrayList<>());
        if (!unassignedMembers.isEmpty() && organizeType != null && organizeType == 1) {
            OrganizeArchiRoleVo unassignedNode = OrganizeArchiRoleVo.builder()
                    .roleOrId("-1")
                    .pid("0")
                    .organizeType(organizeType)
                    .organizeId(String.valueOf(organizeId))
                    .roleOrName("未分配角色")
                    .status(1)
                    .children(new ArrayList<>())
                    .members(unassignedMembers)
                    .build();
            roleMap.put(-1L, unassignedNode);
        }

        // 6. 构建树形结构
        List<OrganizeArchiRoleVo> rootNodes = new ArrayList<>();
        for (OrganizeArchiRoleVo vo : roleMap.values()) {
            String pidStr = vo.getPid();
            if (pidStr == null || pidStr.equals("0") || pidStr.isEmpty()) {
                // 根节点（pid 为 null 或 0）
                rootNodes.add(vo);
            } else {
                // 子节点，添加到父节点的 children 中
                Long pid = Long.valueOf(pidStr);
                OrganizeArchiRoleVo parentVo = roleMap.get(pid);
                if (parentVo != null) {
                    parentVo.getChildren().add(vo);
                } else {
                    // 如果找不到父节点，当作根节点处理
                    rootNodes.add(vo);
                }
            }
        }

        log.info("查询组织架构角色树成功 - 组织ID: {}, 根节点数: {}, 总角色数: {}",
                organizeId, rootNodes.size(), roleMap.size());

        return rootNodes;
    }

}
