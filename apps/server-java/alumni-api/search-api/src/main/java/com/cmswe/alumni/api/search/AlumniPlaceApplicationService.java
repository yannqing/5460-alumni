package com.cmswe.alumni.api.search;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.ApplyAlumniPlaceDto;
import com.cmswe.alumni.common.dto.ApproveAlumniPlaceApplicationDto;
import com.cmswe.alumni.common.dto.QueryAlumniPlaceApplicationDto;
import com.cmswe.alumni.common.entity.AlumniPlaceApplication;
import com.cmswe.alumni.common.vo.AlumniPlaceApplicationVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 校友企业/场所申请 Service 接口
 *
 * @author CNI Alumni System
 */
public interface AlumniPlaceApplicationService extends IService<AlumniPlaceApplication> {

    /**
     * 用户申请创建校友企业/场所
     *
     * @param wxId     申请人ID
     * @param applyDto 申请信息
     * @return 申请是否成功
     */
    boolean applyAlumniPlace(Long wxId, ApplyAlumniPlaceDto applyDto);

    /**
     * 管理员分页查询企业/场所申请列表
     *
     * @param queryDto 查询条件
     * @return 申请列表
     */
    PageVo<AlumniPlaceApplicationVo> getApplicationPage(QueryAlumniPlaceApplicationDto queryDto);

    /**
     * 管理员审核企业/场所申请
     *
     * @param reviewUserId 审核人ID
     * @param approveDto   审核信息
     * @return 审核是否成功
     */
    boolean approveApplication(Long reviewUserId, ApproveAlumniPlaceApplicationDto approveDto);
}
