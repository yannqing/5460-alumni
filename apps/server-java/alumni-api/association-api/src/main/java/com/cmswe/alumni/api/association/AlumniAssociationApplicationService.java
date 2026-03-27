package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.ApplyCreateAlumniAssociationDto;
import com.cmswe.alumni.common.dto.UpdatePendingAlumniAssociationApplicationDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationApplicationListDto;
import com.cmswe.alumni.common.dto.QuerySystemAdminApplicationListDto;
import com.cmswe.alumni.common.dto.ReviewAlumniAssociationApplicationDto;
import com.cmswe.alumni.common.entity.AlumniAssociationApplication;
import com.cmswe.alumni.common.vo.AlumniAssociationApplicationDetailVo;
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
     * 申请人编辑待审核的创建校友会申请（仅驻会代表本人、且状态为待审核）
     */
    boolean updatePendingApplication(Long wxId, UpdatePendingAlumniAssociationApplicationDto dto);

    /**
     * 申请人撤销待审核的创建校友会申请（仅驻会代表本人、且状态为待审核）
     */
    boolean cancelPendingApplication(Long wxId, Long applicationId);

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

    /**
     * 系统管理员分页查询所有校友会创建申请列表
     *
     * @param queryDto 查询条件
     * @return 申请列表
     */
    PageVo<AlumniAssociationApplicationListVo> querySystemAdminApplicationPage(QuerySystemAdminApplicationListDto queryDto);

    /**
     * 根据申请ID查询申请详情
     *
     * @param applicationId 申请ID
     * @return 申请详情
     */
    AlumniAssociationApplicationDetailVo getApplicationDetailById(Long applicationId);
}
