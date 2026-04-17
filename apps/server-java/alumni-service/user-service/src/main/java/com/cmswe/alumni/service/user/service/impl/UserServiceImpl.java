package com.cmswe.alumni.service.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.UserPrivacySettingService;
import com.cmswe.alumni.common.constant.CommonConstant;
import com.cmswe.alumni.common.constant.KafkaTopicConstants;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.model.UserOnlineStatusKaf;
import com.cmswe.alumni.common.utils.JwtUtils;
import com.cmswe.alumni.kafka.utils.KafkaUtils;
import com.cmswe.alumni.api.association.SchoolService;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.dto.*;
import com.cmswe.alumni.common.vo.*;
import com.cmswe.alumni.common.vo.TagVo;
import com.cmswe.alumni.service.system.mapper.SysConfigMapper;
import com.cmswe.alumni.service.system.service.SysTagRelationService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.api.user.UserFollowService;
import com.cmswe.alumni.service.user.mapper.AlumniEducationMapper;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import com.cmswe.alumni.service.user.mapper.WxUserWorkMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yanqing
 * @description 针对表【sys_user(用户信息表)】的数据库操作Service实现
 * @createDate 2025-05-28 09:32:20
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<WxUserMapper, WxUser>
        implements UserService {

    @Resource
    private WxUserInfoMapper wxUserInfoMapper;

    @Resource
    private SysConfigMapper sysConfigMapper;

    @Resource
    private SchoolService schoolService;

    @Resource
    private AlumniEducationMapper alumniEducationMapper;

    @Resource
    private WxUserWorkMapper wxUserWorkMapper;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private UserPrivacySettingService userPrivacySettingService;

    @Resource
    private SysTagRelationService sysTagRelationService;

    @Resource
    private KafkaUtils kafkaUtils;

    @Resource
    private UserFollowService userFollowService;

    @Resource
    private WxUserMapper wxUserMapper;

    @Resource
    private com.cmswe.alumni.api.user.RoleUserService roleUserService;

    @Resource
    private com.cmswe.alumni.api.user.RoleService roleService;

    @org.springframework.context.annotation.Lazy
    @Resource
    private com.cmswe.alumni.api.association.AlumniAssociationService alumniAssociationService;

    @org.springframework.context.annotation.Lazy
    @Resource
    private com.cmswe.alumni.api.association.LocalPlatformService localPlatformService;

    @org.springframework.context.annotation.Lazy
    @Resource
    private com.cmswe.alumni.api.search.ShopService shopService;

    @org.springframework.context.annotation.Lazy
    @Resource
    private com.cmswe.alumni.api.system.MerchantService merchantService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(Long wxId, UpdateUserInfoDto updateDto) throws JsonProcessingException {

        //1.参数校验
        if(updateDto == null){
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        //2.查询用户是否存在
        WxUserInfo loginUser = wxUserInfoMapper.findByWxId(wxId);
        if (loginUser == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "用户不存在");
        }

        //3.信息更新 - 使用 DTO 转换为实体
        WxUserInfo updateUserInfo = UpdateUserInfoDto.dtoToObj(updateDto);

        //4.业务校验
        // 性别校验
        Integer gender = updateDto.getGender();
        if (gender != null) {
            if (gender < 0 || gender > 2) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "性别只能是：0-未知，1-男，2-女");
            }
        }

        // 生日校验
        LocalDate birthDate = updateDto.getBirthDate();
        if (birthDate != null) {
            LocalDate today = LocalDate.now();
            if (birthDate.isAfter(today)) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "生日不能是未来的日期");
            }
        }

        // 证件类型校验
        Integer identifyType = updateDto.getIdentifyType();
        if (identifyType != null) {
            if (identifyType < 0 || identifyType > 1) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "证件类型只能是：0-身份证，1-护照");
            }
        }

        // 星座校验（如果需要）
        Integer constellation = updateDto.getConstellation();
        if (constellation != null) {
            if (constellation < 1 || constellation > 12) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "星座范围应该在 1-12 之间");
            }
        }

        //5.设置更新的用户ID并执行更新
        updateUserInfo.setId(loginUser.getId());
        updateUserInfo.setWxId(wxId);
        int rows = wxUserInfoMapper.updateById(updateUserInfo);

        //6.处理教育经历信息更新
        List<AlumniEducationDto> alumniEducationList = updateDto.getAlumniEducationList();
        if (alumniEducationList != null && !alumniEducationList.isEmpty()) {
            updateAlumniEducation(wxId, alumniEducationList);
        }

        //6.5.处理工作经历信息更新（支持空列表，表示清空所有工作经历）
        List<WxUserWorkDto> workExperienceList = updateDto.getWorkExperienceList();
        if (workExperienceList != null) {
            updateWorkExperience(wxId, workExperienceList);
        }

        //7.记录日志
        if (rows > 0) {
            log.info("用户信息更新成功，用户ID: {}", wxId);
        } else {
            log.warn("用户信息更新失败，用户ID: {}", wxId);
        }

        //8.返回
        return rows > 0;
    }

    @Override
    public UserDetailVo getUserById(Long wxId) throws JsonProcessingException {
        //1. 参数校验
        if (wxId == null) {
            throw new  BusinessException(ErrorType.SYSTEM_ERROR);
        }

        //2. 查询数据库
        WxUserInfo loginUser = wxUserInfoMapper.findByWxId(wxId);

        //3. 把 User 转为 UserVo
        UserDetailVo userDetailVo = UserDetailVo.objToVo(loginUser);
        userDetailVo.setWxId(String.valueOf(wxId));

        // 获取用户对应的教育经历信息
        List<AlumniEducation> alumniEducations = alumniEducationMapper.selectList(new LambdaQueryWrapper<AlumniEducation>().eq(AlumniEducation::getWxId, wxId));
        if (alumniEducations == null || alumniEducations.isEmpty()) {
            alumniEducations = new ArrayList<>();
            AlumniEducation alumniEducation = new AlumniEducation();
            alumniEducation.setWxId(wxId);
            alumniEducations.add(alumniEducation);
        }

        List<AlumniEducationListVo> alumniEducationListVos = alumniEducations.stream().map(alumniEducation -> {
            AlumniEducationListVo alumniEducationListVo = AlumniEducationListVo.objToVo(alumniEducation);
            if (alumniEducation.getSchoolId() != null) {
                School school = schoolService.getById(alumniEducation.getSchoolId());
                SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
                schoolListVo.setSchoolId(String.valueOf(school.getSchoolId()));
                alumniEducationListVo.setSchoolInfo(schoolListVo);
            } else {
                alumniEducationListVo.setSchoolInfo(null);
            }

            return alumniEducationListVo;
        }).toList();

        userDetailVo.setAlumniEducationList(alumniEducationListVos);

        // 获取用户的工作经历信息
        List<WxUserWork> workExperiences = wxUserWorkMapper.selectList(
                new LambdaQueryWrapper<WxUserWork>().eq(WxUserWork::getWxId, wxId)
                        .orderByDesc(WxUserWork::getIsCurrent)
                        .orderByDesc(WxUserWork::getStartDate)
        );
        List<WxUserWorkVo> workExperienceVoList = workExperiences.stream()
                .map(WxUserWorkVo::objToVo)
                .toList();
        userDetailVo.setWorkExperienceList(workExperienceVoList);

        // 获取用户标签列表
        List<SysTag> userTags = sysTagRelationService.getTagsByTarget(wxId, 1); // targetType=1 表示校友(User)
        List<TagVo> tagVoList = userTags.stream()
                .map(TagVo::objToVo)
                .toList();
        userDetailVo.setTagList(tagVoList);

        //4. 记录日志
        log.info("获取用户信息：{}", userDetailVo);

        //5. 返回结果
        return userDetailVo;
    }

    @Override
    public Map<String, Object> generateTestToken(Long userId) {
        try {
            // 根据用户ID获取用户信息
            WxUser wxUser = this.getById(userId);
            if (wxUser == null) {
                throw new BusinessException("用户不存在");
            }

            return generateTokenForUser(wxUser, "用户ID: " + userId);

        } catch (Exception e) {
            log.error("生成测试token失败，userId: {}", userId, e);
            throw new BusinessException("Token生成失败: " + e.getMessage());
        }
    }

    @Override
    public List<UserPrivacySettingListVo> getUserPrivacy(Long wxId) {
        //1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        //2. 直接查询用户的隐私设置（注册时已通过 Kafka 异步初始化）
        List<UserPrivacySetting> userPrivacySettings = userPrivacySettingService.getByUserId(wxId);

        //3. 如果用户隐私设置为空（可能是老用户或初始化失败），进行兜底初始化
        if (userPrivacySettings == null || userPrivacySettings.isEmpty()) {
            log.warn("用户隐私设置为空，执行兜底初始化 - wxId: {}", wxId);
            userPrivacySettings = initUserPrivacySettingsSync(wxId);
        }

        //4. 封装 vo 返回
        List<UserPrivacySettingListVo> userPrivacySettingListVos = userPrivacySettings.stream().map(userPrivacySetting -> {
            UserPrivacySettingListVo userPrivacySettingListVo = UserPrivacySettingListVo.objToVo(userPrivacySetting);
            userPrivacySettingListVo.setWxId(String.valueOf(userPrivacySetting.getWxId()));
            userPrivacySettingListVo.setUserPrivacySettingId(String.valueOf(userPrivacySetting.getUserPrivacySettingId()));
            return userPrivacySettingListVo;
        }).toList();

        log.info("查询用户的隐私设置，用户id：{}，设置项数：{}", wxId, userPrivacySettingListVos.size());
        return userPrivacySettingListVos;
    }

    /**
     * 同步初始化用户隐私设置（兜底方法）
     * <p>用于老用户或 Kafka 初始化失败的场景
     *
     * @param wxId 用户ID
     * @return 初始化后的隐私设置列表
     */
    private List<UserPrivacySetting> initUserPrivacySettingsSync(Long wxId) {
        try {
            // 查询系统配置的隐私字段
            SysConfig parentConfig = sysConfigMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>()
                            .eq(SysConfig::getConfigKey, "user_privacy_setting")
            );

            if (parentConfig == null) {
                log.error("未找到系统隐私配置 - configKey: user_privacy_setting");
                return List.of();
            }

            List<SysConfig> privacyConfigs = sysConfigMapper.selectList(
                    new LambdaQueryWrapper<SysConfig>()
                            .eq(SysConfig::getParentId, parentConfig.getConfigId())
                            .eq(SysConfig::getStatus, 1)
            );

            if (privacyConfigs == null || privacyConfigs.isEmpty()) {
                log.warn("未找到可用的隐私配置项 - wxId: {}", wxId);
                return List.of();
            }

            // 为用户初始化隐私设置
            List<UserPrivacySetting> settings = privacyConfigs.stream().map(config -> {
                UserPrivacySetting setting = new UserPrivacySetting();
                setting.setWxId(wxId);
                setting.setFieldName(config.getConfigName());
                setting.setFieldCode(config.getConfigKey());
                setting.setType(1); // 用户配置
                setting.setVisibility(0); // 默认可见
                setting.setSearchable(0); // 默认可搜索
                userPrivacySettingService.save(setting);
                return setting;
            }).toList();

            log.info("用户隐私设置兜底初始化完成 - wxId: {}, 初始化项数: {}", wxId, settings.size());
            return settings;

        } catch (Exception e) {
            log.error("用户隐私设置兜底初始化失败 - wxId: {}, error: {}", wxId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public UserDetailVo getAlumniInfo(Long id) {
        //1. 参数校验
        if (id == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        //2. 获取用户信息
        WxUserInfo alumni = wxUserInfoMapper.findByWxId(id);
        if (alumni == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        // TODO 这里的隐私设置，由网关层来控制隐藏，所以这里正常执行查询即可，但是代码有优化空间

//        //3. 获取用户隐私设置并转换为Map便于查找
//        List<UserPrivacySetting> userPrivacySettings = userPrivacySettingService.getByUserId(id);
//        Map<String, UserPrivacySetting> privacyMap = userPrivacySettings.stream()
//                .collect(Collectors.toMap(UserPrivacySetting::getFieldName, setting -> setting));
//
//        //4. 创建UserDetailVo并根据隐私设置过滤字段
//        UserDetailVo userDetailVo = UserDetailVo.objToVo(alumni);
//
//        //5. 根据隐私设置处理字段可见性
//        applyPrivacySettings(userDetailVo, privacyMap);
//
//        log.info("根据隐私设置查询用户信息，用户ID: {}", id);

        UserDetailVo userDetailVo = UserDetailVo.objToVo(alumni);
        userDetailVo.setWxId(String.valueOf(id));


        // 获取用户对应的教育经历信息
        List<AlumniEducation> alumniEducations = alumniEducationMapper.selectList(new LambdaQueryWrapper<AlumniEducation>().eq(AlumniEducation::getWxId, id));
        if (alumniEducations == null || alumniEducations.isEmpty()) {
            alumniEducations = new ArrayList<>();
            AlumniEducation alumniEducation = new AlumniEducation();
            alumniEducation.setWxId(id);
            alumniEducations.add(alumniEducation);
        }

        List<AlumniEducationListVo> alumniEducationListVos = alumniEducations.stream().map(alumniEducation -> {
            AlumniEducationListVo alumniEducationListVo = AlumniEducationListVo.objToVo(alumniEducation);
            if (alumniEducation.getSchoolId() != null) {
                School school = schoolService.getById(alumniEducation.getSchoolId());
                SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
                schoolListVo.setSchoolId(String.valueOf(school.getSchoolId()));
                alumniEducationListVo.setSchoolInfo(schoolListVo);
            } else {
                alumniEducationListVo.setSchoolInfo(null);
            }

            return alumniEducationListVo;
        }).toList();

        userDetailVo.setAlumniEducationList(alumniEducationListVos);

        // 获取用户的工作经历信息
        List<WxUserWork> workExperiences = wxUserWorkMapper.selectList(
                new LambdaQueryWrapper<WxUserWork>().eq(WxUserWork::getWxId, id)
                        .orderByDesc(WxUserWork::getIsCurrent)
                        .orderByDesc(WxUserWork::getStartDate)
        );
        List<WxUserWorkVo> workExperienceVoList = workExperiences.stream()
                .map(WxUserWorkVo::objToVo)
                .toList();
        userDetailVo.setWorkExperienceList(workExperienceVoList);

        // 获取用户标签列表
        List<SysTag> userTags = sysTagRelationService.getTagsByTarget(id, 1); // targetType=1 表示校友(User)
        List<TagVo> tagVoList = userTags.stream()
                .map(TagVo::objToVo)
                .toList();
        userDetailVo.setTagList(tagVoList);

        return userDetailVo;
    }

    @Override
    public AlumniDetailVo getAlumniInfoWithStatus(Long id, Long currentUserId) {
        //1. 参数校验
        if (id == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        //2. 获取基础用户信息（复用 getAlumniInfo 方法）
        UserDetailVo baseUserDetail = this.getAlumniInfo(id);

        //3. 创建 AlumniDetailVo 并复制基础信息
        AlumniDetailVo alumniDetailVo = new AlumniDetailVo();
        BeanUtils.copyProperties(baseUserDetail, alumniDetailVo);

        //4. 查询 wx_users 表获取 certification_flag 字段
        WxUser wxUser = wxUserMapper.selectById(id);
        if (wxUser != null) {
            alumniDetailVo.setCertificationFlag(wxUser.getCertificationFlag());
        } else {
            alumniDetailVo.setCertificationFlag(0);
        }

        //5. 如果提供了 currentUserId，查询关注状态
        if (currentUserId != null && !currentUserId.equals(id)) {
            // 查询当前用户是否关注了目标用户
            UserFollow myFollow = userFollowService.getOne(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getWxId, currentUserId)
                            .eq(UserFollow::getTargetType, 1) // 1-用户
                            .eq(UserFollow::getTargetId, id)
                            .in(UserFollow::getFollowStatus, 1, 2, 3) // 1-正常关注，2-特别关注，3-免打扰
            );

            if (myFollow != null) {
                alumniDetailVo.setFollowStatus(myFollow.getFollowStatus());
                alumniDetailVo.setIsFollowed(true);
            } else {
                alumniDetailVo.setFollowStatus(null);
                alumniDetailVo.setIsFollowed(false);
            }

            // 查询目标用户是否也关注了当前用户（判断是否是好友）
            UserFollow theirFollow = userFollowService.getOne(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getWxId, id)
                            .eq(UserFollow::getTargetType, 1) // 1-用户
                            .eq(UserFollow::getTargetId, currentUserId)
                            .in(UserFollow::getFollowStatus, 1, 2, 3)
            );

            // 双向关注才是好友
            alumniDetailVo.setIsFriend(myFollow != null && theirFollow != null);
        } else {
            // 查看自己的信息或未登录，不显示关注状态
            alumniDetailVo.setFollowStatus(null);
            alumniDetailVo.setIsFollowed(false);
            alumniDetailVo.setIsFriend(false);
        }

        log.info("查询校友详情（含关注状态），目标用户ID: {}, 当前用户ID: {}", id, currentUserId);
        return alumniDetailVo;
    }

    @Override
    @Cacheable(value = "alumniList",
            key = "#queryAlumniListDto.hashCode() + '_' + #wxId",
            unless = "#result == null || #result.records.isEmpty()")
    public Page<UserListResponse> queryAlumniList(QueryAlumniListDto queryAlumniListDto, Long wxId) {
        if (queryAlumniListDto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        String nickname = queryAlumniListDto.getNickname();
        String name = queryAlumniListDto.getName();
        String phone = queryAlumniListDto.getPhone();
        String wxNum = queryAlumniListDto.getWxNum();
        String qqNum = queryAlumniListDto.getQqNum();
        String email = queryAlumniListDto.getEmail();
        String curContinent = queryAlumniListDto.getCurContinent();
        String curCountry = queryAlumniListDto.getCurCountry();
        String curProvince = queryAlumniListDto.getCurProvince();
        String curCity = queryAlumniListDto.getCurCity();
        Integer constellation = queryAlumniListDto.getConstellation();
        String signature = queryAlumniListDto.getSignature();
        Integer gender = queryAlumniListDto.getGender();
        String identifyCode = queryAlumniListDto.getIdentifyCode();
        LocalDate birthDate = queryAlumniListDto.getBirthDate();
        Integer myFollow = queryAlumniListDto.getMyFollow();
        int current = queryAlumniListDto.getCurrent();
        int pageSize = queryAlumniListDto.getPageSize();
        String sortField = queryAlumniListDto.getSortField();
        String sortOrder = queryAlumniListDto.getSortOrder();

        // 处理"我的关注"筛选：查询用户关注的校友 ID 列表
        List<Long> followedUserIds = null;
        if (myFollow != null && myFollow == 1 && wxId != null) {
            followedUserIds = userFollowService.getFollowedTargetIds(wxId, 1); // 1-用户

            // 如果用户没有关注任何校友，直接返回空结果
            if (followedUserIds.isEmpty()) {
                Page<UserListResponse> emptyPage = new Page<>(current, pageSize, 0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
        }

        // 设置默认排序字段
        if (sortField == null) {
            sortField = "createdTime";
        }

        // 检查是否有 OR 字段有值
        boolean hasOrCondition = StringUtils.isNotBlank(nickname)
                || StringUtils.isNotBlank(name)
                || StringUtils.isNotBlank(phone);

        LambdaQueryWrapper<WxUserInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 用户名称、昵称、手机号使用 OR 连接（用户可能输入的内容）
        if (hasOrCondition) {
            queryWrapper.and(wrapper -> {
                boolean hasCondition = false;

                if (StringUtils.isNotBlank(nickname)) {
                    wrapper.like(WxUserInfo::getNickname, nickname);
                    hasCondition = true;
                }
                if (StringUtils.isNotBlank(name)) {
                    if (hasCondition) wrapper.or();
                    wrapper.like(WxUserInfo::getName, name);
                    hasCondition = true;
                }
                if (StringUtils.isNotBlank(phone)) {
                    if (hasCondition) wrapper.or();
                    wrapper.like(WxUserInfo::getPhone, phone);
                }
            });
        }

        // 其他筛选条件使用 AND 连接
        queryWrapper
                .like(StringUtils.isNotBlank(email), WxUserInfo::getEmail, email)
                .like(StringUtils.isNotBlank(curContinent), WxUserInfo::getCurContinent, curContinent)
                .like(StringUtils.isNotBlank(curCountry), WxUserInfo::getCurCountry, curCountry)
                .like(StringUtils.isNotBlank(curProvince), WxUserInfo::getCurProvince, curProvince)
                .like(StringUtils.isNotBlank(curCity), WxUserInfo::getCurCity, curCity)
                .like(StringUtils.isNotBlank(signature), WxUserInfo::getSignature, signature)
                .eq(birthDate != null, WxUserInfo::getBirthDate, birthDate)
                .eq(gender != null, WxUserInfo::getGender, gender)
                .eq(constellation != null, WxUserInfo::getConstellation, constellation)
                .like(StringUtils.isNotBlank(identifyCode), WxUserInfo::getIdentifyCode, identifyCode);

        // 过滤：name 和 nickname 不能同时为空（至少有一个不为空）
        queryWrapper.and(wrapper -> wrapper.isNotNull(WxUserInfo::getName).or().isNotNull(WxUserInfo::getNickname));

        // 应用"我的关注"筛选
        if (followedUserIds != null && !followedUserIds.isEmpty()) {
            queryWrapper.in(WxUserInfo::getWxId, followedUserIds);
        }

        // 排序：先按指定字段排序，再按主键排序（确保排序稳定，避免分页重复）
        if ("createdTime".equals(sortField)) {
            queryWrapper.orderBy(true, CommonConstant.SORT_ORDER_ASC.equals(sortOrder), WxUserInfo::getCreatedTime);
        }
        queryWrapper.orderByDesc(WxUserInfo::getWxId);

        Page<WxUserInfo> wxUserInfoPage = wxUserInfoMapper.selectPage(new Page<>(current, pageSize), queryWrapper);

        // 批量查询主要教育经历和认证标识
        Map<Long, AlumniEducationListVo> primaryEducationMap = new HashMap<>();
        Map<Long, Integer> certificationFlagMap = new HashMap<>();

        if (!wxUserInfoPage.getRecords().isEmpty()) {
            List<Long> wxIds = wxUserInfoPage.getRecords().stream()
                    .map(WxUserInfo::getWxId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询 wx_users 表获取 certification_flag 字段
            List<WxUser> wxUsers = wxUserMapper.selectBatchIds(wxIds);
            certificationFlagMap = wxUsers.stream()
                    .collect(Collectors.toMap(
                            WxUser::getWxId,
                            wxUser -> wxUser.getCertificationFlag() != null ? wxUser.getCertificationFlag() : 0,
                            (v1, v2) -> v1
                    ));

            // 查询所有用户的主要教育经历（type=1）
            List<AlumniEducation> primaryEducations = alumniEducationMapper.selectList(
                    new LambdaQueryWrapper<AlumniEducation>()
                            .in(AlumniEducation::getWxId, wxIds)
                            .eq(AlumniEducation::getType, 1) // type=1 表示主要经历
            );

            // 提取所有涉及的学校ID并批量查询学校信息
            Map<Long, SchoolListVo> schoolMap = new HashMap<>();
            if (!primaryEducations.isEmpty()) {
                List<Long> schoolIds = primaryEducations.stream()
                        .map(AlumniEducation::getSchoolId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                if (!schoolIds.isEmpty()) {
                    List<School> schools = schoolService.listByIds(schoolIds);
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

            // 转换为 Map，key 为 wxId，value 为教育经历VO（包含学校信息）
            Map<Long, SchoolListVo> finalSchoolMap = schoolMap;
            primaryEducationMap = primaryEducations.stream()
                    .collect(Collectors.toMap(
                            AlumniEducation::getWxId,
                            education -> {
                                AlumniEducationListVo vo = AlumniEducationListVo.objToVo(education);
                                // 设置学校信息
                                if (education.getSchoolId() != null) {
                                    vo.setSchoolInfo(finalSchoolMap.get(education.getSchoolId()));
                                }
                                return vo;
                            },
                            (v1, v2) -> v1)); // 如果有多个主要经历，保留第一个
        }

        // 批量查询所有用户的标签（性能优化：避免 N+1 查询）
        Map<Long, List<SysTag>> userTagsMap = new HashMap<>();
        if (!wxUserInfoPage.getRecords().isEmpty()) {
            List<Long> wxIds = wxUserInfoPage.getRecords().stream()
                    .map(WxUserInfo::getWxId)
                    .distinct()
                    .collect(Collectors.toList());

            userTagsMap = sysTagRelationService.batchGetTagsByTargets(wxIds, 1); // targetType=1 表示校友(User)
            log.debug("批量查询用户标签完成 - 用户数: {}, 有标签的用户数: {}", wxIds.size(), userTagsMap.size());

            // 批量预热隐私设置缓存（性能优化：避免 AOP 处理时的 N+1 查询）
            userPrivacySettingService.batchWarmupCache(wxIds);
        }

        Map<Long, AlumniEducationListVo> finalPrimaryEducationMap = primaryEducationMap;
        Map<Long, Integer> finalCertificationFlagMap = certificationFlagMap;
        Map<Long, List<SysTag>> finalUserTagsMap = userTagsMap;

        List<UserListResponse> userListResponses = wxUserInfoPage.getRecords().stream().map(wxUserInfo -> {
            UserListResponse userListResponse = UserListResponse.ObjToVo(wxUserInfo);
            userListResponse.setWxId(String.valueOf(wxUserInfo.getWxId()));

            // 从批量查询结果中获取用户标签列表
            List<SysTag> userTags = finalUserTagsMap.getOrDefault(wxUserInfo.getWxId(), new ArrayList<>());
            List<TagVo> tagVoList = userTags.stream()
                    .map(TagVo::objToVo)
                    .toList();
            userListResponse.setTagList(tagVoList);

            // 设置主要教育经历
            userListResponse.setPrimaryEducation(finalPrimaryEducationMap.get(wxUserInfo.getWxId()));

            // 设置认证标识
            userListResponse.setCertificationFlag(finalCertificationFlagMap.getOrDefault(wxUserInfo.getWxId(), 0));

            return userListResponse;
        }).toList();

        return new Page<UserListResponse>(current, pageSize, wxUserInfoPage.getTotal()).setRecords(userListResponses);
    }

    @Override
    public boolean updateUserPrivacy(Long wxId, UpdateUserPrivacySettingsRequest updateUserPrivacySettingsRequest) {
        Optional.ofNullable(updateUserPrivacySettingsRequest)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Long userPrivacySettingId = updateUserPrivacySettingsRequest.getUserPrivacySettingId();
        Integer visibility = updateUserPrivacySettingsRequest.getVisibility();
        Integer searchable = updateUserPrivacySettingsRequest.getSearchable();


        boolean updateResult = userPrivacySettingService.updateSetting(wxId, userPrivacySettingId, visibility, searchable);

        log.info("用户 id：{} 更新个人隐私设置", wxId);

        return updateResult;
    }

    /**
     * 更新用户的工作经历信息（Diff 增量更新）
     * 1. 有 userWorkId 的记录：updateById 更新
     * 2. 无 userWorkId 的记录：insert 新增
     * 3. 传入列表之外的记录：逻辑删除
     *
     * @param wxId               用户ID
     * @param workExperienceList 工作经历列表（可为空，表示清空所有）
     */
    private void updateWorkExperience(Long wxId, List<WxUserWorkDto> workExperienceList) {
        // 校验工作经历数据
        for (WxUserWorkDto workDto : workExperienceList) {
            // 校验入职日期和离职日期的逻辑关系
            if (workDto.getStartDate() != null && workDto.getEndDate() != null) {
                if (workDto.getStartDate().isAfter(workDto.getEndDate())) {
                    throw new BusinessException(ErrorType.ARGS_ERROR, "入职日期不能晚于离职日期");
                }
            }

            // 如果是当前在职，离职日期应为空
            if (workDto.getIsCurrent() != null && workDto.getIsCurrent() == 1) {
                if (workDto.getEndDate() != null) {
                    throw new BusinessException(ErrorType.ARGS_ERROR, "当前在职的工作经历不应有离职日期");
                }
            }
        }

        // 1. 先处理传入列表：更新已有记录，新增无ID记录（此时记录尚未删除，updateById 可成功）
        Set<Long> incomingIds = new HashSet<>();
        for (WxUserWorkDto workDto : workExperienceList) {
            WxUserWork wxUserWork = new WxUserWork();
            BeanUtils.copyProperties(workDto, wxUserWork);
            wxUserWork.setWxId(wxId);

            // 如果离职日期为空且未明确标记为在职，则默认设置为在职
            if (wxUserWork.getEndDate() == null && wxUserWork.getIsCurrent() == null) {
                wxUserWork.setIsCurrent(1);
            }

            if (workDto.getUserWorkId() != null) {
                Long userWorkId = workDto.getUserWorkId();
                incomingIds.add(userWorkId);
                wxUserWork.setUserWorkId(userWorkId);
                wxUserWorkMapper.updateById(wxUserWork);
                log.info("更新工作经历成功，用户ID: {}, 工作经历ID: {}", wxId, userWorkId);
            } else {
                wxUserWorkMapper.insert(wxUserWork);
                incomingIds.add(wxUserWork.getUserWorkId());
                log.info("新增工作经历成功，用户ID: {}, 工作经历ID: {}", wxId, wxUserWork.getUserWorkId());
            }
        }

        // 2. 删除传入列表之外的记录（逻辑删除）
        List<WxUserWork> existingList = wxUserWorkMapper.selectList(
                new LambdaQueryWrapper<WxUserWork>().eq(WxUserWork::getWxId, wxId));
        for (WxUserWork existing : existingList) {
            if (!incomingIds.contains(existing.getUserWorkId())) {
                wxUserWorkMapper.deleteById(existing.getUserWorkId());
                log.info("删除工作经历，用户ID: {}, 工作经历ID: {}", wxId, existing.getUserWorkId());
            }
        }
    }

    /**
     * 更新用户的教育经历信息
     *
     * @param wxId                用户ID
     * @param alumniEducationList 教育经历列表
     */
    private void updateAlumniEducation(Long wxId, List<AlumniEducationDto> alumniEducationList) {
        // 校验教育经历数据
        for (AlumniEducationDto educationDto : alumniEducationList) {
            // 校验学校是否存在
            School school = schoolService.getById(educationDto.getSchoolId());
            if (school == null) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "学校ID不存在: " + educationDto.getSchoolId());
            }

            // 校验入学年份和毕业年份的逻辑关系
            if (educationDto.getEnrollmentYear() != null && educationDto.getGraduationYear() != null) {
                if (educationDto.getEnrollmentYear() > educationDto.getGraduationYear()) {
                    throw new BusinessException(ErrorType.ARGS_ERROR, "入学年份不能晚于毕业年份");
                }
            }
        }

        // 删除该用户现有的所有教育经历（逻辑删除）
        alumniEducationMapper.delete(new LambdaQueryWrapper<AlumniEducation>()
                .eq(AlumniEducation::getWxId, wxId));

        // 插入或更新新的教育经历
        for (AlumniEducationDto educationDto : alumniEducationList) {
            AlumniEducation alumniEducation = new AlumniEducation();
            BeanUtils.copyProperties(educationDto, alumniEducation);
            alumniEducation.setWxId(wxId);

            if (educationDto.getAlumniEducationId() != null) {
                // 如果有ID，则更新
                alumniEducation.setAlumniEducationId(educationDto.getAlumniEducationId());
                alumniEducationMapper.updateById(alumniEducation);
                log.info("更新教育经历成功，用户ID: {}, 教育经历ID: {}", wxId, educationDto.getAlumniEducationId());
            } else {
                // 如果没有ID，则新增
                alumniEducationMapper.insert(alumniEducation);
                log.info("新增教育经历成功，用户ID: {}", wxId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserTags(Long wxId, UpdateUserTagsDto updateUserTagsDto) {
        // 1. 参数校验
        if (wxId == null || updateUserTagsDto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        List<Long> tagIds = updateUserTagsDto.getTagIds();
        if (tagIds == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "标签列表不能为空");
        }

        // 2. 检查用户是否存在
        WxUser user = this.getById(wxId);
        if (user == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "用户不存在");
        }

        // 3. 清空用户现有的所有标签
        sysTagRelationService.clearTargetTags(wxId, 1); // targetType=1 表示校友(User)

        // 4. 如果标签列表为空，则只清空不添加
        if (tagIds.isEmpty()) {
            log.info("清空用户标签成功，用户ID: {}", wxId);
            return true;
        }

        // 5. 批量添加新的标签
        boolean result = sysTagRelationService.batchAddTagsToTarget(
                tagIds,
                wxId,
                1, // targetType=1 表示校友(User)
                wxId.toString() // 使用用户自己的ID作为操作人
        );

        if (result) {
            log.info("更新用户标签成功，用户ID: {}, 标签数量: {}", wxId, tagIds.size());
        } else {
            log.error("更新用户标签失败，用户ID: {}", wxId);
        }

        return result;
    }

    @Override
    public String onlineByToken(String token) throws JsonProcessingException {
        Long loginUserId = jwtUtils.getUserIdFromToken(token);
        if (loginUserId == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        WxUserInfo loginUser = wxUserInfoMapper.findByWxId(loginUserId);
        log.info("==============================用户 id:{} nickname:{} 上线==============================", loginUserId, loginUser.getNickname());

        // 发送上线消息到 Kafka
        sendOnlineStatusToKafka(loginUserId, UserOnlineStatusKaf.StatusAction.ONLINE);

        return String.valueOf(loginUserId);
    }

    @Override
    public void offline(String userId) {
        try {
            Long wxId = Long.parseLong(userId);
            WxUserInfo user = wxUserInfoMapper.findByWxId(wxId);
            if (user != null) {
                log.info("==============================用户 id:{} nickname:{} 下线==============================", wxId, user.getNickname());
            } else {
                log.info("==============================用户 id:{} 下线==============================", wxId);
            }

            // 发送下线消息到 Kafka
            sendOnlineStatusToKafka(wxId, UserOnlineStatusKaf.StatusAction.OFFLINE);

        } catch (NumberFormatException e) {
            log.error("用户ID格式错误: {}", userId, e);
            throw new BusinessException(ErrorType.ARGS_ERROR, "用户ID格式错误");
        }
    }

    /**
     * 发送用户在线状态消息到 Kafka
     *
     * @param wxId 用户 ID
     * @param action 动作类型（ONLINE/OFFLINE/HEARTBEAT）
     */
    private void sendOnlineStatusToKafka(Long wxId, UserOnlineStatusKaf.StatusAction action) {
        // 构建在线状态消息
        UserOnlineStatusKaf statusMessage = new UserOnlineStatusKaf();
        statusMessage.setWxId(wxId);
        statusMessage.setAction(action);
        statusMessage.setTimestamp(System.currentTimeMillis());
        // serverId, clientIp, deviceId 等字段可根据实际需求填充

        // 使用工具类发送消息（使用 userId 作为 key，保证同一用户的消息顺序）
        kafkaUtils.sendAsync(
                KafkaTopicConstants.USER_ONLINE_STATUS_TOPIC,
                String.valueOf(wxId),
                statusMessage
        );
    }

    /**
     * 为用户生成token的通用方法
     *
     * @param wxUser     用户信息
     * @param identifier 用户标识符（用于日志）
     * @return token信息
     */
    private Map<String, Object> generateTokenForUser(WxUser wxUser, String identifier) {
        try {
            // 将用户信息转换为JSON字符串
            String userInfo = JSON.toJSONString(wxUser);

            String token = jwtUtils.token(userInfo, null, 1000L * 60 * 60 * 24 * 30);

            // 返回token信息
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("wxId", wxUser.getWxId());
            result.put("openId", wxUser.getOpenid());
//            result.put("userInfo", user);

            log.info("为用户生成测试token成功并存储至Redis，{}", identifier);
            return result;

        } catch (Exception e) {
            log.error("生成token失败，{}", identifier, e);
            throw new RuntimeException("Token生成失败: " + e.getMessage());
        }
    }

    @Override
    public List<ManagedOrganizationListVo> getManagedOrganizations(Long wxId, Integer type, boolean roleScopedOnly) {
        // 1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }

        log.info("查询用户可管理的组织列表 - 用户ID: {}, 类型: {}, 仅role_user: {}", wxId, type, roleScopedOnly);

        List<ManagedOrganizationListVo> result = new ArrayList<>();

        // 2. 查询用户的角色
        List<Role> userRoles = roleService.getRolesByUserId(wxId);

        // 3. 判断是否是系统管理员
        boolean isSystemAdmin = userRoles.stream()
                .anyMatch(role -> "SYSTEM_SUPER_ADMIN".equals(role.getRoleCode()));

        if (roleScopedOnly) {
            // 仅 role_user 表绑定，与待办统计、加入审核列表范围一致
            result.addAll(getManagedOrganizationsByRole(wxId, type));
        } else if (isSystemAdmin) {
            result.addAll(getAllOrganizationsByType(type));
        } else {
            result.addAll(getManagedOrganizationsByRole(wxId, type));
        }

        log.info("查询用户可管理的组织列表成功 - 用户ID: {}, 类型: {}, 组织数量: {}", wxId, type, result.size());
        return result;
    }

    @Override
    public Set<Long> getManagedAlumniAssociationIdsByRole(Long wxId) {
        List<ManagedOrganizationListVo> orgs = getManagedOrganizationsByRole(wxId, 0);
        return orgs.stream()
                .map(ManagedOrganizationListVo::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> getManagedPlatformIdsByRole(Long wxId) {
        List<ManagedOrganizationListVo> orgs = getManagedOrganizationsByRole(wxId, 1);
        return orgs.stream()
                .map(ManagedOrganizationListVo::getId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取所有组织（系统管理员）
     *
     * @param type 组织类型（null-全部）
     * @return 组织列表
     */
    private List<ManagedOrganizationListVo> getAllOrganizationsByType(Integer type) {
        List<ManagedOrganizationListVo> result = new ArrayList<>();

        // 0-校友会
        if (type == null || type == 0) {
            List<AlumniAssociation> associations = alumniAssociationService.list(
                    new LambdaQueryWrapper<AlumniAssociation>()
                            .eq(AlumniAssociation::getStatus, 1)
                            .orderByDesc(AlumniAssociation::getCreateTime));

            associations.forEach(association -> {
                ManagedOrganizationListVo vo = new ManagedOrganizationListVo();
                vo.setId(association.getAlumniAssociationId());
                vo.setType(0);
                vo.setLogo(association.getLogo());
                vo.setName(association.getAssociationName());
                vo.setLocation(association.getLocation());
                result.add(vo);
            });
        }

        // 1-校促会
        if (type == null || type == 1) {
            List<LocalPlatform> platforms = localPlatformService.list(
                    new LambdaQueryWrapper<LocalPlatform>()
                            .eq(LocalPlatform::getStatus, 1)
                            .orderByDesc(LocalPlatform::getCreateTime));

            platforms.forEach(platform -> {
                ManagedOrganizationListVo vo = new ManagedOrganizationListVo();
                vo.setId(platform.getPlatformId());
                vo.setType(1);
                vo.setLogo(platform.getAvatar());
                vo.setName(platform.getPlatformName());
                vo.setLocation(platform.getCity());
                result.add(vo);
            });
        }

        // 2-商户（门店）
        if (type == null || type == 2) {
            List<Shop> shops = shopService.list(
                    new LambdaQueryWrapper<Shop>()
                            .eq(Shop::getStatus, 1)
                            .eq(Shop::getReviewStatus, 1)
                            .orderByDesc(Shop::getCreateTime));

            shops.forEach(shop -> {
                ManagedOrganizationListVo vo = new ManagedOrganizationListVo();
                vo.setId(shop.getShopId());
                vo.setType(2);
                vo.setLogo(null); // Shop 表没有 logo 字段
                vo.setName(shop.getShopName());
                // 拼接完整地址
                String location = (shop.getCity() != null ? shop.getCity() : "") +
                        (shop.getDistrict() != null ? shop.getDistrict() : "") +
                        (shop.getAddress() != null ? shop.getAddress() : "");
                vo.setLocation(location.isEmpty() ? null : location);
                result.add(vo);
            });
        }

        // 3-校友总会（目前系统中没有对应的表，暂不处理）
        if (type != null && type == 3) {
            log.warn("校友总会类型（type=3）暂未实现");
        }

        return result;
    }

    /**
     * 根据角色权限获取可管理的组织
     *
     * @param wxId 用户ID
     * @param type 组织类型（null-全部）
     * @return 组织列表
     */
    private List<ManagedOrganizationListVo> getManagedOrganizationsByRole(Long wxId, Integer type) {
        List<ManagedOrganizationListVo> result = new ArrayList<>();

        // 查询用户的组织角色
        LambdaQueryWrapper<RoleUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoleUser::getWxId, wxId)
                .isNotNull(RoleUser::getOrganizeId);

        // 如果指定了类型，添加类型过滤
        if (type != null) {
            // RoleUser.type: 1-校处会，2-校友会，3-商户，4-门店
            // 请求参数 type: 0-校友会，1-校促会，2-商户
            Integer roleUserType = convertTypeToRoleUserType(type);
            if (roleUserType != null) {
                if (roleUserType == 3) {
                    // 若前端请求商户(2)，则对应查询 role_user 中的商户(3)和门店(4)
                    queryWrapper.in(RoleUser::getType, java.util.Arrays.asList(3, 4));
                } else {
                    queryWrapper.eq(RoleUser::getType, roleUserType);
                }
            }
        }

        List<RoleUser> roleUsers = roleUserService.list(queryWrapper);

        if (roleUsers.isEmpty()) {
            log.info("用户无管理的组织 - 用户ID: {}", wxId);
            return result;
        }

        // 获取所有角色ID并批量查询角色信息
        List<Long> roleIds = roleUsers.stream()
                .map(RoleUser::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        List<Role> roles = roleService.listByIds(roleIds);

        // 创建角色ID到角色Code的映射
        Map<Long, String> roleIdToCodeMap = roles.stream()
                .collect(Collectors.toMap(Role::getRoleId, Role::getRoleCode));

        // 过滤出只有管理员角色的 RoleUser 记录
        List<RoleUser> adminRoleUsers = roleUsers.stream()
                .filter(roleUser -> {
                    String roleCode = roleIdToCodeMap.get(roleUser.getRoleId());
                    // 只保留管理员角色：校友会管理员、校处会管理员、商户管理员、门店管理员
                    return roleCode != null && (
                            "ORGANIZE_ALUMNI_ADMIN".equals(roleCode) ||
                                    "ORGANIZE_LOCAL_ADMIN".equals(roleCode) ||
                                    "ORGANIZE_MERCHANT_ADMIN".equals(roleCode) ||
                                    "ORGANIZE_SHOP_ADMIN".equals(roleCode)
                    );
                })
                .collect(Collectors.toList());

        if (adminRoleUsers.isEmpty()) {
            log.info("用户无管理员权限的组织 - 用户ID: {}", wxId);
            return result;
        }

        // 按类型分组
        Map<Integer, List<Long>> organizeIdsByType = adminRoleUsers.stream()
                .collect(Collectors.groupingBy(
                        RoleUser::getType,
                        Collectors.mapping(RoleUser::getOrganizeId, Collectors.toList())
                ));

        // 查询校友会（type=2）
        if (organizeIdsByType.containsKey(2)) {
            List<Long> associationIds = organizeIdsByType.get(2);
            if (!associationIds.isEmpty()) {
                List<AlumniAssociation> associations = alumniAssociationService.listByIds(associationIds);
                associations.forEach(association -> {
                    if (association.getStatus() == 1) { // 只返回启用的
                        ManagedOrganizationListVo vo = new ManagedOrganizationListVo();
                        vo.setId(association.getAlumniAssociationId());
                        vo.setType(0);
                        vo.setLogo(association.getLogo());
                        vo.setName(association.getAssociationName());
                        vo.setLocation(association.getLocation());
                        result.add(vo);
                    }
                });
            }
        }

        // 查询校促会（type=1）
        if (organizeIdsByType.containsKey(1)) {
            List<Long> platformIds = organizeIdsByType.get(1);
            if (!platformIds.isEmpty()) {
                List<LocalPlatform> platforms = localPlatformService.listByIds(platformIds);
                platforms.forEach(platform -> {
                    if (platform.getStatus() == 1) { // 只返回启用的
                        ManagedOrganizationListVo vo = new ManagedOrganizationListVo();
                        vo.setId(platform.getPlatformId());
                        vo.setType(1);
                        vo.setLogo(platform.getAvatar());
                        vo.setName(platform.getPlatformName());
                        vo.setLocation(platform.getCity());
                        result.add(vo);
                    }
                });
            }
        }

        // 查询商户（type=3）
        if (organizeIdsByType.containsKey(3)) {
            List<Long> merchantIds = organizeIdsByType.get(3);
            if (!merchantIds.isEmpty()) {
                List<com.cmswe.alumni.common.entity.Merchant> merchants = merchantService.listByIds(merchantIds);
                merchants.forEach(merchant -> {
                    if (merchant.getStatus() == 1 && merchant.getReviewStatus() == 1) { // 只返回启用且审核通过的
                        ManagedOrganizationListVo vo = new ManagedOrganizationListVo();
                        vo.setId(merchant.getMerchantId());
                        vo.setType(2); // 2-商户
                        vo.setLogo(merchant.getLogo());
                        vo.setName(merchant.getMerchantName());
                        vo.setLocation(null);
                        result.add(vo);
                    }
                });
            }
        }

        // 查询门店（type=4）
        if (organizeIdsByType.containsKey(4)) {
            List<Long> shopIds = organizeIdsByType.get(4);
            if (!shopIds.isEmpty()) {
                List<Shop> shops = shopService.listByIds(shopIds);
                shops.forEach(shop -> {
                    if (shop.getStatus() == 1 && shop.getReviewStatus() == 1) { // 只返回营业中且审核通过的
                        ManagedOrganizationListVo vo = new ManagedOrganizationListVo();
                        vo.setId(shop.getShopId());
                        vo.setType(2); // 门店在列表页也统一映射为商户类型(2)
                        vo.setLogo(null); // Shop 表没有 logo 字段
                        vo.setName(shop.getShopName());
                        // 拼接完整地址
                        String location = (shop.getCity() != null ? shop.getCity() : "") +
                                (shop.getDistrict() != null ? shop.getDistrict() : "") +
                                (shop.getAddress() != null ? shop.getAddress() : "");
                        vo.setLocation(location.isEmpty() ? null : location);
                        result.add(vo);
                    }
                });
            }
        }

        return result;
    }

    /**
     * 将请求参数的类型转换为 RoleUser 表的类型
     *
     * @param type 请求参数类型（0-校友会，1-校促会，2-商户，3-校友总会）
     * @return RoleUser 类型（1-校处会，2-校友会，3-商户，4-门店）
     */
    private Integer convertTypeToRoleUserType(Integer type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case 0 -> 2; // 校友会
            case 1 -> 1; // 校促会
            case 2 -> 3; // 商户
            case 3 -> null; // 校友总会（暂不支持）
            default -> null;
        };
    }
}




