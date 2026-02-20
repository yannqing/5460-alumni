package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.ApplyAlumniAssociationDto;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationJoinApplicationListDto;
import com.cmswe.alumni.common.dto.ReviewAlumniAssociationJoinApplicationDto;
import com.cmswe.alumni.common.dto.UpdateAlumniAssociationJoinApplicationDto;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApplication;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplicationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplicationListVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 校友会加入申请服务接口
 */
public interface AlumniAssociationJoinApplicationService extends IService<AlumniAssociationJoinApplication> {

    /**
     * 申请加入校友会（普通用户）
     *
     * @param wxId     用户ID
     * @param applyDto 申请信息
     * @return 是否申请成功
     */
    boolean applyToJoinAssociation(Long wxId, ApplyAlumniAssociationDto applyDto);

    /**
     * 审核校友会加入申请
     *
     * @param reviewerId 审核人ID
     * @param reviewDto  审核信息
     * @return 是否审核成功
     */
    boolean reviewApplication(Long reviewerId, ReviewAlumniAssociationJoinApplicationDto reviewDto);

    /**
     * 分页查询校友会加入申请列表
     *
     * @param queryDto 查询条件
     * @return 申请列表
     */
    PageVo<AlumniAssociationJoinApplicationListVo> queryApplicationPage(QueryAlumniAssociationJoinApplicationListDto queryDto);

    /**
     * 查看用户自己的申请详情
     *
     * @param wxId                用户ID
     * @param alumniAssociationId 校友会ID
     * @return 申请详情
     */
    AlumniAssociationJoinApplicationDetailVo getApplicationDetail(Long wxId, Long alumniAssociationId);

    /**
     * 编辑并重新提交待审核的校友会加入申请（普通用户）
     *
     * @param wxId      用户ID
     * @param updateDto 更新信息
     * @return 是否更新成功
     */
    boolean updateAndResubmitApplication(Long wxId, UpdateAlumniAssociationJoinApplicationDto updateDto);

    /**
     * 撤销校友会申请（普通用户）
     *
     * @param wxId          用户ID
     * @param applicationId 申请ID
     * @return 是否撤销成功
     */
    boolean cancelApplication(Long wxId, Long applicationId);

    /**
     * 退出校友会（普通用户）
     *
     * @param wxId                用户ID
     * @param alumniAssociationId 校友会ID
     * @return 是否退出成功
     */
    boolean quitAlumniAssociation(Long wxId, Long alumniAssociationId);
}
