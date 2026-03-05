package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.association.LocalPlatformMemberService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.association.LocalPlatformPrivacySettingService;
import com.cmswe.alumni.api.system.HomePageArticleService;
import com.cmswe.alumni.api.user.FileService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.user.RoleUserService;
import com.cmswe.alumni.api.user.UserFollowService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.constant.CommonConstant;
import com.cmswe.alumni.common.dto.AddLocalPlatformDto;
import com.cmswe.alumni.common.dto.MiniProgramLinkDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationByPlatformDto;
import com.cmswe.alumni.common.dto.QueryLocalPlatformListDto;
import com.cmswe.alumni.common.dto.QueryLocalPlatformMemberListDto;
import com.cmswe.alumni.common.dto.UpdateLocalPlatformDto;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.AlumniAssociationMember;
import com.cmswe.alumni.common.entity.HomePageArticle;
import com.cmswe.alumni.common.entity.LocalPlatform;

import com.cmswe.alumni.common.entity.Files;
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
import com.cmswe.alumni.common.vo.FilesVo;
import com.cmswe.alumni.common.vo.HomePageArticleVo;
import com.cmswe.alumni.common.vo.LocalPlatformDetailVo;
import com.cmswe.alumni.common.vo.LocalPlatformListVo;
import com.cmswe.alumni.common.vo.ManagedOrganizationVo;
import com.cmswe.alumni.common.vo.LocalPlatformMemberListVo;
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
    private ObjectMapper objectMapper;

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

    @Lazy
    @Resource
    private HomePageArticleService homePageArticleService;

    @Resource
    private FileService fileService;

    @Resource
    private LocalPlatformPrivacySettingService localPlatformPrivacySettingService;

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
        // 4.1 使用 LocalPlatform 表中的会员数量字段
        // 注意：memberCount 已经通过 objToVo 方法从实体类复制到 VO 中

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

        // 4.4 查询该校促会的文章列表（最新6篇）
        LambdaQueryWrapper<HomePageArticle> articleQueryWrapper = new LambdaQueryWrapper<>();
        articleQueryWrapper
                .eq(HomePageArticle::getPublishType, "LOCAL_PLATFORM") // 发布者类型：校促会
                .eq(HomePageArticle::getPublishWxId, id) // 发布者ID等于当前校促会ID
                .eq(HomePageArticle::getArticleStatus, 1) // 状态：1-启用
                .eq(HomePageArticle::getApplyStatus, 1) // 审核状态：1-审核通过
                .eq(HomePageArticle::getPid, 0L) // 只查询父文章（pid=0）
                .orderByDesc(HomePageArticle::getCreateTime) // 按创建时间倒序
                .last("LIMIT 6"); // 限制返回6篇

        List<HomePageArticle> articleList = homePageArticleService.list(articleQueryWrapper);
        List<HomePageArticleVo> articleListVos = articleList.stream()
                .map(article -> {
                    HomePageArticleVo vo = HomePageArticleVo.objToVo(article);

                    // 查询并设置封面图信息
                    if (article.getCoverImg() != null) {
                        try {
                            Files file = fileService.getById(article.getCoverImg());
                            if (file != null) {
                                vo.setCoverImg(FilesVo.objToVo(file));
                            }
                        } catch (Exception e) {
                            log.warn("查询封面图失败 - ArticleId: {}, CoverImgId: {}, Error: {}",
                                    article.getHomeArticleId(), article.getCoverImg(), e.getMessage());
                        }
                    }

                    return vo;
                })
                .collect(Collectors.toList());
        localPlatformDetailVo.setArticleList(articleListVos);

        // 4.5 手动映射字段 (因为VO中重命名为 localPlatformPhone 与 实体类 phone 不一致，BeanUtils无法自动映射)
        // 映射必须在隐私脱敏逻辑之前，否则脱敏设置的 null 会被重新覆盖
        localPlatformDetailVo.setLocalPlatformPhone(localPlatform.getPhone());

        // 4.5.5 解析小程序链接JSON
        if (localPlatform.getMiniProgramLinks() != null && !localPlatform.getMiniProgramLinks().trim().isEmpty()) {
            try {
                List<MiniProgramLinkDto> miniProgramLinks = objectMapper.readValue(
                        localPlatform.getMiniProgramLinks(),
                        new TypeReference<List<MiniProgramLinkDto>>() {});
                localPlatformDetailVo.setMiniProgramLinks(miniProgramLinks);
            } catch (Exception e) {
                log.warn("解析小程序链接JSON失败 - PlatformId: {}, Error: {}", id, e.getMessage());
                localPlatformDetailVo.setMiniProgramLinks(new ArrayList<>());
            }
        } else {
            localPlatformDetailVo.setMiniProgramLinks(new ArrayList<>());
        }

        // 4.6 应用隐私设置
        try {
            List<com.cmswe.alumni.common.entity.LocalPlatformPrivacySetting> privacySettings = localPlatformPrivacySettingService
                    .getPlatformPrivacy(id);
            Map<String, Integer> privacyMap = privacySettings.stream()
                    .collect(Collectors.toMap(com.cmswe.alumni.common.entity.LocalPlatformPrivacySetting::getFieldCode,
                            com.cmswe.alumni.common.entity.LocalPlatformPrivacySetting::getVisibility, (v1, v2) -> v1));

            // 根据配置脱敏
            if (privacyMap.getOrDefault("description", 0) == 0)
                localPlatformDetailVo.setDescription(null);
            if (privacyMap.getOrDefault("memberCount", 0) == 0)
                localPlatformDetailVo.setMemberCount(null);
            if (privacyMap.getOrDefault("principalName", 0) == 0)
                localPlatformDetailVo.setPrincipalName(null);
            if (privacyMap.getOrDefault("principalPosition", 0) == 0)
                localPlatformDetailVo.setPrincipalPosition(null);
            if (privacyMap.getOrDefault("local_platform_phone", 0) == 0)
                localPlatformDetailVo.setLocalPlatformPhone(null);
        } catch (Exception e) {
            log.error("应用校促会隐私设置失败 - PlatformId: {}", id, e);
            // 异常时保持原有字段值
        }

        log.info("根据id获取校处会详情id:{}, 会员数:{}, 校友会数:{}, 文章数:{}",
                id, localPlatform.getMemberCount(), associationCount, articleListVos.size());

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

        // 设置默认排序字段
        if (sortField == null) {
            sortField = "createTime";
        }

        // 3.构建查询条件（强制只查询启用状态的校处会）
        LambdaQueryWrapper<LocalPlatform> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                // 强制条件：只返回启用状态（status=1）的校处会
                .eq(LocalPlatform::getStatus, 1)
                // 其他可选查询条件
                .like(StringUtils.isNotBlank(platformName), LocalPlatform::getPlatformName, platformName)
                .like(StringUtils.isNotBlank(city), LocalPlatform::getCity, city)
                .like(StringUtils.isNotBlank(scope), LocalPlatform::getScope, scope);

        // 4.添加排序：先按指定字段排序，再按主键排序（确保排序稳定，避免分页重复）
        if ("createTime".equals(sortField)) {
            queryWrapper.orderBy(true, CommonConstant.SORT_ORDER_ASC.equals(sortOrder), LocalPlatform::getCreateTime);
        }
        queryWrapper.orderByDesc(LocalPlatform::getPlatformId);

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
    public boolean inviteMember(Long localPlatformId, Long wxId, Long roleOrId, String username, String roleName, String contactInformation, String socialDuties) {
        // 1. 参数校验
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校处会ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员用户ID不能为空");
        }


        log.info("开始邀请成员加入校处会 - 校处会ID: {}, 成员用户ID: {}, 角色ID: {}, 用户名: {}, 角色名称: {}, 联系方式: {}, 社会职务: {}",
                localPlatformId, wxId, roleOrId, username, roleName, contactInformation, socialDuties);

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

        // 直接使用前端传入的角色名称，不自动查询

        // 6. 创建新成员记录
        LocalPlatformMember newMember = new LocalPlatformMember();
        newMember.setWxId(wxId);
        newMember.setLocalPlatformId(localPlatformId);
        newMember.setRoleOrId(roleOrId);
        newMember.setUsername(username);
        newMember.setRoleName(roleName);
        newMember.setContactInformation(contactInformation);
        newMember.setSocialDuties(socialDuties);
        newMember.setJoinTime(java.time.LocalDateTime.now());
        newMember.setStatus(1); // 状态：1-正常

        boolean saveResult = localPlatformMemberService.save(newMember);
        if (!saveResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "邀请成员失败");
        }

        log.info("邀请成员加入校处会成功 - 校处会ID: {}, 成员用户ID: {}, 角色ID: {}, 用户名: {}, 角色名称: {}, 联系方式: {}, 社会职务: {}",
                localPlatformId, wxId, roleOrId, username, roleName, contactInformation, socialDuties);

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
                        vo.setIsFollowed(
                                finalFollowStatusMap.getOrDefault(association.getAlumniAssociationId(), false));
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

        // 4. 先查询成员表，获取该校处会的所有成员（包括 wx_id 为空的预设成员）
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
                .filter(id -> id != null && id != 0L)
                .distinct()
                .collect(Collectors.toList());

        // 7. 批量查询用户信息（一次查询，避免 N+1 问题）
        Map<Long, WxUserInfo> userInfoMap = new HashMap<>();
        if (!wxIds.isEmpty()) {
            LambdaQueryWrapper<WxUserInfo> userInfoWrapper = new LambdaQueryWrapper<>();
            userInfoWrapper
                    .in(WxUserInfo::getWxId, wxIds);

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

        // 10. 组装结果（包括 wxId 为空的预设成员）
        Map<Long, OrganizeArchiRole> finalOrganizeArchiRoleMap = organizeArchiRoleMap;
        Map<Long, WxUserInfo> finalUserInfoMap = userInfoMap;
        List<OrganizationMemberResponse> responseList = memberResultPage.getRecords().stream()
                .map(member -> {
                    // 构建 VO 响应结果
                    OrganizationMemberResponse response = new OrganizationMemberResponse();
                    
                    // 检查是否已加入平台（有 wxId）
                    boolean isJoined = member.getWxId() != null && member.getWxId() != 0L;
                    response.setJoined(isJoined);
                    
                    // 设置成员基本信息
                    response.setId(member.getId());
                    response.setUsername(member.getUsername());
                    response.setRoleName(member.getRoleName());
                    response.setContactInformation(member.getContactInformation());
                    response.setSocialDuties(member.getSocialDuties());
                    
                    // 如果已加入平台，填充用户信息
                    if (isJoined) {
                        WxUserInfo userInfo = finalUserInfoMap.get(member.getWxId());
                        if (userInfo != null) { 
                            // 应用用户信息过滤条件
                            boolean matchFilter = true;
                            if (StringUtils.isNotBlank(nickname) && !userInfo.getNickname().contains(nickname)) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(name) && !userInfo.getName().contains(name)) {
                                matchFilter = false;
                            }
                            if (matchFilter && gender != null && !gender.equals(userInfo.getGender())) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(curProvince) && !userInfo.getCurProvince().contains(curProvince)) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(curCity) && !userInfo.getCurCity().contains(curCity)) {
                                matchFilter = false;
                            }
                            
                            if (!matchFilter) {
                                return null;
                            }
                            
                            // 填充用户信息
                            response.setNickname(userInfo.getNickname());
                            response.setName(userInfo.getName());
                            response.setGender(userInfo.getGender());
                            response.setCurProvince(userInfo.getCurProvince());
                            response.setCurCity(userInfo.getCurCity());
                            response.setAvatarUrl(userInfo.getAvatarUrl());
                            response.setWxId(String.valueOf(userInfo.getWxId()));
                        }
                    } else {
                        // 未加入平台的成员，只设置基本信息
                        response.setNickname(member.getUsername()); // 使用用户名作为昵称
                        response.setName(member.getUsername()); // 使用用户名作为姓名
                        response.setWxId(null);
                    }

                    // 设置架构详细信息
                    if (member.getRoleOrId() != null) {
                        OrganizeArchiRole role = finalOrganizeArchiRoleMap.get(member.getRoleOrId());
                        if (role != null) {
                            response.setOrganizeArchiRole(OrganizeArchiRoleVo.objToVo(role));
                        }
                    }

                    log.debug("成员: {} (wxId: {}, 已加入: {})", member.getUsername(), member.getWxId(), isJoined);
                    return response;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("查询校处会成员列表 - 校处会ID: {}, 总记录数: {}, 过滤后记录数: {}",
                localPlatformId, memberResultPage.getTotal(), responseList.size());

        // 11. 构建分页结果
        Page<OrganizationMemberResponse> resultPage = new Page<>(current, pageSize, (long) responseList.size());
        resultPage.setRecords(responseList);
        return resultPage;
    }

    @Override
    public boolean updateMemberRole(Long operatorWxId, Long localPlatformId, Long wxId, Long roleOrId, String roleName, String contactInformation, String socialDuties) {
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


        log.info("开始更新校处会成员角色 - 操作人ID: {}, 校处会ID: {}, 成员用户ID: {}, 新角色ID: {}, 角色名称: {}, 联系方式: {}, 社会职务: {}",
                operatorWxId, localPlatformId, wxId, roleOrId, roleName, contactInformation, socialDuties);

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

        // 4. 查询新的组织架构角色是否存在且有效（仅当 roleOrId 不为 null 时）
        if (roleOrId != null) {
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
        }

        // 5. 更新成员信息
        Long oldRoleOrId = member.getRoleOrId();
        member.setRoleOrId(roleOrId);
        if (roleName != null) {
            member.setRoleName(roleName);
        }
        if (contactInformation != null) {
            member.setContactInformation(contactInformation);
        }
        if (socialDuties != null) {
            member.setSocialDuties(socialDuties);
        }

        boolean updateResult = localPlatformMemberService.updateById(member);

        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新成员信息失败");
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

        // 4. 查询新的组织架构角色是否存在且有效（仅当 roleOrId 不为 null 时）
        if (roleOrId != null) {
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
                .eq(RoleUser::getType, 1) // type=1 表示校处会
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
                new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, wxId));
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
                .eq(RoleUser::getType, 1) // type=1 表示校处会
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
        roleUser.setType(1); // 1-校处会
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
                .eq(RoleUser::getType, 1) // type=1 表示校处会
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

    @Override
    public List<ManagedOrganizationVo> getManagedPlatforms(Long wxId) {
        // 1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }

        // 2. 查询用户的角色
        List<Role> userRoles = roleService.getRolesByUserId(wxId);

        // 3. 判断是否是系统管理员
        boolean isSystemAdmin = userRoles.stream()
                .anyMatch(role -> "SYSTEM_SUPER_ADMIN".equals(role.getRoleCode()));

        List<ManagedOrganizationVo> result = new ArrayList<>();

        if (isSystemAdmin) {
            // 4. 系统管理员：返回所有校促会
            List<LocalPlatform> allPlatforms = this.list(
                    new LambdaQueryWrapper<LocalPlatform>()
                            .eq(LocalPlatform::getStatus, 1) // 只返回启用的
                            .orderByDesc(LocalPlatform::getCreateTime));

            result = allPlatforms.stream()
                    .map(platform -> {
                        ManagedOrganizationVo vo = new ManagedOrganizationVo();
                        vo.setOrganizationId(String.valueOf(platform.getPlatformId()));
                        vo.setOrganizationName(platform.getPlatformName());
                        vo.setAvatar(platform.getAvatar());
                        vo.setMemberCount(platform.getMemberCount());
                        vo.setMonthlyHomepageArticleQuota(platform.getMonthlyHomepageArticleQuota());
                        return vo;
                    })
                    .collect(Collectors.toList());

            log.info("系统管理员查询所有校促会 - 用户ID: {}, 校促会数量: {}", wxId, result.size());

        } else {
            // 5. 组织管理员：查询有权限管理的校促会
            List<RoleUser> roleUsers = roleUserService.list(
                    new LambdaQueryWrapper<RoleUser>()
                            .eq(RoleUser::getWxId, wxId)
                            .eq(RoleUser::getType, 1) // 1-校促会
                            .isNotNull(RoleUser::getOrganizeId));

            if (roleUsers.isEmpty()) {
                log.info("用户无管理的校促会 - 用户ID: {}", wxId);
                return result;
            }

            // 提取校促会ID列表
            List<Long> platformIds = roleUsers.stream()
                    .map(RoleUser::getOrganizeId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询校促会信息
            List<LocalPlatform> platforms = this.listByIds(platformIds);
            result = platforms.stream()
                    .filter(platform -> platform.getStatus() == 1) // 过滤启用的
                    .map(platform -> {
                        ManagedOrganizationVo vo = new ManagedOrganizationVo();
                        vo.setOrganizationId(String.valueOf(platform.getPlatformId()));
                        vo.setOrganizationName(platform.getPlatformName());
                        vo.setAvatar(platform.getAvatar());
                        vo.setMemberCount(platform.getMemberCount());
                        vo.setMonthlyHomepageArticleQuota(platform.getMonthlyHomepageArticleQuota());
                        return vo;
                    })
                    .collect(Collectors.toList());

            log.info("组织管理员查询管理的校促会 - 用户ID: {}, 校促会数量: {}", wxId, result.size());
        }

        return result;
    }

    @Override
    public List<com.cmswe.alumni.common.entity.LocalPlatformPrivacySetting> getPrivacySettings(Long platformId) {
        return localPlatformPrivacySettingService.getPlatformPrivacy(platformId);
    }

    @Override
    public boolean updatePrivacySetting(Long platformId, String fieldCode, Integer visibility) {
        return localPlatformPrivacySettingService.updatePrivacy(platformId, fieldCode, visibility);
    }

    @Override
    public com.cmswe.alumni.common.vo.LocalPlatformAdminVo getAdminLocalPlatformById(Long platformId) {
        if (platformId == null) {
            throw new com.cmswe.alumni.common.exception.BusinessException(
                    com.cmswe.alumni.common.enums.ErrorType.ARGS_NOT_NULL);
        }
        LocalPlatform localPlatform = this.getById(platformId);
        if (localPlatform == null) {
            throw new com.cmswe.alumni.common.exception.BusinessException(
                    com.cmswe.alumni.common.enums.ErrorType.NOT_FOUND_ERROR, "未找到校处会信息");
        }
        return com.cmswe.alumni.common.vo.LocalPlatformAdminVo.objToVo(localPlatform);
    }

    @Override
    public boolean updateLocalPlatform(com.cmswe.alumni.common.dto.UpdateLocalPlatformDto updateDto) {
        if (updateDto == null || updateDto.getPlatformId() == null) {
            throw new com.cmswe.alumni.common.exception.BusinessException(
                    com.cmswe.alumni.common.enums.ErrorType.ARGS_NOT_NULL);
        }
        LocalPlatform localPlatform = new LocalPlatform();
        org.springframework.beans.BeanUtils.copyProperties(updateDto, localPlatform);
        // 特殊处理单字段不一致 (DTO中的phone对应Entity中的phone)
        localPlatform.setPhone(updateDto.getPhone());
        return this.updateById(localPlatform);
    }

    @Override
    public boolean addPresetMember(Long localPlatformId, String username, String roleName, Long roleOrId, String contactInformation, String socialDuties) {
        // 1. 参数校验
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校促会ID不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户名不能为空");
        }



        log.info("开始添加校促会预设成员 - 校促会ID: {}, 用户名: {}, 角色名称: {}, 角色ID: {}, 联系方式: {}, 社会职务: {}",
                localPlatformId, username, roleName, roleOrId, contactInformation, socialDuties);

        // 2. 查询校促会是否存在
        LocalPlatform localPlatform = this.getById(localPlatformId);
        if (localPlatform == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校促会不存在");
        }

        // 3. 查询组织架构角色是否存在（仅当 roleOrId 不为 null 时）
        if (roleOrId != null) {
            OrganizeArchiRole role = organizeArchiRoleService.getOne(
                    new LambdaQueryWrapper<OrganizeArchiRole>()
                            .eq(OrganizeArchiRole::getRoleOrId, roleOrId)
                            .eq(OrganizeArchiRole::getOrganizeId, localPlatformId)
                            .eq(OrganizeArchiRole::getOrganizeType, 1) // 1-校处会
                            .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
            );

            if (role == null) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该组织架构角色不存在或未启用");
            }
        }

        // 4. 创建预设成员记录（wxId 为 null，表示未加入平台的假人）
        LocalPlatformMember presetMember = new LocalPlatformMember();
        presetMember.setWxId(null); // 预设成员 wxId 为 null
        presetMember.setLocalPlatformId(localPlatformId);
        presetMember.setRoleOrId(roleOrId);
        presetMember.setUsername(username);
        presetMember.setRoleName(roleName);
        presetMember.setContactInformation(contactInformation);
        presetMember.setSocialDuties(socialDuties);
        presetMember.setJoinTime(java.time.LocalDateTime.now());
        presetMember.setStatus(1); // 状态：1-正常
        presetMember.setIsNu(1); // 1-是组织架构成员

        boolean saveResult = localPlatformMemberService.save(presetMember);
        if (!saveResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "添加预设成员失败");
        }

        log.info("添加校促会预设成员成功 - 校促会ID: {}, 用户名: {}, 角色名称: {}, 角色ID: {}",
                localPlatformId, username, roleName, roleOrId);

        return saveResult;
    }

    @Override
    public boolean updatePresetMember(Long memberId, Long wxId) {
        // 1. 参数校验
        if (memberId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }

        log.info("开始更新校促会预设成员 - 成员ID: {}, 用户ID: {}", memberId, wxId);

        // 2. 查询成员记录是否存在
        LocalPlatformMember member = localPlatformMemberService.getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "成员记录不存在");
        }

        // 3. 检查成员是否是预设成员（wxId 为 null）
        if (member.getWxId() != null) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该成员已经关联了用户，不能重复关联");
        }

        // 4. 查询用户是否存在
        WxUser wxUser = userService.getById(wxId);
        if (wxUser == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "用户不存在");
        }

        // 5. 检查该用户是否已经是该校促会的成员
        LocalPlatformMember existingMember = localPlatformMemberService.getOne(
                new LambdaQueryWrapper<LocalPlatformMember>()
                        .eq(LocalPlatformMember::getLocalPlatformId, member.getLocalPlatformId())
                        .eq(LocalPlatformMember::getWxId, wxId)
                        .eq(LocalPlatformMember::getStatus, 1) // 状态：1-正常
        );

        if (existingMember != null) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该用户已经是该校促会的成员");
        }

        // 6. 更新成员记录，关联用户ID
        member.setWxId(wxId);
        boolean updateResult = localPlatformMemberService.updateById(member);

        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新预设成员失败");
        }

        log.info("更新校促会预设成员成功 - 成员ID: {}, 用户ID: {}", memberId, wxId);

        return updateResult;
    }

    @Override
    public boolean updatePresetMemberInfo(Long memberId, String username, String roleName, String contactInformation, String socialDuties) {
        // 1. 参数校验
        if (memberId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员ID不能为空");
        }

        log.info("开始更新校促会预设成员信息 - 成员ID: {}, 用户名: {}, 角色名称: {}, 联系方式: {}, 社会职务: {}",
                memberId, username, roleName, contactInformation, socialDuties);

        // 2. 查询成员记录是否存在
        LocalPlatformMember member = localPlatformMemberService.getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "成员记录不存在");
        }

        // 3. 检查成员是否是预设成员（wxId 为 null）
        if (member.getWxId() != null) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该成员已经关联了用户，不能通过此接口更新信息");
        }

        // 4. 更新成员信息
        if (username != null) {
            member.setUsername(username);
        }
        if (roleName != null) {
            member.setRoleName(roleName);
        }
        if (contactInformation != null) {
            member.setContactInformation(contactInformation);
        }
        if (socialDuties != null) {
            member.setSocialDuties(socialDuties);
        }

        boolean updateResult = localPlatformMemberService.updateById(member);

        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新预设成员信息失败");
        }

        log.info("更新校促会预设成员信息成功 - 成员ID: {}", memberId);

        return updateResult;
    }

    @Override
    public boolean deletePresetMember(Long memberId) {
        // 1. 参数校验
        if (memberId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员ID不能为空");
        }

        log.info("开始删除校促会预设成员 - 成员ID: {}", memberId);

        // 2. 查询成员记录是否存在
        LocalPlatformMember member = localPlatformMemberService.getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "成员记录不存在");
        }

        // 3. 检查成员是否是预设成员（wxId 为 null）
        if (member.getWxId() != null) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该成员已经关联了用户，不能通过此接口删除");
        }

        // 4. 删除成员记录
        boolean deleteResult = localPlatformMemberService.removeById(memberId);

        if (!deleteResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "删除预设成员失败");
        }

        log.info("删除校促会预设成员成功 - 成员ID: {}", memberId);

        return deleteResult;
    }

    @Override
    public List<LocalPlatformMemberListVo> getLocalPlatformMemberList(Long localPlatformId) {
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校促会ID不能为空");
        }
        List<LocalPlatformMember> members = localPlatformMemberService.list(
                new LambdaQueryWrapper<LocalPlatformMember>()
                        .eq(LocalPlatformMember::getLocalPlatformId, localPlatformId)
                        .eq(LocalPlatformMember::getStatus, 1)
        );
        if (members.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> roleOrIds = members.stream()
                .map(LocalPlatformMember::getRoleOrId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> roleNameMap = new HashMap<>();
        if (!roleOrIds.isEmpty()) {
            List<OrganizeArchiRole> roles = organizeArchiRoleService.listByIds(roleOrIds);
            roleNameMap = roles.stream()
                    .collect(Collectors.toMap(OrganizeArchiRole::getRoleOrId, OrganizeArchiRole::getRoleOrName, (a, b) -> a));
        }
        List<Long> wxIds = members.stream()
                .map(LocalPlatformMember::getWxId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> avatarMap = new HashMap<>();
        if (!wxIds.isEmpty()) {
            List<WxUserInfo> userInfos = wxUserInfoService.list(
                    new LambdaQueryWrapper<WxUserInfo>().in(WxUserInfo::getWxId, wxIds));
            avatarMap = userInfos.stream()
                    .filter(u -> u.getAvatarUrl() != null)
                    .collect(Collectors.toMap(WxUserInfo::getWxId, WxUserInfo::getAvatarUrl, (a, b) -> a));
        }
        final Map<Long, String> finalRoleNameMap = roleNameMap;
        final Map<Long, String> finalAvatarMap = avatarMap;
        return members.stream()
                .map(m -> LocalPlatformMemberListVo.builder()
                        .memberId(m.getId())
                        .wxId(m.getWxId() != null ? String.valueOf(m.getWxId()) : null)
                        .username(m.getUsername())
                        .roleName(m.getRoleName())
                        .roleOrId(m.getRoleOrId())
                        .roleOrName(m.getRoleOrId() != null ? finalRoleNameMap.get(m.getRoleOrId()) : null)
                        .contactInformation(m.getContactInformation())
                        .socialDuties(m.getSocialDuties())
                        .avatarUrl(m.getWxId() != null ? finalAvatarMap.get(m.getWxId()) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMemberToStructure(Long localPlatformId, Long memberId, Long roleOrId) {
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校促会ID不能为空");
        }
        if (memberId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员ID不能为空");
        }
        if (roleOrId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "组织架构角色ID不能为空");
        }
        log.info("为校促会架构添加成员 - 校促会ID: {}, 成员ID: {}, 角色ID: {}", localPlatformId, memberId, roleOrId);

        // 1. 校验成员是否存在且属于该校促会
        LocalPlatformMember member = localPlatformMemberService.getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "成员不存在");
        }
        if (!localPlatformId.equals(member.getLocalPlatformId())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "该成员不属于该校促会");
        }
        if (!Integer.valueOf(1).equals(member.getStatus())) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "成员状态异常，无法添加至架构");
        }

        // 2. 校验组织架构角色是否存在且属于该校促会
        OrganizeArchiRole role = organizeArchiRoleService.getOne(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getRoleOrId, roleOrId)
                        .eq(OrganizeArchiRole::getOrganizeId, localPlatformId)
                        .eq(OrganizeArchiRole::getOrganizeType, 1)
                        .eq(OrganizeArchiRole::getStatus, 1));
        if (role == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "组织架构角色不存在或未启用");
        }

        // 3. 更新成员的 role_or_id 和 role_name
        member.setRoleOrId(roleOrId);
        member.setRoleName(role.getRoleOrName());
        member.setIsNu(1);

        boolean result = localPlatformMemberService.updateById(member);
        if (!result) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新成员架构角色失败");
        }
        log.info("为校促会架构添加成员成功 - 校促会ID: {}, 成员ID: {}, 角色ID: {}, 角色名: {}",
                localPlatformId, memberId, roleOrId, role.getRoleOrName());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMemberFromStructure(Long localPlatformId, Long memberId) {
        if (localPlatformId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校促会ID不能为空");
        }
        if (memberId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员ID不能为空");
        }
        log.info("将成员从校促会架构移除 - 校促会ID: {}, 成员ID: {}", localPlatformId, memberId);

        LocalPlatformMember member = localPlatformMemberService.getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "成员不存在");
        }
        if (!localPlatformId.equals(member.getLocalPlatformId())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "该成员不属于该校促会");
        }

        // 清空 role_or_id、role_name，设置 is_nu=0（使用 lambdaUpdate 显式设置 null，因 updateById 会忽略 null 值）
        boolean result = localPlatformMemberService.lambdaUpdate()
                .set(LocalPlatformMember::getRoleOrId, null)
                .set(LocalPlatformMember::getRoleName, null)
                .set(LocalPlatformMember::getIsNu, 0)
                .eq(LocalPlatformMember::getId, memberId)
                .eq(LocalPlatformMember::getLocalPlatformId, localPlatformId)
                .update();
        if (!result) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "移除成员架构角色失败");
        }
        log.info("将成员从校促会架构移除成功 - 校促会ID: {}, 成员ID: {}", localPlatformId, memberId);
        return true;
    }
}
