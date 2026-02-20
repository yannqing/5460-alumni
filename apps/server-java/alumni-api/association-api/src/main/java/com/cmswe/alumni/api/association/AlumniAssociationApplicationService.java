package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.ApplyCreateAlumniAssociationDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationApplicationListDto;
import com.cmswe.alumni.common.dto.ReviewAlumniAssociationApplicationDto;
import com.cmswe.alumni.common.entity.AlumniAssociationApplication;
import com.cmswe.alumni.common.vo.AlumniAssociationApplicationListVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 校友会创建申请服务接口
 */
public interface AlumniAssociationApplicationService extends IService<AlumniAssociationApplication> {

    /**
     * 申请创建校友会
     *
     * @param wxId     申请人微信用户ID
     * @param applyDto 申请信息
     * @return 是否申请成功
     */
    boolean applyToCreateAssociation(Long wxId, ApplyCreateAlumniAssociationDto applyDto);

    /**
     * 分页查询校友会创建申请列表
     *
     * @param queryDto 查询条件
     * @return 申请列表
     */
    PageVo<AlumniAssociationApplicationListVo> queryApplicationPage(QueryAlumniAssociationApplicationListDto queryDto);

    /**
     * 审核校友会创建申请
     *
     * @param reviewerId 审核人ID
     * @param reviewDto  审核信息
     * @return 是否审核成功
     */
    boolean reviewApplication(Long reviewerId, ReviewAlumniAssociationApplicationDto reviewDto);
}
