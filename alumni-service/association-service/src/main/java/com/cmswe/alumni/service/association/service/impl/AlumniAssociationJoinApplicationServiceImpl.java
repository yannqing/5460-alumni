package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationJoinApplicationService;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.user.AlumniEducationService;
import com.cmswe.alumni.api.user.FileService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.user.RoleUserService;
import com.cmswe.alumni.api.user.UnifiedMessageApiService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.dto.ApplyAlumniAssociationDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationJoinApplicationListDto;
import com.cmswe.alumni.common.dto.ReviewAlumniAssociationJoinApplicationDto;
import com.cmswe.alumni.common.dto.UpdateAlumniAssociationJoinApplicationDto;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.enums.NotificationType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplicationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplicationListVo;
import com.cmswe.alumni.common.vo.FilesVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.UserListResponse;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationJoinApplicationMapper;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationMapper;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationMemberMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 校友会加入申请服务实现类
 */
@Slf4j
@Service
public class AlumniAssociationJoinApplicationServiceImpl
        extends ServiceImpl<AlumniAssociationJoinApplicationMapper, AlumniAssociationJoinApplication>
        implements AlumniAssociationJoinApplicationService {

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Resource
    private AlumniAssociationMemberMapper alumniAssociationMemberMapper;

    @Resource
    private AlumniAssociationMapper alumniAssociationMapper;

    @Resource
    private RoleService roleService;

    @Resource
    private UnifiedMessageApiService unifiedMessageApiService;

    @Resource
    private FileService fileService;

    @Resource
    private AlumniEducationService alumniEducationService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private AlumniAssociationJoinApplicationMapper alumniAssociationJoinApplicationMapper;

    @Resource
    private com.cmswe.alumni.api.association.SchoolService schoolService;

    @Resource
    private UserService userService;

    @Resource
    private RoleUserService roleUserService;

    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyToJoinAssociation(Long wxId, ApplyAlumniAssociationDto applyDto) {
        // 1. 检查用户是否已经是该校友会成员
        LambdaQueryWrapper<AlumniAssociationMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(AlumniAssociationMember::getWxId, wxId)
                .eq(AlumniAssociationMember::getAlumniAssociationId, applyDto.getAlumniAssociationId())
                .eq(AlumniAssociationMember::getStatus, 1);

        Long memberCount = alumniAssociationMemberMapper.selectCount(memberQuery);
        if (memberCount > 0) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "您已经是该校友会成员");
        }

        // 2. 检查是否有待审核的申请
        LambdaQueryWrapper<AlumniAssociationJoinApplication> applicationQuery = new LambdaQueryWrapper<>();
        applicationQuery.eq(AlumniAssociationJoinApplication::getTargetId, wxId)
                .eq(AlumniAssociationJoinApplication::getAlumniAssociationId, applyDto.getAlumniAssociationId())
                .eq(AlumniAssociationJoinApplication::getApplicantType, 1)
                .eq(AlumniAssociationJoinApplication::getApplicationStatus, 0);

        Long applicationCount = this.count(applicationQuery);
        if (applicationCount > 0) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "您已有待审核的申请，请勿重复提交");
        }

        // 3. 更新或创建用户信息
        LambdaQueryWrapper<WxUserInfo> userInfoQuery = new LambdaQueryWrapper<>();
        userInfoQuery.eq(WxUserInfo::getWxId, wxId);
        WxUserInfo userInfo = wxUserInfoService.getOne(userInfoQuery);

        if (userInfo == null) {
            // 创建新的用户信息
            userInfo = new WxUserInfo();
            userInfo.setWxId(wxId);
            userInfo.setName(applyDto.getName());
            userInfo.setIdentifyType(0); // 默认为身份证
            userInfo.setIdentifyCode(applyDto.getIdentifyCode());
            if (applyDto.getPhone() != null) {
                userInfo.setPhone(applyDto.getPhone());
            }
            wxUserInfoService.save(userInfo);
        } else {
            // 更新用户信息
            userInfo.setName(applyDto.getName());
            userInfo.setIdentifyCode(applyDto.getIdentifyCode());
            if (applyDto.getPhone() != null) {
                userInfo.setPhone(applyDto.getPhone());
            }
            wxUserInfoService.updateById(userInfo);
        }

        // 4. 保存或更新教育经历信息（仅当 schoolId 不为空时）
        if (applyDto.getSchoolId() != null) {
            AlumniEducation education = new AlumniEducation();
            education.setWxId(wxId);
            education.setSchoolId(applyDto.getSchoolId());
            education.setEnrollmentYear(applyDto.getEnrollmentYear());
            education.setGraduationYear(applyDto.getGraduationYear());
            education.setDepartment(applyDto.getDepartment());
            education.setMajor(applyDto.getMajor());
            education.setClassName(applyDto.getClassName());
            education.setEducationLevel(applyDto.getEducationLevel());
            education.setCertificationStatus(0); // 默认未认证

            alumniEducationService.saveOrUpdateByWxIdAndSchoolId(education);
            log.info("用户{}的教育经历信息已保存/更新 - schoolId: {}", wxId, applyDto.getSchoolId());
        }

        // 5. 创建申请记录
        AlumniAssociationJoinApplication application = new AlumniAssociationJoinApplication();
        application.setAlumniAssociationId(applyDto.getAlumniAssociationId());
        application.setApplicantType(1); // 1-用户
        application.setTargetId(wxId);
        application.setApplicationReason(applyDto.getApplicationReason());
        application.setApplicationStatus(0); // 0-待审核
        application.setApplyTime(LocalDateTime.now());

        // 保存教育经历信息到申请记录中
        application.setSchoolId(applyDto.getSchoolId());
        application.setEnrollmentYear(applyDto.getEnrollmentYear());
        application.setGraduationYear(applyDto.getGraduationYear());
        application.setDepartment(applyDto.getDepartment());
        application.setMajor(applyDto.getMajor());
        application.setClassName(applyDto.getClassName());
        application.setEducationLevel(applyDto.getEducationLevel());

        // 将附件ID列表转换为JSON字符串
        if (applyDto.getAttachmentIds() != null && !applyDto.getAttachmentIds().isEmpty()) {
            try {
                String attachmentIdsJson = objectMapper.writeValueAsString(applyDto.getAttachmentIds());
                application.setAttachmentIds(attachmentIdsJson);
            } catch (JsonProcessingException e) {
                log.error("转换附件ID列表为JSON失败", e);
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "附件信息处理失败");
            }
        }

        boolean result = this.save(application);

        if (result) {
            log.info("用户{}成功申请加入校友会{}", wxId, applyDto.getAlumniAssociationId());

            // 发送申请提交成功通知
            sendApplicationSubmittedNotification(wxId, applyDto.getAlumniAssociationId());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewApplication(Long reviewerId, ReviewAlumniAssociationJoinApplicationDto reviewDto) {
        // 1. 查询申请记录
        AlumniAssociationJoinApplication application = this.getById(reviewDto.getApplicationId());
        if (application == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "申请记录不存在");
        }

        // 2. 检查申请状态
        if (application.getApplicationStatus() != 0) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "该申请已被审核，无法重复审核");
        }

        // 3. 更新申请状态
        application.setApplicationStatus(reviewDto.getReviewResult());
        application.setReviewerId(reviewerId);
        application.setReviewTime(LocalDateTime.now());
        application.setReviewComment(reviewDto.getReviewComment());

        boolean updateResult = this.updateById(application);

        // 4. 根据审核结果发送通知
        if (updateResult) {
            if (reviewDto.getReviewResult() == 1) {
                // 4.1 审核通过：立即发送系统通知
                sendApprovalNotification(application);

                // 4.2 发送业务通知到 Kafka，触发责任链处理（添加成员、分配角色、更新 isAlumni 等）
                sendAlumniJoinApprovedBusinessNotification(application);
            } else if (reviewDto.getReviewResult() == 2) {
                // 4.3 审核拒绝：发送拒绝通知
                sendRejectionNotification(application);
            }
        }

        return updateResult;
    }

    /**
     * 发送申请提交成功通知
     *
     * @param wxId               申请人用户ID
     * @param alumniAssociationId 校友会ID
     */
    private void sendApplicationSubmittedNotification(Long wxId, Long alumniAssociationId) {
        try {
            // 查询校友会信息
            AlumniAssociation association = alumniAssociationMapper.selectById(alumniAssociationId);
            if (association == null) {
                log.warn("校友会不存在，无法发送通知 - 校友会ID: {}", alumniAssociationId);
                return;
            }

            String associationName = association.getAssociationName();

            // 发送系统通知
            String title = "校友会申请已提交";
            String content = "您的加入【" + associationName + "】校友会申请已经提交，请耐心等待审核";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    alumniAssociationId,
                    "ASSOCIATION"
            );

            if (success) {
                log.info("校友会申请提交通知已发送 - 用户: {}, 校友会: {}", wxId, associationName);
            } else {
                log.error("校友会申请提交通知发送失败 - 用户: {}, 校友会: {}", wxId, associationName);
            }

        } catch (Exception e) {
            log.error("发送校友会申请提交通知异常 - 用户: {}, 校友会ID: {}, Error: {}",
                    wxId, alumniAssociationId, e.getMessage(), e);
        }
    }

    /**
     * 发送审核通过通知
     *
     * @param application 申请记录
     */
    private void sendApprovalNotification(AlumniAssociationJoinApplication application) {
        try {
            // 查询校友会信息
            AlumniAssociation association = alumniAssociationMapper.selectById(application.getAlumniAssociationId());
            String associationName = association != null ? association.getAssociationName() : "校友会";

            // 发送系统通知
            String title = "校友会申请审核通过";
            String content = "恭喜！您申请加入【" + associationName + "】的申请已通过审核";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    application.getTargetId(),
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    application.getAlumniAssociationId(),
                    "ASSOCIATION"
            );

            if (success) {
                log.info("校友会申请审核通过通知已发送 - 用户: {}, 校友会: {}", application.getTargetId(), associationName);
            } else {
                log.error("校友会申请审核通过通知发送失败 - 用户: {}, 校友会: {}", application.getTargetId(), associationName);
            }

        } catch (Exception e) {
            log.error("发送校友会申请审核通过通知异常 - 用户: {}, Error: {}",
                    application.getTargetId(), e.getMessage(), e);
        }
    }

    /**
     * 发送审核拒绝通知
     *
     * @param application 申请记录
     */
    private void sendRejectionNotification(AlumniAssociationJoinApplication application) {
        try {
            // 查询校友会信息
            AlumniAssociation association = alumniAssociationMapper.selectById(application.getAlumniAssociationId());
            String associationName = association != null ? association.getAssociationName() : "校友会";

            // 构建通知内容，如果有审核意见则包含进去
            String title = "校友会申请审核未通过";
            String content = "很遗憾，您申请加入【" + associationName + "】的申请未通过审核";

            if (application.getReviewComment() != null && !application.getReviewComment().trim().isEmpty()) {
                content += "。审核意见：" + application.getReviewComment();
            }

            // 发送系统通知
            boolean success = unifiedMessageApiService.sendSystemNotification(
                    application.getTargetId(),
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    application.getAlumniAssociationId(),
                    "ASSOCIATION"
            );

            if (success) {
                log.info("校友会申请审核拒绝通知已发送 - 用户: {}, 校友会: {}", application.getTargetId(), associationName);
            } else {
                log.error("校友会申请审核拒绝通知发送失败 - 用户: {}, 校友会: {}", application.getTargetId(), associationName);
            }

        } catch (Exception e) {
            log.error("发送校友会申请审核拒绝通知异常 - 用户: {}, Error: {}",
                    application.getTargetId(), e.getMessage(), e);
        }
    }

    /**
     * 发送校友会加入申请审核通过业务通知（触发 Kafka 责任链处理）
     *
     * <p>该业务通知会触发以下操作：
     * <ul>
     *   <li>添加用户到 role_user 表（分配校友会成员角色）</li>
     *   <li>创建或获取普通成员的架构角色</li>
     *   <li>添加用户到校友会成员表，设置架构角色</li>
     *   <li>更新用户的 isAlumni 字段为 1</li>
     * </ul>
     *
     * @param application 申请记录
     */
    private void sendAlumniJoinApprovedBusinessNotification(AlumniAssociationJoinApplication application) {
        try {
            // 查询校友会信息
            AlumniAssociation association = alumniAssociationMapper.selectById(application.getAlumniAssociationId());
            String associationName = association != null ? association.getAssociationName() : "校友会";

            String title = "校友会加入申请审核通过";
            String content = "恭喜！您申请加入【" + associationName + "】的申请已通过审核";

            // 发送业务通知到 Kafka，触发责任链处理
            // 消息类型：ALUMNI_JOIN_APPROVED（用于加入申请审核通过）
            boolean success = unifiedMessageApiService.sendBusinessNotification(
                    application.getTargetId(),
                    "ALUMNI_JOIN_APPROVED",
                    title,
                    content,
                    application.getAlumniAssociationId(),
                    "ASSOCIATION"
            );

            if (success) {
                log.info("校友会加入申请审核通过业务通知已发送到 Kafka - 用户: {}, 校友会: {}",
                        application.getTargetId(), associationName);
            } else {
                log.error("校友会加入申请审核通过业务通知发送到 Kafka 失败 - 用户: {}, 校友会: {}",
                        application.getTargetId(), associationName);
            }

        } catch (Exception e) {
            log.error("发送校友会加入申请审核通过业务通知异常 - 用户: {}, Error: {}",
                    application.getTargetId(), e.getMessage(), e);
        }
    }

    @Override
    public PageVo<AlumniAssociationJoinApplicationListVo> queryApplicationPage(QueryAlumniAssociationJoinApplicationListDto queryDto) {
        // 1. 构建分页对象
        Page<AlumniAssociationJoinApplication> page = new Page<>(queryDto.getCurrent(), queryDto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<AlumniAssociationJoinApplication> queryWrapper = new LambdaQueryWrapper<>();

        // 2.1 校友会ID筛选
        if (queryDto.getAlumniAssociationId() != null) {
            queryWrapper.eq(AlumniAssociationJoinApplication::getAlumniAssociationId, queryDto.getAlumniAssociationId());
        }

        // 2.2 申请状态筛选
        if (queryDto.getApplicationStatus() != null) {
            queryWrapper.eq(AlumniAssociationJoinApplication::getApplicationStatus, queryDto.getApplicationStatus());
        }

        // 2.3 按申请时间倒序排列
        queryWrapper.orderByDesc(AlumniAssociationJoinApplication::getApplyTime);

        // 3. 执行分页查询
        Page<AlumniAssociationJoinApplication> applicationPage = this.page(page, queryWrapper);

        // 4. 如果查询结果为空，直接返回空列表
        if (applicationPage.getRecords().isEmpty()) {
            return new PageVo<>(new ArrayList<>(), 0L, (long) queryDto.getCurrent(), (long) queryDto.getPageSize());
        }

        // 5. 提取所有需要查询的ID
        List<Long> targetIds = applicationPage.getRecords().stream()
                .map(AlumniAssociationJoinApplication::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> reviewerIds = applicationPage.getRecords().stream()
                .map(AlumniAssociationJoinApplication::getReviewerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> alumniAssociationIds = applicationPage.getRecords().stream()
                .map(AlumniAssociationJoinApplication::getAlumniAssociationId)
                .distinct()
                .collect(Collectors.toList());

        // 6. 批量查询用户信息
        LambdaQueryWrapper<WxUserInfo> userQuery = new LambdaQueryWrapper<>();
        userQuery.in(WxUserInfo::getWxId, targetIds);

        // 添加模糊搜索条件
        if (StringUtils.isNotBlank(queryDto.getApplicantName())) {
            userQuery.like(WxUserInfo::getName, queryDto.getApplicantName());
        }
        if (StringUtils.isNotBlank(queryDto.getApplicantPhone())) {
            userQuery.like(WxUserInfo::getPhone, queryDto.getApplicantPhone());
        }

        List<WxUserInfo> userInfoList = wxUserInfoService.list(userQuery);
        Map<Long, WxUserInfo> userInfoMap = userInfoList.stream()
                .collect(Collectors.toMap(WxUserInfo::getWxId, user -> user, (v1, v2) -> v1));

        // 7. 批量查询审核人信息
        Map<Long, WxUserInfo> reviewerInfoMap = new HashMap<>();
        if (!reviewerIds.isEmpty()) {
            List<WxUserInfo> reviewerInfoList = wxUserInfoService.list(
                    new LambdaQueryWrapper<WxUserInfo>().in(WxUserInfo::getWxId, reviewerIds)
            );
            reviewerInfoMap = reviewerInfoList.stream()
                    .collect(Collectors.toMap(WxUserInfo::getWxId, user -> user, (v1, v2) -> v1));
        }

        // 8. 批量查询校友会信息
        Map<Long, AlumniAssociation> associationMap = alumniAssociationMapper.selectBatchIds(alumniAssociationIds).stream()
                .collect(Collectors.toMap(AlumniAssociation::getAlumniAssociationId, association -> association, (v1, v2) -> v1));

        // 9. 组装VO列表
        Map<Long, WxUserInfo> finalReviewerInfoMap = reviewerInfoMap;
        List<AlumniAssociationJoinApplicationListVo> voList = applicationPage.getRecords().stream()
                .map(application -> {
                    // 如果申请人不符合搜索条件，过滤掉
                    WxUserInfo userInfo = userInfoMap.get(application.getTargetId());
                    if (userInfo == null) {
                        return null;
                    }

                    AlumniAssociationJoinApplicationListVo vo = new AlumniAssociationJoinApplicationListVo();

                    // 基本信息（Long转String避免精度丢失）
                    vo.setApplicationId(String.valueOf(application.getApplicationId()));
                    vo.setAlumniAssociationId(String.valueOf(application.getAlumniAssociationId()));
                    vo.setApplicantType(application.getApplicantType());
                    vo.setTargetId(String.valueOf(application.getTargetId()));
                    vo.setApplicationReason(application.getApplicationReason());
                    vo.setApplicationStatus(application.getApplicationStatus());
                    vo.setApplicationStatusText(AlumniAssociationJoinApplicationListVo.getApplicationStatusText(application.getApplicationStatus()));
                    vo.setReviewComment(application.getReviewComment());
                    vo.setApplyTime(application.getApplyTime());
                    vo.setReviewTime(application.getReviewTime());
                    vo.setCreateTime(application.getCreateTime());
                    vo.setUpdateTime(application.getUpdateTime());

                    // 校友会信息
                    AlumniAssociation association = associationMap.get(application.getAlumniAssociationId());
                    if (association != null) {
                        vo.setAlumniAssociationName(association.getAssociationName());
                    }

                    // 申请人信息
                    UserListResponse applicantVo = UserListResponse.ObjToVo(userInfo);
                    applicantVo.setWxId(String.valueOf(userInfo.getWxId()));
                    vo.setApplicantInfo(applicantVo);

                    // 审核人信息
                    if (application.getReviewerId() != null) {
                        vo.setReviewerId(String.valueOf(application.getReviewerId()));
                        WxUserInfo reviewerInfo = finalReviewerInfoMap.get(application.getReviewerId());
                        if (reviewerInfo != null) {
                            UserListResponse reviewerVo = UserListResponse.ObjToVo(reviewerInfo);
                            reviewerVo.setWxId(String.valueOf(reviewerInfo.getWxId()));
                            vo.setReviewerInfo(reviewerVo);
                        }
                    }

                    // 附件文件信息
                    if (StringUtils.isNotBlank(application.getAttachmentIds())) {
                        try {
                            List<Long> fileIds = objectMapper.readValue(
                                    application.getAttachmentIds(),
                                    new TypeReference<List<Long>>() {}
                            );
                            if (fileIds != null && !fileIds.isEmpty()) {
                                List<Files> files = fileService.listByIds(fileIds);
                                List<FilesVo> filesVos = files.stream()
                                        .map(FilesVo::objToVo)
                                        .collect(Collectors.toList());
                                vo.setAttachmentFiles(filesVos);
                            }
                        } catch (JsonProcessingException e) {
                            log.error("解析附件ID失败，applicationId: {}", application.getApplicationId(), e);
                            vo.setAttachmentFiles(new ArrayList<>());
                        }
                    } else {
                        vo.setAttachmentFiles(new ArrayList<>());
                    }

                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 10. 构建返回结果
        return new PageVo<>(voList, applicationPage.getTotal(), applicationPage.getCurrent(), applicationPage.getSize());
    }

    @Override
    public AlumniAssociationJoinApplicationDetailVo getApplicationDetail(Long wxId, Long alumniAssociationId) {
        // 1. 查询申请记录
        LambdaQueryWrapper<AlumniAssociationJoinApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlumniAssociationJoinApplication::getTargetId, wxId)
                .eq(AlumniAssociationJoinApplication::getAlumniAssociationId, alumniAssociationId)
                .eq(AlumniAssociationJoinApplication::getApplicantType, 1)
                .orderByDesc(AlumniAssociationJoinApplication::getApplyTime)
                .last("LIMIT 1");

        AlumniAssociationJoinApplication application = this.getOne(queryWrapper);
        if (application == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "未找到申请记录");
        }

        // 2. 查询用户信息
        LambdaQueryWrapper<WxUserInfo> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(WxUserInfo::getWxId, wxId);
        WxUserInfo userInfo = wxUserInfoService.getOne(userQuery);

        // 3. 查询校友会信息
        AlumniAssociation association = alumniAssociationMapper.selectById(alumniAssociationId);

        // 4. 组装VO
        AlumniAssociationJoinApplicationDetailVo vo = new AlumniAssociationJoinApplicationDetailVo();

        // 基本申请信息
        vo.setApplicationId(String.valueOf(application.getApplicationId()));
        vo.setAlumniAssociationId(String.valueOf(application.getAlumniAssociationId()));
        vo.setApplicationReason(application.getApplicationReason());
        vo.setApplicationStatus(application.getApplicationStatus());
        vo.setApplicationStatusText(AlumniAssociationJoinApplicationDetailVo.getApplicationStatusText(application.getApplicationStatus()));
        vo.setReviewComment(application.getReviewComment());
        vo.setApplyTime(application.getApplyTime());
        vo.setReviewTime(application.getReviewTime());

        // 校友会信息
        if (association != null) {
            vo.setAlumniAssociationName(association.getAssociationName());
        }

        // 用户信息
        if (userInfo != null) {
            vo.setName(userInfo.getName());
            vo.setIdentifyCode(userInfo.getIdentifyCode());
            vo.setPhone(userInfo.getPhone());
        }

        // 从申请记录中获取教育经历信息（申请时的快照）
        vo.setSchoolId(application.getSchoolId() != null ? String.valueOf(application.getSchoolId()) : null);
        vo.setEnrollmentYear(application.getEnrollmentYear());
        vo.setGraduationYear(application.getGraduationYear());
        vo.setDepartment(application.getDepartment());
        vo.setMajor(application.getMajor());
        vo.setClassName(application.getClassName());
        vo.setEducationLevel(application.getEducationLevel());

        // 查询学校信息
        if (application.getSchoolId() != null) {
            School school = schoolService.getById(application.getSchoolId());
            if (school != null) {
                vo.setSchoolName(school.getSchoolName());
            }
        }

        // 附件文件信息
        if (StringUtils.isNotBlank(application.getAttachmentIds())) {
            try {
                List<Long> fileIds = objectMapper.readValue(
                        application.getAttachmentIds(),
                        new TypeReference<List<Long>>() {}
                );
                if (fileIds != null && !fileIds.isEmpty()) {
                    List<Files> files = fileService.listByIds(fileIds);
                    List<FilesVo> filesVos = files.stream()
                            .map(FilesVo::objToVo)
                            .collect(Collectors.toList());
                    vo.setAttachmentFiles(filesVos);
                }
            } catch (JsonProcessingException e) {
                log.error("解析附件ID失败，applicationId: {}", application.getApplicationId(), e);
                vo.setAttachmentFiles(new ArrayList<>());
            }
        } else {
            vo.setAttachmentFiles(new ArrayList<>());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAndResubmitApplication(Long wxId, UpdateAlumniAssociationJoinApplicationDto updateDto) {
        // 1. 查询申请记录
        AlumniAssociationJoinApplication application = this.getById(updateDto.getApplicationId());
        if (application == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "申请记录不存在");
        }

        // 2. 检查申请是否属于当前用户
        if (!application.getTargetId().equals(wxId)) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "您无权修改此申请");
        }

        // 3. 检查申请状态是否为待审核
        if (application.getApplicationStatus() != 0) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "只能编辑待审核的申请");
        }

        // 4. 更新用户信息
        LambdaQueryWrapper<WxUserInfo> userInfoQuery = new LambdaQueryWrapper<>();
        userInfoQuery.eq(WxUserInfo::getWxId, wxId);
        WxUserInfo userInfo = wxUserInfoService.getOne(userInfoQuery);

        if (userInfo == null) {
            // 创建新的用户信息
            userInfo = new WxUserInfo();
            userInfo.setWxId(wxId);
            userInfo.setName(updateDto.getName());
            userInfo.setIdentifyType(0); // 默认为身份证
            userInfo.setIdentifyCode(updateDto.getIdentifyCode());
            if (updateDto.getPhone() != null) {
                userInfo.setPhone(updateDto.getPhone());
            }
            wxUserInfoService.save(userInfo);
        } else {
            // 更新用户信息
            userInfo.setName(updateDto.getName());
            userInfo.setIdentifyCode(updateDto.getIdentifyCode());
            if (updateDto.getPhone() != null) {
                userInfo.setPhone(updateDto.getPhone());
            }
            wxUserInfoService.updateById(userInfo);
        }

        // 5. 保存或更新教育经历信息（仅当 schoolId 不为空时）
        if (updateDto.getSchoolId() != null) {
            AlumniEducation education = new AlumniEducation();
            education.setWxId(wxId);
            education.setSchoolId(updateDto.getSchoolId());
            education.setEnrollmentYear(updateDto.getEnrollmentYear());
            education.setGraduationYear(updateDto.getGraduationYear());
            education.setDepartment(updateDto.getDepartment());
            education.setMajor(updateDto.getMajor());
            education.setClassName(updateDto.getClassName());
            education.setEducationLevel(updateDto.getEducationLevel());
            education.setCertificationStatus(0); // 默认未认证

            alumniEducationService.saveOrUpdateByWxIdAndSchoolId(education);
            log.info("用户{}的教育经历信息已保存/更新 - schoolId: {}", wxId, updateDto.getSchoolId());
        }

        // 6. 更新申请记录
        application.setApplicationReason(updateDto.getApplicationReason());
        application.setSchoolId(updateDto.getSchoolId());
        application.setEnrollmentYear(updateDto.getEnrollmentYear());
        application.setGraduationYear(updateDto.getGraduationYear());
        application.setDepartment(updateDto.getDepartment());
        application.setMajor(updateDto.getMajor());
        application.setClassName(updateDto.getClassName());
        application.setEducationLevel(updateDto.getEducationLevel());

        // 将附件ID列表转换为JSON字符串
        if (updateDto.getAttachmentIds() != null && !updateDto.getAttachmentIds().isEmpty()) {
            try {
                String attachmentIdsJson = objectMapper.writeValueAsString(updateDto.getAttachmentIds());
                application.setAttachmentIds(attachmentIdsJson);
            } catch (JsonProcessingException e) {
                log.error("转换附件ID列表为JSON失败", e);
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "附件信息处理失败");
            }
        } else {
            // 如果附件列表为空，清空附件字段
            application.setAttachmentIds(null);
        }

        // 更新申请时间为当前时间（重新提交）
        application.setApplyTime(LocalDateTime.now());

        boolean result = this.updateById(application);

        if (result) {
            log.info("用户{}成功更新并重新提交校友会申请{}", wxId, updateDto.getApplicationId());

            // 发送申请重新提交通知
            sendApplicationResubmittedNotification(wxId, application.getAlumniAssociationId());
        }

        return result;
    }

    /**
     * 发送申请重新提交通知
     *
     * @param wxId               申请人用户ID
     * @param alumniAssociationId 校友会ID
     */
    private void sendApplicationResubmittedNotification(Long wxId, Long alumniAssociationId) {
        try {
            // 查询校友会信息
            AlumniAssociation association = alumniAssociationMapper.selectById(alumniAssociationId);
            if (association == null) {
                log.warn("校友会不存在，无法发送通知 - 校友会ID: {}", alumniAssociationId);
                return;
            }

            String associationName = association.getAssociationName();

            // 发送系统通知
            String title = "校友会申请已重新提交";
            String content = "您已成功编辑并重新提交【" + associationName + "】校友会申请，请耐心等待审核";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    alumniAssociationId,
                    "ASSOCIATION"
            );

            if (success) {
                log.info("校友会申请重新提交通知已发送 - 用户: {}, 校友会: {}", wxId, associationName);
            } else {
                log.error("校友会申请重新提交通知发送失败 - 用户: {}, 校友会: {}", wxId, associationName);
            }

        } catch (Exception e) {
            log.error("发送校友会申请重新提交通知异常 - 用户: {}, 校友会ID: {}, Error: {}",
                    wxId, alumniAssociationId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelApplication(Long wxId, Long applicationId) {
        // 1. 查询申请记录
        AlumniAssociationJoinApplication application = this.getById(applicationId);
        if (application == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "申请记录不存在");
        }

        // 2. 检查申请是否属于当前用户
        if (!application.getTargetId().equals(wxId)) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "您无权撤销此申请");
        }

        // 3. 检查申请状态是否为待审核
        if (application.getApplicationStatus() != 0) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "只能撤销待审核的申请");
        }

        // 4. 更新申请状态为已撤销（状态：3）
        application.setApplicationStatus(3);

        boolean result = this.updateById(application);

        if (result) {
            log.info("用户{}成功撤销校友会申请{}", wxId, applicationId);

            // 发送申请撤销通知
            sendApplicationCancelledNotification(wxId, application.getAlumniAssociationId());
        }

        return result;
    }

    /**
     * 发送申请撤销通知
     *
     * @param wxId               申请人用户ID
     * @param alumniAssociationId 校友会ID
     */
    private void sendApplicationCancelledNotification(Long wxId, Long alumniAssociationId) {
        try {
            // 查询校友会信息
            AlumniAssociation association = alumniAssociationMapper.selectById(alumniAssociationId);
            if (association == null) {
                log.warn("校友会不存在，无法发送通知 - 校友会ID: {}", alumniAssociationId);
                return;
            }

            String associationName = association.getAssociationName();

            // 发送系统通知
            String title = "校友会申请已撤销";
            String content = "您已成功撤销加入【" + associationName + "】校友会的申请";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    alumniAssociationId,
                    "ASSOCIATION"
            );

            if (success) {
                log.info("校友会申请撤销通知已发送 - 用户: {}, 校友会: {}", wxId, associationName);
            } else {
                log.error("校友会申请撤销通知发送失败 - 用户: {}, 校友会: {}", wxId, associationName);
            }

        } catch (Exception e) {
            log.error("发送校友会申请撤销通知异常 - 用户: {}, 校友会ID: {}, Error: {}",
                    wxId, alumniAssociationId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitAlumniAssociation(Long wxId, Long alumniAssociationId) {
        log.info("用户退出校友会 - 用户ID: {}, 校友会ID: {}", wxId, alumniAssociationId);

        // 1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }
        if (alumniAssociationId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "校友会ID不能为空");
        }

        // 2. 查询校友会信息
        AlumniAssociation association = alumniAssociationMapper.selectById(alumniAssociationId);
        if (association == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
        }

        // 3. 查询用户信息
        LambdaQueryWrapper<WxUserInfo> userInfoQuery = new LambdaQueryWrapper<>();
        userInfoQuery.eq(WxUserInfo::getWxId, wxId);
        WxUserInfo userInfo = wxUserInfoService.getOne(userInfoQuery);
        if (userInfo == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "用户不存在");
        }

        // 4. 查询用户是否是该校友会成员
        LambdaQueryWrapper<AlumniAssociationMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(AlumniAssociationMember::getWxId, wxId)
                .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
                .eq(AlumniAssociationMember::getStatus, 1);

        AlumniAssociationMember member = alumniAssociationMemberMapper.selectOne(memberQuery);
        if (member == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "您不是该校友会成员");
        }

        // 5. 删除成员记录（逻辑删除）
        boolean deleteMemberResult = alumniAssociationMemberMapper.deleteById(member.getId()) > 0;
        if (!deleteMemberResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "退出校友会失败");
        }

        // 6. 更新校友会成员数量（-1）
        Integer currentMemberCount = association.getMemberCount();
        if (currentMemberCount != null && currentMemberCount > 0) {
            association.setMemberCount(currentMemberCount - 1);
            boolean updateResult = alumniAssociationMapper.updateById(association) > 0;
            if (!updateResult) {
                throw new BusinessException(ErrorType.OPERATION_ERROR, "更新校友会成员数量失败");
            }
        }

        // 7. 删除该用户在该校友会的所有申请记录（逻辑删除）
        LambdaQueryWrapper<AlumniAssociationJoinApplication> applicationQuery = new LambdaQueryWrapper<>();
        applicationQuery.eq(AlumniAssociationJoinApplication::getTargetId, wxId)
                .eq(AlumniAssociationJoinApplication::getAlumniAssociationId, alumniAssociationId)
                .eq(AlumniAssociationJoinApplication::getApplicantType, 1);

        List<AlumniAssociationJoinApplication> applications = this.list(applicationQuery);
        if (!applications.isEmpty()) {
            boolean deleteApplicationsResult = this.removeByIds(
                    applications.stream()
                            .map(AlumniAssociationJoinApplication::getApplicationId)
                            .collect(Collectors.toList())
            );
            log.info("删除用户在校友会的申请记录 - 用户ID: {}, 校友会ID: {}, 删除数量: {}, 结果: {}",
                    wxId, alumniAssociationId, applications.size(), deleteApplicationsResult);
        }

        // 8. 检查用户是否还在其他校友会，更新 wx_users 表的 is_alumni 字段
        LambdaQueryWrapper<AlumniAssociationMember> remainingMemberQuery = new LambdaQueryWrapper<>();
        remainingMemberQuery.eq(AlumniAssociationMember::getWxId, wxId)
                .eq(AlumniAssociationMember::getStatus, 1);

        Long remainingAssociationCount = alumniAssociationMemberMapper.selectCount(remainingMemberQuery);
        log.info("用户剩余校友会数量 - 用户ID: {}, 剩余数量: {}", wxId, remainingAssociationCount);

        if (remainingAssociationCount == 0) {
            // 用户不再是任何校友会成员，更新 isAlumni 为 0
            WxUser wxUser = userService.getById(wxId);
            if (wxUser != null && wxUser.getIsAlumni() != null && wxUser.getIsAlumni() == 1) {
                wxUser.setIsAlumni(0);
                boolean updateAlumniStatus = userService.updateById(wxUser);
                if (updateAlumniStatus) {
                    log.info("用户校友状态已更新 - 用户ID: {}, isAlumni: 1 -> 0", wxId);
                } else {
                    log.warn("用户校友状态更新失败 - 用户ID: {}", wxId);
                }
            }
        }

        // 9. 发送通知给当前用户
        sendQuitNotificationToUser(wxId, association.getAssociationName());

        // 10. 发送通知给校友会管理员
        sendQuitNotificationToAdmins(wxId, alumniAssociationId, association.getAssociationName(), userInfo.getName());

        log.info("用户退出校友会成功 - 用户ID: {}, 校友会ID: {}, 当前成员数: {}",
                wxId, alumniAssociationId, association.getMemberCount());

        return true;
    }

    /**
     * 发送退出通知给当前用户
     *
     * @param wxId            用户ID
     * @param associationName 校友会名称
     */
    private void sendQuitNotificationToUser(Long wxId, String associationName) {
        try {
            String title = "已退出校友会";
            String content = "您已成功退出【" + associationName + "】校友会";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    null,
                    "ASSOCIATION"
            );

            if (success) {
                log.info("退出校友会通知已发送给用户 - 用户: {}, 校友会: {}", wxId, associationName);
            } else {
                log.error("退出校友会通知发送失败 - 用户: {}, 校友会: {}", wxId, associationName);
            }

        } catch (Exception e) {
            log.error("发送退出校友会通知异常 - 用户: {}, 校友会: {}, Error: {}",
                    wxId, associationName, e.getMessage(), e);
        }
    }

    /**
     * 发送退出通知给校友会管理员
     *
     * @param wxId                用户ID
     * @param alumniAssociationId 校友会ID
     * @param associationName     校友会名称
     * @param userName            用户姓名
     */
    private void sendQuitNotificationToAdmins(Long wxId, Long alumniAssociationId, String associationName, String userName) {
        try {
            // 1. 查询 ORGANIZE_ALUMNI_ADMIN 角色
            Role adminRole = roleService.getRoleByCodeInner("ORGANIZE_ALUMNI_ADMIN");
            if (adminRole == null) {
                log.error("未找到校友会管理员角色，角色代码: ORGANIZE_ALUMNI_ADMIN");
                return;
            }

            // 2. 查询 role_user 表，获取该校友会的所有管理员用户ID
            LambdaQueryWrapper<RoleUser> roleUserQueryWrapper = new LambdaQueryWrapper<>();
            roleUserQueryWrapper
                    .eq(RoleUser::getRoleId, adminRole.getRoleId())
                    .eq(RoleUser::getType, 2)  // type=2 表示校友会
                    .eq(RoleUser::getOrganizeId, alumniAssociationId);

            List<RoleUser> roleUserList = roleUserService.list(roleUserQueryWrapper);

            if (roleUserList.isEmpty()) {
                log.info("该校友会暂无管理员，无需发送通知 - 校友会ID: {}", alumniAssociationId);
                return;
            }

            // 3. 发送通知给每个管理员
            String title = "成员退出校友会";
            String content = "用户【" + (userName != null ? userName : "未知用户") + "】已退出【" + associationName + "】校友会";

            for (RoleUser roleUser : roleUserList) {
                Long adminWxId = roleUser.getWxId();

                // 不给自己发通知（如果退出的用户本身就是管理员）
                if (adminWxId.equals(wxId)) {
                    continue;
                }

                boolean success = unifiedMessageApiService.sendSystemNotification(
                        adminWxId,
                        NotificationType.SYSTEM_ANNOUNCEMENT,
                        title,
                        content,
                        alumniAssociationId,
                        "ASSOCIATION"
                );

                if (success) {
                    log.info("退出校友会通知已发送给管理员 - 管理员: {}, 退出用户: {}, 校友会: {}",
                            adminWxId, wxId, associationName);
                } else {
                    log.error("退出校友会通知发送失败 - 管理员: {}, 退出用户: {}, 校友会: {}",
                            adminWxId, wxId, associationName);
                }
            }

        } catch (Exception e) {
            log.error("发送退出校友会通知给管理员异常 - 用户: {}, 校友会ID: {}, Error: {}",
                    wxId, alumniAssociationId, e.getMessage(), e);
        }
    }
}
