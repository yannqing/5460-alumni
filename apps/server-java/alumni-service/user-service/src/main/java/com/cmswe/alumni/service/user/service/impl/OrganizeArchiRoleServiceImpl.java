package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.common.dto.AddOrganizeArchiRoleDto;
import com.cmswe.alumni.common.dto.UpdateOrganizeArchiRoleDto;
import com.cmswe.alumni.common.entity.AlumniAssociationMember;
import com.cmswe.alumni.common.entity.OrganizeArchiRole;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.OrganizeArchiRoleVo;
import com.cmswe.alumni.service.user.mapper.OrganizeArchiRoleMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addOrganizeArchiRole(AddOrganizeArchiRoleDto addDto) {
        // 1. 参数校验
        Optional.ofNullable(addDto).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        log.info("开始新增组织架构角色 - 组织ID: {}, 角色名: {}, 角色代码: {}",
                addDto.getOrganizeId(), addDto.getRoleOrName(), addDto.getRoleOrCode());

        // 2. 检查角色代码是否已存在（同一组织下不能有重复的角色代码）
        OrganizeArchiRole existingRole = this.getOne(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getOrganizeId, addDto.getOrganizeId())
                        .eq(OrganizeArchiRole::getRoleOrCode, addDto.getRoleOrCode())
        );

        if (existingRole != null) {
            log.warn("角色代码重复 - 组织ID: {}, 角色代码: {}", addDto.getOrganizeId(), addDto.getRoleOrCode());
            throw new BusinessException(ErrorType.OPERATION_ERROR, "角色代码重复，该组织下已存在相同的角色代码");
        }

        // 3. 构建实体对象
        OrganizeArchiRole role = new OrganizeArchiRole();
        BeanUtils.copyProperties(addDto, role);

        // 设置父ID，如果为null则设置为0（根节点）
        if (role.getPid() == null) {
            role.setPid(0L);
        }

        // 设置默认状态为启用
        role.setStatus(1);

        // 4. 保存到数据库
        boolean result = this.save(role);
        if (!result) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "新增组织架构角色失败");
        }

        log.info("新增组织架构角色成功 - 组织ID: {}, 角色ID: {}, 角色名: {}, 角色代码: {}",
                addDto.getOrganizeId(), role.getRoleOrId(), addDto.getRoleOrName(), addDto.getRoleOrCode());

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
    public List<OrganizeArchiRoleVo> getOrganizeArchiRoleTree(Long organizeId, Integer organizeType,
                                                                String roleOrName, Integer status) {
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

        // 4. 构建树形结构
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
