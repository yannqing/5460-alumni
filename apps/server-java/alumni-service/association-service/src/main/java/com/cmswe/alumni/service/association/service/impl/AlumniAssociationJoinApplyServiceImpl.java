package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationJoinApplyService;
import com.cmswe.alumni.common.dto.ApplyAssociationJoinPlatformDto;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApply;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationJoinApplyMapper;
import org.springframework.stereotype.Service;

/**
 * 校友会申请加入校促会服务实现类
 */
@Service
public class AlumniAssociationJoinApplyServiceImpl
        extends ServiceImpl<AlumniAssociationJoinApplyMapper, AlumniAssociationJoinApply>
        implements AlumniAssociationJoinApplyService {

    @jakarta.annotation.Resource
    private com.cmswe.alumni.api.association.AlumniAssociationService alumniAssociationService;

    @Override
    public boolean applyJoinPlatform(ApplyAssociationJoinPlatformDto applyDto) {
        AlumniAssociationJoinApply apply = new AlumniAssociationJoinApply();
        apply.setAlumniAssociationId(applyDto.getAlumniAssociationId());
        apply.setPlatformId(applyDto.getPlatformId());
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

        // 3. 如果审核通过，更新校友会关联的校促会ID
        if (reviewDto.getStatus() == 1) { // 1-已通过
            com.cmswe.alumni.common.entity.AlumniAssociation association = alumniAssociationService
                    .getById(apply.getAlumniAssociationId());
            if (association != null) {
                association.setPlatformId(apply.getPlatformId());
                boolean updateAssociation = alumniAssociationService.updateById(association);
                if (!updateAssociation) {
                    throw new com.cmswe.alumni.common.exception.BusinessException("更新校友会关联校促会失败");
                }
            }
        }
        return true;
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
                    return com.cmswe.alumni.common.vo.AlumniAssociationJoinApplyVo.objToVo(apply, association);
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

        // 4. 转换为VO
        return com.cmswe.alumni.common.vo.AlumniAssociationJoinApplyVo.objToVo(apply, association);
    }
}
