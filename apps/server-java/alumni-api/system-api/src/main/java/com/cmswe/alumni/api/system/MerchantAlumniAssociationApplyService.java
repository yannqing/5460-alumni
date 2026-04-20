package com.cmswe.alumni.api.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.ApplyMerchantAssociationJoinDto;
import com.cmswe.alumni.common.dto.QueryMerchantAssociationJoinApplyDto;
import com.cmswe.alumni.common.dto.ReviewMerchantAssociationJoinApplyDto;
import com.cmswe.alumni.common.entity.MerchantAlumniAssociationApply;
import com.cmswe.alumni.common.vo.MerchantAssociationJoinApplyVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 商户入驻校友会申请服务接口
 */
public interface MerchantAlumniAssociationApplyService extends IService<MerchantAlumniAssociationApply> {
    /**
     * 商户申请加入校友会
     */
    boolean applyJoinAssociation(Long wxId, ApplyMerchantAssociationJoinDto applyDto);

    /**
     * 分页查询商户加入校友会申请列表
     */
    PageVo<MerchantAssociationJoinApplyVo> queryJoinApplyPage(QueryMerchantAssociationJoinApplyDto queryDto);

    /**
     * 审核商户加入校友会申请
     */
    boolean reviewJoinApply(Long reviewerId, ReviewMerchantAssociationJoinApplyDto reviewDto);

    /**
     * 获取商户加入校友会申请详情
     */
    MerchantAssociationJoinApplyVo getJoinApplyDetail(Long id);

    /**
     * 根据商户ID获取最近一次加入校友会申请详情
     */
    MerchantAssociationJoinApplyVo getLatestJoinApplyDetailByMerchantId(Long merchantId);
}
