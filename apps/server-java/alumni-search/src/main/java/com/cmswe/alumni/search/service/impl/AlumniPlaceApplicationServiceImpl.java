package com.cmswe.alumni.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.search.AlumniPlaceApplicationService;
import com.cmswe.alumni.api.search.AlumniPlaceService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.dto.ApplyAlumniPlaceDto;
import com.cmswe.alumni.common.dto.ApproveAlumniPlaceApplicationDto;
import com.cmswe.alumni.common.dto.QueryAlumniPlaceApplicationDto;
import com.cmswe.alumni.common.entity.AlumniAssociationMember;
import com.cmswe.alumni.common.entity.AlumniPlace;
import com.cmswe.alumni.common.entity.AlumniPlaceApplication;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniPlaceApplicationVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.search.mapper.AlumniPlaceApplicationMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 校友企业/场所申请 Service 实现
 *
 * @author CNI Alumni System
 */
@Slf4j
@Service
public class AlumniPlaceApplicationServiceImpl
        extends ServiceImpl<AlumniPlaceApplicationMapper, AlumniPlaceApplication>
        implements AlumniPlaceApplicationService {

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Resource
    private AlumniAssociationMemberService alumniAssociationMemberService;

    @Resource
    private AlumniPlaceService alumniPlaceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyAlumniPlace(Long wxId, ApplyAlumniPlaceDto applyDto) {
        log.info("用户申请创建校友企业/场所，用户 ID: {}, 场所名称: {}", wxId, applyDto.getPlaceName());

        // 1. 参数校验
        Optional.ofNullable(wxId).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空"));
        Optional.ofNullable(applyDto).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "申请信息不能为空"));

        // 2. 验证用户是否为校友（是否有用户信息）
        WxUserInfo wxUserInfo = wxUserInfoService.getOne(
                new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, wxId)
        );
        if (wxUserInfo == null) {
            throw new BusinessException("用户信息不存在，请先完善个人信息");
        }

        // 3. 验证用户是否已加入指定的校友会
        LambdaQueryWrapper<AlumniAssociationMember> memberQueryWrapper = new LambdaQueryWrapper<>();
        memberQueryWrapper
                .eq(AlumniAssociationMember::getWxId, wxId)
                .eq(AlumniAssociationMember::getAlumniAssociationId, applyDto.getAlumniAssociationId())
                .eq(AlumniAssociationMember::getStatus, 1); // 状态：1-正常

        AlumniAssociationMember member = alumniAssociationMemberService.getOne(memberQueryWrapper);
        if (member == null) {
            throw new BusinessException("您尚未加入该校友会，无法申请创建企业/场所");
        }

        // 4. 检查是否有重复的待审核申请
        LambdaQueryWrapper<AlumniPlaceApplication> applicationQueryWrapper = new LambdaQueryWrapper<>();
        applicationQueryWrapper
                .eq(AlumniPlaceApplication::getApplicantId, wxId)
                .eq(AlumniPlaceApplication::getPlaceName, applyDto.getPlaceName())
                .eq(AlumniPlaceApplication::getApplicationStatus, 0); // 0-待审核

        long pendingCount = this.count(applicationQueryWrapper);
        if (pendingCount > 0) {
            throw new BusinessException("您已提交过相同名称的企业/场所申请，请等待审核");
        }

        // 5. 创建申请记录
        AlumniPlaceApplication application = new AlumniPlaceApplication();
        application.setApplicantId(wxId);
        application.setApplicantName(wxUserInfo.getName());
        application.setApplicantPhone(wxUserInfo.getPhone());
        application.setPlaceName(applyDto.getPlaceName());
        application.setPlaceType(applyDto.getPlaceType());
        application.setAlumniAssociationId(applyDto.getAlumniAssociationId());
        application.setProvince(applyDto.getProvince());
        application.setCity(applyDto.getCity());
        application.setDistrict(applyDto.getDistrict());
        application.setAddress(applyDto.getAddress());
        application.setLatitude(applyDto.getLatitude());
        application.setLongitude(applyDto.getLongitude());
        application.setContactPhone(applyDto.getContactPhone());
        application.setContactEmail(applyDto.getContactEmail());
        application.setBusinessHours(applyDto.getBusinessHours());
        application.setImages(applyDto.getImages());
        application.setLogo(applyDto.getLogo());
        application.setDescription(applyDto.getDescription());
        application.setEstablishedTime(applyDto.getEstablishedTime());
        application.setApplicationStatus(0); // 0-待审核
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());

        boolean saveResult = this.save(application);

        if (saveResult) {
            log.info("用户申请创建校友企业/场所成功，用户 ID: {}, 申请 ID: {}", wxId, application.getApplicationId());
        } else {
            log.error("用户申请创建校友企业/场所失败，用户 ID: {}", wxId);
            throw new BusinessException("申请提交失败，请重试");
        }

        return saveResult;
    }

    @Override
    public PageVo<AlumniPlaceApplicationVo> getApplicationPage(QueryAlumniPlaceApplicationDto queryDto) {
        log.info("管理员查询企业/场所申请列表，查询条件: {}", queryDto);

        // 1. 参数校验
        Optional.ofNullable(queryDto).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "查询参数不能为空"));

        // 2. 构建查询条件
        LambdaQueryWrapper<AlumniPlaceApplication> queryWrapper = new LambdaQueryWrapper<>();

        // 场所/企业名称模糊查询
        if (StringUtils.isNotBlank(queryDto.getPlaceName())) {
            queryWrapper.like(AlumniPlaceApplication::getPlaceName, queryDto.getPlaceName());
        }

        // 申请状态
        if (queryDto.getApplicationStatus() != null) {
            queryWrapper.eq(AlumniPlaceApplication::getApplicationStatus, queryDto.getApplicationStatus());
        }

        // 类型
        if (queryDto.getPlaceType() != null) {
            queryWrapper.eq(AlumniPlaceApplication::getPlaceType, queryDto.getPlaceType());
        }

        // 所属校友会ID
        if (queryDto.getAlumniAssociationId() != null) {
            queryWrapper.eq(AlumniPlaceApplication::getAlumniAssociationId, queryDto.getAlumniAssociationId());
        }

        // 申请人姓名模糊查询
        if (StringUtils.isNotBlank(queryDto.getApplicantName())) {
            queryWrapper.like(AlumniPlaceApplication::getApplicantName, queryDto.getApplicantName());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(AlumniPlaceApplication::getCreateTime);

        // 3. 执行分页查询
        long current = queryDto.getCurrent();
        long pageSize = queryDto.getPageSize();
        Page<AlumniPlaceApplication> applicationPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 4. 转换为 VO
        List<AlumniPlaceApplicationVo> voList = applicationPage.getRecords().stream()
                .map(AlumniPlaceApplicationVo::objToVo)
                .collect(Collectors.toList());

        log.info("管理员查询企业/场所申请列表成功，总记录数: {}", applicationPage.getTotal());

        // 5. 返回结果
        Page<AlumniPlaceApplicationVo> resultPage = new Page<AlumniPlaceApplicationVo>(current, pageSize,
                applicationPage.getTotal()).setRecords(voList);
        return PageVo.of(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveApplication(Long reviewUserId, ApproveAlumniPlaceApplicationDto approveDto) {
        log.info("管理员审核企业/场所申请，审核人 ID: {}, 申请 ID: {}, 审核状态: {}",
                reviewUserId, approveDto.getApplicationId(), approveDto.getApplicationStatus());

        // 1. 参数校验
        Optional.ofNullable(reviewUserId).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "审核人ID不能为空"));
        Optional.ofNullable(approveDto).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "审核信息不能为空"));

        if (approveDto.getApplicationStatus() != 1 && approveDto.getApplicationStatus() != 2) {
            throw new BusinessException("审核状态只能是 1-审核通过 或 2-审核拒绝");
        }

        // 审核拒绝时必须填写备注
        if (approveDto.getApplicationStatus() == 2 && StringUtils.isBlank(approveDto.getReviewRemark())) {
            throw new BusinessException("审核拒绝时必须填写审核备注");
        }

        // 2. 查询申请记录
        AlumniPlaceApplication application = this.getById(approveDto.getApplicationId());
        if (application == null) {
            throw new BusinessException("申请记录不存在");
        }

        // 3. 检查申请状态（只能审核待审核状态的申请）
        if (application.getApplicationStatus() != 0) {
            throw new BusinessException("该申请已审核，无法重复审核");
        }

        // 4. 查询审核人信息
        WxUserInfo reviewUser = wxUserInfoService.getOne(
                new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, reviewUserId)
        );

        // 5. 更新申请记录
        application.setApplicationStatus(approveDto.getApplicationStatus());
        application.setReviewUserId(reviewUserId);
        application.setReviewUserName(reviewUser != null ? reviewUser.getName() : null);
        application.setReviewTime(LocalDateTime.now());
        application.setReviewRemark(approveDto.getReviewRemark());
        application.setUpdateTime(LocalDateTime.now());

        // 6. 如果审核通过，创建企业/场所记录
        if (approveDto.getApplicationStatus() == 1) {
            AlumniPlace alumniPlace = new AlumniPlace();
            // 复制申请信息到企业记录
            alumniPlace.setPlaceName(application.getPlaceName());
            alumniPlace.setPlaceType(application.getPlaceType());
            alumniPlace.setAlumniId(application.getApplicantId());
            alumniPlace.setAlumniAssociationId(application.getAlumniAssociationId());
            alumniPlace.setProvince(application.getProvince());
            alumniPlace.setCity(application.getCity());
            alumniPlace.setDistrict(application.getDistrict());
            alumniPlace.setAddress(application.getAddress());
            alumniPlace.setLatitude(application.getLatitude());
            alumniPlace.setLongitude(application.getLongitude());
            alumniPlace.setContactPhone(application.getContactPhone());
            alumniPlace.setContactEmail(application.getContactEmail());
            alumniPlace.setBusinessHours(application.getBusinessHours());
            alumniPlace.setImages(application.getImages());
            alumniPlace.setLogo(application.getLogo());
            alumniPlace.setDescription(application.getDescription());
            alumniPlace.setEstablishedTime(application.getEstablishedTime());
            alumniPlace.setStatus(1); // 状态：1-正常运营
            alumniPlace.setReviewStatus(1); // 审核状态：1-审核通过
            alumniPlace.setIsRecommended(0); // 校友会推荐：0-否
            alumniPlace.setViewCount(0L);
            alumniPlace.setClickCount(0L);
            alumniPlace.setCreatedBy(application.getApplicantId());
            alumniPlace.setCreateTime(LocalDateTime.now());
            alumniPlace.setUpdateTime(LocalDateTime.now());

            // 保存企业记录
            boolean savePlaceResult = alumniPlaceService.save(alumniPlace);
            if (!savePlaceResult) {
                log.error("创建企业/场所记录失败，申请 ID: {}", approveDto.getApplicationId());
                throw new BusinessException("创建企业/场所记录失败");
            }

            // 将企业ID关联到申请记录
            application.setPlaceId(alumniPlace.getPlaceId());
            log.info("审核通过，已创建企业/场所记录，场所 ID: {}", alumniPlace.getPlaceId());
        }

        // 7. 更新申请记录
        boolean updateResult = this.updateById(application);

        if (updateResult) {
            log.info("管理员审核企业/场所申请成功，申请 ID: {}, 审核结果: {}",
                    approveDto.getApplicationId(),
                    approveDto.getApplicationStatus() == 1 ? "通过" : "拒绝");
        } else {
            log.error("管理员审核企业/场所申请失败，申请 ID: {}", approveDto.getApplicationId());
            throw new BusinessException("审核失败，请重试");
        }

        return updateResult;
    }
}
