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
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.dto.*;
import com.cmswe.alumni.common.vo.*;
import com.cmswe.alumni.common.vo.TagVo;
import com.cmswe.alumni.service.association.mapper.SchoolMapper;
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
    private SchoolMapper schoolMapper;

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

        //6.5.处理工作经历信息更新
        List<WxUserWorkDto> workExperienceList = updateDto.getWorkExperienceList();
        if (workExperienceList != null && !workExperienceList.isEmpty()) {
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
                School school = schoolMapper.selectById(alumniEducation.getSchoolId());
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
        //1. 获取用户信息
        if (wxId == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        //2. 查询配置开放的字段（用户隐私设置）TODO 这里配置的 key 不能写死，要写在枚举类或常量中
        Long sysUserPrivacySettingsConfigId = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, "user_privacy_setting")
        ).getConfigId();
        List<SysConfig> sysUserPrivacySettingsConfigs = sysConfigMapper.selectList(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getParentId, sysUserPrivacySettingsConfigId)
                        .eq(SysConfig::getStatus, 1)
        );

        //3. 查询个人设置中的字段，如果没有则直接从配置中取默认返回用户；然后封装为 List<Obj> 集合
        // TODO 这里的查询有一个逻辑需要优化：如果配置中和用户设置都存在一个字段，但是配置中设置了隐藏，那么用户是无法查询出来这个设置
        List<UserPrivacySetting> userPrivacySettings = sysUserPrivacySettingsConfigs.stream().map(sysConfig -> {
            UserPrivacySetting userPrivacySettingServiceOne = userPrivacySettingService.getOne(
                    new LambdaQueryWrapper<UserPrivacySetting>()
                            .eq(UserPrivacySetting::getWxId, wxId)
                            .eq(UserPrivacySetting::getFieldCode, sysConfig.getConfigKey())
            );
            if (userPrivacySettingServiceOne == null) {
                userPrivacySettingServiceOne = new UserPrivacySetting();
                userPrivacySettingServiceOne.setWxId(wxId);
                userPrivacySettingServiceOne.setFieldName(sysConfig.getConfigName());
                userPrivacySettingServiceOne.setFieldCode(sysConfig.getConfigKey());
                userPrivacySettingServiceOne.setType(1);            // 用户配置
                userPrivacySettingServiceOne.setVisibility(0);      // 默认可见
                userPrivacySettingServiceOne.setSearchable(0);      // 默认可见

                userPrivacySettingService.save(userPrivacySettingServiceOne);
            }
            return userPrivacySettingServiceOne;
        }).toList();

//        List<UserPrivacySetting> userPrivacySettings = userPrivacySettingService.getByUserId(loginUserId);

        //3. 封装 vo 返回
        List<UserPrivacySettingListVo> userPrivacySettingListVos = userPrivacySettings.stream().map(userPrivacySetting -> {
            UserPrivacySettingListVo userPrivacySettingListVo = UserPrivacySettingListVo.objToVo(userPrivacySetting);
            userPrivacySettingListVo.setWxId(String.valueOf(userPrivacySetting.getWxId()));
            userPrivacySettingListVo.setUserPrivacySettingId(String.valueOf(userPrivacySetting.getUserPrivacySettingId()));
            return userPrivacySettingListVo;
        }).toList();

        log.info("查询用户的隐私设置，用户id：{} ", wxId);
        return userPrivacySettingListVos;
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
                School school = schoolMapper.selectById(alumniEducation.getSchoolId());
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

        //4. 查询 wx_users 表获取 is_alumni 字段
        WxUser wxUser = wxUserMapper.selectById(id);
        if (wxUser != null) {
            alumniDetailVo.setIsAlumni(wxUser.getIsAlumni() != null && wxUser.getIsAlumni() == 1);
        } else {
            alumniDetailVo.setIsAlumni(false);
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
    public Page<UserListResponse> queryAlumniList(QueryAlumniListDto queryAlumniListDto) {
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
        int current = queryAlumniListDto.getCurrent();
        int pageSize = queryAlumniListDto.getPageSize();
        String sortField = queryAlumniListDto.getSortField();
        String sortOrder = queryAlumniListDto.getSortOrder();

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

        // 排序：先按指定字段排序，再按主键排序（确保排序稳定，避免分页重复）
        if ("createdTime".equals(sortField)) {
            queryWrapper.orderBy(true, CommonConstant.SORT_ORDER_ASC.equals(sortOrder), WxUserInfo::getCreatedTime);
        }
        queryWrapper.orderByDesc(WxUserInfo::getWxId);

        Page<WxUserInfo> wxUserInfoPage = wxUserInfoMapper.selectPage(new Page<>(current, pageSize), queryWrapper);

        // 批量查询主要教育经历和校友状态
        Map<Long, AlumniEducationListVo> primaryEducationMap = new HashMap<>();
        Map<Long, Boolean> isAlumniMap = new HashMap<>();

        if (!wxUserInfoPage.getRecords().isEmpty()) {
            List<Long> wxIds = wxUserInfoPage.getRecords().stream()
                    .map(WxUserInfo::getWxId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询 wx_users 表获取 is_alumni 字段
            List<WxUser> wxUsers = wxUserMapper.selectBatchIds(wxIds);
            isAlumniMap = wxUsers.stream()
                    .collect(Collectors.toMap(
                            WxUser::getWxId,
                            wxUser -> wxUser.getIsAlumni() != null && wxUser.getIsAlumni() == 1,
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

        Map<Long, AlumniEducationListVo> finalPrimaryEducationMap = primaryEducationMap;
        Map<Long, Boolean> finalIsAlumniMap = isAlumniMap;

        List<UserListResponse> userListResponses = wxUserInfoPage.getRecords().stream().map(wxUserInfo -> {
            UserListResponse userListResponse = UserListResponse.ObjToVo(wxUserInfo);
            userListResponse.setWxId(String.valueOf(wxUserInfo.getWxId()));

            // 获取用户标签列表
            List<SysTag> userTags = sysTagRelationService.getTagsByTarget(wxUserInfo.getWxId(), 1); // targetType=1 表示校友(User)
            List<TagVo> tagVoList = userTags.stream()
                    .map(TagVo::objToVo)
                    .toList();
            userListResponse.setTagList(tagVoList);

            // 设置主要教育经历
            userListResponse.setPrimaryEducation(finalPrimaryEducationMap.get(wxUserInfo.getWxId()));

            // 设置校友认证状态
            userListResponse.setIsAlumni(finalIsAlumniMap.getOrDefault(wxUserInfo.getWxId(), false));

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
     * 更新用户的工作经历信息
     *
     * @param wxId               用户ID
     * @param workExperienceList 工作经历列表
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

        // 删除该用户现有的所有工作经历（逻辑删除）
        wxUserWorkMapper.delete(new LambdaQueryWrapper<WxUserWork>()
                .eq(WxUserWork::getWxId, wxId));

        // 插入或更新新的工作经历
        for (WxUserWorkDto workDto : workExperienceList) {
            WxUserWork wxUserWork = new WxUserWork();
            BeanUtils.copyProperties(workDto, wxUserWork);
            wxUserWork.setWxId(wxId);

            // 如果离职日期为空且未明确标记为在职，则默认设置为在职
            if (wxUserWork.getEndDate() == null && wxUserWork.getIsCurrent() == null) {
                wxUserWork.setIsCurrent(1);
            }

            if (workDto.getUserWorkId() != null) {
                // 如果有ID，则更新
                wxUserWork.setUserWorkId(workDto.getUserWorkId());
                wxUserWorkMapper.updateById(wxUserWork);
                log.info("更新工作经历成功，用户ID: {}, 工作经历ID: {}", wxId, workDto.getUserWorkId());
            } else {
                // 如果没有ID，则新增
                wxUserWorkMapper.insert(wxUserWork);
                log.info("新增工作经历成功，用户ID: {}", wxId);
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
            School school = schoolMapper.selectById(educationDto.getSchoolId());
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
}




