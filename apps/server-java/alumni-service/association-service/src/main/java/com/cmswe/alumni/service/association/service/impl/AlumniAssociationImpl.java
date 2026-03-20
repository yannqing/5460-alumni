package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.user.*;
import com.cmswe.alumni.common.constant.CommonConstant;
import com.cmswe.alumni.common.dto.AddAlumniAssociationDto;
import com.cmswe.alumni.common.dto.PublishActivityDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationListDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationMemberListRequest;
import com.cmswe.alumni.common.dto.UpdateActivityDto;
import com.cmswe.alumni.common.dto.UpdateAlumniAssociationDto;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.entity.Activity;
import com.cmswe.alumni.common.entity.AlumniPlace;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.enums.NotificationType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniAssociationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;
import com.cmswe.alumni.common.vo.LocalPlatformDetailVo;
import com.cmswe.alumni.common.vo.ManagedOrganizationVo;
import com.cmswe.alumni.common.vo.OrganizationMemberVo;
import com.cmswe.alumni.common.vo.OrganizationMemberV2Vo;
import com.cmswe.alumni.common.vo.OrganizationTreeVo;
import com.cmswe.alumni.common.vo.OrganizationTreeV2Vo;
import com.cmswe.alumni.common.vo.OrganizeArchiRoleVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.OrganizationMemberResponse;
import com.cmswe.alumni.common.vo.SchoolListVo;
import com.cmswe.alumni.common.vo.ActivityListVo;
import com.cmswe.alumni.common.vo.ActivityDetailVo;
import com.cmswe.alumni.common.vo.AlumniPlaceListVo;
import com.cmswe.alumni.common.vo.CoreMemberVo;
import com.cmswe.alumni.common.vo.UserListResponse;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationJoinApplicationMapper;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationMapper;
import com.cmswe.alumni.service.association.mapper.SchoolMapper;
import com.cmswe.alumni.api.system.ActivityService;
import com.cmswe.alumni.api.system.HomePageArticleService;
import com.cmswe.alumni.api.search.AlumniPlaceService;
import com.cmswe.alumni.common.entity.HomePageArticle;
import com.cmswe.alumni.common.vo.FilesVo;
import com.cmswe.alumni.common.vo.HomePageArticleVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlumniAssociationImpl extends ServiceImpl<AlumniAssociationMapper, AlumniAssociation>
        implements AlumniAssociationService {

    @Lazy
    @Resource
    private LocalPlatformService localPlatformService;

    @Lazy
    @Resource
    private AlumniAssociationMemberService alumniAssociationMemberService;

    @Resource
    private SchoolMapper schoolMapper;

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private RoleUserService roleUserService;

    @Resource
    private OrganizeArchiRoleService organizeArchiRoleService;

    @Resource
    private AlumniAssociationJoinApplicationMapper alumniAssociationJoinApplicationMapper;

    @Resource
    private UnifiedMessageApiService unifiedMessageApiService;

    @Resource
    @Lazy
    private ActivityService activityService;

    @Resource
    @Lazy
    private AlumniPlaceService alumniPlaceService;

    @Resource
    @Lazy
    private HomePageArticleService homePageArticleService;

    @Resource
    private FileService fileService;

    @Resource
    private UserFollowService userFollowService;

    @Resource
    private com.cmswe.alumni.service.association.mapper.AlumniAssociationInvitationMapper alumniAssociationInvitationMapper;

    // 分页查询 获取校友会列表
    @Override
    public PageVo<AlumniAssociationListVo> selectByPage(QueryAlumniAssociationListDto alumniAssociationListDto,
            Long currentUserId) {
        // 1.参数校验
        Optional.ofNullable(alumniAssociationListDto)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        // 2.获取参数
        String associationName = alumniAssociationListDto.getAssociationName();
        String presidentUsername = alumniAssociationListDto.getAssociationName();
        String contactInfo = alumniAssociationListDto.getContactInfo();
        String location = alumniAssociationListDto.getLocation();
        Integer myFollow = alumniAssociationListDto.getMyFollow();
        int current = alumniAssociationListDto.getCurrent();
        int pageSize = alumniAssociationListDto.getPageSize();
        String sortField = alumniAssociationListDto.getSortField();
        String sortOrder = alumniAssociationListDto.getSortOrder();

        // 设置默认排序字段
        if (sortField == null) {
            sortField = "createTime";
        }

        // 2.5 处理"我的关注"筛选：查询用户关注的校友会 ID 列表
        List<Long> followedAssociationIds = null;
        if (myFollow != null && myFollow == 1 && currentUserId != null) {
            followedAssociationIds = userFollowService.getFollowedTargetIds(currentUserId, 2); // 2-校友会

            // 如果用户没有关注任何校友会，直接返回空结果
            if (followedAssociationIds.isEmpty()) {
                Page<AlumniAssociationListVo> emptyPage = new Page<>(current, pageSize, 0);
                emptyPage.setRecords(new ArrayList<>());
                return PageVo.of(emptyPage);
            }
        }

        // 3.构建查询条件
        LambdaQueryWrapper<AlumniAssociation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.isNotBlank(associationName), AlumniAssociation::getAssociationName, associationName)
                .like(StringUtils.isNotBlank(contactInfo), AlumniAssociation::getContactInfo, contactInfo)
                .like(StringUtils.isNotBlank(location), AlumniAssociation::getLocation, location)
                // 排序：先按指定字段排序，再按主键排序（确保排序稳定，避免分页重复）
                .orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                        AlumniAssociation.getSortMethod(sortField))
                .orderByDesc(AlumniAssociation::getAlumniAssociationId);

        // 3.5 应用我的关注筛选
        if (followedAssociationIds != null) {
            queryWrapper.in(AlumniAssociation::getAlumniAssociationId, followedAssociationIds);
        }

        // 4.执行分页查询
        Page<AlumniAssociation> alumniAssociationPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 5. 如果查询结果不为空，批量查询学校信息
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

        // 6. 如果当前用户已登录，批量查询用户与这些校友会的成员关系和关注关系
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

        // 7. 转换为 VO，使用 @JsonSerialize 注解避免前端精度丢失，并设置学校信息、加入状态和关注状态
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

        log.info("分页查询校友会列表，当前用户ID：{}，总记录数：{}", currentUserId, alumniAssociationPage.getTotal());

        // 8.转换结果并返回
        Page<AlumniAssociationListVo> resultPage = new Page<AlumniAssociationListVo>(current, pageSize,
                alumniAssociationPage.getTotal()).setRecords(list);
        return PageVo.of(resultPage);
    }

    // 根据id获取校友会详情
    @Override
    public AlumniAssociationDetailVo getAlumniAssociationDetailVoById(Long id, Long wxId) {
        // 1.校验id
        if (id == null) {
            throw new BusinessException("参数不能为空,请重试");
        }

        // 2.查询数据库
        AlumniAssociation alumniAssociation = this.getById(id);

        // 3.返回校验值
        if (alumniAssociation == null) {
            throw new BusinessException("数据不存在,请重试");
        }

        AlumniAssociationDetailVo alumniAssociationDetailVo = AlumniAssociationDetailVo.objToVo(alumniAssociation);

        // 4. 构建其他详情
        // 4.1 构建母校信息
        Long schoolId = alumniAssociation.getSchoolId();
        SchoolListVo schoolListVo = SchoolListVo.objToVo(schoolMapper.selectById(schoolId));
        schoolListVo.setSchoolId(String.valueOf(schoolId));
        alumniAssociationDetailVo.setSchoolInfo(schoolListVo);
        // 4.2 构建校处会信息
        Long platformId = alumniAssociation.getPlatformId();
        if (platformId != null) {
            LocalPlatformDetailVo localPlatformDetailVo = localPlatformService.getLocalPlatformById(platformId);
            if (localPlatformDetailVo != null) {
                localPlatformDetailVo.setPlatformId(String.valueOf(platformId));
                alumniAssociationDetailVo.setPlatform(localPlatformDetailVo);
            }
        }
        // 4.3 构建会长信息 - 暂时不设置，需要用户服务模块
        // Long presidentUserId = alumniAssociation.getPresidentUserId();
        // alumniAssociationDetailVo.setPresident(userService.getUserDetailVoById(presidentUserId));

        // 4.4 查询当前用户是否已加入该校友会（基于成员表）
        if (wxId != null) {
            LambdaQueryWrapper<AlumniAssociationMember> memberQueryWrapper = new LambdaQueryWrapper<>();
            memberQueryWrapper
                    .eq(AlumniAssociationMember::getAlumniAssociationId, id)
                    .eq(AlumniAssociationMember::getWxId, wxId)
                    .eq(AlumniAssociationMember::getStatus, 1); // 状态：1-正常

            AlumniAssociationMember member = alumniAssociationMemberService.getOne(memberQueryWrapper);
            if (member != null) {
                // 用户在成员表中且状态正常，表示已加入
                alumniAssociationDetailVo.setApplicationStatus(1); // 1-已通过（已加入）
            } else {
                // 用户不在成员表中或状态不正常，表示未加入
                alumniAssociationDetailVo.setApplicationStatus(null); // null-未申请
            }
        }

        // 4.5 查询该校友会的活动列表
        LambdaQueryWrapper<Activity> activityQueryWrapper = new LambdaQueryWrapper<>();
        activityQueryWrapper
                .eq(Activity::getOrganizerType, 1) // 主办方类型：1-校友会
                .eq(Activity::getOrganizerId, id) // 主办方ID等于当前校友会ID
                .eq(Activity::getIsPublic, 1) // 是否公开：1-公开
                .eq(Activity::getReviewStatus, 1) // 审核状态：1-审核通过
                .orderByDesc(Activity::getCreateTime); // 按创建时间倒序

        List<Activity> activityList = activityService.list(activityQueryWrapper);
        List<ActivityListVo> activityListVos = activityList.stream()
                .map(ActivityListVo::objToVo)
                .collect(Collectors.toList());
        alumniAssociationDetailVo.setActivityList(activityListVos);

        // 4.6 查询该校友会的企业列表
        LambdaQueryWrapper<AlumniPlace> placeQueryWrapper = new LambdaQueryWrapper<>();
        placeQueryWrapper
                .eq(AlumniPlace::getPlaceType, 1) // 类型：1-企业
                .eq(AlumniPlace::getAlumniAssociationId, id) // 所属校友会ID等于当前校友会ID
                .isNotNull(AlumniPlace::getAlumniAssociationId) // alumni_association_id 不为空
                .eq(AlumniPlace::getReviewStatus, 1) // 审核状态：1-审核通过
                .eq(AlumniPlace::getIsRecommended, 1) // 是否推荐：1-是
                .in(AlumniPlace::getStatus, 1, 2) // 状态：1-正常运营 或 2-筹建中
                .orderByDesc(AlumniPlace::getCreateTime); // 按创建时间倒序

        List<AlumniPlace> placeList = alumniPlaceService.list(placeQueryWrapper);
        List<AlumniPlaceListVo> placeListVos = placeList.stream()
                .map(AlumniPlaceListVo::objToVo)
                .collect(Collectors.toList());
        alumniAssociationDetailVo.setEnterpriseList(placeListVos);

        // 4.7 查询该校友会的文章列表（最新6篇）
        LambdaQueryWrapper<HomePageArticle> articleQueryWrapper = new LambdaQueryWrapper<>();
        articleQueryWrapper
                .eq(HomePageArticle::getPublishType, "ASSOCIATION") // 发布者类型：校友会
                .eq(HomePageArticle::getPublishWxId, id) // 发布者ID等于当前校友会ID
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
        alumniAssociationDetailVo.setArticleList(articleListVos);

        // 4.8 查询该校友会的核心成员列表（在主页展示的成员）
        LambdaQueryWrapper<AlumniAssociationMember> coreMemberQueryWrapper = new LambdaQueryWrapper<>();
        coreMemberQueryWrapper
                .eq(AlumniAssociationMember::getAlumniAssociationId, id) // 校友会ID
                .eq(AlumniAssociationMember::getIsShowOnHome, 1) // 在主页展示
                .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
                .orderByAsc(AlumniAssociationMember::getId); // 按ID升序

        List<AlumniAssociationMember> coreMemberList = alumniAssociationMemberService.list(coreMemberQueryWrapper);

        // 收集所有有 wx_id 的成员ID，批量查询用户信息
        List<Long> wxIds = coreMemberList.stream()
                .map(AlumniAssociationMember::getWxId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, WxUserInfo> userInfoMap = new HashMap<>();
        if (!wxIds.isEmpty()) {
            List<WxUserInfo> userInfos = wxUserInfoService.list(
                    new LambdaQueryWrapper<WxUserInfo>().in(WxUserInfo::getWxId, wxIds));
            userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(WxUserInfo::getWxId, Function.identity(), (v1, v2) -> v1));
        }

        // 构建核心成员VO列表（排除已在「主要负责人」「主要联系人」中展示的成员，避免重复展示）
        Long chargeWxId = alumniAssociation.getChargeWxId();
        Long zhWxId = alumniAssociation.getZhWxId();
        Map<Long, WxUserInfo> finalUserInfoMap = userInfoMap;
        List<CoreMemberVo> coreMemberVoList = coreMemberList.stream()
                .filter(member -> {
                    Long memberWxId = member.getWxId();
                    if (memberWxId != null) {
                        // 已注册成员：按 wxId 排除负责人和驻会代表
                        return !memberWxId.equals(chargeWxId) && !memberWxId.equals(zhWxId);
                    }
                    // 预设成员（wxId 为 null）：按姓名排除与负责人、驻会代表相同的，避免重复展示
                    String memberName = member.getUsername();
                    if (chargeWxId == null && StringUtils.isNotBlank(alumniAssociation.getChargeName())
                            && Objects.equals(memberName, alumniAssociation.getChargeName())) {
                        return false; // 是预设的负责人，已在「主要负责人」展示，排除
                    }
                    if (zhWxId == null && StringUtils.isNotBlank(alumniAssociation.getZhName())
                            && Objects.equals(memberName, alumniAssociation.getZhName())) {
                        return false; // 是预设的驻会代表，已在「主要联系人」展示，排除
                    }
                    return true;
                })
                .map(member -> {
                    String username = member.getUsername();
                    String userPhone = member.getUserPhone();

                    // 如果 wx_id 不为空，使用用户信息表中的数据
                    if (member.getWxId() != null) {
                        WxUserInfo userInfo = finalUserInfoMap.get(member.getWxId());
                        if (userInfo != null) {
                            if (userInfo.getName() != null) {
                                username = userInfo.getName();
                            }
                            if (userInfo.getPhone() != null) {
                                userPhone = userInfo.getPhone();
                            }
                        }
                    }

                    return CoreMemberVo.builder()
                            .wxId(member.getWxId())
                            .roleName(member.getRoleName())
                            .username(username)
                            .userPhone(userPhone)
                            .userAffiliation(member.getUserAffiliation())
                            .build();
                })
                .collect(Collectors.toList());
        alumniAssociationDetailVo.setCoreMemberList(coreMemberVoList);

        log.info("根据id查询校友会信息 id:{}, wxId:{}, 活动数量:{}, 企业数量:{}, 文章数量:{}, 核心成员数量:{}",
                id, wxId, activityListVos.size(), placeListVos.size(), articleListVos.size(), coreMemberVoList.size());

        return alumniAssociationDetailVo;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertAlumniAssociation(AddAlumniAssociationDto addAlumniAssociationDto) {
        Optional.ofNullable(addAlumniAssociationDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        AlumniAssociation alumniAssociation = AddAlumniAssociationDto.dtoToObject(addAlumniAssociationDto);

        boolean saveResult = this.save(alumniAssociation);

        // 新增会长
        alumniAssociationMemberService.insertAlumniAssociationMember(addAlumniAssociationDto.getPresidentUserId(),
                alumniAssociation.getAlumniAssociationId());

        log.info("新增校友会：{}", addAlumniAssociationDto.getAssociationName());

        return saveResult;
    }

    @Override
    public boolean updateAlumniAssociation(UpdateAlumniAssociationDto updateAlumniAssociationDto) {
        // 1. 参数校验
        Optional.ofNullable(updateAlumniAssociationDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Long alumniAssociationId = updateAlumniAssociationDto.getAlumniAssociationId();
        Optional.ofNullable(alumniAssociationId)
                .orElseThrow(() -> new BusinessException("校友会ID不能为空"));

        // 2. 检查校友会是否存在
        AlumniAssociation existingAssociation = this.getById(alumniAssociationId);
        if (existingAssociation == null) {
            throw new BusinessException("校友会不存在");
        }

        // 3. 更新字段（只更新非空字段）
        if (updateAlumniAssociationDto.getContactInfo() != null) {
            existingAssociation.setContactInfo(updateAlumniAssociationDto.getContactInfo());
        }
        if (updateAlumniAssociationDto.getLocation() != null) {
            existingAssociation.setLocation(updateAlumniAssociationDto.getLocation());
        }
        if (updateAlumniAssociationDto.getLogo() != null) {
            existingAssociation.setLogo(updateAlumniAssociationDto.getLogo());
        }
        if (updateAlumniAssociationDto.getBgImg() != null) {
            existingAssociation.setBgImg(updateAlumniAssociationDto.getBgImg());
        }
        if (updateAlumniAssociationDto.getAssociationProfile() != null) {
            existingAssociation.setAssociationProfile(updateAlumniAssociationDto.getAssociationProfile());
        }
        // chargeWxId 支持传 null 来清空（使用 ALWAYS 更新策略）
        existingAssociation.setChargeWxId(updateAlumniAssociationDto.getChargeWxId());
        if (updateAlumniAssociationDto.getChargeName() != null) {
            existingAssociation.setChargeName(updateAlumniAssociationDto.getChargeName());
        }
        if (updateAlumniAssociationDto.getChargeRole() != null) {
            existingAssociation.setChargeRole(updateAlumniAssociationDto.getChargeRole());
        }
        if (updateAlumniAssociationDto.getChargeSocialAffiliation() != null) {
            existingAssociation.setChargeSocialAffiliation(updateAlumniAssociationDto.getChargeSocialAffiliation());
        }
        // zhWxId 支持传 null 来清空（使用 ALWAYS 更新策略）
        existingAssociation.setZhWxId(updateAlumniAssociationDto.getZhWxId());
        if (updateAlumniAssociationDto.getZhName() != null) {
            existingAssociation.setZhName(updateAlumniAssociationDto.getZhName());
        }
        if (updateAlumniAssociationDto.getZhPhone() != null) {
            existingAssociation.setZhPhone(updateAlumniAssociationDto.getZhPhone());
        }
        if (updateAlumniAssociationDto.getZhRole() != null) {
            existingAssociation.setZhRole(updateAlumniAssociationDto.getZhRole());
        }
        if (updateAlumniAssociationDto.getZhSocialAffiliation() != null) {
            existingAssociation.setZhSocialAffiliation(updateAlumniAssociationDto.getZhSocialAffiliation());
        }

        // 4. 执行更新
        boolean updateResult = this.updateById(existingAssociation);

        if (updateResult) {
            log.info("更新校友会成功，校友会 ID: {}, 名称: {}", alumniAssociationId, existingAssociation.getAssociationName());
        } else {
            log.error("更新校友会失败，校友会 ID: {}", alumniAssociationId);
        }

        return updateResult;
    }

    @Override
    public Page<OrganizationMemberResponse> getAlumniAssociationMemberPage(
            QueryAlumniAssociationMemberListRequest queryAlumniAssociationMemberListRequest,
            Long currentUserId) {
        // 1. 参数校验
        Optional.ofNullable(queryAlumniAssociationMemberListRequest)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        // 2. 提取查询参数
        Long alumniAssociationId = queryAlumniAssociationMemberListRequest.getAlumniAssociationId();
        String keyword = queryAlumniAssociationMemberListRequest.getKeyword(); // 统一搜索关键词
        String nickname = queryAlumniAssociationMemberListRequest.getNickname();
        String name = queryAlumniAssociationMemberListRequest.getName();
        String phone = queryAlumniAssociationMemberListRequest.getPhone();
        String wxNum = queryAlumniAssociationMemberListRequest.getWxNum();
        String qqNum = queryAlumniAssociationMemberListRequest.getQqNum();
        String email = queryAlumniAssociationMemberListRequest.getEmail();
        String curContinent = queryAlumniAssociationMemberListRequest.getCurContinent();
        String curCountry = queryAlumniAssociationMemberListRequest.getCurCountry();
        String curProvince = queryAlumniAssociationMemberListRequest.getCurProvince();
        String curCity = queryAlumniAssociationMemberListRequest.getCurCity();
        Integer constellation = queryAlumniAssociationMemberListRequest.getConstellation();
        String signature = queryAlumniAssociationMemberListRequest.getSignature();
        Integer gender = queryAlumniAssociationMemberListRequest.getGender();
        String identifyCode = queryAlumniAssociationMemberListRequest.getIdentifyCode();
        LocalDate birthDate = queryAlumniAssociationMemberListRequest.getBirthDate();
        int current = queryAlumniAssociationMemberListRequest.getCurrent();
        int pageSize = queryAlumniAssociationMemberListRequest.getPageSize();

        // 3. 先对成员表分页查询
        Page<AlumniAssociationMember> memberPage = new Page<>(current, pageSize);
        Page<AlumniAssociationMember> memberResultPage = alumniAssociationMemberService.page(
                memberPage,
                new LambdaQueryWrapper<AlumniAssociationMember>()
                        .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                        .orderByDesc(AlumniAssociationMember::getJoinTime));

        // 4. 如果成员列表为空，直接返回空分页结果
        if (memberResultPage.getRecords().isEmpty()) {
            Page<OrganizationMemberResponse> emptyPage = new Page<>(current, pageSize);
            emptyPage.setTotal(0);
            return emptyPage;
        }

        // 5. 提取所有非空的 wxId
        List<Long> wxIds = memberResultPage.getRecords().stream()
                .map(AlumniAssociationMember::getWxId)
                .filter(Objects::nonNull) // 过滤掉 null 的 wxId
                .distinct()
                .collect(Collectors.toList());

        // 6. 批量查询用户信息（一次查询，避免 N+1 问题）
        final Map<Long, WxUserInfo> userInfoMap;
        if (!wxIds.isEmpty()) {
            LambdaQueryWrapper<WxUserInfo> queryWrapper = new LambdaQueryWrapper<WxUserInfo>()
                    .in(WxUserInfo::getWxId, wxIds);

            // 如果提供了 keyword，使用 OR 条件匹配姓名或昵称
            if (StringUtils.isNotBlank(keyword)) {
                queryWrapper.and(wrapper -> wrapper
                        .like(WxUserInfo::getName, keyword)
                        .or()
                        .like(WxUserInfo::getNickname, keyword));
            }

            // 其他独立的过滤条件（AND 关系）
            queryWrapper.like(StringUtils.isNotBlank(nickname), WxUserInfo::getNickname, nickname)
                    .like(StringUtils.isNotBlank(name), WxUserInfo::getName, name)
                    .like(StringUtils.isNotBlank(phone), WxUserInfo::getPhone, phone)
                    .like(StringUtils.isNotBlank(wxNum), WxUserInfo::getWxNum, wxNum)
                    .like(StringUtils.isNotBlank(qqNum), WxUserInfo::getQqNum, qqNum)
                    .like(StringUtils.isNotBlank(email), WxUserInfo::getEmail, email)
                    .like(StringUtils.isNotBlank(curContinent), WxUserInfo::getCurContinent, curContinent)
                    .like(StringUtils.isNotBlank(curCountry), WxUserInfo::getCurCountry, curCountry)
                    .like(StringUtils.isNotBlank(curProvince), WxUserInfo::getCurProvince, curProvince)
                    .like(StringUtils.isNotBlank(curCity), WxUserInfo::getCurCity, curCity)
                    .like(StringUtils.isNotBlank(signature), WxUserInfo::getSignature, signature)
                    .like(StringUtils.isNotBlank(identifyCode), WxUserInfo::getIdentifyCode, identifyCode)
                    .eq(constellation != null, WxUserInfo::getConstellation, constellation)
                    .eq(gender != null, WxUserInfo::getGender, gender)
                    .eq(birthDate != null, WxUserInfo::getBirthDate, birthDate);

            List<WxUserInfo> userInfoList = wxUserInfoService.list(queryWrapper);

            // 7. 转成 Map，方便查找（key: wxId, value: WxUserInfo）
            userInfoMap = userInfoList.stream()
                    .collect(Collectors.toMap(WxUserInfo::getWxId, Function.identity(), (v1, v2) -> v1));
        } else {
            userInfoMap = new HashMap<>();
        }

        // 8. 提取所有 roleOrId 并批量查询组织架构角色信息
        List<Long> roleOrIds = memberResultPage.getRecords().stream()
                .map(AlumniAssociationMember::getRoleOrId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, OrganizeArchiRoleVo> organizeArchiRoleMap = new HashMap<>();
        if (!roleOrIds.isEmpty()) {
            organizeArchiRoleMap = organizeArchiRoleService.listByIds(roleOrIds).stream()
                    .map(OrganizeArchiRoleVo::objToVo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            role -> Long.valueOf(role.getRoleOrId()),
                            Function.identity(),
                            (v1, v2) -> v1));
        }

        // 9. 如果当前用户已登录，批量查询关注关系
        Map<Long, Boolean> followStatusMap = new HashMap<>();
        if (currentUserId != null && !wxIds.isEmpty()) {
            List<UserFollow> followList = userFollowService.list(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getWxId, currentUserId)
                            .eq(UserFollow::getTargetType, 1) // 1-用户
                            .in(UserFollow::getTargetId, wxIds)
                            .in(UserFollow::getFollowStatus, 1, 2, 3) // 正常关注、特别关注、免打扰（排除已取消）
            );
            followStatusMap = followList.stream()
                    .collect(Collectors.toMap(
                            UserFollow::getTargetId,
                            follow -> true,
                            (v1, v2) -> v1));
        }

        // 10. 组装结果（包括预设成员）
        Map<Long, OrganizeArchiRoleVo> finalOrganizeArchiRoleMap = organizeArchiRoleMap;
        Map<Long, Boolean> finalFollowStatusMap = followStatusMap;
        final String finalKeyword = keyword; // 用于 lambda 表达式中访问
        List<OrganizationMemberResponse> responses = memberResultPage.getRecords().stream()
                .map(member -> {
                    OrganizationMemberResponse response = new OrganizationMemberResponse();

                    // 判断是否已加入平台（是否有 wxId）
                    boolean isJoined = member.getWxId() != null && member.getWxId() != 0L;
                    response.setJoined(isJoined);

                    if (isJoined) {
                        // 已加入平台：从 wx_user_info 获取详细信息
                        WxUserInfo userInfo = userInfoMap.get(member.getWxId());
                        if (userInfo != null) {
                            // 应用用户信息过滤条件
                            // 注意：keyword、nickname、name 已经在数据库查询时过滤，这里不需要再次过滤
                            boolean matchFilter = true;
                            if (matchFilter && StringUtils.isNotBlank(phone) && (userInfo.getPhone() == null || !userInfo.getPhone().contains(phone))) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(wxNum) && (userInfo.getWxNum() == null || !userInfo.getWxNum().contains(wxNum))) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(qqNum) && (userInfo.getQqNum() == null || !userInfo.getQqNum().contains(qqNum))) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(email) && (userInfo.getEmail() == null || !userInfo.getEmail().contains(email))) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(curContinent) && (userInfo.getCurContinent() == null || !userInfo.getCurContinent().contains(curContinent))) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(curCountry) && (userInfo.getCurCountry() == null || !userInfo.getCurCountry().contains(curCountry))) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(curProvince) && (userInfo.getCurProvince() == null || !userInfo.getCurProvince().contains(curProvince))) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(curCity) && (userInfo.getCurCity() == null || !userInfo.getCurCity().contains(curCity))) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(signature) && (userInfo.getSignature() == null || !userInfo.getSignature().contains(signature))) {
                                matchFilter = false;
                            }
                            if (matchFilter && StringUtils.isNotBlank(identifyCode) && (userInfo.getIdentifyCode() == null || !userInfo.getIdentifyCode().contains(identifyCode))) {
                                matchFilter = false;
                            }
                            if (matchFilter && constellation != null && !constellation.equals(userInfo.getConstellation())) {
                                matchFilter = false;
                            }
                            if (matchFilter && gender != null && !gender.equals(userInfo.getGender())) {
                                matchFilter = false;
                            }
                            if (matchFilter && birthDate != null && !birthDate.equals(userInfo.getBirthDate())) {
                                matchFilter = false;
                            }

                            if (!matchFilter) {
                                return null; // 不符合过滤条件，跳过
                            }

                            // 填充用户详细信息
                            response = OrganizationMemberResponse.objToVo(userInfo);
                            response.setJoined(true);
                            response.setWxId(String.valueOf(member.getWxId()));

                            // 设置成员表的 ID（用于更新成员信息）
                            response.setId(member.getId());

                            // 角色名称来自 alumni_association_member 表的 role_name
                            response.setRoleName(member.getRoleName());

                            // 联系方式展示 alumni_association_member 表的 user_phone
                            response.setContactInformation(member.getUserPhone());

                            // 设置关注状态
                            if (currentUserId != null) {
                                response.setIsFollowed(finalFollowStatusMap.getOrDefault(member.getWxId(), false));
                            } else {
                                response.setIsFollowed(null); // 未登录
                            }

                            // 设置是否展示在主页
                            response.setIsShowOnHome(member.getIsShowOnHome());
                        } else {
                            // 有 wxId 但在 wx_user_info 中无记录（如负责人未注册/未完善信息），使用成员表数据展示，避免在成员列表中丢失
                            if (StringUtils.isNotBlank(finalKeyword)) {
                                String username = member.getUsername();
                                if (username == null || !username.contains(finalKeyword)) {
                                    return null;
                                }
                            }
                            response.setId(member.getId());
                            response.setWxId(String.valueOf(member.getWxId()));
                            response.setUsername(member.getUsername());
                            response.setName(member.getUsername() != null ? member.getUsername() : "未知用户");
                            response.setNickname(member.getUsername());
                            response.setRoleName(member.getRoleName());
                            response.setContactInformation(member.getUserPhone());
                            response.setSocialDuties(member.getUserAffiliation());
                            response.setIsShowOnHome(member.getIsShowOnHome());
                            response.setJoined(true);
                            response.setIsFollowed(currentUserId != null ? finalFollowStatusMap.getOrDefault(member.getWxId(), false) : null);
                        }
                    } else {
                        // 未加入平台的预设成员：从成员表获取基本信息

                        // 应用 keyword 过滤：匹配 username
                        if (StringUtils.isNotBlank(finalKeyword)) {
                            String username = member.getUsername();
                            if (username == null || !username.contains(finalKeyword)) {
                                return null; // 不符合搜索条件，跳过
                            }
                        }

                        response.setId(member.getId());
                        response.setUsername(member.getUsername());
                        response.setName(member.getUsername()); // 使用 username 作为姓名
                        response.setNickname(member.getUsername()); // 使用 username 作为昵称
                        response.setRoleName(member.getRoleName());
                        response.setContactInformation(member.getUserPhone()); // 联系方式
                        response.setSocialDuties(member.getUserAffiliation()); // 社会职务
                        response.setIsShowOnHome(member.getIsShowOnHome()); // 是否展示在主页
                        response.setWxId(null); // 预设成员没有 wxId
                        response.setIsFollowed(null); // 预设成员不支持关注
                    }

                    // 设置组织架构角色信息（已加入和未加入的都设置）
                    if (member.getRoleOrId() != null) {
                        OrganizeArchiRoleVo organizeArchiRoleVo = finalOrganizeArchiRoleMap.get(member.getRoleOrId());
                        if (organizeArchiRoleVo != null) {
                            response.setOrganizeArchiRole(organizeArchiRoleVo);
                        }
                    }

                    return response;
                })
                .filter(Objects::nonNull) // 过滤掉不符合条件的记录
                .collect(Collectors.toList());

        // 11. 构建分页结果
        Page<OrganizationMemberResponse> resultPage = new Page<>(current, pageSize);
        resultPage.setRecords(responses);
        resultPage.setTotal(memberResultPage.getTotal());

        log.info("查询校友会成员列表成功 - 校友会ID: {}, 当前用户ID: {}, 总记录数: {}, 当前页: {}, 每页大小: {}",
                alumniAssociationId, currentUserId, resultPage.getTotal(), current, pageSize);

        return resultPage;
    }

    @Override
    public PageVo<AlumniAssociationListVo> getMyPresidentAssociationPage(QueryAlumniAssociationListDto queryDto,
            Long wxId) {
        // 1. 参数校验
        Optional.ofNullable(queryDto).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));
        if (wxId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 2. 常量定义
        final Long SUPER_ADMIN_ROLE_ID = 2002944992284250113L; // 系统管理员
        final Long PRESIDENT_ROLE_ID = 2002944405488537602L; // 校友会会长

        // 3. 检查用户是否是超级管理员
        List<RoleUser> roleUserList = roleUserService.getSystemRoleUserListByWxIdInner(wxId);
        boolean isSuperAdmin = roleUserList.stream()
                .anyMatch(roleUser -> SUPER_ADMIN_ROLE_ID.equals(roleUser.getRoleId()));

        // 4. 如果是超级管理员，返回所有校友会列表
        if (isSuperAdmin) {
            log.info("用户 {} 是超级管理员，返回所有校友会列表", wxId);
            return selectByPage(queryDto, wxId);
        }

        // 5. 不是超级管理员，查询该用户是会长的校友会
        List<AlumniAssociationMember> memberList = alumniAssociationMemberService.list(
                new LambdaQueryWrapper<AlumniAssociationMember>()
                        .eq(AlumniAssociationMember::getWxId, wxId)
                        .eq(AlumniAssociationMember::getRoleOrId, PRESIDENT_ROLE_ID)
                        .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
        );

        // 6. 如果该用户不是任何校友会的会长，返回空列表
        if (memberList.isEmpty()) {
            log.info("用户 {} 不是任何校友会的会长", wxId);
            Page<AlumniAssociationListVo> emptyPage = new Page<>(queryDto.getCurrent(), queryDto.getPageSize());
            emptyPage.setTotal(0);
            return PageVo.of(emptyPage);
        }

        // 7. 提取校友会ID列表
        List<Long> alumniAssociationIds = memberList.stream()
                .map(AlumniAssociationMember::getAlumniAssociationId)
                .distinct()
                .collect(Collectors.toList());

        // 8. 获取查询参数
        String associationName = queryDto.getAssociationName();
        String contactInfo = queryDto.getContactInfo();
        String location = queryDto.getLocation();
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();
        String sortField = queryDto.getSortField();
        String sortOrder = queryDto.getSortOrder();

        // 9. 构建查询条件 - 只查询该用户是会长的校友会
        LambdaQueryWrapper<AlumniAssociation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .in(AlumniAssociation::getAlumniAssociationId, alumniAssociationIds)
                .like(StringUtils.isNotBlank(associationName), AlumniAssociation::getAssociationName, associationName)
                .like(StringUtils.isNotBlank(contactInfo), AlumniAssociation::getContactInfo, contactInfo)
                .like(StringUtils.isNotBlank(location), AlumniAssociation::getLocation, location)
                .orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                        AlumniAssociation.getSortMethod(sortField));

        // 10. 执行分页查询
        Page<AlumniAssociation> alumniAssociationPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 11. 转换结果，使用 @JsonSerialize 注解避免前端精度丢失
        List<AlumniAssociationListVo> list = alumniAssociationPage.getRecords().stream()
                .map(AlumniAssociationListVo::objToVo)
                .toList();

        log.info("查询用户 {} 作为会长的校友会列表成功，总数: {}", wxId, list.size());

        // 12. 返回结果
        Page<AlumniAssociationListVo> resultPage = new Page<AlumniAssociationListVo>(current, pageSize,
                alumniAssociationPage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMember(Long alumniAssociationId, Long id, Long wxId) {
        // 1. 参数校验
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }
        if (id == null && wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员记录ID和成员用户ID不能同时为空");
        }

        log.info("开始删除校友会成员 - 校友会ID: {}, 成员记录ID: {}, 成员用户ID: {}", alumniAssociationId, id, wxId);

        // 2. 查询校友会是否存在
        AlumniAssociation alumniAssociation = this.getById(alumniAssociationId);
        if (alumniAssociation == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
        }

        // 3. 查询成员记录是否存在
        AlumniAssociationMember member = null;

        if (id != null) {
            // 通过成员记录ID查询（用于删除未注册成员）
            member = alumniAssociationMemberService.getOne(
                    new LambdaQueryWrapper<AlumniAssociationMember>()
                            .eq(AlumniAssociationMember::getId, id)
                            .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                            .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
            );
        } else {
            // 通过用户ID查询（用于删除已注册成员）
            member = alumniAssociationMemberService.getOne(
                    new LambdaQueryWrapper<AlumniAssociationMember>()
                            .eq(AlumniAssociationMember::getWxId, wxId)
                            .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                            .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
            );
        }

        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该成员不存在或已被删除");
        }

        // 4. 删除成员记录（逻辑删除）
        boolean deleteResult = alumniAssociationMemberService.removeById(member.getId());
        if (!deleteResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "删除成员失败");
        }

        // 5. 更新校友会成员数量（-1）
        this.updateMemberCount(alumniAssociationId, -1);

        // 6. 检查用户是否还加入了其他校友会，如果没有则更新 isAlumni 为 0
        updateUserAlumniStatus(wxId);

        log.info("删除校友会成员成功 - 校友会ID: {}, 成员用户ID: {}, 当前成员数: {}",
                alumniAssociationId, wxId, alumniAssociation.getMemberCount());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean inviteMember(Long alumniAssociationId, Long wxId, Long roleOrId) {
        // 1. 参数校验
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友用户ID不能为空");
        }

        log.info("开始发送校友会邀请通知 - 校友会ID: {}, 校友用户ID: {}, 角色ID: {}",
                alumniAssociationId, wxId, roleOrId);

        // 2. 查询校友会是否存在
        AlumniAssociation alumniAssociation = this.getById(alumniAssociationId);
        if (alumniAssociation == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
        }

        // 3. 查询用户是否存在
        WxUser wxUser = userService.getById(wxId);
        if (wxUser == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "用户不存在");
        }

        // 4. 检查用户是否已经是该校友会成员
        AlumniAssociationMember existingMember = alumniAssociationMemberService.getOne(
                new LambdaQueryWrapper<AlumniAssociationMember>()
                        .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                        .eq(AlumniAssociationMember::getWxId, wxId)
                        .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
        );

        if (existingMember != null) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该用户已经是该校友会成员");
        }

        // 5. 检查是否已有待处理的邀请
        AlumniAssociationInvitation existingInvitation = alumniAssociationInvitationMapper.selectOne(
                new LambdaQueryWrapper<AlumniAssociationInvitation>()
                        .eq(AlumniAssociationInvitation::getAlumniAssociationId, alumniAssociationId)
                        .eq(AlumniAssociationInvitation::getInviteeId, wxId)
                        .eq(AlumniAssociationInvitation::getStatus, 0) // 0-待处理
        );

        if (existingInvitation != null) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该用户已有待处理的邀请，请勿重复发送");
        }

        // 6. 构建邀请通知消息内容
        String invitationContent = String.format("%s 邀请您加入", alumniAssociation.getAssociationName());

        // 7. 创建邀请记录（先创建记录，用于在action_data中存储invitationId）
        AlumniAssociationInvitation invitation = new AlumniAssociationInvitation();
        invitation.setAlumniAssociationId(alumniAssociationId);
        invitation.setInviterId(0L); // 暂时设为0，实际应该从SecurityContext获取
        invitation.setInviteeId(wxId);
        invitation.setRoleOrId(roleOrId);
        invitation.setNotificationId(null); // 暂时为空，前端会传递通知ID
        invitation.setStatus(0); // 0-待处理
        alumniAssociationInvitationMapper.insert(invitation);
        Long invitationId = invitation.getId();

        log.info("已创建邀请记录 - 邀请ID: {}, 校友会ID: {}, 被邀请人ID: {}",
                invitationId, alumniAssociationId, wxId);

        log.info("校友会邀请记录创建成功 - 校友会ID: {}, 被邀请人ID: {}, 邀请ID: {}",
                alumniAssociationId, wxId, invitationId);

        // 8. 异步发送邀请通知（避免阻塞主线程）
        // 使用 CompletableFuture 异步执行
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // 发送邀请通知给被邀请人
                unifiedMessageApiService.sendBusinessNotification(
                        wxId,
                        "ASSOCIATION_INVITATION",
                        "校友会邀请",
                        invitationContent,
                        invitationId, // 使用 invitationId 作为 relatedId
                        "INVITATION"
                );

                // 发送确认通知给管理员
                unifiedMessageApiService.sendSystemNotification(
                        0L, // 应该发给当前操作的管理员，暂时用0
                        com.cmswe.alumni.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                        "邀请通知已发送",
                        "您的邀请通知已成功发送",
                        alumniAssociationId,
                        "ASSOCIATION"
                );

                log.info("校友会邀请通知异步发送成功 - 校友会ID: {}, 被邀请人ID: {}, 邀请ID: {}",
                        alumniAssociationId, wxId, invitationId);
            } catch (Exception e) {
                log.error("校友会邀请通知异步发送失败 - 校友会ID: {}, 被邀请人ID: {}, 错误: {}",
                        alumniAssociationId, wxId, e.getMessage(), e);
            }
        });

        return true;
    }

    @Override
    public List<OrganizationTreeVo> getOrganizationTree(Long alumniAssociationId) {
        // 1. 参数校验
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }

        log.info("开始查询组织架构树 - 校友会ID: {}", alumniAssociationId);

        // 2. 查询该校友会的所有组织架构角色
        List<OrganizeArchiRole> allRoles = organizeArchiRoleService.list(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getOrganizeId, alumniAssociationId)
                        .eq(OrganizeArchiRole::getOrganizeType, 0) // 0-校友会
                        .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
                        .orderByAsc(OrganizeArchiRole::getRoleOrId));

        if (allRoles.isEmpty()) {
            log.info("该校友会暂无组织架构 - 校友会ID: {}", alumniAssociationId);
            return new ArrayList<>();
        }

        // 3. 查询该校友会的所有成员
        List<AlumniAssociationMember> allMembers = alumniAssociationMemberService.list(
                new LambdaQueryWrapper<AlumniAssociationMember>()
                        .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                        .eq(AlumniAssociationMember::getStatus, 1) // 1-正常
        );

        // 4. 提取所有成员的wxId并批量查询用户信息
        Map<Long, WxUserInfo> userInfoMap = new HashMap<>();
        if (!allMembers.isEmpty()) {
            List<Long> wxIds = allMembers.stream()
                    .map(AlumniAssociationMember::getWxId)
                    .distinct()
                    .collect(Collectors.toList());

            List<WxUserInfo> userInfoList = wxUserInfoService.list(
                    new LambdaQueryWrapper<WxUserInfo>()
                            .in(WxUserInfo::getWxId, wxIds));
            userInfoMap = userInfoList.stream()
                    .collect(Collectors.toMap(WxUserInfo::getWxId, Function.identity(), (v1, v2) -> v1));
        }

        // 5. 按角色ID分组成员（过滤掉 roleOrId 为 null 的成员）
        Map<Long, List<AlumniAssociationMember>> membersByRole = allMembers.stream()
                .filter(member -> member.getRoleOrId() != null) // 过滤掉没有分配角色的成员
                .collect(Collectors.groupingBy(AlumniAssociationMember::getRoleOrId));

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
                    .sort(role.getSort())
                    .children(new ArrayList<>())
                    .members(new ArrayList<>())
                    .build();

            // 为该角色添加成员信息
            List<AlumniAssociationMember> roleMembers = membersByRole.getOrDefault(role.getRoleOrId(),
                    new ArrayList<>());
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

        // 8. 按 sort 同级别排序（数值越小越靠前）
        sortTreeBySort(rootNodes);

        log.info("查询组织架构树成功 - 校友会ID: {}, 根节点数: {}, 总角色数: {}",
                alumniAssociationId, rootNodes.size(), roleNodeMap.size());

        return rootNodes;
    }

    /**
     * 按 sort 字段对树节点进行同级别排序（数值越小越靠前，null 视为最大值排最后）
     */
    private void sortTreeBySort(List<OrganizationTreeVo> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(Comparator.comparing(OrganizationTreeVo::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        for (OrganizationTreeVo node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTreeBySort(node.getChildren());
            }
        }
    }

    @Override
    public List<OrganizationTreeV2Vo> getOrganizationTreeV2(Long alumniAssociationId) {
        // 1. 参数校验
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }

        log.info("开始查询组织架构树V2 - 校友会ID: {}", alumniAssociationId);

        // 2. 查询该校友会的所有组织架构角色（按 sort 升序，同级别排序）
        List<OrganizeArchiRole> allRoles = organizeArchiRoleService.list(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getOrganizeId, alumniAssociationId)
                        .eq(OrganizeArchiRole::getOrganizeType, 0) // 0-校友会
                        .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
                        .orderByAsc(OrganizeArchiRole::getSort)
                        .orderByAsc(OrganizeArchiRole::getRoleOrId));

        if (allRoles.isEmpty()) {
            log.info("该校友会暂无组织架构 - 校友会ID: {}", alumniAssociationId);
            return new ArrayList<>();
        }

        // 3. 查询该校友会的所有成员
        List<AlumniAssociationMember> allMembers = alumniAssociationMemberService.list(
                new LambdaQueryWrapper<AlumniAssociationMember>()
                        .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                        .eq(AlumniAssociationMember::getStatus, 1) // 1-正常
        );

        // 4. 提取所有非空wxId的成员并批量查询用户信息
        Map<Long, WxUserInfo> userInfoMap = new HashMap<>();
        if (!allMembers.isEmpty()) {
            List<Long> wxIds = allMembers.stream()
                    .map(AlumniAssociationMember::getWxId)
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

        // 5. 按角色ID分组成员（过滤掉 roleOrId 为 null 的成员）
        Map<Long, List<AlumniAssociationMember>> membersByRole = allMembers.stream()
                .filter(member -> member.getRoleOrId() != null) // 过滤掉没有分配角色的成员
                .collect(Collectors.groupingBy(AlumniAssociationMember::getRoleOrId));

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
                    .sort(role.getSort())
                    .children(new ArrayList<>())
                    .members(new ArrayList<>())
                    .build();

            // 为该角色添加成员信息
            List<AlumniAssociationMember> roleMembers = membersByRole.getOrDefault(role.getRoleOrId(),
                    new ArrayList<>());
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

        // 8. 按 sort 同级别排序（数值越小越靠前）
        sortTreeBySortV2(rootNodes);

        log.info("查询组织架构树V2成功 - 校友会ID: {}, 根节点数: {}, 总角色数: {}",
                alumniAssociationId, rootNodes.size(), roleNodeMap.size());

        return rootNodes;
    }

    /**
     * 按 sort 字段对树节点进行同级别排序（数值越小越靠前，null 视为最大值排最后）
     */
    private void sortTreeBySortV2(List<OrganizationTreeV2Vo> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(Comparator.comparing(OrganizationTreeV2Vo::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        for (OrganizationTreeV2Vo node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTreeBySortV2(node.getChildren());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberRole(Long operatorWxId, Long alumniAssociationId, Long wxId, Long roleOrId, String roleName) {
        // 1. 参数校验
        if (operatorWxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "操作人用户ID不能为空");
        }
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员用户ID不能为空");
        }
        if (roleOrId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "组织架构角色ID不能为空");
        }

        log.info("开始更新校友会成员角色 - 操作人ID: {}, 校友会ID: {}, 成员用户ID: {}, 新角色ID: {}, 角色名称: {}",
                operatorWxId, alumniAssociationId, wxId, roleOrId, roleName);

        // 2. 查询校友会是否存在
        AlumniAssociation alumniAssociation = this.getById(alumniAssociationId);
        if (alumniAssociation == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
        }

        // 3. 查询成员记录是否存在
        AlumniAssociationMember member = alumniAssociationMemberService.getOne(
                new LambdaQueryWrapper<AlumniAssociationMember>()
                        .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                        .eq(AlumniAssociationMember::getWxId, wxId)
                        .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
        );

        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该用户不是该校友会的成员");
        }

        // 4. 查询新的组织架构角色是否存在且有效
        OrganizeArchiRole organizeArchiRole = organizeArchiRoleService.getOne(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getRoleOrId, roleOrId)
                        .eq(OrganizeArchiRole::getOrganizeId, alumniAssociationId)
                        .eq(OrganizeArchiRole::getOrganizeType, 0) // 0-校友会
                        .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
        );

        if (organizeArchiRole == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该组织架构角色不存在或未启用");
        }

        // 5. 更新成员角色和角色名称
        Long oldRoleOrId = member.getRoleOrId();
        member.setRoleOrId(roleOrId);
        if (roleName != null && !roleName.trim().isEmpty()) {
            member.setRoleName(roleName);
        }
        boolean updateResult = alumniAssociationMemberService.updateById(member);

        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新成员角色失败");
        }

        log.info("更新校友会成员角色成功 - 校友会ID: {}, 成员用户ID: {}, 原角色ID: {}, 新角色ID: {}, 角色名称: {}",
                alumniAssociationId, wxId, oldRoleOrId, roleOrId, roleName);

        // 6. 发送角色更新通知
        sendRoleUpdateNotification(operatorWxId, wxId, alumniAssociationId,
                alumniAssociation.getAssociationName(), organizeArchiRole.getRoleOrName());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberRoleV2(Long operatorWxId, Long alumniAssociationId, Long id, String username,
            Long roleOrId, String roleName) {
        // 1. 参数校验
        if (operatorWxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "操作人用户ID不能为空");
        }
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员用户名不能为空");
        }
        if (roleOrId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "组织架构角色ID不能为空");
        }

        log.info("开始处理校友会成员角色V2 - 操作人ID: {}, 校友会ID: {}, 成员ID: {}, 成员用户名: {}, 新角色ID: {}, 角色名称: {}",
                operatorWxId, alumniAssociationId, id, username, roleOrId, roleName);

        // 2. 查询校友会是否存在
        AlumniAssociation alumniAssociation = this.getById(alumniAssociationId);
        if (alumniAssociation == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
        }

        // 3. 查询或者创建成员记录
        AlumniAssociationMember member = null;
        if (id != null) {
            // 情况A：提供了 ID，按 ID 查询
            member = alumniAssociationMemberService.getById(id);
            if (member == null) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该成员记录不存在");
            }
        } else {
            // 情况B：未提供 ID，先按用户名查询，防止重复
            member = alumniAssociationMemberService.getOne(
                    new LambdaQueryWrapper<AlumniAssociationMember>()
                            .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                            .eq(AlumniAssociationMember::getUsername, username)
                            .last("LIMIT 1"));
        }

        if (member == null) {
            // 情况C：完全找不到记录，新增一个（架构成员）
            member = new AlumniAssociationMember();
            member.setAlumniAssociationId(alumniAssociationId);
            member.setUsername(username);
            member.setJoinTime(LocalDateTime.now());
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
                        .eq(OrganizeArchiRole::getOrganizeId, alumniAssociationId)
                        .eq(OrganizeArchiRole::getOrganizeType, 0) // 0-校友会
                        .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
        );

        if (organizeArchiRole == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该组织架构角色不存在或未启用");
        }

        // 5. 保存或更新成员
        boolean saveOrUpdateResult = alumniAssociationMemberService.saveOrUpdate(member);

        if (!saveOrUpdateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "保存成员角色失败");
        }

        log.info("处理校友会成员角色V2成功 - 校友会ID: {}, 成员记录ID: {}, 成员用户名: {}, 新角色ID: {}, 角色名称: {}",
                alumniAssociationId, member.getId(), username, roleOrId, roleName);

        // 6. 发送角色更新通知（仅当 wxId 不为空时发送）
        if (member.getWxId() != null) {
            sendRoleUpdateNotification(operatorWxId, member.getWxId(), alumniAssociationId,
                    alumniAssociation.getAssociationName(), organizeArchiRole.getRoleOrName());
        }

        return true;
    }

    /**
     * 发送角色更新通知
     *
     * @param operatorWxId        操作人（管理员）用户ID
     * @param targetWxId          被更新角色的用户ID
     * @param alumniAssociationId 校友会ID
     * @param associationName     校友会名称
     * @param roleName            新角色名称
     */
    private void sendRoleUpdateNotification(Long operatorWxId, Long targetWxId, Long alumniAssociationId,
            String associationName, String roleName) {
        try {
            // 查询操作人信息
            WxUserInfo operatorInfo = wxUserInfoService.getById(operatorWxId);
            String operatorName = "管理员";
            if (operatorInfo != null) {
                operatorName = operatorInfo.getNickname() != null ? operatorInfo.getNickname()
                        : (operatorInfo.getName() != null ? operatorInfo.getName() : "管理员");
            }

            // 发送系统通知
            String title = "组织架构角色更新";
            String content = operatorName + " 已将您设为【" + associationName + "】校友会的【" + roleName + "】";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    targetWxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    alumniAssociationId,
                    "ASSOCIATION");

            if (success) {
                log.info("角色更新通知已发送 - 操作人: {} ({}), 目标用户: {}, 校友会: {}, 新角色: {}",
                        operatorWxId, operatorName, targetWxId, associationName, roleName);
            } else {
                log.error("角色更新通知发送失败 - 操作人: {}, 目标用户: {}, 校友会: {}, 新角色: {}",
                        operatorWxId, targetWxId, associationName, roleName);
            }

        } catch (Exception e) {
            log.error("发送角色更新通知异常 - 操作人: {}, 目标用户: {}, 校友会: {}, Error: {}",
                    operatorWxId, targetWxId, associationName, e.getMessage(), e);
        }
    }

    /**
     * 发送邀请成功通知
     *
     * @param wxId                被邀请的用户ID
     * @param alumniAssociationId 校友会ID
     * @param associationName     校友会名称
     */
    private void sendInvitationSuccessNotification(Long wxId, Long alumniAssociationId, String associationName) {
        try {
            // 发送系统通知
            String title = "加入校友会成功";
            String content = "恭喜您成功加入【" + associationName + "】校友会";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    alumniAssociationId,
                    "ASSOCIATION");

            if (success) {
                log.info("邀请成功通知已发送 - 用户: {}, 校友会: {}", wxId, associationName);
            } else {
                log.error("邀请成功通知发送失败 - 用户: {}, 校友会: {}", wxId, associationName);
            }

        } catch (Exception e) {
            log.error("发送邀请成功通知异常 - 用户: {}, 校友会: {}, Error: {}",
                    wxId, associationName, e.getMessage(), e);
        }
    }

    /**
     * 更新用户的校友状态（isAlumni 字段）
     *
     * <p>
     * 在删除校友会成员后调用，检查用户是否还加入了其他校友会
     * <ul>
     * <li>如果用户还有其他校友会成员身份，保持 isAlumni = 1</li>
     * <li>如果用户不再是任何校友会的成员，设置 isAlumni = 0</li>
     * </ul>
     *
     * @param wxId 用户ID
     */
    private void updateUserAlumniStatus(Long wxId) {
        try {
            log.info("开始检查用户校友状态 - 用户ID: {}", wxId);

            // 1. 查询用户当前的 isAlumni 状态
            WxUser wxUser = userService.getById(wxId);
            if (wxUser == null) {
                log.warn("用户不存在，无法更新校友状态 - 用户ID: {}", wxId);
                return;
            }

            // 2. 如果用户当前没有认证（certificationFlag = 0 或 null），无需处理
            if (wxUser.getCertificationFlag() == null || wxUser.getCertificationFlag() == 0) {
                log.debug("用户当前没有认证，无需更新 - 用户ID: {}, certificationFlag: {}", wxId, wxUser.getCertificationFlag());
                return;
            }

            // 3. 查询用户是否还加入了其他校友会（状态为正常的成员记录）
            Long remainingAssociationCount = alumniAssociationMemberService.count(
                    new LambdaQueryWrapper<AlumniAssociationMember>()
                            .eq(AlumniAssociationMember::getWxId, wxId)
                            .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
            );

            log.info("用户剩余校友会数量 - 用户ID: {}, 剩余数量: {}", wxId, remainingAssociationCount);

            // 4. 如果用户不再是任何校友会的成员，更新 certificationFlag 为 0
            if (remainingAssociationCount == 0) {
                // 使用 UserService 的 updateById 方法更新
                wxUser.setCertificationFlag(0);
                boolean updateResult = userService.updateById(wxUser);

                if (updateResult) {
                    log.info("用户认证状态已更新 - 用户ID: {}, certificationFlag: {} -> 0", wxId, wxUser.getCertificationFlag());
                } else {
                    log.warn("用户认证状态更新失败 - 用户ID: {}", wxId);
                }
            } else {
                log.info("用户还是其他校友会成员，保持校友状态 - 用户ID: {}, 校友会数量: {}", wxId, remainingAssociationCount);
            }

        } catch (Exception e) {
            log.error("更新用户校友状态异常 - 用户ID: {}, Error: {}", wxId, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    public List<ActivityListVo> getActivitiesByAssociationId(Long alumniAssociationId) {
        log.info("查询校友会的活动列表，校友会 ID: {}", alumniAssociationId);

        // 构建查询条件：organizer_type=1 且 organizer_id=alumniAssociationId
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Activity::getOrganizerType, 1) // 主办方类型：1-校友会
                .eq(Activity::getOrganizerId, alumniAssociationId) // 主办方ID等于校友会ID
                .orderByDesc(Activity::getCreateTime); // 按创建时间倒序

        List<Activity> activityList = activityService.list(queryWrapper);
        List<ActivityListVo> activityListVos = activityList.stream()
                .map(ActivityListVo::objToVo)
                .collect(Collectors.toList());

        log.info("查询校友会的活动列表成功，校友会 ID: {}, 活动数量: {}", alumniAssociationId, activityListVos.size());
        return activityListVos;
    }

    @Override
    public ActivityDetailVo getActivityDetail(Long activityId) {
        log.info("查询活动详情，活动 ID: {}", activityId);

        // 根据活动ID查询活动
        Activity activity = activityService.getById(activityId);

        if (activity == null) {
            log.error("活动不存在，活动 ID: {}", activityId);
            throw new BusinessException("活动不存在");
        }

        // 转换为VO
        ActivityDetailVo activityDetailVo = ActivityDetailVo.objToVo(activity);

        log.info("查询活动详情成功，活动 ID: {}", activityId);
        return activityDetailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishActivity(Long createdBy, PublishActivityDto publishDto) {
        log.info("为校友会发布活动，创建人 ID: {}, 校友会 ID: {}, 活动标题: {}",
                createdBy, publishDto.getAlumniAssociationId(), publishDto.getActivityTitle());

        // 1. 查询校友会是否存在
        AlumniAssociation alumniAssociation = this.getById(publishDto.getAlumniAssociationId());
        if (alumniAssociation == null) {
            log.error("校友会不存在，校友会 ID: {}", publishDto.getAlumniAssociationId());
            throw new BusinessException("校友会不存在");
        }

        // 2. 创建活动实体
        Activity activity = new Activity();
        activity.setActivityTitle(publishDto.getActivityTitle());
        activity.setOrganizerType(1); // 主办方类型：1-校友会
        activity.setOrganizerId(publishDto.getAlumniAssociationId());
        activity.setOrganizerName(alumniAssociation.getAssociationName());
        activity.setOrganizerAvatar(alumniAssociation.getLogo());
        activity.setCoverImage(publishDto.getCoverImage());
        activity.setActivityImages(publishDto.getActivityImages());
        activity.setDescription(publishDto.getDescription());
        activity.setActivityCategory(publishDto.getActivityCategory());
        activity.setStartTime(publishDto.getStartTime());
        activity.setEndTime(publishDto.getEndTime());
        activity.setIsSignup(publishDto.getIsSignup());
        activity.setRegistrationStartTime(publishDto.getRegistrationStartTime());
        activity.setRegistrationEndTime(publishDto.getRegistrationEndTime());
        activity.setProvince(publishDto.getProvince());
        activity.setCity(publishDto.getCity());
        activity.setDistrict(publishDto.getDistrict());
        activity.setAddress(publishDto.getAddress());
        activity.setLatitude(publishDto.getLatitude());
        activity.setLongitude(publishDto.getLongitude());
        activity.setMaxParticipants(publishDto.getMaxParticipants());
        activity.setCurrentParticipants(0); // 初始报名人数为0
        activity.setIsNeedReview(publishDto.getIsNeedReview() != null ? publishDto.getIsNeedReview() : 0);
        activity.setContactPerson(publishDto.getContactPerson());
        activity.setContactPhone(publishDto.getContactPhone());
        activity.setContactEmail(publishDto.getContactEmail());
        activity.setStatus(1); // 状态：1-报名中
        activity.setReviewStatus(1); // 管理员发布，默认审核通过
        activity.setIsPublic(publishDto.getIsPublic() != null ? publishDto.getIsPublic() : 1);
        activity.setShowOnHomepage(publishDto.getShowOnHomepage() != null ? publishDto.getShowOnHomepage() : 0);
        activity.setIsRecommended(0); // 默认不推荐
        // 处理 tagsId，如果为空则设置为 null（避免 MySQL JSON 字段报错）
        String tagsId = publishDto.getTagsId();
        if (tagsId != null && tagsId.trim().isEmpty()) {
            tagsId = null;
        }
        activity.setTagsId(tagsId);
        activity.setRemark(publishDto.getRemark());
        activity.setViewCount(0L); // 初始浏览次数为0
        activity.setCreatedBy(createdBy); // 设置创建人ID

        // 3. 保存活动
        boolean insertResult = activityService.save(activity);

        if (insertResult) {
            log.info("为校友会发布活动成功，校友会 ID: {}, 活动 ID: {}, 活动标题: {}",
                    publishDto.getAlumniAssociationId(), activity.getActivityId(), publishDto.getActivityTitle());
            return true;
        } else {
            log.error("为校友会发布活动失败，校友会 ID: {}, 活动标题: {}",
                    publishDto.getAlumniAssociationId(), publishDto.getActivityTitle());
            throw new BusinessException("发布活动失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateActivity(UpdateActivityDto updateDto) {
        log.info("编辑活动，活动 ID: {}", updateDto.getActivityId());

        // 1. 查询活动是否存在
        Activity existingActivity = activityService.getById(updateDto.getActivityId());
        if (existingActivity == null) {
            log.error("活动不存在，活动 ID: {}", updateDto.getActivityId());
            throw new BusinessException("活动不存在");
        }

        // 2. 更新活动信息
        Activity activity = new Activity();
        activity.setActivityId(updateDto.getActivityId());
        activity.setActivityTitle(updateDto.getActivityTitle());
        activity.setOrganizerName(updateDto.getOrganizerName());
        activity.setOrganizerAvatar(updateDto.getOrganizerAvatar());
        activity.setCoverImage(updateDto.getCoverImage());
        activity.setActivityImages(updateDto.getActivityImages());
        activity.setDescription(updateDto.getDescription());
        activity.setActivityCategory(updateDto.getActivityCategory());
        activity.setStartTime(updateDto.getStartTime());
        activity.setEndTime(updateDto.getEndTime());
        activity.setIsSignup(updateDto.getIsSignup());
        activity.setRegistrationStartTime(updateDto.getRegistrationStartTime());
        activity.setRegistrationEndTime(updateDto.getRegistrationEndTime());
        activity.setProvince(updateDto.getProvince());
        activity.setCity(updateDto.getCity());
        activity.setDistrict(updateDto.getDistrict());
        activity.setAddress(updateDto.getAddress());
        activity.setLatitude(updateDto.getLatitude());
        activity.setLongitude(updateDto.getLongitude());
        activity.setMaxParticipants(updateDto.getMaxParticipants());
        activity.setIsNeedReview(updateDto.getIsNeedReview());
        activity.setShowOnHomepage(
                updateDto.getShowOnHomepage() != null
                        ? updateDto.getShowOnHomepage()
                        : existingActivity.getShowOnHomepage());
        activity.setContactPerson(updateDto.getContactPerson());
        activity.setContactPhone(updateDto.getContactPhone());
        activity.setContactEmail(updateDto.getContactEmail());
        activity.setRemark(updateDto.getRemark());

        // 3. 执行更新
        boolean updateResult = activityService.updateById(activity);

        if (updateResult) {
            log.info("编辑活动成功，活动 ID: {}, 活动标题: {}",
                    updateDto.getActivityId(), updateDto.getActivityTitle());
            return true;
        } else {
            log.error("编辑活动失败，活动 ID: {}", updateDto.getActivityId());
            throw new BusinessException("编辑活动失败");
        }
    }

    @Override
    public List<AlumniAssociationListVo> getMyJoinedAssociations(Long wxId) {
        log.info("查询用户加入的校友会列表，用户 ID: {}", wxId);

        // 1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }

        // 2. 查询用户加入的校友会成员关系（状态为正常的）
        LambdaQueryWrapper<AlumniAssociationMember> memberQueryWrapper = new LambdaQueryWrapper<>();
        memberQueryWrapper
                .eq(AlumniAssociationMember::getWxId, wxId)
                .eq(AlumniAssociationMember::getStatus, 1); // 状态：1-正常

        List<AlumniAssociationMember> memberList = alumniAssociationMemberService.list(memberQueryWrapper);

        if (memberList.isEmpty()) {
            log.info("用户未加入任何校友会，用户 ID: {}", wxId);
            return Collections.emptyList();
        }

        // 3. 获取校友会ID列表
        List<Long> associationIds = memberList.stream()
                .map(AlumniAssociationMember::getAlumniAssociationId)
                .distinct()
                .collect(Collectors.toList());

        // 4. 查询校友会详细信息
        LambdaQueryWrapper<AlumniAssociation> associationQueryWrapper = new LambdaQueryWrapper<>();
        associationQueryWrapper
                .in(AlumniAssociation::getAlumniAssociationId, associationIds)
                .eq(AlumniAssociation::getStatus, 1) // 状态：1-正常
                .orderByDesc(AlumniAssociation::getCreateTime);

        List<AlumniAssociation> associationList = this.list(associationQueryWrapper);

        // 5. 转换为 VO
        List<AlumniAssociationListVo> voList = associationList.stream()
                .map(AlumniAssociationListVo::objToVo)
                .collect(Collectors.toList());

        log.info("查询用户加入的校友会列表成功，用户 ID: {}, 校友会数量: {}", wxId, voList.size());

        return voList;
    }

    @Override
    public List<UserListResponse> getAdminsByAssociationId(Long alumniAssociationId) {
        log.info("查询校友会管理员列表，校友会 ID: {}", alumniAssociationId);

        // 1. 参数校验
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }

        // 2. 查询 ORGANIZE_ALUMNI_ADMIN 角色
        Role adminRole = roleService.getRoleByCodeInner("ORGANIZE_ALUMNI_ADMIN");
        if (adminRole == null) {
            log.error("未找到校友会管理员角色，角色代码: ORGANIZE_ALUMNI_ADMIN");
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "系统角色配置错误");
        }

        // 3. 查询 role_user 表，获取该校友会的所有管理员用户ID
        LambdaQueryWrapper<RoleUser> roleUserQueryWrapper = new LambdaQueryWrapper<>();
        roleUserQueryWrapper
                .eq(RoleUser::getRoleId, adminRole.getRoleId())
                .eq(RoleUser::getType, 2) // type=2 表示校友会
                .eq(RoleUser::getOrganizeId, alumniAssociationId);

        List<RoleUser> roleUserList = roleUserService.list(roleUserQueryWrapper);

        if (roleUserList.isEmpty()) {
            log.info("该校友会暂无管理员，校友会 ID: {}", alumniAssociationId);
            return Collections.emptyList();
        }

        // 4. 提取所有管理员的 wxId
        List<Long> adminWxIds = roleUserList.stream()
                .map(RoleUser::getWxId)
                .distinct()
                .collect(Collectors.toList());

        log.info("查询到 {} 名管理员，校友会 ID: {}", adminWxIds.size(), alumniAssociationId);

        // 5. 批量查询用户信息
        LambdaQueryWrapper<WxUserInfo> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(WxUserInfo::getWxId, adminWxIds);
        List<WxUserInfo> userInfoList = wxUserInfoService.list(userQueryWrapper);

        // 6. 转换为 UserListResponse
        List<UserListResponse> responseList = userInfoList.stream()
                .map(UserListResponse::ObjToVo)
                .collect(Collectors.toList());

        log.info("查询校友会管理员列表成功，校友会 ID: {}, 管理员数量: {}", alumniAssociationId, responseList.size());

        return responseList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAdminToAssociation(Long alumniAssociationId, Long wxId) {
        log.info("添加校友会管理员，校友会 ID: {}, 用户 ID: {}", alumniAssociationId, wxId);

        // 1. 参数校验
        if (alumniAssociationId == null || wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID和用户ID不能为空");
        }

        // 2. 校验校友会是否存在
        AlumniAssociation association = this.getById(alumniAssociationId);
        if (association == null) {
            log.error("校友会不存在，校友会 ID: {}", alumniAssociationId);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "校友会不存在");
        }

        // 3. 校验用户是否存在
        WxUserInfo userInfo = wxUserInfoService.getOne(
                new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, wxId));
        if (userInfo == null) {
            log.error("用户不存在，用户 ID: {}", wxId);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "用户不存在");
        }

        // 4. 查询管理员角色
        Role adminRole = roleService.getRoleByCodeInner("ORGANIZE_ALUMNI_ADMIN");
        if (adminRole == null) {
            log.error("未找到校友会管理员角色，角色代码: ORGANIZE_ALUMNI_ADMIN");
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "系统角色配置错误");
        }

        // 5. 检查用户是否已经是该校友会的管理员
        LambdaQueryWrapper<RoleUser> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper
                .eq(RoleUser::getWxId, wxId)
                .eq(RoleUser::getRoleId, adminRole.getRoleId())
                .eq(RoleUser::getType, 2) // type=2 表示校友会
                .eq(RoleUser::getOrganizeId, alumniAssociationId);

        RoleUser existingRoleUser = roleUserService.getOne(checkWrapper);
        if (existingRoleUser != null) {
            log.warn("用户已经是该校友会的管理员，校友会 ID: {}, 用户 ID: {}", alumniAssociationId, wxId);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "该用户已经是管理员");
        }

        // 6. 创建 role_user 记录
        RoleUser roleUser = new RoleUser();
        roleUser.setWxId(wxId);
        roleUser.setRoleId(adminRole.getRoleId());
        roleUser.setType(2); // 2-校友会
        roleUser.setOrganizeId(alumniAssociationId);
        roleUser.setCreateTime(LocalDateTime.now());
        roleUser.setUpdateTime(LocalDateTime.now());

        boolean result = roleUserService.save(roleUser);

        if (result) {
            log.info("添加校友会管理员成功，校友会 ID: {}, 用户 ID: {}", alumniAssociationId, wxId);
        } else {
            log.error("添加校友会管理员失败，校友会 ID: {}, 用户 ID: {}", alumniAssociationId, wxId);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAdminFromAssociation(Long alumniAssociationId, Long wxId) {
        log.info("移除校友会管理员，校友会 ID: {}, 用户 ID: {}", alumniAssociationId, wxId);

        // 1. 参数校验
        if (alumniAssociationId == null || wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID和用户ID不能为空");
        }

        // 2. 查询管理员角色
        Role adminRole = roleService.getRoleByCodeInner("ORGANIZE_ALUMNI_ADMIN");
        if (adminRole == null) {
            log.error("未找到校友会管理员角色，角色代码: ORGANIZE_ALUMNI_ADMIN");
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "系统角色配置错误");
        }

        // 3. 查询该用户在该校友会的管理员记录
        LambdaQueryWrapper<RoleUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(RoleUser::getWxId, wxId)
                .eq(RoleUser::getRoleId, adminRole.getRoleId())
                .eq(RoleUser::getType, 2) // type=2 表示校友会
                .eq(RoleUser::getOrganizeId, alumniAssociationId);

        RoleUser roleUser = roleUserService.getOne(queryWrapper);
        if (roleUser == null) {
            log.warn("该用户不是该校友会的管理员，校友会 ID: {}, 用户 ID: {}", alumniAssociationId, wxId);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "该用户不是管理员");
        }

        // 4. 删除 role_user 记录（逻辑删除）
        boolean result = roleUserService.removeById(roleUser.getId());

        if (result) {
            log.info("移除校友会管理员成功，校友会 ID: {}, 用户 ID: {}", alumniAssociationId, wxId);
        } else {
            log.error("移除校友会管理员失败，校友会 ID: {}, 用户 ID: {}", alumniAssociationId, wxId);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindMemberToUser(Long memberId, Long wxId) {
        log.info("绑定校友会成员与系统用户 - 成员ID: {}, 用户ID: {}", memberId, wxId);

        // 1. 参数校验
        if (memberId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员表ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户微信ID不能为空");
        }

        // 2. 查询成员记录是否存在
        AlumniAssociationMember member = alumniAssociationMemberService.getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "成员记录不存在");
        }

        // 3. 校验用户是否存在
        WxUser wxUser = userService.getById(wxId);
        if (wxUser == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "用户不存在");
        }

        // 4. 检查该用户是否已经绑定到该校友会的其他成员记录
        LambdaQueryWrapper<AlumniAssociationMember> existingBindQuery = new LambdaQueryWrapper<>();
        existingBindQuery
                .eq(AlumniAssociationMember::getWxId, wxId)
                .eq(AlumniAssociationMember::getAlumniAssociationId, member.getAlumniAssociationId())
                .ne(AlumniAssociationMember::getId, memberId)
                .eq(AlumniAssociationMember::getStatus, 1);

        Long existingBindCount = alumniAssociationMemberService.count(existingBindQuery);
        if (existingBindCount > 0) {
            log.warn("该用户已绑定到该校友会的其他成员记录，用户ID: {}, 校友会ID: {}",
                    wxId, member.getAlumniAssociationId());
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该用户已绑定到该校友会的其他成员记录");
        }

        // 5. 更新成员记录的 wx_id 字段
        member.setWxId(wxId);
        boolean updateResult = alumniAssociationMemberService.updateById(member);

        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "绑定失败");
        }

        log.info("绑定校友会成员与系统用户成功 - 成员ID: {}, 用户ID: {}", memberId, wxId);
        return true;
    }

    @Override
    public List<ManagedOrganizationVo> getManagedAssociations(Long wxId) {
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
            // 4. 系统管理员：返回所有校友会
            List<AlumniAssociation> allAssociations = this.list(
                    new LambdaQueryWrapper<AlumniAssociation>()
                            .eq(AlumniAssociation::getStatus, 1) // 只返回启用的
                            .orderByDesc(AlumniAssociation::getCreateTime));

            result = allAssociations.stream()
                    .map(association -> {
                        ManagedOrganizationVo vo = new ManagedOrganizationVo();
                        vo.setOrganizationId(String.valueOf(association.getAlumniAssociationId()));
                        vo.setOrganizationName(association.getAssociationName());
                        vo.setAvatar(association.getLogo());
                        vo.setMemberCount(association.getMemberCount());
                        vo.setMonthlyHomepageArticleQuota(association.getMonthlyHomepageArticleQuota());
                        return vo;
                    })
                    .collect(Collectors.toList());

            log.info("系统管理员查询所有校友会 - 用户ID: {}, 校友会数量: {}", wxId, result.size());

        } else {
            // 5. 组织管理员：查询有权限管理的校友会
            List<RoleUser> roleUsers = roleUserService.list(
                    new LambdaQueryWrapper<RoleUser>()
                            .eq(RoleUser::getWxId, wxId)
                            .eq(RoleUser::getType, 2) // 2-校友会
                            .isNotNull(RoleUser::getOrganizeId));

            if (roleUsers.isEmpty()) {
                log.info("用户无管理的校友会 - 用户ID: {}", wxId);
                return result;
            }

            // 提取校友会ID列表
            List<Long> associationIds = roleUsers.stream()
                    .map(RoleUser::getOrganizeId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询校友会信息
            List<AlumniAssociation> associations = this.listByIds(associationIds);
            result = associations.stream()
                    .filter(association -> association.getStatus() == 1) // 过滤启用的
                    .map(association -> {
                        ManagedOrganizationVo vo = new ManagedOrganizationVo();
                        vo.setOrganizationId(String.valueOf(association.getAlumniAssociationId()));
                        vo.setOrganizationName(association.getAssociationName());
                        vo.setAvatar(association.getLogo());
                        vo.setMemberCount(association.getMemberCount());
                        vo.setMonthlyHomepageArticleQuota(association.getMonthlyHomepageArticleQuota());
                        return vo;
                    })
                    .collect(Collectors.toList());

            log.info("组织管理员查询管理的校友会 - 用户ID: {}, 校友会数量: {}", wxId, result.size());
        }

        return result;
    }

    @Override
    public boolean updateMemberInfo(Long id, String username, String roleName, String userPhone,
                                    String userAffiliation, Integer isShowOnHome) {
        // 1. 参数校验
        if (id == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员ID不能为空");
        }

        log.info("开始更新校友会成员信息 - 成员ID: {}, 用户名: {}, 角色名: {}, 电话: {}, 社会职务: {}, 是否展示在主页: {}",
                id, username, roleName, userPhone, userAffiliation, isShowOnHome);

        // 2. 查询成员记录是否存在
        AlumniAssociationMember member = alumniAssociationMemberService.getById(id);
        if (member == null) {
            log.error("成员记录不存在 - 成员ID: {}", id);
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "成员记录不存在");
        }

        // 3. 更新成员信息（只更新非空字段）
        boolean needUpdate = false;

        if (username != null && !username.equals(member.getUsername())) {
            member.setUsername(username);
            needUpdate = true;
        }

        if (roleName != null && !roleName.equals(member.getRoleName())) {
            member.setRoleName(roleName);
            needUpdate = true;
        }

        if (userPhone != null && !userPhone.equals(member.getUserPhone())) {
            member.setUserPhone(userPhone);
            needUpdate = true;
        }

        if (userAffiliation != null && !userAffiliation.equals(member.getUserAffiliation())) {
            member.setUserAffiliation(userAffiliation);
            needUpdate = true;
        }

        if (isShowOnHome != null && !isShowOnHome.equals(member.getIsShowOnHome())) {
            member.setIsShowOnHome(isShowOnHome);
            needUpdate = true;
        }

        // 4. 如果没有需要更新的字段，直接返回成功
        if (!needUpdate) {
            log.info("成员信息无变化，无需更新 - 成员ID: {}", id);
            return true;
        }

        // 5. 执行更新
        boolean result = alumniAssociationMemberService.updateById(member);

        if (result) {
            log.info("更新校友会成员信息成功 - 成员ID: {}", id);
        } else {
            log.error("更新校友会成员信息失败 - 成员ID: {}", id);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMemberToBranch(Long alumniAssociationId, Long wxId, Long roleOrId) {
        // 1. 参数校验
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员用户ID不能为空");
        }
        if (roleOrId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "分支ID不能为空");
        }

        log.info("开始添加成员到分支 - 校友会ID: {}, 成员用户ID: {}, 分支ID: {}", alumniAssociationId, wxId, roleOrId);

        // 2. 查询校友会是否存在
        AlumniAssociation alumniAssociation = this.getById(alumniAssociationId);
        if (alumniAssociation == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
        }

        // 3. 查询分支（组织架构角色）是否存在且有效
        OrganizeArchiRole organizeArchiRole = organizeArchiRoleService.getOne(
                new LambdaQueryWrapper<OrganizeArchiRole>()
                        .eq(OrganizeArchiRole::getRoleOrId, roleOrId)
                        .eq(OrganizeArchiRole::getOrganizeId, alumniAssociationId)
                        .eq(OrganizeArchiRole::getOrganizeType, 0) // 0-校友会
                        .eq(OrganizeArchiRole::getStatus, 1) // 1-启用
        );

        if (organizeArchiRole == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该组织架构角色不存在或未启用");
        }

        // 4. 查询成员记录是否存在
        AlumniAssociationMember member = alumniAssociationMemberService.getOne(
                new LambdaQueryWrapper<AlumniAssociationMember>()
                        .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                        .eq(AlumniAssociationMember::getWxId, wxId)
                        .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
        );

        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该用户不是该校友会的成员");
        }

        // 5. 更新成员的分支（角色）
        Long oldRoleOrId = member.getRoleOrId();
        member.setRoleOrId(roleOrId);
        boolean updateResult = alumniAssociationMemberService.updateById(member);

        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "添加成员到分支失败");
        }

        log.info("添加成员到分支成功 - 校友会ID: {}, 成员用户ID: {}, 原分支ID: {}, 新分支ID: {}",
                alumniAssociationId, wxId, oldRoleOrId, roleOrId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMemberFromBranch(Long alumniAssociationId, Long wxId) {
        // 1. 参数校验
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "成员用户ID不能为空");
        }

        log.info("开始从分支移除成员 - 校友会ID: {}, 成员用户ID: {}", alumniAssociationId, wxId);

        // 2. 查询校友会是否存在
        AlumniAssociation alumniAssociation = this.getById(alumniAssociationId);
        if (alumniAssociation == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
        }

        // 3. 查询成员记录是否存在
        AlumniAssociationMember member = alumniAssociationMemberService.getOne(
                new LambdaQueryWrapper<AlumniAssociationMember>()
                        .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                        .eq(AlumniAssociationMember::getWxId, wxId)
                        .eq(AlumniAssociationMember::getStatus, 1) // 状态：1-正常
        );

        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该用户不是该校友会的成员");
        }

        // 4. 记录原分支ID
        Long oldRoleOrId = member.getRoleOrId();

        // 5. 将成员的分支（角色）设置为 null
        // 注意：使用 UpdateWrapper 显式设置为 null，因为 updateById 默认忽略 null 值
        boolean updateResult = alumniAssociationMemberService.update(
                new LambdaUpdateWrapper<AlumniAssociationMember>()
                        .set(AlumniAssociationMember::getRoleOrId, null)
                        .eq(AlumniAssociationMember::getId, member.getId())
        );

        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "从分支移除成员失败");
        }

        log.info("从分支移除成员成功 - 校友会ID: {}, 成员用户ID: {}, 原分支ID: {}",
                alumniAssociationId, wxId, oldRoleOrId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUnregisteredMember(Long alumniAssociationId, String username, String roleName, String userPhone, String userAffiliation) {
        // 1. 参数校验
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户名字不能为空");
        }

        log.info("开始添加未注册成员 - 校友会ID: {}, 用户名: {}, 角色名称: {}, 联系电话: {}, 社会职务: {}",
                alumniAssociationId, username, roleName, userPhone, userAffiliation);

        // 2. 查询校友会是否存在
        AlumniAssociation alumniAssociation = this.getById(alumniAssociationId);
        if (alumniAssociation == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
        }

        // 3. 创建新的成员记录
        AlumniAssociationMember member = new AlumniAssociationMember();
        member.setAlumniAssociationId(alumniAssociationId);
        member.setUsername(username);
        member.setRoleName(roleName);
        member.setUserPhone(userPhone);
        member.setUserAffiliation(userAffiliation);
        member.setWxId(null); // 未注册用户，wxId 为空
        member.setStatus(1); // 状态：1-正常
        member.setIsNu(0); // 是否是架构成员：0-否
        member.setIsShowOnHome(0); // 默认不展示在主页
        member.setJoinTime(LocalDateTime.now());

        // 4. 保存成员记录
        boolean saveResult = alumniAssociationMemberService.save(member);

        if (!saveResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "添加未注册成员失败");
        }

        // 5. 更新校友会成员数量（+1）
        this.updateMemberCount(alumniAssociationId, 1);

        log.info("添加未注册成员成功 - 校友会ID: {}, 成员ID: {}, 用户名: {}",
                alumniAssociationId, member.getId(), username);

        return true;
    }

    @Override
    public int bindPresetMembersByPhone(String phone, String name, Long wxId) {
        if (phone == null || phone.trim().isEmpty() || wxId == null) {
            return 0;
        }
        String normalizedPhone = phone.trim();
        String normalizedName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        LambdaQueryWrapper<AlumniAssociationMember> queryWrapper = new LambdaQueryWrapper<AlumniAssociationMember>()
                .isNull(AlumniAssociationMember::getWxId)
                .eq(AlumniAssociationMember::getStatus, 1)
                .eq(AlumniAssociationMember::getUserPhone, normalizedPhone);
        if (normalizedName != null) {
            queryWrapper.eq(AlumniAssociationMember::getUsername, normalizedName);
        }
        List<AlumniAssociationMember> presetMembers = alumniAssociationMemberService.list(queryWrapper);
        int boundCount = 0;
        for (AlumniAssociationMember member : presetMembers) {
            // 检查该用户是否已经是该校友会的成员
            AlumniAssociationMember existingMember = alumniAssociationMemberService.getOne(
                    new LambdaQueryWrapper<AlumniAssociationMember>()
                            .eq(AlumniAssociationMember::getAlumniAssociationId, member.getAlumniAssociationId())
                            .eq(AlumniAssociationMember::getWxId, wxId)
                            .eq(AlumniAssociationMember::getStatus, 1)
            );
            if (existingMember != null) {
                log.debug("用户已是该校友会成员，跳过绑定 - alumniAssociationId: {}, wxId: {}", member.getAlumniAssociationId(), wxId);
                continue;
            }
            member.setWxId(wxId);
            if (alumniAssociationMemberService.updateById(member)) {
                boundCount++;
                log.info("绑定校友会预设成员成功 - memberId: {}, alumniAssociationId: {}, wxId: {}",
                        member.getId(), member.getAlumniAssociationId(), wxId);
            }
        }
        if (boundCount > 0) {
            log.info("根据手机号(和姓名)绑定校友会预设成员完成 - phone: {}****{}, name: {}, wxId: {}, boundCount: {}",
                    normalizedPhone.length() >= 3 ? normalizedPhone.substring(0, 3) : "***",
                    normalizedPhone.length() >= 4 ? normalizedPhone.substring(normalizedPhone.length() - 4) : "****",
                    normalizedName, wxId, boundCount);
        }
        return boundCount;
    }

    @Override
    public boolean updateMemberCount(Long alumniAssociationId, Integer delta) {
        if (alumniAssociationId == null || delta == null || delta == 0) {
            return false;
        }
        log.info("更新校友会成员数量 - 校友会ID: {}, 变化值: {}", alumniAssociationId, delta);
        int result = this.baseMapper.updateMemberCount(alumniAssociationId, delta);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAlumniAssociationCompletely(Long alumniAssociationId) {
        if (alumniAssociationId == null) {
            log.error("[deleteAlumniAssociationCompletely] 校友会ID不能为空");
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }

        log.info("[deleteAlumniAssociationCompletely] 开始删除校友会及所有相关数据 - 校友会ID: {}", alumniAssociationId);

        try {
            // 1. 删除校友会成员表
            LambdaQueryWrapper<AlumniAssociationMember> memberQuery = new LambdaQueryWrapper<>();
            memberQuery.eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId);
            long memberCount = alumniAssociationMemberService.count(memberQuery);
            if (memberCount > 0) {
                boolean memberDeleted = alumniAssociationMemberService.remove(memberQuery);
                log.info("[deleteAlumniAssociationCompletely] 删除校友会成员 - 校友会ID: {}, 删除数量: {}, 结果: {}",
                        alumniAssociationId, memberCount, memberDeleted);
            }

            // 2. 删除校友会加入申请表
            LambdaQueryWrapper<AlumniAssociationJoinApplication> applicationQuery = new LambdaQueryWrapper<>();
            applicationQuery.eq(AlumniAssociationJoinApplication::getAlumniAssociationId, alumniAssociationId);
            List<AlumniAssociationJoinApplication> applications = alumniAssociationJoinApplicationMapper.selectList(applicationQuery);
            if (!applications.isEmpty()) {
                int applicationCount = alumniAssociationJoinApplicationMapper.delete(applicationQuery);
                log.info("[deleteAlumniAssociationCompletely] 删除校友会加入申请 - 校友会ID: {}, 删除数量: {}",
                        alumniAssociationId, applicationCount);
            }

            // 3. 删除校友会加入校促会申请表（注：需要在对应的Service中添加删除方法）
            log.info("[deleteAlumniAssociationCompletely] 跳过删除校友会加入校促会申请（如需要请手动处理） - 校友会ID: {}",
                    alumniAssociationId);

            // 4. 删除校友会邀请记录表
            LambdaQueryWrapper<AlumniAssociationInvitation> invitationQuery = new LambdaQueryWrapper<>();
            invitationQuery.eq(AlumniAssociationInvitation::getAlumniAssociationId, alumniAssociationId);
            int invitationCount = alumniAssociationInvitationMapper.delete(invitationQuery);
            log.info("[deleteAlumniAssociationCompletely] 删除校友会邀请记录 - 校友会ID: {}, 删除数量: {}",
                    alumniAssociationId, invitationCount);

            // 5. 删除该校友会的所有活动（通过 ActivityService）
            log.info("[deleteAlumniAssociationCompletely] 删除校友会活动（需在ActivityService中实现） - 校友会ID: {}",
                    alumniAssociationId);

            // 6. 删除该校友会发布的文章（通过 HomePageArticleService）
            log.info("[deleteAlumniAssociationCompletely] 删除校友会文章（需在HomePageArticleService中实现） - 校友会ID: {}",
                    alumniAssociationId);

            // 7. 删除角色用户关联表（组织类型为校友会的角色）
            LambdaQueryWrapper<RoleUser> roleUserQuery = new LambdaQueryWrapper<>();
            roleUserQuery.eq(RoleUser::getType, 2) // 2-组织角色
                    .eq(RoleUser::getOrganizeId, alumniAssociationId);
            long roleUserCount = roleUserService.count(roleUserQuery);
            if (roleUserCount > 0) {
                boolean roleUserDeleted = roleUserService.remove(roleUserQuery);
                log.info("[deleteAlumniAssociationCompletely] 删除校友会角色关联 - 校友会ID: {}, 删除数量: {}, 结果: {}",
                        alumniAssociationId, roleUserCount, roleUserDeleted);
            }

            // 8. 删除组织架构角色
            LambdaQueryWrapper<OrganizeArchiRole> archiRoleQuery = new LambdaQueryWrapper<>();
            archiRoleQuery.eq(OrganizeArchiRole::getOrganizeType, 0) // 0-校友会
                    .eq(OrganizeArchiRole::getOrganizeId, alumniAssociationId);
            long archiRoleCount = organizeArchiRoleService.count(archiRoleQuery);
            if (archiRoleCount > 0) {
                boolean archiRoleDeleted = organizeArchiRoleService.remove(archiRoleQuery);
                log.info("[deleteAlumniAssociationCompletely] 删除组织架构角色 - 校友会ID: {}, 删除数量: {}, 结果: {}",
                        alumniAssociationId, archiRoleCount, archiRoleDeleted);
            }

            // 9. 删除用户关注记录
            LambdaQueryWrapper<UserFollow> followQuery = new LambdaQueryWrapper<>();
            followQuery.eq(UserFollow::getTargetId, alumniAssociationId)
                    .eq(UserFollow::getTargetType, 2); // 2-校友会
            long followCount = userFollowService.count(followQuery);
            if (followCount > 0) {
                boolean followDeleted = userFollowService.remove(followQuery);
                log.info("[deleteAlumniAssociationCompletely] 删除用户关注记录 - 校友会ID: {}, 删除数量: {}, 结果: {}",
                        alumniAssociationId, followCount, followDeleted);
            }

            // 10. 最后删除校友会主表
            boolean associationDeleted = this.removeById(alumniAssociationId);
            log.info("[deleteAlumniAssociationCompletely] 删除校友会主表 - 校友会ID: {}, 结果: {}",
                    alumniAssociationId, associationDeleted);

            if (associationDeleted) {
                log.info("[deleteAlumniAssociationCompletely] 校友会及所有相关数据删除成功 - 校友会ID: {}", alumniAssociationId);
                return true;
            } else {
                log.error("[deleteAlumniAssociationCompletely] 删除校友会主表失败 - 校友会ID: {}", alumniAssociationId);
                throw new BusinessException(ErrorType.OPERATION_ERROR, "删除校友会失败");
            }

        } catch (Exception e) {
            log.error("[deleteAlumniAssociationCompletely] 删除校友会异常 - 校友会ID: {}, Error: {}",
                    alumniAssociationId, e.getMessage(), e);
            throw new BusinessException(ErrorType.OPERATION_ERROR, "删除校友会失败：" + e.getMessage());
        }
    }

}
