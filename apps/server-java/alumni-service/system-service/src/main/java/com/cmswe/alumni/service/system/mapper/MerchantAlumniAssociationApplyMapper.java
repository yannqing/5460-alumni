package com.cmswe.alumni.service.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmswe.alumni.common.entity.MerchantAlumniAssociationApply;
import com.cmswe.alumni.common.vo.MerchantAssociationJoinApplyVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商户入驻校友会申请 Mapper
 */
@Mapper
public interface MerchantAlumniAssociationApplyMapper extends BaseMapper<MerchantAlumniAssociationApply> {

    /**
     * 分页查询商户加入校友会申请列表
     */
    IPage<MerchantAssociationJoinApplyVo> selectJoinApplyPage(
            Page<MerchantAssociationJoinApplyVo> page,
            @Param("alumniAssociationId") Long alumniAssociationId,
            @Param("merchantId") Long merchantId,
            @Param("status") Integer status);

    /**
     * 根据ID查询商户加入校友会申请详情
     */
    MerchantAssociationJoinApplyVo selectJoinApplyDetail(@Param("id") Long id);

    /**
     * 根据商户ID查询最近一次加入校友会申请详情
     */
    MerchantAssociationJoinApplyVo selectLatestJoinApplyDetailByMerchantId(@Param("merchantId") Long merchantId);
}
