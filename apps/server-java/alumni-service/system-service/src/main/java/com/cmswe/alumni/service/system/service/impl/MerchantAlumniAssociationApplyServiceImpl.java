package com.cmswe.alumni.service.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.system.MerchantAlumniAssociationApplyService;
import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.api.user.UnifiedMessageApiService;
import com.cmswe.alumni.common.dto.ApplyMerchantAssociationJoinDto;
import com.cmswe.alumni.common.dto.QueryMerchantAssociationJoinApplyDto;
import com.cmswe.alumni.common.dto.ReviewMerchantAssociationJoinApplyDto;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.Merchant;
import com.cmswe.alumni.common.entity.MerchantAlumniAssociationApply;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.enums.NotificationType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;
import com.cmswe.alumni.common.vo.MerchantApprovalVo;
import com.cmswe.alumni.common.vo.MerchantAssociationJoinApplyVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.service.system.mapper.MerchantAlumniAssociationApplyMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 商户入驻校友会申请服务实现类
 */
@Slf4j
@Service
public class MerchantAlumniAssociationApplyServiceImpl 
        extends ServiceImpl<MerchantAlumniAssociationApplyMapper, MerchantAlumniAssociationApply> 
        implements MerchantAlumniAssociationApplyService {

    @Resource
    private MerchantService merchantService;

    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Resource
    private UnifiedMessageApiService unifiedMessageApiService;

    private List<Long> parseAssociationIds(String associationIdStr) {
        if (StringUtils.isBlank(associationIdStr)) {
            return new ArrayList<>();
        }
        String trimmed = associationIdStr.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                List<Long> ids = JSON.parseArray(trimmed, Long.class);
                ids.removeIf(id -> id == null || id <= 0);
                return new ArrayList<>(new LinkedHashSet<>(ids));
            } catch (Exception e) {
                log.error("解析校友会ID数组失败: {}", trimmed, e);
            }
        }
        try {
            Long id = Long.parseLong(trimmed);
            if (id <= 0) {
                return new ArrayList<>();
            }
            return new ArrayList<>(Collections.singletonList(id));
        } catch (NumberFormatException e) {
            log.warn("校友会ID字段格式非数字且非数组: {}", trimmed);
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyJoinAssociation(Long wxId, ApplyMerchantAssociationJoinDto applyDto) {
        // 1. 校验商户是否存在且属于当前用户
        Merchant merchant = merchantService.getById(applyDto.getMerchantId());
        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户不存在");
        }
        if (!wxId.equals(merchant.getUserId())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "您无权为该商户提交申请");
        }

        // 2. 校验商户是否已经加入该校友会
        if (StringUtils.isNotBlank(merchant.getAlumniAssociationId())) {
            List<Long> associationIds = parseAssociationIds(merchant.getAlumniAssociationId());
            if (associationIds.contains(applyDto.getAlumniAssociationId())) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "商户已在该校友会中");
            }
        }

        // 3. 校验是否有待审核的申请
        LambdaQueryWrapper<MerchantAlumniAssociationApply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantAlumniAssociationApply::getMerchantId, applyDto.getMerchantId())
                .eq(MerchantAlumniAssociationApply::getAlumniAssociationId, applyDto.getAlumniAssociationId())
                .eq(MerchantAlumniAssociationApply::getStatus, 0); // 0-待审核
        
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该商户已有待审核的加入申请，请勿重复提交");
        }

        // 4. 创建申请记录
        MerchantAlumniAssociationApply apply = new MerchantAlumniAssociationApply();
        apply.setMerchantId(applyDto.getMerchantId());
        apply.setAlumniAssociationId(applyDto.getAlumniAssociationId());
        apply.setApplicantWxId(wxId);
        apply.setStatus(0); // 0-待审核

        boolean saved = this.save(apply);
        if (saved) {
            // 发送申请提交通知
            sendJoinApplicationNotification(wxId, merchant.getMerchantName(), applyDto.getAlumniAssociationId());
        }
        return saved;
    }

    /**
     * 发送商户申请加入校友会通知
     */
    private void sendJoinApplicationNotification(Long wxId, String merchantName, Long alumniAssociationId) {
        try {
            AlumniAssociation association = alumniAssociationService.getById(alumniAssociationId);
            String associationName = association != null ? association.getAssociationName() : "校友会";

            String title = "加入校友会申请已提交";
            String content = "您的【" + merchantName + "】商户申请加入【" + associationName + "】校友会已提交，请耐心等待审核";

            unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    alumniAssociationId,
                    "MERCHANT_ASSOCIATION_JOIN"
            );
        } catch (Exception e) {
            log.error("发送商户加入校友会申请通知失败", e);
        }
    }

    /**
     * 发送商户加入校友会审核结果通知
     */
    private void sendJoinReviewNotification(Long wxId, String merchantName, Long alumniAssociationId, Integer status, String reviewComment) {
        try {
            AlumniAssociation association = alumniAssociationService.getById(alumniAssociationId);
            String associationName = association != null ? association.getAssociationName() : "校友会";

            String title;
            String content;

            if (status == 1) {
                title = "加入校友会申请已通过";
                content = "恭喜！您的【" + merchantName + "】商户加入【" + associationName + "】校友会的申请已审核通过";
            } else {
                title = "加入校友会申请未通过";
                content = "很抱歉，您的【" + merchantName + "】商户加入【" + associationName + "】校友会的申请未通过审核";
                if (StringUtils.isNotBlank(reviewComment)) {
                    content += "。原因：" + reviewComment;
                }
            }

            unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    alumniAssociationId,
                    "MERCHANT_ASSOCIATION_JOIN"
            );
        } catch (Exception e) {
            log.error("发送商户加入校友会审核结果通知失败", e);
        }
    }

    @Override
    public PageVo<MerchantAssociationJoinApplyVo> queryJoinApplyPage(QueryMerchantAssociationJoinApplyDto queryDto) {
        Page<MerchantAssociationJoinApplyVo> page = new Page<>(queryDto.getCurrent(), queryDto.getPageSize());
        IPage<MerchantAssociationJoinApplyVo> resultPage = baseMapper.selectJoinApplyPage(
                page, queryDto.getAlumniAssociationId(), queryDto.getMerchantId(), queryDto.getStatus());
        return new PageVo<>(resultPage.getRecords(), resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewJoinApply(Long reviewerId, ReviewMerchantAssociationJoinApplyDto reviewDto) {
        // 1. 查询申请记录
        MerchantAlumniAssociationApply apply = this.getById(reviewDto.getId());
        if (apply == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "申请记录不存在");
        }

        // 3. 校验状态
        if (apply.getStatus() != 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该申请已处理，请勿重复操作");
        }

        // 4. 更新申请状态
        apply.setStatus(reviewDto.getStatus());
        apply.setReviewerId(reviewerId);
        apply.setReviewTime(LocalDateTime.now());
        apply.setReviewComment(reviewDto.getReviewComment());
        boolean updateApplyResult = this.updateById(apply);

        if (!updateApplyResult) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "更新申请状态失败");
        }

        // 5. 如果审核通过，更新商户关联的校友会ID及状态
        if (reviewDto.getStatus() == 1) {
            Merchant merchant = merchantService.getById(apply.getMerchantId());
            if (merchant == null) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户不存在");
            }
            // 设置关联校友会ID (追加到 JSON 数组)
            List<Long> associationIds;
            if (StringUtils.isNotBlank(merchant.getAlumniAssociationId())) {
                associationIds = parseAssociationIds(merchant.getAlumniAssociationId());
            } else {
                associationIds = new ArrayList<>();
            }
            
            if (!associationIds.contains(apply.getAlumniAssociationId())) {
                associationIds.add(apply.getAlumniAssociationId());
            }
            
            merchant.setAlumniAssociationId(JSON.toJSONString(associationIds));
            // 更新为校友商铺
            merchant.setMerchantType(1); // 1-校友商铺
            // 设置为已校友认证
            merchant.setIsAlumniCertified(1); // 1-是
            merchant.setCertifiedTime(LocalDateTime.now());
            
            boolean updateMerchantResult = merchantService.updateById(merchant);
            if (!updateMerchantResult) {
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "更新商户信息失败");
            }
        }

        // 发送审核结果通知
        Merchant merchant = merchantService.getById(apply.getMerchantId());
        if (merchant != null) {
            sendJoinReviewNotification(merchant.getUserId(), merchant.getMerchantName(), apply.getAlumniAssociationId(), reviewDto.getStatus(), reviewDto.getReviewComment());
        }

        return true;
    }

    @Override
    public MerchantAssociationJoinApplyVo getJoinApplyDetail(Long id) {
        MerchantAssociationJoinApplyVo vo = baseMapper.selectJoinApplyDetail(id);
        if (vo != null) {
            populateJoinedAssociations(vo);
        }
        return vo;
    }

    @Override
    public MerchantAssociationJoinApplyVo getLatestJoinApplyDetailByMerchantId(Long merchantId) {
        MerchantAssociationJoinApplyVo vo = baseMapper.selectLatestJoinApplyDetailByMerchantId(merchantId);
        if (vo == null) {
            // 如果没有加入校友会的申请，则回退到查询商户自身的注册审核信息
            MerchantApprovalVo merchantApprovalVo = merchantService.getApprovalRecordByMerchantId(merchantId);
            if (merchantApprovalVo != null) {
                vo = new MerchantAssociationJoinApplyVo();
                vo.setMerchantId(merchantId.toString());
                vo.setMerchantName(merchantApprovalVo.getMerchantName());
                vo.setLogo(merchantApprovalVo.getLogo());
                vo.setMerchantType(merchantApprovalVo.getMerchantType());
                vo.setBusinessLicense(merchantApprovalVo.getBusinessLicense());
                vo.setUnifiedSocialCreditCode(merchantApprovalVo.getUnifiedSocialCreditCode());
                vo.setLegalPerson(merchantApprovalVo.getLegalPerson());
                vo.setPhone(merchantApprovalVo.getPhone());
                vo.setContactPhone(merchantApprovalVo.getContactPhone());
                vo.setBusinessScope(merchantApprovalVo.getBusinessScope());
                vo.setBackgroundImage(merchantApprovalVo.getBackgroundImage());
                vo.setApplicantName(merchantApprovalVo.getApplicantName());
                vo.setApplicantPhone(merchantApprovalVo.getContactPhone());
                // 这里使用的是商户本身的审核状态，可能不是加入校友会的审核状态
                // 但在 my-merchant 详情页展示，也算是合情合理
                vo.setStatus(merchantApprovalVo.getReviewStatus());
                vo.setCreateTime(merchantApprovalVo.getCreateTime());
                vo.setReviewTime(merchantApprovalVo.getReviewTime());
                vo.setReviewComment(merchantApprovalVo.getReviewReason());
                vo.setAlumniAssociation(merchantApprovalVo.getAlumniAssociation());
            }
        }
        if (vo != null) {
            populateJoinedAssociations(vo);
        }
        return vo;
    }

    private void populateJoinedAssociations(MerchantAssociationJoinApplyVo vo) {
        if (vo.getMerchantId() == null) {
            return;
        }
        Merchant merchant = merchantService.getById(Long.parseLong(vo.getMerchantId()));
        if (merchant != null && StringUtils.isNotBlank(merchant.getAlumniAssociationId())) {
            List<Long> associationIds = parseAssociationIds(merchant.getAlumniAssociationId());
            if (!associationIds.isEmpty()) {
                List<AlumniAssociationListVo> joinedList = new ArrayList<>();
                for (Long assocId : associationIds) {
                    AlumniAssociation association = alumniAssociationService.getById(assocId);
                    if (association != null) {
                        AlumniAssociationListVo assocVo = new AlumniAssociationListVo();
                        assocVo.setAlumniAssociationId(association.getAlumniAssociationId());
                        assocVo.setAssociationName(association.getAssociationName());
                        assocVo.setLogo(association.getLogo());
                        joinedList.add(assocVo);
                    }
                }
                vo.setJoinedAssociations(joinedList);
            }
        }
    }
}
