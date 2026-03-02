package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.AlumniHeadquarters;
import com.cmswe.alumni.common.vo.AlumniHeadquartersDetailVo;

import com.cmswe.alumni.common.dto.ApplyActivateHeadquartersRequest;
import com.cmswe.alumni.common.dto.AuditHeadquartersRequest;
import com.cmswe.alumni.common.dto.QueryAlumniHeadquartersListDto;
import com.cmswe.alumni.common.vo.AlumniHeadquartersListVo;
import com.cmswe.alumni.common.vo.InactiveAlumniHeadquartersVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 校友总会服务接口
 */
public interface AlumniHeadquartersService extends IService<AlumniHeadquarters> {

    /**
     * 根据id获取校友总会详情
     * 
     * @param id 校友总会id
     * @return 返回结果
     */
    AlumniHeadquartersDetailVo getAlumniHeadquartersDetailById(Long id);

    /**
     * 分页查询校友总会列表
     * 
     * @param infoDTO 分页查询DTO
     * @return 分页结果
     */
    PageVo<AlumniHeadquartersListVo> selectByPage(QueryAlumniHeadquartersListDto infoDTO);

    /**
     * 申请激活校友总会
     * 
     * @param request 激活请求参数
     * @param userId  操作用户ID
     * @return 激活结果
     */
    boolean applyActivateHeadquarters(ApplyActivateHeadquartersRequest request, Long userId);

    /**
     * 分页查询未激活校友总会列表 (仅返回 id 和 名称)
     * 
     * @param pageRequest 分页参数
     * @return 分页结果
     */
    PageVo<InactiveAlumniHeadquartersVo> selectInactiveByPage(QueryAlumniHeadquartersListDto pageRequest);

    /**
     * 审核校友总会
     * 
     * @param request 审核请求参数
     * @param userId  操作人ID
     * @return 审核结果
     */
    boolean auditHeadquarters(AuditHeadquartersRequest request, Long userId);

    /**
     * 分页查询待审核校友总会列表
     * 
     * @param infoDTO 分页查询DTO
     * @return 分页结果
     */
    PageVo<AlumniHeadquartersListVo> selectPendingByPage(QueryAlumniHeadquartersListDto infoDTO);

    /**
     * 管理员查看校友总会申请详情（不限状态）
     *
     * @param headquartersId 校友总会ID
     * @return 详情VO
     */
    AlumniHeadquartersDetailVo getApplyDetailByAdmin(Long headquartersId);
}
