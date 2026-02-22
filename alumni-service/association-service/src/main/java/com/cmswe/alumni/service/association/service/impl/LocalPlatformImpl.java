package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.association.LocalPlatformMemberService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.user.RoleUserService;
import com.cmswe.alumni.api.user.UserFollowService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.constant.CommonConstant;
import com.cmswe.alumni.common.dto.AddLocalPlatformDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationByPlatformDto;
import com.cmswe.alumni.common.dto.QueryLocalPlatformListDto;
import com.cmswe.alumni.common.dto.QueryLocalPlatformMemberListDto;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.AlumniAssociationMember;
import com.cmswe.alumni.common.entity.LocalPlatform;

import com.cmswe.alumni.common.entity.LocalPlatformMember;
import com.cmswe.alumni.common.entity.OrganizeArchiRole;
import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.entity.RoleUser;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.entity.UserFollow;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;
import com.cmswe.alumni.common.vo.LocalPlatformDetailVo;
import com.cmswe.alumni.common.vo.LocalPlatformListVo;
import com.cmswe.alumni.common.vo.OrganizationMemberVo;
import com.cmswe.alumni.common.vo.OrganizationMemberV2Vo;
import com.cmswe.alumni.common.vo.OrganizationTreeVo;
import com.cmswe.alumni.common.vo.OrganizationTreeV2Vo;
import com.cmswe.alumni.common.vo.OrganizeArchiRoleVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.OrganizationMemberResponse;
import com.cmswe.alumni.common.vo.SchoolListVo;
import com.cmswe.alumni.common.vo.WxUserInfoVo;
import com.cmswe.alumni.common.vo.UserListResponse;
import com.cmswe.alumni.service.association.mapper.LocalPlatformMapper;
import com.cmswe.alumni.service.association.mapper.SchoolMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocalPlatformImpl extends ServiceImpl<LocalPlatformMapper, LocalPlatform> implements LocalPlatformService {
    @Resource
    private LocalPlatformMapper localPlatformMapper;

    @Resource
    private UserService userService;

    @Resource
    private LocalPlatformMemberService localPlatformMemberService;

    @Resource
    private RoleService roleService;

    @Resource
    private RoleUserService roleUserService;

    @Lazy
    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Lazy
    @Resource
    private AlumniAssociationMemberService alumniAssociationMemberService;

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Resource
    private OrganizeArchiRoleService organizeArchiRoleService;

    @Resource
    private UserFollowService userFollowService;

    @Resource
    private SchoolMapper schoolMapper;

    @Override
    public LocalPlatformDetailVo getLocalPlatformById(Long id) {
        // 1.校验id
        if (id == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2.查询数据库
        LocalPlatform localPlatform = localPlatformMapper.selectById(id);

        // 3.返回值校验
        if (localPlatform == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LocalPlatformDetailVo localPlatformDetailVo = LocalPlatformDetailVo.objToVo(localPlatform);
        localPlatformDetailVo.setPlatformId(String.valueOf(id));

        // 4. 构建其他详情
        // 4.1 统计会员数量
        long memberCount = localPlatformMemberService.count(
                new LambdaQueryWrapper<LocalPlatformMember>()
                        .eq(LocalPlatformMember::getLocalPlatformId, id)
                        .eq(LocalPlatformMember::getStatus, 1) // 状态：1-正常
        );
        localPlatformDetailVo.setMemberCount((int) memberCount);

        // 4.2 统计关联的校友会数量
        long associationCount = alumniAssociationService.count(
                new LambdaQueryWrapper<AlumniAssociation>()
                        .eq(AlumniAssociation::getPlatformId, id)
                        .eq(AlumniAssociation::getStatus, 1) // 状态：1-启用
        );
        localPlatformDetailVo.setAssociationCount((int) associationCount);

        // 4.3 查询会长信息（假设 roleId = 2002944405488537602L 是会长角色）
        final Long PRESIDENT_ROLE_ID = 2002944405488537602L; // 校处会会长角色ID（需要根据实际情况调整）
        LocalPlatformMember presidentMember = localPlatformMemberService.getOne(
                new LambdaQueryWrapper<LocalPlatformMember>()
                        .eq(LocalPlatformMember::getLocalPlatformId, id)
                        .eq(LocalPlatformMember::getRoleOrId, PRESIDENT_ROLE_ID)
                        .eq(LocalPlatformMember::getStatus, 1)
                        .last("LIMIT 1"));

        if (presidentMember != null) {
            WxUserInfo presidentUserInfo = wxUserInfoService.getById(presidentMember.getWxId());
            if (presidentUserInfo != null) {
                WxUserInfoVo presidentInfoVo = WxUserInfoVo.objToVo(presidentUserInfo);
                presidentInfoVo.setWxId(String.valueOf(presidentUserInfo.getWxId()));
                localPlatformDetailVo.setPresidentInfo(presidentInfoVo);
            }
        }

        log.info("根据id获取校处会详情id:{}", id);

        return localPlatformDetailVo;
    }

    @Override
    public boolean insertLocalPlatform(AddLocalPlatformDto addLocalPlatformDto) {
        Optional.ofNullable(addLocalPlatformDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Long adminUserId = addLocalPlatformDto.getWxId();
        if (!userService.exists(new LambdaQueryWrapper<WxUser>().eq(WxUser::getWxId, adminUserId))) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        LocalPlatform localPlatform = AddLocalPlatformDto.dtoToObj(addLocalPlatformDto);

        boolean saveResult = this.save(localPlatform);

        // 新增会长
        localPlatformMemberService.insertLocalPlatformMember(addLocalPlatformDto.getWxId(),
                localPlatform.getPlatformId());

        log.info("新增校处会：{}", addLocalPlatformDto.getPlatformName());

        return saveResult;
    }

    /**
     * 分页查询校处会列表
     *
     * @param queryLocalPlatformListDto 查询条件
     * @return 分页结果
     */
    @Override
    public PageVo<LocalPlatformListVo> selectByPage(QueryLocalPlatformListDto queryLocalPlatformListDto) {
        // 1.参数校验
        Optional.ofNullable(queryLocalPlatformListDto)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        // 2.获取参数
        String platformName = queryLocalPlatformListDto.getPlatformName();
        String city = queryLocalPlatformListDto.getCity();
        String scope = queryLocalPlatformListDto.getScope();
        int current = queryLocalPlatformListDto.getCurrent();
        int pageSize = queryLocalPlatformListDto.getPageSize();
        String sortField = queryLocalPlatformListDto.getSortField();
        String sortOrder = queryLocalPlatformListDto.getSortOrder();

        // 3.构建查询条件（强制只查询启用状态的校处会）
        LambdaQueryWrapper<LocalPlatform> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                // 强制条件：只返回启用状态（status=1）的校处会
                .eq(LocalPlatform::getStatus, 1)
                // 其他可选查询条件
                .like(StringUtils.isNotBlank(platformName), LocalPlatform::getPlatformName, platformName)
                .like(StringUtils.isNotBlank(city), LocalPlatform::getCity, city)
                .like(StringUtils.isNotBlank(scope), LocalPlatform::getScope, scope);

        // 4.添加排序
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            if ("createTime".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, LocalPlatform::getCreateTime);
            } else {
                queryWrapper.orderByDesc(LocalPlatform::getCreateTime);
            }
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc(LocalPlatform::getCreateTime);
        }

        // 5.执行分页查询
        Page<LocalPlatform> localPlatformPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 6.转换为VO（处理Long类型精度丢失问题）
        List<LocalPlatformListVo> list = localPlatformPage.getRecords().stream().map(localPlatform -> {
            LocalPlatformListVo localPlatformListVo = LocalPlatformListVo.objToVo(localPlatform);
            // 将Long转换为String，避免前端精度丢失
            localPlatformListVo.setPlatformId(String.valueOf(localPlatform.getPlatformId()));
            return localPlatformListVo;
        }).toList();

        log.info("分页查询校处会列表，当前页：{}，每页数量：{}，总记录数：{}", current, pageSize, localPlatformPage.getTotal());

        // 7.转换结果并返回
        Page<LocalPlatformListVo> resultPage = new Page<LocalPlatformListVo>(current, pageSize,
                localPlatformPage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public List<OrganizationTreeVo> getOrganizationTree(Long localPlatformId) {
        // 1. 参数校验
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }

        log.info("开始查询组织架构树 - 校处会ID: {}", localPlatformId);

        // 2. 查询该校处会的所有组织架构角色
        List<OrganizeArchiRole> allRoles = organizeArchiRoleService.list(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getOrganizeId, localPlatformId)
                        .eq(OrganizeArchiRole::getOrganizeType, 1) // 1-校处会
                        .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
                        .orderByAsc(OrganizeArchiRole::getRoleOrId));

        if (allRoles.isEmpty()) {
            log.info("该校处会暂无组织架构 - 校处会ID: {}", localPlatformId);
            return new ArrayList<>();
        }

        // 3. 查询该校处会的所有成员
        List<LocalPlatformMember> allMembers = localPlatformMemberService.list(
                new LambdaQueryWrapper<LocalPlatformMember>()
                        .eq(LocalPlatformMember::getLocalPlatformId, localPlatformId)
                        .eq(LocalPlatformMember::getStatus, 1) // 1-正常
        );

        // 4. 提取所有成员的wxId并批量查询用户信息
        Map<Long, WxUserInfo> userInfoMap = new HashMap<>();
        if (!allMembers.isEmpty()) {
            List<Long> wxIds = allMembers.stream()
                    .map(LocalPlatformMember::getWxId)
                    .distinct()
                    .collect(Collectors.toList());

            List<WxUserInfo> userInfoList = wxUserInfoService.list(
                    new LambdaQueryWrapper<WxUserInfo>()
                            .in(WxUserInfo::getWxId, wxIds));
            userInfoMap = userInfoList.stream()
                    .collect(Collectors.toMap(WxUserInfo::getWxId, Function.identity(), (v1, v2) -> v1));
        }

        // 5. 按角色ID分组成员
        Map<Long, List<LocalPlatformMember>> membersByRole = allMembers.stream()
                .collect(Collectors.groupingBy(LocalPlatformMember::getRoleOrId));

        // 6. 构建角色树节点的Map（key: roleOrId, value: OrganizationTreeVo）
        Map<Long, OrganizationTreeVo> roleNodeMap = new HashMap<>();
        Map<Long, WxUserInfo> finalUserInfoMap = userInfoMap;

        for (OrganizeArchiRole role : allRoles) {
            OrganizationTreeVo treeNode = OrganizationTreeVo.builder()
                    .roleOrId(role.getRoleOrId())
                    .pid(role.getPid())
                    .roleOrName(role.getRoleOrName())
                    .roleOrCode(role.getRoleOrCode())
                    .remark(role.getRemark())
                    .children(new ArrayList<>())
                    .members(new ArrayList<>())
                    .build();

            // 为该角色添加成员信息
            List<LocalPlatformMember> roleMembers = membersByRole.getOrDefault(role.getRoleOrId(), new ArrayList<>());
            List<OrganizationMemberVo> memberVos = roleMembers.stream()
                    .map(member -> {
                        WxUserInfo userInfo = finalUserInfoMap.get(member.getWxId());
                        if (userInfo == null) {
                            return null;
                        }

                        return OrganizationMemberVo.builder()
                                .wxId(member.getWxId())
                                .nickname(userInfo.getNickname())
                                .name(userInfo.getName())
                                .avatarUrl(userInfo.getAvatarUrl())
                                .gender(userInfo.getGender())
                                .joinTime(member.getJoinTime())
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            treeNode.setMembers(memberVos);
            roleNodeMap.put(role.getRoleOrId(), treeNode);
        }

        // 7. 构建树形结构
        List<OrganizationTreeVo> rootNodes = new ArrayList<>();
        for (OrganizationTreeVo node : roleNodeMap.values()) {
            if (node.getPid() == null || node.getPid() == 0) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 子节点，添加到父节点的children中
                OrganizationTreeVo parentNode = roleNodeMap.get(node.getPid());
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                } else {
                    // 如果找不到父节点，当作根节点处理
                    rootNodes.add(node);
                }
            }
        }

        log.info("查询组织架构树成功 - 校处会ID: {}, 根节点数: {}, 总角色数: {}",
                localPlatformId, rootNodes.size(), roleNodeMap.size());

        return rootNodes;
    }

    @Override
    public List<OrganizationTreeV2Vo> getOrganizationTreeV2(Long localPlatformId) {
        // 1. 参数校验
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }

        log.info("开始查询组织架构树V2 - 校处会ID: {}", localPlatformId);

        // 2. 查询该校处会的所有组织架构角色
        List<OrganizeArchiRole> allRoles = organizeArchiRoleService.list(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getOrganizeId, localPlatformId)
                        .eq(OrganizeArchiRole::getOrganizeType, 1) // 1-校处会
                        .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
                        .orderByAsc(OrganizeArchiRole::getRoleOrId));

        if (allRoles.isEmpty()) {
            log.info("该校处会暂无组织架构 - 校处会ID: {}", localPlatformId);
            return new ArrayList<>();
        }

        // 3. 查询该校处会的所有成员（仅查询架构成员 is_nu = 1）
        List<LocalPlatformMember> allMembers = localPlatformMemberService.list(
                new LambdaQueryWrapper<LocalPlatformMember>()
                        .eq(LocalPlatformMember::getLocalPlatformId, localPlatformId)
                        .eq(LocalPlatformMember::getIsNu, 1) // 仅架构成员
                        .eq(LocalPlatformMember::getStatus, 1) // 1-正常
        );

        // 4. 提取所有非空wxId的成员并批量查询用户信息
        Map<Long, WxUserInfo> userInfoMap = new HashMap<>();
        if (!allMembers.isEmpty()) {
            List<Long> wxIds = allMembers.stream()
                    .map(LocalPlatformMember::getWxId)
                    .filter(Objects::nonNull) // 过滤掉null的wxId
                    .distinct()
                    .collect(Collectors.toList());

            if (!wxIds.isEmpty()) {
                List<WxUserInfo> userInfoList = wxUserInfoService.list(
                        new LambdaQueryWrapper<WxUserInfo>()
                                .in(WxUserInfo::getWxId, wxIds));
                userInfoMap = userInfoList.stream()
                        .collect(Collectors.toMap(WxUserInfo::getWxId, Function.identity(), (v1, v2) -> v1));
            }
        }

        // 5. 按角色ID分组成员
        Map<Long, List<LocalPlatformMember>> membersByRole = allMembers.stream()
                .collect(Collectors.groupingBy(LocalPlatformMember::getRoleOrId));

        // 6. 构建角色树节点的Map（key: roleOrId, value: OrganizationTreeV2Vo）
        Map<Long, OrganizationTreeV2Vo> roleNodeMap = new HashMap<>();
        Map<Long, WxUserInfo> finalUserInfoMap = userInfoMap;

        for (OrganizeArchiRole role : allRoles) {
            OrganizationTreeV2Vo treeNode = OrganizationTreeV2Vo.builder()
                    .roleOrId(role.getRoleOrId())
                    .pid(role.getPid())
                    .roleOrName(role.getRoleOrName())
                    .roleOrCode(role.getRoleOrCode())
                    .remark(role.getRemark())
                    .children(new ArrayList<>())
                    .members(new ArrayList<>())
                    .build();

            // 为该角色添加成员信息
            List<LocalPlatformMember> roleMembers = membersByRole.getOrDefault(role.getRoleOrId(), new ArrayList<>());
            List<OrganizationMemberV2Vo> memberVos = roleMembers.stream()
                    .map(member -> {
                        // V2版本：优先使用username，wxId可能为空
                        OrganizationMemberV2Vo.OrganizationMemberV2VoBuilder builder = OrganizationMemberV2Vo.builder()
                                .id(member.getId())
                                .username(member.getUsername())
                                .wxId(member.getWxId())
                                .roleName(member.getRoleName())
                                .joinTime(member.getJoinTime());

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

            treeNode.setMembers(memberVos);
            roleNodeMap.put(role.getRoleOrId(), treeNode);
        }

        // 7. 构建树形结构
        List<OrganizationTreeV2Vo> rootNodes = new ArrayList<>();
        for (OrganizationTreeV2Vo node : roleNodeMap.values()) {
            if (node.getPid() == null || node.getPid() == 0) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 子节点，添加到父节点的children中
                OrganizationTreeV2Vo parentNode = roleNodeMap.get(node.getPid());
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                } else {
                    // 如果找不到父节点，当作根节点处理
                    rootNodes.add(node);
                }
            }
        }

        log.info("查询组织架构树V2成功 - 校处会ID: {}, 根节点数: {}, 总角色数: {}",
                localPlatformId, rootNodes.size(), roleNodeMap.size());

        return rootNodes;
    }

    @Override
    public boolean deleteMember(Long localPlatformId, Long wxId) {
        // 1. 参数校验
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员用户ID不能为空");
        }

        log.info("开始删除校处会成员 - 校处会ID: {}, 成员用户ID: {}", localPlatformId, wxId);

        // 2. 查询校处会是否存在
        LocalPlatform localPlatform = this.getById(localPlatformId);
        if (localPlatform == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校处会不存在");
        }

        // 3. 查询成员记录是否存在
        LocalPlatformMember member = localPlatformMemberService.getOne(
                new LambdaQueryWrapper<LocalPlatformMember>()
                        .eq(LocalPlatformMember::getLocalPlatformId, localPlatformId)
                        .eq(LocalPlatformMember::getWxId, wxId)
                        .eq(LocalPlatformMember::getStatus, 1) // 状态：1-正常
        );

        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该用户不是该校处会的成员");
        }

        // 4. 删除成员记录（逻辑删除）
        boolean deleteResult = localPlatformMemberService.removeById(member.getId());

        if (deleteResult) {
            log.info("删除校处会成员成功 - 校处会ID: {}, 成员用户ID: {}", localPlatformId, wxId);
        } else {
            log.error("删除校处会成员失败 - 校处会ID: {}, 成员用户ID: {}", localPlatformId, wxId);
            throw new BusinessException(ErrorType.OPERATION_ERROR, "删除成员失败");
        }

        return deleteResult;
    }

    @Override
    public boolean inviteMember(Long localPlatformId, Long wxId, Long roleOrId) {
        // 1. 参数校验
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员用户ID不能为空");
        }
        if (roleOrId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "组织架构角色ID不能为空");
        }

        log.info("开始邀请成员加入校处会 - 校处会ID: {}, 成员用户ID: {}, 角色ID: {}",
                localPlatformId, wxId, roleOrId);

        // 2. 查询校处会是否存在
        LocalPlatform localPlatform = this.getById(localPlatformId);
        if (localPlatform == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校处会不存在");
        }

        // 3. 查询用户是否存在
        WxUser wxUser = userService.getById(wxId);
        if (wxUser == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "用户不存在");
        }

        // 4. 检查用户是否已经是该校处会成员
        LocalPlatformMember existingMember = localPlatformMemberService.getOne(
                new LambdaQueryWrapper<LocalPlatformMember>()
                        .eq(LocalPlatformMember::getLocalPlatformId, localPlatformId)
                        .eq(LocalPlatformMember::getWxId, wxId)
                        .eq(LocalPlatformMember::getStatus, 1) // 状态：1-正常
        );

        if (existingMember != null) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该用户已经是该校处会成员");
        }

        // 5. 创建新成员记录
        LocalPlatformMember newMember = new LocalPlatformMember();
        newMember.setWxId(wxId);
        newMember.setLocalPlatformId(localPlatformId);
        newMember.setRoleOrId(roleOrId);
        newMember.setJoinTime(java.time.LocalDateTime.now());
        newMember.setStatus(1); // 状态：1-正常

        boolean saveResult = localPlatformMemberService.save(newMember);
        if (!saveResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "邀请成员失败");
        }

        log.info("邀请成员加入校处会成功 - 校处会ID: {}, 成员用户ID: {}, 角色ID: {}",
                localPlatformId, wxId, roleOrId);

        return saveResult;
    }

    @Override
    public PageVo<AlumniAssociationListVo> getAlumniAssociationsByPlatformId(
            QueryAlumniAssociationByPlatformDto queryDto,
            Long currentUserId) {
        // 1. 参数校验
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        Long platformId = queryDto.getPlatformId();
        if (platformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }

        // 2. 校验校处会是否存在
        LocalPlatform localPlatform = this.getById(platformId);
        if (localPlatform == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校处会不存在");
        }

        // 3. 获取查询参数
        String associationName = queryDto.getAssociationName();
        String location = queryDto.getLocation();
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();
        String sortField = queryDto.getSortField();
        String sortOrder = queryDto.getSortOrder();

        // 4. 构建查询条件
        LambdaQueryWrapper<AlumniAssociation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                // 强制条件：根据校处会ID查询
                .eq(AlumniAssociation::getPlatformId, platformId)
                // 强制条件：只返回启用状态（status=1）的校友会
                .eq(AlumniAssociation::getStatus, 1)
                // 其他可选查询条件
                .like(StringUtils.isNotBlank(associationName), AlumniAssociation::getAssociationName, associationName)
                .like(StringUtils.isNotBlank(location), AlumniAssociation::getLocation, location);

        // 5. 添加排序
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            if ("memberCount".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, AlumniAssociation::getMemberCount);
            } else if ("createTime".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, AlumniAssociation::getCreateTime);
            } else {
                queryWrapper.orderByDesc(AlumniAssociation::getCreateTime);
            }
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc(AlumniAssociation::getCreateTime);
        }

        // 6. 执行分页查询
        Page<AlumniAssociation> alumniAssociationPage = alumniAssociationService.page(
                new Page<>(current, pageSize), queryWrapper);

        // 7. 批量查询学校信息
        Map<Long, SchoolListVo> schoolMap = new HashMap<>();
        if (!alumniAssociationPage.getRecords().isEmpty()) {
            List<Long> schoolIds = alumniAssociationPage.getRecords().stream()
                    .map(AlumniAssociation::getSchoolId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (!schoolIds.isEmpty()) {
                List<School> schools = schoolMapper.selectBatchIds(schoolIds);
                schoolMap = schools.stream()
                        .map(school -> {
                            SchoolListVo vo = SchoolListVo.objToVo(school);
                            vo.setSchoolId(String.valueOf(school.getSchoolId()));
                            return vo;
                        })
                        .collect(Collectors.toMap(
                                vo -> Long.valueOf(vo.getSchoolId()),
                                Function.identity(),
                                (v1, v2) -> v1));
            }
        }

        // 8. 如果当前用户已登录，批量查询用户与这些校友会的成员关系和关注关系
        Map<Long, Boolean> memberStatusMap = new HashMap<>();
        Map<Long, Boolean> followStatusMap = new HashMap<>();
        if (currentUserId != null && !alumniAssociationPage.getRecords().isEmpty()) {
            List<Long> associationIds = alumniAssociationPage.getRecords().stream()
                    .map(AlumniAssociation::getAlumniAssociationId)
                    .collect(Collectors.toList());

            // 批量查询成员关系
            List<AlumniAssociationMember> memberList = alumniAssociationMemberService.list(
                    new LambdaQueryWrapper<AlumniAssociationMember>()
                            .eq(AlumniAssociationMember::getWxId, currentUserId)
                            .in(AlumniAssociationMember::getAlumniAssociationId, associationIds)
                            .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
            );

            memberStatusMap = memberList.stream()
                    .collect(Collectors.toMap(
                            AlumniAssociationMember::getAlumniAssociationId,
                            member -> true,
                            (v1, v2) -> v1));

            // 批量查询关注关系
            List<UserFollow> followList = userFollowService.list(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getWxId, currentUserId)
                            .eq(UserFollow::getTargetType, 2) // 2-校友会
                            .in(UserFollow::getTargetId, associationIds)
                            .in(UserFollow::getFollowStatus, 1, 2, 3) // 正常关注、特别关注、免打扰（排除已取消）
            );

            followStatusMap = followList.stream()
                    .collect(Collectors.toMap(
                            UserFollow::getTargetId,
                            follow -> true,
                            (v1, v2) -> v1));
        }

        // 9. 转换为VO，使用 @JsonSerialize 注解避免前端精度丢失，并设置学校信息、加入状态和关注状态
        Map<Long, SchoolListVo> finalSchoolMap = schoolMap;
        Map<Long, Boolean> finalMemberStatusMap = memberStatusMap;
        Map<Long, Boolean> finalFollowStatusMap = followStatusMap;
        List<AlumniAssociationListVo> list = alumniAssociationPage.getRecords().stream()
                .map(association -> {
                    AlumniAssociationListVo vo = AlumniAssociationListVo.objToVo(association);
                    // 设置学校信息
                    if (association.getSchoolId() != null) {
                        vo.setSchool(finalSchoolMap.get(association.getSchoolId()));
                    }
                    // 设置加入状态和关注状态
                    if (currentUserId != null) {
                        vo.setIsMember(finalMemberStatusMap.getOrDefault(association.getAlumniAssociationId(), false));
                        vo.setIsFollowed(finalFollowStatusMap.getOrDefault(association.getAlumniAssociationId(), false));
                    } else {
                        vo.setIsMember(null); // 未登录
                        vo.setIsFollowed(null); // 未登录
                    }
                    return vo;
                })
                .toList();

        log.info("根据校处会ID分页查询校友会列表，校处会ID：{}，当前用户ID：{}，当前页：{}，每页数量：{}，总记录数：{}",
                platformId, currentUserId, current, pageSize, alumniAssociationPage.getTotal());

        // 10. 转换结果并返回
        Page<AlumniAssociationListVo> resultPage = new Page<AlumniAssociationListVo>(current, pageSize,
                alumniAssociationPage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public Page<OrganizationMemberResponse> getLocalPlatformMemberPage(QueryLocalPlatformMemberListDto queryDto) {
        // 1. 参数校验
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Long localPlatformId = queryDto.getLocalPlatformId();
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }

        // 2. 校验校处会是否存在
        LocalPlatform localPlatform = this.getById(localPlatformId);
        if (localPlatform == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校处会不存在");
        }

        // 3. 提取查询参数
        String nickname = queryDto.getNickname();
        String name = queryDto.getName();
        Integer gender = queryDto.getGender();
        String curProvince = queryDto.getCurProvince();
        String curCity = queryDto.getCurCity();
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();
        String sortField = queryDto.getSortField();
        String sortOrder = queryDto.getSortOrder();

        // 4. 先查询成员表，获取该校处会的所有成员
        Page<LocalPlatformMember> memberPage = new Page<>(current, pageSize);
        LambdaQueryWrapper<LocalPlatformMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper
                .eq(LocalPlatformMember::getLocalPlatformId, localPlatformId)
                .eq(LocalPlatformMember::getStatus, 1); // 状态：1-正常

        // 添加排序
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            if ("joinTime".equals(sortField)) {
                memberWrapper.orderBy(true, isAsc, LocalPlatformMember::getJoinTime);
            } else {
                memberWrapper.orderByDesc(LocalPlatformMember::getJoinTime);
            }
        } else {
            // 默认按加入时间降序
            memberWrapper.orderByDesc(LocalPlatformMember::getJoinTime);
        }

        Page<LocalPlatformMember> memberResultPage = localPlatformMemberService.page(memberPage, memberWrapper);

        // 5. 如果成员列表为空，直接返回空分页结果
        if (memberResultPage.getRecords().isEmpty()) {
            Page<OrganizationMemberResponse> emptyPage = new Page<>(current, pageSize);
            emptyPage.setTotal(0);
            return emptyPage;
        }

        // 6. 提取所有非空的 wxId
        List<Long> wxIds = memberResultPage.getRecords().stream()
                .map(LocalPlatformMember::getWxId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 7. 批量查询用户信息（一次查询，避免 N+1 问题）
        Map<Long, WxUserInfo> userInfoMap = new HashMap<>();
        if (!wxIds.isEmpty()) {
            LambdaQueryWrapper<WxUserInfo> userInfoWrapper = new LambdaQueryWrapper<>();
            userInfoWrapper
                    .in(WxUserInfo::getWxId, wxIds)
                    .like(StringUtils.isNotBlank(nickname), WxUserInfo::getNickname, nickname)
                    .like(StringUtils.isNotBlank(name), WxUserInfo::getName, name)
                    .eq(gender != null, WxUserInfo::getGender, gender)
                    .like(StringUtils.isNotBlank(curProvince), WxUserInfo::getCurProvince, curProvince)
                    .like(StringUtils.isNotBlank(curCity), WxUserInfo::getCurCity, curCity);

            List<WxUserInfo> userInfoList = wxUserInfoService.list(userInfoWrapper);

            // 8. 转成 Map，方便查找（key: wxId, value: WxUserInfo）
            userInfoMap = userInfoList.stream()
                    .collect(Collectors.toMap(WxUserInfo::getWxId, Function.identity(), (v1, v2) -> v1));
        }

        // 9. 提取所有 roleOrId 并批量查询组织架构角色信息
        List<Long> roleOrIds = memberResultPage.getRecords().stream()
                .map(LocalPlatformMember::getRoleOrId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, OrganizeArchiRole> organizeArchiRoleMap = new HashMap<>();
        if (!roleOrIds.isEmpty()) {
            List<OrganizeArchiRole> roles = organizeArchiRoleService.listByIds(roleOrIds);
            organizeArchiRoleMap = roles.stream()
                    .collect(Collectors.toMap(OrganizeArchiRole::getRoleOrId, Function.identity(), (v1, v2) -> v1));
        }

        // 10. 组装结果（按成员列表的顺序，包括wx_id为null的成员）
        Map<Long, OrganizeArchiRole> finalOrganizeArchiRoleMap = organizeArchiRoleMap;
        Map<Long, WxUserInfo> finalUserInfoMap = userInfoMap;
        List<OrganizationMemberResponse> responseList = memberResultPage.getRecords().stream()
                .map(member -> {
                    OrganizationMemberResponse response = new OrganizationMemberResponse();

                    // 如果 wx_id 不为空，尝试从用户信息中填充基本字段
                    if (member.getWxId() != null) {
                        WxUserInfo userInfo = finalUserInfoMap.get(member.getWxId());
                        if (userInfo != null) {
                            response = OrganizationMemberResponse.objToVo(userInfo);
                            response.setWxId(String.valueOf(userInfo.getWxId()));
                        } else {
                            // wx_id不为空，但没有找到用户信息（可能被查询条件过滤掉了）
                            return null;
                        }
                    }

                    // 从成员表设置 username 和 roleName（这两个字段总是来自成员表）
                    response.setUsername(member.getUsername());
                    response.setRoleName(member.getRoleName());

                    // 设置组织架构角色信息
                    if (member.getRoleOrId() != null) {
                        OrganizeArchiRole role = finalOrganizeArchiRoleMap.get(member.getRoleOrId());
                        if (role != null) {
                            response.setOrganizeArchiRole(OrganizeArchiRoleVo.objToVo(role));
                        }
                    }

                    return response;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("根据校处会ID分页查询成员列表，校处会ID：{}，当前页：{}，每页数量：{}，总记录数：{}",
                localPlatformId, current, pageSize, memberResultPage.getTotal());

        // 11. 构建分页结果
        Page<OrganizationMemberResponse> resultPage = new Page<>(current, pageSize, memberResultPage.getTotal());
        resultPage.setRecords(responseList);
        return resultPage;
    }

    @Override
    public boolean updateMemberRole(Long operatorWxId, Long localPlatformId, Long wxId, Long roleOrId) {
        // 1. 参数校验
        if (operatorWxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "操作人用户ID不能为空");
        }
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员用户ID不能为空");
        }
        if (roleOrId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "组织架构角色ID不能为空");
        }

        log.info("开始更新校处会成员角色 - 操作人ID: {}, 校处会ID: {}, 成员用户ID: {}, 新角色ID: {}",
                operatorWxId, localPlatformId, wxId, roleOrId);

        // 2. 查询校处会是否存在
        LocalPlatform localPlatform = this.getById(localPlatformId);
        if (localPlatform == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校处会不存在");
        }

        // 3. 查询成员记录是否存在
        LocalPlatformMember member = localPlatformMemberService.getOne(
                new LambdaQueryWrapper<LocalPlatformMember>()
                        .eq(LocalPlatformMember::getLocalPlatformId, localPlatformId)
                        .eq(LocalPlatformMember::getWxId, wxId)
                        .eq(LocalPlatformMember::getStatus, 1) // 状态：1-正常
        );

        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该用户不是该校处会的成员");
        }

        // 4. 查询新的组织架构角色是否存在且有效
        OrganizeArchiRole organizeArchiRole = organizeArchiRoleService.getOne(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getRoleOrId, roleOrId)
                        .eq(OrganizeArchiRole::getOrganizeId, localPlatformId)
                        .eq(OrganizeArchiRole::getOrganizeType, 1) // 1-校处会
                        .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
        );

        if (organizeArchiRole == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该组织架构角色不存在或未启用");
        }

        // 5. 更新成员角色
        Long oldRoleOrId = member.getRoleOrId();
        member.setRoleOrId(roleOrId);
        boolean updateResult = localPlatformMemberService.updateById(member);

        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新成员角色失败");
        }

        log.info("更新校处会成员角色成功 - 校处会ID: {}, 成员用户ID: {}, 原角色ID: {}, 新角色ID: {}",
                localPlatformId, wxId, oldRoleOrId, roleOrId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberRoleV2(Long operatorWxId, Long localPlatformId, Long id, String username,
            Long roleOrId, String roleName) {
        // 1. 参数校验
        if (operatorWxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "操作人用户ID不能为空");
        }
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员用户名不能为空");
        }
        if (roleOrId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "组织架构角色ID不能为空");
        }

        log.info("开始处理校处会成员角色V2 - 操作人ID: {}, 校处会ID: {}, 成员ID: {}, 成员用户名: {}, 新角色ID: {}, 角色名称: {}",
                operatorWxId, localPlatformId, id, username, roleOrId, roleName);

        // 2. 查询校处会是否存在
        LocalPlatform localPlatform = this.getById(localPlatformId);
        if (localPlatform == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校处会不存在");
        }

        // 3. 查询或者创建成员记录
        LocalPlatformMember member = null;
        if (id != null) {
            // 情况A：提供了 ID，按 ID 查询
            member = localPlatformMemberService.getById(id);
            if (member == null) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该成员记录不存在");
            }
        } else {
            // 情况B：未提供 ID，先按用户名查询，防止重复
            member = localPlatformMemberService.getOne(
                    new LambdaQueryWrapper<LocalPlatformMember>()
                            .eq(LocalPlatformMember::getLocalPlatformId, localPlatformId)
                            .eq(LocalPlatformMember::getUsername, username)
                            .last("LIMIT 1"));
        }

        if (member == null) {
            // 情况C：完全找不到记录，新增一个（架构成员）
            member = new LocalPlatformMember();
            member.setLocalPlatformId(localPlatformId);
            member.setUsername(username);
            member.setJoinTime(java.time.LocalDateTime.now());
        }

        // 统一设置必要字段
        member.setUsername(username); // 确保用户名同步
        member.setRoleOrId(roleOrId);
        member.setRoleName(roleName); // 设置角色名称
        member.setIsNu(1); // 设为架构成员
        member.setStatus(1); // 设为正常状态

        // 4. 查询新的组织架构角色是否存在且有效
        OrganizeArchiRole organizeArchiRole = organizeArchiRoleService.getOne(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getRoleOrId, roleOrId)
                        .eq(OrganizeArchiRole::getOrganizeId, localPlatformId)
                        .eq(OrganizeArchiRole::getOrganizeType, 1) // 1-校处会
                        .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
        );

        if (organizeArchiRole == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该组织架构角色不存在或未启用");
        }

        // 5. 保存或更新成员
        boolean saveOrUpdateResult = localPlatformMemberService.saveOrUpdate(member);

        if (!saveOrUpdateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "保存成员角色失败");
        }

        log.info("处理校处会成员角色V2成功 - 校处会ID: {}, 成员记录ID: {}, 成员用户名: {}, 新角色ID: {}, 角色名称: {}",
                localPlatformId, member.getId(), username, roleOrId, roleName);

        return true;
    }

    @Override
    public List<UserListResponse> getAdminsByLocalPlatformId(Long localPlatformId) {
        log.info("查询校处会管理员列表，校处会 ID: {}", localPlatformId);

        // 1. 参数校验
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }

        // 2. 查询 ORGANIZE_LOCAL_ADMIN 角色
        Role adminRole = roleService.getRoleByCodeInner("ORGANIZE_LOCAL_ADMIN");
        if (adminRole == null) {
            log.error("未找到校处会管理员角色，角色代码: ORGANIZE_LOCAL_ADMIN");
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "系统角色配置错误");
        }

        // 3. 查询 role_user 表，获取该校处会的所有管理员用户ID
        LambdaQueryWrapper<RoleUser> roleUserQueryWrapper = new LambdaQueryWrapper<>();
        roleUserQueryWrapper
                .eq(RoleUser::getRoleId, adminRole.getRoleId())
                .eq(RoleUser::getType, 1)  // type=1 表示校处会
                .eq(RoleUser::getOrganizeId, localPlatformId);

        List<RoleUser> roleUserList = roleUserService.list(roleUserQueryWrapper);

        if (roleUserList.isEmpty()) {
            log.info("该校处会暂无管理员，校处会 ID: {}", localPlatformId);
            return Collections.emptyList();
        }

        // 4. 提取所有管理员的 wxId
        List<Long> adminWxIds = roleUserList.stream()
                .map(RoleUser::getWxId)
                .distinct()
                .collect(Collectors.toList());

        log.info("查询到 {} 名管理员，校处会 ID: {}", adminWxIds.size(), localPlatformId);

        // 5. 批量查询用户信息
        LambdaQueryWrapper<WxUserInfo> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(WxUserInfo::getWxId, adminWxIds);
        List<WxUserInfo> userInfoList = wxUserInfoService.list(userQueryWrapper);

        // 6. 转换为 UserListResponse
        List<UserListResponse> responseList = userInfoList.stream()
                .map(UserListResponse::ObjToVo)
                .collect(Collectors.toList());

        log.info("查询校处会管理员列表成功，校处会 ID: {}, 管理员数量: {}", localPlatformId, responseList.size());

        return responseList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAdminToLocalPlatform(Long localPlatformId, Long wxId) {
        log.info("添加校处会管理员，校处会 ID: {}, 用户 ID: {}", localPlatformId, wxId);

        // 1. 参数校验
        if (localPlatformId == null || wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID和用户ID不能为空");
        }

        // 2. 校验校处会是否存在
        LocalPlatform localPlatform = this.getById(localPlatformId);
        if (localPlatform == null) {
            log.error("校处会不存在，校处会 ID: {}", localPlatformId);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "校处会不存在");
        }

        // 3. 校验用户是否存在
        WxUserInfo userInfo = wxUserInfoService.getOne(
                new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, wxId)
        );
        if (userInfo == null) {
            log.error("用户不存在，用户 ID: {}", wxId);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "用户不存在");
        }

        // 4. 查询管理员角色
        Role adminRole = roleService.getRoleByCodeInner("ORGANIZE_LOCAL_ADMIN");
        if (adminRole == null) {
            log.error("未找到校处会管理员角色，角色代码: ORGANIZE_LOCAL_ADMIN");
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "系统角色配置错误");
        }

        // 5. 检查用户是否已经是该校处会的管理员
        LambdaQueryWrapper<RoleUser> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper
                .eq(RoleUser::getWxId, wxId)
                .eq(RoleUser::getRoleId, adminRole.getRoleId())
                .eq(RoleUser::getType, 1)  // type=1 表示校处会
                .eq(RoleUser::getOrganizeId, localPlatformId);

        RoleUser existingRoleUser = roleUserService.getOne(checkWrapper);
        if (existingRoleUser != null) {
            log.warn("用户已经是该校处会的管理员，校处会 ID: {}, 用户 ID: {}", localPlatformId, wxId);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "该用户已经是管理员");
        }

        // 6. 创建 role_user 记录
        RoleUser roleUser = new RoleUser();
        roleUser.setWxId(wxId);
        roleUser.setRoleId(adminRole.getRoleId());
        roleUser.setType(1);  // 1-校处会
        roleUser.setOrganizeId(localPlatformId);
        roleUser.setCreateTime(LocalDateTime.now());
        roleUser.setUpdateTime(LocalDateTime.now());

        boolean result = roleUserService.save(roleUser);

        if (result) {
            log.info("添加校处会管理员成功，校处会 ID: {}, 用户 ID: {}", localPlatformId, wxId);
        } else {
            log.error("添加校处会管理员失败，校处会 ID: {}, 用户 ID: {}", localPlatformId, wxId);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAdminFromLocalPlatform(Long localPlatformId, Long wxId) {
        log.info("移除校处会管理员，校处会 ID: {}, 用户 ID: {}", localPlatformId, wxId);

        // 1. 参数校验
        if (localPlatformId == null || wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID和用户ID不能为空");
        }

        // 2. 查询管理员角色
        Role adminRole = roleService.getRoleByCodeInner("ORGANIZE_LOCAL_ADMIN");
        if (adminRole == null) {
            log.error("未找到校处会管理员角色，角色代码: ORGANIZE_LOCAL_ADMIN");
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "系统角色配置错误");
        }

        // 3. 查询该用户在该校处会的管理员记录
        LambdaQueryWrapper<RoleUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(RoleUser::getWxId, wxId)
                .eq(RoleUser::getRoleId, adminRole.getRoleId())
                .eq(RoleUser::getType, 1)  // type=1 表示校处会
                .eq(RoleUser::getOrganizeId, localPlatformId);

        RoleUser roleUser = roleUserService.getOne(queryWrapper);
        if (roleUser == null) {
            log.warn("该用户不是该校处会的管理员，校处会 ID: {}, 用户 ID: {}", localPlatformId, wxId);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "该用户不是管理员");
        }

        // 4. 删除 role_user 记录（逻辑删除）
        boolean result = roleUserService.removeById(roleUser.getId());

        if (result) {
            log.info("移除校处会管理员成功，校处会 ID: {}, 用户 ID: {}", localPlatformId, wxId);
        } else {
            log.error("移除校处会管理员失败，校处会 ID: {}, 用户 ID: {}", localPlatformId, wxId);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindMemberToUser(Long memberId, Long wxId) {
        log.info("绑定校处会成员与系统用户 - 成员ID: {}, 用户ID: {}", memberId, wxId);

        // 1. 参数校验
        if (memberId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员表ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户微信ID不能为空");
        }

        // 2. 查询成员记录是否存在
        LocalPlatformMember member = localPlatformMemberService.getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "成员记录不存在");
        }

        // 3. 校验用户是否存在
        WxUser wxUser = userService.getById(wxId);
        if (wxUser == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "用户不存在");
        }

        // 4. 检查该用户是否已经绑定到该校处会的其他成员记录
        LambdaQueryWrapper<LocalPlatformMember> existingBindQuery = new LambdaQueryWrapper<>();
        existingBindQuery
                .eq(LocalPlatformMember::getWxId, wxId)
                .eq(LocalPlatformMember::getLocalPlatformId, member.getLocalPlatformId())
                .ne(LocalPlatformMember::getId, memberId)
                .eq(LocalPlatformMember::getStatus, 1);

        Long existingBindCount = localPlatformMemberService.count(existingBindQuery);
        if (existingBindCount > 0) {
            log.warn("该用户已绑定到该校处会的其他成员记录，用户ID: {}, 校处会ID: {}",
                    wxId, member.getLocalPlatformId());
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该用户已绑定到该校处会的其他成员记录");
        }

        // 5. 更新成员记录的 wx_id 字段
        member.setWxId(wxId);
        boolean updateResult = localPlatformMemberService.updateById(member);

        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "绑定失败");
        }

        log.info("绑定校处会成员与系统用户成功 - 成员ID: {}, 用户ID: {}", memberId, wxId);
        return true;
    }

}
