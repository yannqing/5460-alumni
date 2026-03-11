package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationApplicationService;
import com.cmswe.alumni.api.user.OrganizeArchiTemplateService;
import com.cmswe.alumni.api.user.UnifiedMessageApiService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.dto.ApplyCreateAlumniAssociationDto;
import com.cmswe.alumni.common.dto.InitialMemberDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationApplicationListDto;
import com.cmswe.alumni.common.dto.QuerySystemAdminApplicationListDto;
import com.cmswe.alumni.common.dto.ReviewAlumniAssociationApplicationDto;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.entity.OrganizeArchiTemplate;
import com.cmswe.alumni.common.enums.NotificationType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniAssociationApplicationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationApplicationListVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.SchoolListVo;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationApplicationMapper;
import com.cmswe.alumni.service.association.mapper.SchoolMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 校友会创建申请服务实现类
 */
@Slf4j
@Service
public class AlumniAssociationApplicationServiceImpl
        extends ServiceImpl<AlumniAssociationApplicationMapper, AlumniAssociationApplication>
        implements AlumniAssociationApplicationService {

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Resource
    private UnifiedMessageApiService unifiedMessageApiService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private SchoolMapper schoolMapper;

    @Resource
    private com.cmswe.alumni.api.user.RoleService roleService;

    @Resource
    private com.cmswe.alumni.api.user.RoleUserService roleUserService;

    @Resource
    private com.cmswe.alumni.api.user.OrganizeArchiRoleService organizeArchiRoleService;

    @Resource
    private com.cmswe.alumni.api.association.AlumniAssociationService alumniAssociationService;

    @Resource
    private com.cmswe.alumni.api.association.AlumniAssociationMemberService alumniAssociationMemberService;

    @Resource
    private OrganizeArchiTemplateService organizeArchiTemplateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyToCreateAssociation(Long wxId, ApplyCreateAlumniAssociationDto applyDto) {
        // 1. 验证申请人是否存在
        LambdaQueryWrapper<WxUserInfo> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(WxUserInfo::getWxId, wxId);
        WxUserInfo applicant = wxUserInfoService.getOne(userQuery);
        if (applicant == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "申请人不存在");
        }

        // 2. 验证负责人是否存在（如果提供了负责人ID）
        if (applyDto.getChargeWxId() != null && applyDto.getChargeWxId() > 0) {
            LambdaQueryWrapper<WxUserInfo> chargeQuery = new LambdaQueryWrapper<>();
            chargeQuery.eq(WxUserInfo::getWxId, applyDto.getChargeWxId());
            WxUserInfo charge = wxUserInfoService.getOne(chargeQuery);
            if (charge == null) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "指定的负责人用户不存在");
            }
        }

        // 2.1 验证驻会代表（联系人）是否存在
        if (applyDto.getZhWxId() == null || applyDto.getZhWxId() <= 0) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "驻会代表（联系人）用户ID不能为空");
        }
        LambdaQueryWrapper<WxUserInfo> zhQuery = new LambdaQueryWrapper<>();
        zhQuery.eq(WxUserInfo::getWxId, applyDto.getZhWxId());
        WxUserInfo zhUser = wxUserInfoService.getOne(zhQuery);
        if (zhUser == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "指定的驻会代表（联系人）用户不存在");
        }

        // 3. 验证初始成员列表（如果提供了微信ID，则验证用户是否存在）
        if (applyDto.getInitialMembers() != null && !applyDto.getInitialMembers().isEmpty()) {
            for (InitialMemberDto memberDto : applyDto.getInitialMembers()) {
                // 如果提供了微信ID，则进行系统校验
                if (memberDto.getWxId() != null && memberDto.getWxId() > 0) {
                    LambdaQueryWrapper<WxUserInfo> memberQuery = new LambdaQueryWrapper<>();
                    memberQuery.eq(WxUserInfo::getWxId, memberDto.getWxId());
                    WxUserInfo member = wxUserInfoService.getOne(memberQuery);
                    if (member == null) {
                        throw new BusinessException(ErrorType.ARGS_ERROR,
                                "初始成员【" + memberDto.getName() + "】(ID: " + memberDto.getWxId() + ")在系统中不存在");
                    }
                }
            }
            log.info("初始成员列表验证通过 - 成员数: {}", applyDto.getInitialMembers().size());
        }

        // 4. 检查是否已有相同学校和地点的待审核申请
        // LambdaQueryWrapper<AlumniAssociationApplication> checkQuery = new
        // LambdaQueryWrapper<>();
        // checkQuery.eq(AlumniAssociationApplication::getSchoolId,
        // applyDto.getSchoolId())
        // .eq(AlumniAssociationApplication::getLocation, applyDto.getLocation())
        // .eq(AlumniAssociationApplication::getApplicationStatus, 0); // 待审核
        // Long existingCount = this.count(checkQuery);
        // if (existingCount > 0) {
        // throw new BusinessException(ErrorType.ARGS_ERROR,
        // "该学校和地点已有待审核的校友会创建申请，请勿重复提交");
        // }

        // 4. 创建申请记录
        AlumniAssociationApplication application = new AlumniAssociationApplication();
        application.setAssociationName(applyDto.getAssociationName());
        application.setSchoolId(applyDto.getSchoolId());
        application.setPlatformId(applyDto.getPlatformId());
        application.setChargeWxId(applyDto.getChargeWxId());
        application.setChargeName(applyDto.getChargeName());
        application.setChargeRole(applyDto.getChargeRole());
        application.setContactInfo(applyDto.getContactInfo());
        application.setMsocialAffiliation(applyDto.getMsocialAffiliation());
        application.setZhName(applyDto.getZhName());
        application.setZhRole(applyDto.getZhRole());
        application.setZhPhone(applyDto.getZhPhone());
        application.setZhSocialAffiliation(applyDto.getZhSocialAffiliation());
        application.setZhWxId(applyDto.getZhWxId());

        // 将背景图列表转换为 JSON 字符串
        if (applyDto.getBgImg() != null && !applyDto.getBgImg().isEmpty()) {
            try {
                String bgImgJson = objectMapper.writeValueAsString(applyDto.getBgImg());
                application.setBgImg(bgImgJson);
                log.info("申请中的背景图数据: {}", bgImgJson);
            } catch (JsonProcessingException e) {
                log.error("转换背景图列表为JSON失败", e);
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "背景图信息处理失败");
            }
        } else {
            application.setBgImg(null);
        }

        application.setLocation(applyDto.getLocation());
        application.setLogo(applyDto.getLogo());
        application.setApplicationReason(applyDto.getApplicationReason());
        application.setAssociationProfile(applyDto.getAssociationProfile());
        application.setTemplateId(applyDto.getTemplateId());
        application.setApplicationStatus(0); // 0-待审核
        application.setApplyTime(LocalDateTime.now());

        // 5. 将初始成员列表转换为JSON字符串
        if (applyDto.getInitialMembers() != null && !applyDto.getInitialMembers().isEmpty()) {
            try {
                String initialMembersJson = objectMapper.writeValueAsString(applyDto.getInitialMembers());
                application.setInitialMembers(initialMembersJson);
            } catch (JsonProcessingException e) {
                log.error("转换初始成员列表为JSON失败", e);
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "初始成员信息处理失败");
            }
        }

        // 6. 将附件ID列表转换为JSON字符串
        if (applyDto.getAttachmentIds() != null && !applyDto.getAttachmentIds().isEmpty()) {
            try {
                String attachmentIdsJson = objectMapper.writeValueAsString(applyDto.getAttachmentIds());
                application.setAttachmentIds(attachmentIdsJson);
            } catch (JsonProcessingException e) {
                log.error("转换附件ID列表为JSON失败", e);
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "附件信息处理失败");
            }
        }

        // 7. 保存申请记录
        boolean result = this.save(application);

        if (result) {
            log.info("用户{}成功提交创建校友会申请：{}", wxId, applyDto.getAssociationName());

            // 8. 发送申请提交成功通知
            sendApplicationSubmittedNotification(wxId, applyDto.getAssociationName());
        }

        return result;
    }

    /**
     * 发送申请提交成功通知
     *
     * @param wxId            申请人用户ID
     * @param associationName 校友会名称
     */
    private void sendApplicationSubmittedNotification(Long wxId, String associationName) {
        try {
            String title = "校友会创建申请已提交";
            String content = "您的创建【" + associationName + "】校友会申请已经提交，请耐心等待审核";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    null,
                    "ASSOCIATION_APPLICATION");

            if (success) {
                log.info("校友会创建申请提交通知已发送 - 用户: {}, 校友会: {}", wxId, associationName);
            } else {
                log.error("校友会创建申请提交通知发送失败 - 用户: {}, 校友会: {}", wxId, associationName);
            }

        } catch (Exception e) {
            log.error("发送校友会创建申请提交通知异常 - 用户: {}, 校友会: {}, Error: {}",
                    wxId, associationName, e.getMessage(), e);
        }
    }

    @Override
    public PageVo<AlumniAssociationApplicationListVo> queryApplicationPage(
            QueryAlumniAssociationApplicationListDto queryDto) {
        // 1. 参数校验
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "查询参数不能为空"));

        if (queryDto.getPlatformId() == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "校处会ID不能为空");
        }

        // 2. 获取查询参数
        Long platformId = queryDto.getPlatformId();
        String associationName = queryDto.getAssociationName();
        String chargeName = queryDto.getChargeName();
        String location = queryDto.getLocation();
        Integer applicationStatus = queryDto.getApplicationStatus();
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();

        // 3. 构建查询条件
        LambdaQueryWrapper<AlumniAssociationApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(AlumniAssociationApplication::getPlatformId, platformId)
                .like(StringUtils.isNotBlank(associationName), AlumniAssociationApplication::getAssociationName,
                        associationName)
                .like(StringUtils.isNotBlank(chargeName), AlumniAssociationApplication::getChargeName, chargeName)
                .like(StringUtils.isNotBlank(location), AlumniAssociationApplication::getLocation, location)
                .eq(applicationStatus != null, AlumniAssociationApplication::getApplicationStatus, applicationStatus)
                .orderByDesc(AlumniAssociationApplication::getApplyTime);

        // 4. 执行分页查询
        Page<AlumniAssociationApplication> applicationPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 5. 如果记录为空，直接返回
        if (applicationPage.getRecords().isEmpty()) {
            Page<AlumniAssociationApplicationListVo> emptyPage = new Page<>(current, pageSize);
            emptyPage.setTotal(0);
            return PageVo.of(emptyPage);
        }

        // 6. 提取所有 schoolId 并批量查询学校信息
        List<Long> schoolIds = applicationPage.getRecords().stream()
                .map(AlumniAssociationApplication::getSchoolId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, School> schoolMap = schoolMapper.selectBatchIds(schoolIds).stream()
                .collect(Collectors.toMap(School::getSchoolId, Function.identity(), (v1, v2) -> v1));

        // 7. 转换为VO对象
        List<AlumniAssociationApplicationListVo> voList = applicationPage.getRecords().stream()
                .map(application -> {
                    AlumniAssociationApplicationListVo vo = AlumniAssociationApplicationListVo.objToVo(application);

                    // 设置学校信息
                    School school = schoolMap.get(application.getSchoolId());
                    if (school != null) {
                        SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
                        schoolListVo.setSchoolId(String.valueOf(school.getSchoolId()));
                        vo.setSchoolInfo(schoolListVo);
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        // 8. 构建分页结果
        Page<AlumniAssociationApplicationListVo> resultPage = new Page<>(current, pageSize, applicationPage.getTotal());
        resultPage.setRecords(voList);

        log.info("分页查询校友会创建申请列表成功 - 校处会ID: {}, 总记录数: {}, 当前页: {}, 每页大小: {}",
                platformId, resultPage.getTotal(), current, pageSize);

        return PageVo.of(resultPage);
    }

    @Override
    public PageVo<AlumniAssociationApplicationListVo> querySystemAdminApplicationPage(
            QuerySystemAdminApplicationListDto queryDto) {
        // 1. 参数校验
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "查询参数不能为空"));

        // 2. 获取查询参数
        Long schoolId = queryDto.getSchoolId();
        Long platformId = queryDto.getPlatformId();
        String associationName = queryDto.getAssociationName();
        String chargeName = queryDto.getChargeName();
        String location = queryDto.getLocation();
        Integer applicationStatus = queryDto.getApplicationStatus();
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();

        // 3. 构建查询条件
        LambdaQueryWrapper<AlumniAssociationApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(schoolId != null, AlumniAssociationApplication::getSchoolId, schoolId)
                .eq(platformId != null, AlumniAssociationApplication::getPlatformId, platformId)
                .like(StringUtils.isNotBlank(associationName), AlumniAssociationApplication::getAssociationName,
                        associationName)
                .like(StringUtils.isNotBlank(chargeName), AlumniAssociationApplication::getChargeName, chargeName)
                .like(StringUtils.isNotBlank(location), AlumniAssociationApplication::getLocation, location)
                .eq(applicationStatus != null, AlumniAssociationApplication::getApplicationStatus, applicationStatus)
                .orderByDesc(AlumniAssociationApplication::getApplyTime);

        // 4. 执行分页查询
        Page<AlumniAssociationApplication> applicationPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 5. 如果记录为空，直接返回
        if (applicationPage.getRecords().isEmpty()) {
            Page<AlumniAssociationApplicationListVo> emptyPage = new Page<>(current, pageSize);
            emptyPage.setTotal(0);
            return PageVo.of(emptyPage);
        }

        // 6. 提取所有 schoolId 并批量查询学校信息
        List<Long> schoolIds = applicationPage.getRecords().stream()
                .map(AlumniAssociationApplication::getSchoolId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, School> schoolMap = schoolMapper.selectBatchIds(schoolIds).stream()
                .collect(Collectors.toMap(School::getSchoolId, Function.identity(), (v1, v2) -> v1));

        // 7. 转换为VO对象
        List<AlumniAssociationApplicationListVo> voList = applicationPage.getRecords().stream()
                .map(application -> {
                    AlumniAssociationApplicationListVo vo = AlumniAssociationApplicationListVo.objToVo(application);

                    // 设置学校信息
                    School school = schoolMap.get(application.getSchoolId());
                    if (school != null) {
                        SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
                        schoolListVo.setSchoolId(String.valueOf(school.getSchoolId()));
                        vo.setSchoolInfo(schoolListVo);
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        // 8. 构建分页结果
        Page<AlumniAssociationApplicationListVo> resultPage = new Page<>(current, pageSize, applicationPage.getTotal());
        resultPage.setRecords(voList);

        log.info("系统管理员分页查询校友会创建申请列表成功 - 总记录数: {}, 当前页: {}, 每页大小: {}",
                resultPage.getTotal(), current, pageSize);

        return PageVo.of(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewApplication(Long reviewerId, ReviewAlumniAssociationApplicationDto reviewDto) {
        // 1. 参数校验
        Optional.ofNullable(reviewDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "审核信息不能为空"));

        if (reviewDto.getReviewResult() == null || (reviewDto.getReviewResult() != 1 && reviewDto.getReviewResult() != 2)) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "审核结果必须为1(通过)或2(拒绝)");
        }

        if (reviewDto.getReviewResult() == 2 && StringUtils.isBlank(reviewDto.getReviewComment())) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "拒绝申请时必须填写拒绝原因");
        }

        // 2. 查询申请记录
        AlumniAssociationApplication application = this.getById(reviewDto.getApplicationId());
        if (application == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "申请记录不存在");
        }

        // 3. 验证申请状态（只有待审核状态才能审核）
        if (application.getApplicationStatus() != 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该申请已被审核，无法重复审核");
        }

        // 4. 审核拒绝逻辑
        if (reviewDto.getReviewResult() == 2) {
            application.setApplicationStatus(2); // 已拒绝
            application.setReviewerId(reviewerId);
            application.setReviewTime(LocalDateTime.now());
            application.setReviewComment(reviewDto.getReviewComment());

            boolean updateResult = this.updateById(application);
            if (updateResult) {
                log.info("校友会创建申请审核拒绝 - 申请ID: {}, 审核人: {}", application.getApplicationId(), reviewerId);
                // 发送拒绝通知
                sendReviewRejectedNotification(application.getChargeWxId(), application.getAssociationName(), reviewDto.getReviewComment());
            }
            return updateResult;
        }

        // 5. 审核通过逻辑
        try {
            // 5.1 创建校友会
            AlumniAssociation alumniAssociation = new AlumniAssociation();
            alumniAssociation.setAssociationName(application.getAssociationName());
            alumniAssociation.setSchoolId(application.getSchoolId());
            alumniAssociation.setPlatformId(application.getPlatformId());
            alumniAssociation.setContactInfo(application.getContactInfo());
            alumniAssociation.setBgImg(application.getBgImg()); // 同步背景图
            alumniAssociation.setLocation(application.getLocation());
            alumniAssociation.setLogo(application.getLogo());
            alumniAssociation.setAssociationProfile(application.getAssociationProfile()); // 同步校友会简介

            // 同步主要负责人信息
            alumniAssociation.setChargeWxId(application.getChargeWxId());
            alumniAssociation.setChargeName(application.getChargeName());
            alumniAssociation.setChargeRole(application.getChargeRole());
            alumniAssociation.setChargeSocialAffiliation(application.getMsocialAffiliation());

            // 同步驻会代表信息
            alumniAssociation.setZhWxId(application.getZhWxId());
            alumniAssociation.setZhName(application.getZhName());
            alumniAssociation.setZhPhone(application.getZhPhone());
            alumniAssociation.setZhRole(application.getZhRole());
            alumniAssociation.setZhSocialAffiliation(application.getZhSocialAffiliation());

            alumniAssociation.setStatus(1); // 启用
            alumniAssociation.setMemberCount(0); // 初始为0，后续更新

            boolean createAssociationResult = alumniAssociationService.save(alumniAssociation);
            if (!createAssociationResult) {
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "创建校友会失败");
            }

            Long alumniAssociationId = alumniAssociation.getAlumniAssociationId();
            log.info("创建校友会成功 - 校友会ID: {}, 名称: {}", alumniAssociationId, alumniAssociation.getAssociationName());

            // 5.2 查找组织管理员角色
            Role organizeAdminRole = roleService.getRoleByCodeInner("ORGANIZE_ALUMNI_ADMIN");
            if (organizeAdminRole == null) {
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "未找到组织管理员角色");
            }

            // 5.3 插入驻会代表（联系人）到 role_user 表作为管理员
            if (application.getZhWxId() == null || application.getZhWxId() <= 0) {
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "驻会代表（联系人）用户ID无效，无法分配管理员角色");
            }
            RoleUser roleUser = new RoleUser();
            roleUser.setWxId(application.getZhWxId());
            roleUser.setRoleId(organizeAdminRole.getRoleId());
            roleUser.setType(2);
            roleUser.setOrganizeId(alumniAssociationId);
            boolean assignRoleResult = roleUserService.save(roleUser);
            if (!assignRoleResult) {
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "分配组织管理员角色失败");
            }
            log.info("为驻会代表（联系人）分配组织管理员角色 - 用户ID: {}, 角色ID: {}", application.getZhWxId(), organizeAdminRole.getRoleId());

            // 5.4 插入负责人到校友会成员表（若wxid有效，不创建架构角色，role_or_id 为空）；默认在主页展示
            int totalMemberCount = 0;
            if (application.getChargeWxId() != null && application.getChargeWxId() > 0) {
                AlumniAssociationMember chargeMember = new AlumniAssociationMember();
                chargeMember.setWxId(application.getChargeWxId());
                chargeMember.setAlumniAssociationId(alumniAssociationId);
                chargeMember.setUsername(application.getChargeName());
                chargeMember.setRoleName(application.getChargeRole());
                chargeMember.setUserPhone(application.getContactInfo()); // 负责人联系方式
                chargeMember.setUserAffiliation(application.getMsocialAffiliation()); // 负责人社会职务
                chargeMember.setIsShowOnHome(1); // 主要负责人默认在主页展示
                chargeMember.setJoinTime(LocalDateTime.now());
                chargeMember.setStatus(1);
                boolean addChargeMemberResult = alumniAssociationMemberService.save(chargeMember);
                if (!addChargeMemberResult) {
                    throw new BusinessException(ErrorType.SYSTEM_ERROR, "添加负责人到成员表失败");
                }
                totalMemberCount++;
                log.info("添加负责人到成员表成功 - 用户ID: {}", application.getChargeWxId());
            } else {
                log.info("负责人未绑定微信，跳过添加成员记录 - 姓名: {}", application.getChargeName());
            }

            // 5.5 添加驻会代表到校友会成员表（若zh_wx_id有效且与负责人不同人）
            if (application.getZhWxId() != null && application.getZhWxId() > 0
                    && !application.getZhWxId().equals(application.getChargeWxId())) {
                // 检查是否已存在（负责人已添加）
                LambdaQueryWrapper<AlumniAssociationMember> zhMemberCheck = new LambdaQueryWrapper<>();
                zhMemberCheck.eq(AlumniAssociationMember::getWxId, application.getZhWxId())
                        .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId);
                long zhExists = alumniAssociationMemberService.count(zhMemberCheck);
                if (zhExists == 0) {
                    // 验证驻会代表用户是否存在
                    LambdaQueryWrapper<WxUserInfo> zhUserCheck = new LambdaQueryWrapper<>();
                    zhUserCheck.eq(WxUserInfo::getWxId, application.getZhWxId());
                    WxUserInfo zhUser = wxUserInfoService.getOne(zhUserCheck);
                    if (zhUser != null) {
                        AlumniAssociationMember zhMember = new AlumniAssociationMember();
                        zhMember.setWxId(application.getZhWxId());
                        zhMember.setAlumniAssociationId(alumniAssociationId);
                        zhMember.setUsername(application.getZhName());
                        zhMember.setRoleName(application.getZhRole());
                        zhMember.setUserPhone(application.getZhPhone());
                        zhMember.setUserAffiliation(application.getZhSocialAffiliation());
                        zhMember.setIsShowOnHome(1); // 驻会代表（主要联系人）默认在主页展示
                        zhMember.setJoinTime(LocalDateTime.now());
                        zhMember.setStatus(1);
                        boolean addZhMemberResult = alumniAssociationMemberService.save(zhMember);
                        if (addZhMemberResult) {
                            totalMemberCount++;
                            log.info("添加驻会代表到成员表成功 - 用户ID: {}", application.getZhWxId());
                        }
                    } else {
                        log.warn("驻会代表用户不存在，跳过添加成员 - wxId: {}", application.getZhWxId());
                    }
                }
            } else if (application.getZhWxId() != null && application.getZhWxId() > 0
                    && application.getZhWxId().equals(application.getChargeWxId())) {
                // 驻会代表与负责人同一人，负责人已在成员表中，无需重复添加
                log.info("驻会代表与负责人同一人，已包含在成员表中 - 用户ID: {}", application.getZhWxId());
            }

            // 5.6 处理初始成员列表
            if (StringUtils.isNotBlank(application.getInitialMembers())) {
                try {
                    List<InitialMemberDto> initialMembers = objectMapper.readValue(
                            application.getInitialMembers(),
                            new TypeReference<List<InitialMemberDto>>() {}
                    );

                    for (InitialMemberDto memberDto : initialMembers) {
                        // 如果提供了微信ID，验证用户是否存在
                        if (memberDto.getWxId() != null && memberDto.getWxId() > 0) {
                            LambdaQueryWrapper<WxUserInfo> memberCheckQuery = new LambdaQueryWrapper<>();
                            memberCheckQuery.eq(WxUserInfo::getWxId, memberDto.getWxId());
                            WxUserInfo existingMember = wxUserInfoService.getOne(memberCheckQuery);
                            if (existingMember == null) {
                                log.warn("跳过不存在的初始成员 - wxId: {}, name: {}", memberDto.getWxId(), memberDto.getName());
                                continue;
                            }
                        }

                        // 插入成员到校友会成员表（即使没有微信ID，也作为预设成员插入）
                        AlumniAssociationMember member = new AlumniAssociationMember();
                        member.setWxId(memberDto.getWxId());
                        member.setAlumniAssociationId(alumniAssociationId);
                        member.setUsername(memberDto.getName()); // 设置成员姓名
                        member.setRoleName(memberDto.getRole()); // 设置成员角色
                        member.setUserPhone(memberDto.getPhone()); // 成员联系电话
                        member.setUserAffiliation(memberDto.getAffiliation()); // 成员社会职务
                        member.setJoinTime(LocalDateTime.now());
                        member.setStatus(1);
                        alumniAssociationMemberService.save(member);

                        totalMemberCount++;
                    }

                    log.info("处理初始成员列表完成 - 校友会ID: {}, 初始成员数: {}", alumniAssociationId, initialMembers.size());
                } catch (JsonProcessingException e) {
                    log.error("解析初始成员列表失败", e);
                    throw new BusinessException(ErrorType.SYSTEM_ERROR, "解析初始成员信息失败");
                }
            }

            // 5.6.5 根据模板创建组织架构
            if (application.getTemplateId() != null) {
                try {
                    createOrganizeArchitecture(application.getTemplateId(), alumniAssociationId);
                    log.info("根据模板创建组织架构成功 - 校友会ID: {}, 模板ID: {}",
                            alumniAssociationId, application.getTemplateId());
                } catch (Exception e) {
                    log.error("根据模板创建组织架构失败 - 校友会ID: {}, 模板ID: {}",
                            alumniAssociationId, application.getTemplateId(), e);
                    throw new BusinessException(ErrorType.SYSTEM_ERROR, "创建组织架构失败: " + e.getMessage());
                }
            } else {
                log.info("未选择组织架构模板，跳过架构创建 - 校友会ID: {}", alumniAssociationId);
            }

            // 5.7 更新校友会会员数量
            alumniAssociation.setMemberCount(totalMemberCount);
            alumniAssociationService.updateById(alumniAssociation);
            log.info("更新校友会会员数量 - 校友会ID: {}, 会员数: {}", alumniAssociationId, totalMemberCount);

            // 5.8 更新申请记录
            application.setApplicationStatus(1); // 已通过
            application.setReviewerId(reviewerId);
            application.setReviewTime(LocalDateTime.now());
            application.setReviewComment(reviewDto.getReviewComment());
            application.setCreatedAssociationId(alumniAssociationId);
            this.updateById(application);

            log.info("校友会创建申请审核通过 - 申请ID: {}, 审核人: {}, 创建的校友会ID: {}",
                    application.getApplicationId(), reviewerId, alumniAssociationId);

            // 5.9 发送审核通过通知（发送给新管理员：驻会代表）
            sendReviewApprovedNotification(application.getZhWxId(), application.getAssociationName());
            
            // 如果负责人也绑定了微信，也给他发一份
            if (application.getChargeWxId() != null && application.getChargeWxId() > 0) {
                sendReviewApprovedNotification(application.getChargeWxId(), application.getAssociationName());
            }

            return true;

        } catch (BusinessException e) {
            log.error("审核校友会创建申请失败 - 申请ID: {}, 错误: {}", reviewDto.getApplicationId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("审核校友会创建申请异常 - 申请ID: {}, 错误: {}", reviewDto.getApplicationId(), e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "审核处理异常：" + e.getMessage());
        }
    }

    /**
     * 发送审核通过通知
     */
    private void sendReviewApprovedNotification(Long chargeWxId, String associationName) {
        try {
            String title = "校友会创建申请审核通过";
            String content = "恭喜！您申请创建的【" + associationName + "】校友会已审核通过";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    chargeWxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    null,
                    "ASSOCIATION_APPLICATION_APPROVED");

            if (success) {
                log.info("校友会创建申请通过通知已发送 - 用户: {}, 校友会: {}", chargeWxId, associationName);
            } else {
                log.error("校友会创建申请通过通知发送失败 - 用户: {}, 校友会: {}", chargeWxId, associationName);
            }
        } catch (Exception e) {
            log.error("发送校友会创建申请通过通知异常 - 用户: {}, 校友会: {}, Error: {}",
                    chargeWxId, associationName, e.getMessage(), e);
        }
    }

    /**
     * 发送审核拒绝通知
     */
    private void sendReviewRejectedNotification(Long chargeWxId, String associationName, String reviewComment) {
        try {
            String title = "校友会创建申请被拒绝";
            String content = "很抱歉，您申请创建的【" + associationName + "】校友会未通过审核。拒绝原因：" + reviewComment;

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    chargeWxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    null,
                    "ASSOCIATION_APPLICATION_REJECTED");

            if (success) {
                log.info("校友会创建申请拒绝通知已发送 - 用户: {}, 校友会: {}", chargeWxId, associationName);
            } else {
                log.error("校友会创建申请拒绝通知发送失败 - 用户: {}, 校友会: {}", chargeWxId, associationName);
            }
        } catch (Exception e) {
            log.error("发送校友会创建申请拒绝通知异常 - 用户: {}, 校友会: {}, Error: {}",
                    chargeWxId, associationName, e.getMessage(), e);
        }
    }

    /**
     * 根据申请ID查询申请详情
     *
     * @param applicationId 申请ID
     * @return 申请详情
     */
    @Override
    public AlumniAssociationApplicationDetailVo getApplicationDetailById(Long applicationId) {
        // 1. 参数校验
        Optional.ofNullable(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "申请ID不能为空"));

        // 2. 查询申请记录
        AlumniAssociationApplication application = this.getById(applicationId);
        Optional.ofNullable(application)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND_ERROR, "申请记录不存在"));

        // 3. 转换为VO
        AlumniAssociationApplicationDetailVo detailVo = AlumniAssociationApplicationDetailVo.objToVo(application);

        // 4. 查询并设置学校信息
        if (application.getSchoolId() != null) {
            School school = schoolMapper.selectById(application.getSchoolId());
            if (school != null) {
                SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
                schoolListVo.setSchoolId(String.valueOf(school.getSchoolId()));
                detailVo.setSchoolInfo(schoolListVo);
            }
        }

        log.info("查询申请详情成功 - 申请ID: {}", applicationId);
        return detailVo;
    }

    /**
     * 根据模板创建组织架构
     *
     * @param templateId          模板ID
     * @param alumniAssociationId 校友会ID
     */
    private void createOrganizeArchitecture(Long templateId, Long alumniAssociationId) {
        // 1. 查询模板
        OrganizeArchiTemplate template = organizeArchiTemplateService.getById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "组织架构模板不存在");
        }

        if (template.getStatus() != 1) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该模板已被禁用");
        }

        // 2. 解析模板JSON
        try {
            List<Map<String, Object>> templateNodes = objectMapper.readValue(
                    template.getTemplateJson(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // 3. 创建节点ID映射（旧ID -> 新ID）
            Map<String, Long> nodeIdMapping = new HashMap<>();

            // 4. 遍历模板节点，创建组织架构角色
            for (Map<String, Object> nodeData : templateNodes) {
                OrganizeArchiRole role = new OrganizeArchiRole();

                // 生成新的角色ID
                Long newRoleId = generateRoleId();
                role.setRoleOrId(newRoleId);

                // 处理父节点ID
                Object pidObj = nodeData.get("pid");
                if (pidObj != null && !"null".equals(String.valueOf(pidObj))) {
                    String oldPid = String.valueOf(pidObj);
                    Long newPid = nodeIdMapping.get(oldPid);
                    role.setPid(newPid);
                } else {
                    role.setPid(null);
                }

                // 设置组织信息
                role.setOrganizeType(0); // 0-校友会
                role.setOrganizeId(alumniAssociationId);

                // 设置角色信息
                role.setRoleOrName((String) nodeData.get("roleOrName"));

                // 生成唯一的角色代码（模板代码 + 校友会ID + 节点序号）
                String templateRoleCode = (String) nodeData.get("roleOrCode");
                String uniqueRoleCode = templateRoleCode + "_" + alumniAssociationId + "_" + newRoleId;
                role.setRoleOrCode(uniqueRoleCode);

                role.setRemark((String) nodeData.get("remark"));
                role.setStatus(1); // 启用
                role.setCreateTime(LocalDateTime.now());

                // 保存到数据库
                boolean saveResult = organizeArchiRoleService.save(role);
                if (!saveResult) {
                    throw new BusinessException(ErrorType.SYSTEM_ERROR, "创建组织架构角色失败");
                }

                // 记录ID映射
                Object nodeIdObj = nodeData.get("nodeId");
                if (nodeIdObj != null) {
                    nodeIdMapping.put(String.valueOf(nodeIdObj), newRoleId);
                }

                log.info("创建组织架构角色成功 - 角色名: {}, 角色ID: {}, 校友会ID: {}",
                        role.getRoleOrName(), newRoleId, alumniAssociationId);
            }

            log.info("根据模板创建组织架构完成 - 校友会ID: {}, 模板ID: {}, 创建角色数: {}",
                    alumniAssociationId, templateId, templateNodes.size());

        } catch (JsonProcessingException e) {
            log.error("解析组织架构模板JSON失败 - 模板ID: {}", templateId, e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "解析组织架构模板失败");
        }
    }

    /**
     * 生成角色ID（使用雪花ID）
     * 这里简单使用时间戳，实际应该使用雪花ID生成器
     *
     * @return 角色ID
     */
    private Long generateRoleId() {
        // 这里使用MyBatis-Plus的ID生成策略会自动生成
        // 临时使用时间戳 + 随机数
        return System.currentTimeMillis() * 1000 + (long) (Math.random() * 1000);
    }
}
