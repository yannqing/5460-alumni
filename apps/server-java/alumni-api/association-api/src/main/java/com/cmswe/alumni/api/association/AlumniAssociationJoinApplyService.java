package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.ApplyAssociationJoinPlatformDto;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApply;

/**
 * 校友会申请加入校促会服务接口
 */
public interface AlumniAssociationJoinApplyService extends IService<AlumniAssociationJoinApply> {

    /**
     * 校友会申请加入校促会
     *
     * @param applyDto 申请信息
     * @return 是否申请成功
     */
    boolean applyJoinPlatform(ApplyAssociationJoinPlatformDto applyDto);

    /**
     * 审核校友会加入校促会申请
     *
     * @param reviewDto 审核信息
     * @return 是否审核成功
     */
    boolean reviewJoinPlatform(com.cmswe.alumni.common.dto.ReviewAssociationJoinPlatformDto reviewDto);

    /**
     * 分页查询校友会加入校促会申请列表
     *
     * @param queryDto 查询参数
     * @return 分页结果
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<AlumniAssociationJoinApply> queryApplyPage(
            com.cmswe.alumni.common.dto.QueryAssociationJoinApplyDto queryDto);
}
