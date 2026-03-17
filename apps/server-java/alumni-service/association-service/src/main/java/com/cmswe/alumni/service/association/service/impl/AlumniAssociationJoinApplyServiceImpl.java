package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationJoinApplyService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.user.UnifiedMessageApiService;
import com.cmswe.alumni.common.dto.ApplyAssociationJoinPlatformDto;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApply;
import com.cmswe.alumni.common.enums.NotificationType;
import com.cmswe.alumni.common.vo.LocalPlatformDetailVo;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationJoinApplyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 校友会申请加入校促会服务实现类
 */
@Slf4j
@Service
public class AlumniAssociationJoinApplyServiceImpl
        extends ServiceImpl<AlumniAssociationJoinApplyMapper, AlumniAssociationJoinApply>
        implements AlumniAssociationJoinApplyService {

    @jakarta.annotation.Resource
    private com.cmswe.alumni.api.association.AlumniAssociationService alumniAssociationService;

    @jakarta.annotation.Resource
    private com.cmswe.alumni.api.user.WxUserInfoService wxUserInfoService;

    @jakarta.annotation.Resource
    private UnifiedMessageApiService unifiedMessageApiService;

    @jakarta.annotation.Resource
    private LocalPlatformService localPlatformService;

    @Override
    public boolean applyJoinPlatform(ApplyAssociationJoinPlatformDto applyDto) {
        // 1. 校验校友会是否存在
        AlumniAssociation association = alumniAssociationService.getById(applyDto.getAlumniAssociationId());
        if (association == null) {
            throw new com.cmswe.alumni.common.exception.BusinessException("该校友会不存在");
        }

        // 2. 校验是否已加入校促会（一个校友会目前只允许加入一个校促会）
        if (association.getPlatformId() != null && association.getPlatformId() > 0) {
            throw new com.cmswe.alumni.common.exception.BusinessException("该校友会已加入校促会");
        }

        // 3. 校验是否有正在审核中的申请
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlumniAssociationJoinApply> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(AlumniAssociationJoinApply::getAlumniAssociationId, applyDto.getAlumniAssociationId());
        wrapper.eq(AlumniAssociationJoinApply::getStatus, 0); // 0-待审核
        long count = this.count(wrapper);
        if (count > 0) {
            throw new com.cmswe.alumni.common.exception.BusinessException("您已有待审核的加入申请，请勿重复申请");
        }

        // 4. 执行保存申请记录
        AlumniAssociationJoinApply apply = new AlumniAssociationJoinApply();
        apply.setAlumniAssociationId(applyDto.getAlumniAssociationId());
        apply.setPlatformId(applyDto.getPlatformId());
        apply.setApplicantWxId(applyDto.getApplicantWxId());
        apply.setStatus(0); // 0-待审核
        return this.save(apply);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean reviewJoinPlatform(com.cmswe.alumni.common.dto.ReviewAssociationJoinPlatformDto reviewDto) {
        // 1. 获取申请记录
        AlumniAssociationJoinApply apply = this.getById(reviewDto.getId());
        if (apply == null) {
            throw new com.cmswe.alumni.common.exception.BusinessException("申请记录不存在");
        }

        // 2. 更新申请状态
        apply.setStatus(reviewDto.getStatus());
        boolean updateApply = this.updateById(apply);
        if (!updateApply) {
            return false;
        }

        // 3. 如果审核通过，更新校友会关联的校促会ID和认证标识
        if (reviewDto.getStatus() == 1) { // 1-已通过
            com.cmswe.alumni.common.entity.AlumniAssociation association = alumniAssociationService
                    .getById(apply.getAlumniAssociationId());
            if (association != null) {
                association.setPlatformId(apply.getPlatformId());
                association.setCertificationFlag(2); // 设置认证标识为2（校促会认证）
                boolean updateAssociation = alumniAssociationService.updateById(association);
                if (!updateAssociation) {
                    throw new com.cmswe.alumni.common.exception.BusinessException("更新校友会关联校促会失败");
                }
            }
            // 4. 审核通过：发送系统通知给申请人（校友会主要负责人）
            sendApprovalNotification(apply);
        } else if (reviewDto.getStatus() == 2) { // 2-已拒绝
            // 5. 审核拒绝：发送系统通知给申请人（校友会主要负责人），含审核意见
            sendRejectionNotification(apply, reviewDto.getReviewComment());
        }
        return true;
    }

    /**
     * 获取通知接收人：优先申请人（applicantWxId），若无则用校友会主要负责人（chargeWxId）兜底
     */
    private Long getNotificationTargetWxId(AlumniAssociationJoinApply apply, AlumniAssociation association) {
        if (apply.getApplicantWxId() != null) {
            return apply.getApplicantWxId();
        }
        return association != null ? association.getChargeWxId() : null;
    }

    /**
     * 发送审核通过通知
     * <p>通知对象：优先申请人（applicantWxId），若无则校友会主要负责人（chargeWxId）</p>
     *
     * @param apply 申请记录
     */
    private void sendApprovalNotification(AlumniAssociationJoinApply apply) {
        try {
            AlumniAssociation association = alumniAssociationService.getById(apply.getAlumniAssociationId());
            Long targetWxId = getNotificationTargetWxId(apply, association);
            if (targetWxId == null) {
                log.warn("申请人和主要负责人均为空，无法发送审核通过通知 - applyId: {}", apply.getId());
                return;
            }
            String associationName = association != null && association.getAssociationName() != null
                    ? association.getAssociationName() : "校友会";
            String platformName = getPlatformName(apply.getPlatformId());

            String title = "校促会加入申请审核通过";
            String content = String.format("恭喜！您代表的【%s】申请加入【%s】的申请已通过审核", associationName, platformName);

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    targetWxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    apply.getAlumniAssociationId(),
                    "ASSOCIATION"
            );

            if (success) {
                log.info("校促会加入申请审核通过通知已发送 - 用户: {}, 校友会: {}", targetWxId, associationName);
            } else {
                log.error("校促会加入申请审核通过通知发送失败 - 用户: {}, 校友会: {}", targetWxId, associationName);
            }
        } catch (Exception e) {
            log.error("发送校促会加入申请审核通过通知异常 - applyId: {}, Error: {}", apply.getId(), e.getMessage(), e);
        }
    }

    /**
     * 发送审核拒绝通知
     * <p>通知对象：优先申请人（applicantWxId），若无则校友会主要负责人（chargeWxId）</p>
     * <p>若审核人员填写了审核意见（reviewComment），会一并展示在通知内容中</p>
     *
     * @param apply         申请记录
     * @param reviewComment 审核意见（拒绝原因），可为空
     */
    private void sendRejectionNotification(AlumniAssociationJoinApply apply, String reviewComment) {
        try {
            AlumniAssociation association = alumniAssociationService.getById(apply.getAlumniAssociationId());
            Long targetWxId = getNotificationTargetWxId(apply, association);
            if (targetWxId == null) {
                log.warn("申请人和主要负责人均为空，无法发送审核拒绝通知 - applyId: {}", apply.getId());
                return;
            }
            String associationName = association != null && association.getAssociationName() != null
                    ? association.getAssociationName() : "校友会";
            String platformName = getPlatformName(apply.getPlatformId());

            String title = "校促会加入申请审核未通过";
            String content = String.format("很遗憾，您代表的【%s】申请加入【%s】的申请未通过审核", associationName, platformName);
            if (reviewComment != null && !reviewComment.trim().isEmpty()) {
                content += "。审核意见：" + reviewComment.trim();
            }

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    targetWxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    apply.getAlumniAssociationId(),
                    "ASSOCIATION"
            );

            if (success) {
                log.info("校促会加入申请审核拒绝通知已发送 - 用户: {}, 校友会: {}", targetWxId, associationName);
            } else {
                log.error("校促会加入申请审核拒绝通知发送失败 - 用户: {}, 校友会: {}", targetWxId, associationName);
            }
        } catch (Exception e) {
            log.error("发送校促会加入申请审核拒绝通知异常 - applyId: {}, Error: {}", apply.getId(), e.getMessage(), e);
        }
    }

    private String getPlatformName(Long platformId) {
        if (platformId == null) {
            return "校促会";
        }
        try {
            LocalPlatformDetailVo platform = localPlatformService.getLocalPlatformById(platformId);
            return (platform != null && platform.getPlatformName() != null) ? platform.getPlatformName() : "校促会";
        } catch (Exception e) {
            log.debug("获取校促会名称失败，使用默认值 - platformId: {}", platformId);
            return "校促会";
        }
    }

    @Override
    public com.cmswe.alumni.common.vo.PageVo<com.cmswe.alumni.common.vo.AlumniAssociationJoinApplyVo> queryApplyPage(
            com.cmswe.alumni.common.dto.QueryAssociationJoinApplyDto queryDto) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AlumniAssociationJoinApply> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                queryDto.getCurrent(), queryDto.getSize());
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlumniAssociationJoinApply> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        // 按状态筛选
        wrapper.eq(queryDto.getStatus() != null, AlumniAssociationJoinApply::getStatus, queryDto.getStatus());
        // 按校促会ID筛选
        wrapper.eq(queryDto.getPlatformId() != null, AlumniAssociationJoinApply::getPlatformId,
                queryDto.getPlatformId());

        // 按创建时间倒序
        wrapper.orderByDesc(AlumniAssociationJoinApply::getCreateTime);

        // 执行查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AlumniAssociationJoinApply> resultPage = this
                .page(page, wrapper);

        // 转换为VO
        java.util.List<com.cmswe.alumni.common.vo.AlumniAssociationJoinApplyVo> voList = resultPage.getRecords()
                .stream().map(apply -> {
                    com.cmswe.alumni.common.entity.AlumniAssociation association = alumniAssociationService
                            .getById(apply.getAlumniAssociationId());
                    com.cmswe.alumni.common.entity.WxUserInfo applicant = null;
                    if (apply.getApplicantWxId() != null) {
                        applicant = wxUserInfoService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.cmswe.alumni.common.entity.WxUserInfo>()
                                .eq(com.cmswe.alumni.common.entity.WxUserInfo::getWxId, apply.getApplicantWxId()));
                    }
                    return com.cmswe.alumni.common.vo.AlumniAssociationJoinApplyVo.objToVo(apply, association, applicant);
                }).collect(java.util.stream.Collectors.toList());

        // 创建VO分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.cmswe.alumni.common.vo.AlumniAssociationJoinApplyVo> voPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                queryDto.getCurrent(), queryDto.getSize(), resultPage.getTotal());
        voPage.setRecords(voList);

        // 转换为PageVo
        return com.cmswe.alumni.common.vo.PageVo.of(voPage);
    }

    @Override
    public com.cmswe.alumni.common.vo.AlumniAssociationJoinApplyVo getApplyDetailById(Long id) {
        // 1. 校验参数
        if (id == null) {
            throw new com.cmswe.alumni.common.exception.BusinessException("参数不能为空，请重试");
        }

        // 2. 查询申请记录
        AlumniAssociationJoinApply apply = this.getById(id);
        if (apply == null) {
            throw new com.cmswe.alumni.common.exception.BusinessException("申请记录不存在");
        }

        // 3. 查询校友会信息
        com.cmswe.alumni.common.entity.AlumniAssociation association = alumniAssociationService
                .getById(apply.getAlumniAssociationId());

        // 4. 查询申请人信息
        com.cmswe.alumni.common.entity.WxUserInfo applicant = null;
        if (apply.getApplicantWxId() != null) {
            applicant = wxUserInfoService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.cmswe.alumni.common.entity.WxUserInfo>()
                    .eq(com.cmswe.alumni.common.entity.WxUserInfo::getWxId, apply.getApplicantWxId()));
        }

        // 5. 转换为VO
        return com.cmswe.alumni.common.vo.AlumniAssociationJoinApplyVo.objToVo(apply, association, applicant);
    }
}
