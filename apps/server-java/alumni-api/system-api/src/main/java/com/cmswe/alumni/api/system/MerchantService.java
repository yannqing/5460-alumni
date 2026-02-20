package com.cmswe.alumni.api.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.*;
import com.cmswe.alumni.common.entity.Merchant;
import com.cmswe.alumni.common.vo.MerchantApprovalVo;
import com.cmswe.alumni.common.vo.MerchantDetailVo;
import com.cmswe.alumni.common.vo.MerchantListVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 商户 Service 接口
 *
 * @author CNI Alumni System
 */
public interface MerchantService extends IService<Merchant> {

    /**
     * 分页查询商户列表
     *
     * @param queryMerchantListDto 查询条件
     * @return 分页结果
     */
    PageVo<MerchantListVo> selectByPage(QueryMerchantListDto queryMerchantListDto);

    /**
     * 根据商户ID和用户ID查询审核失败的商户申请
     *
     * @param merchantId 商户ID
     * @param wxId       用户ID
     * @return 商户详情
     */
    MerchantDetailVo getPendingMerchantByIdAndUserId(Long merchantId, Long wxId);

    /**
     * 用户提交商户入驻申请
     *
     * @param wxId     用户ID
     * @param applyDto 申请信息
     * @return 是否成功
     */
    boolean applyMerchant(Long wxId, ApplyMerchantDto applyDto);

    /**
     * 管理员审批商户入驻申请
     *
     * @param reviewerId 审核人ID
     * @param approveDto 审批信息
     * @return 是否成功
     */
    boolean approveMerchant(Long reviewerId, ApproveMerchantDto approveDto);

    /**
     * 分页查询商户审批记录
     *
     * @param queryDto 查询条件
     * @return 分页结果
     */
    PageVo<MerchantApprovalVo> selectApprovalRecordsByPage(QueryMerchantApprovalDto queryDto);

    /**
     * 根据商户ID查询商户详情
     *
     * @param merchantId 商户ID
     * @return 商户详情
     */
    MerchantDetailVo getMerchantDetailById(Long merchantId);

    /**
     * 查询用户负责的商户列表（根据角色）
     *
     * @param wxId    用户微信ID
     * @param current 当前页
     * @param size    每页大小
     * @return 商户列表分页数据
     */
    PageVo<MerchantListVo> getMyManagedMerchants(Long wxId, Long current, Long size);

    /**
     * 查询商户成员列表
     *
     * @param merchantId 商户ID
     * @return 商户成员列表
     */
    java.util.List<com.cmswe.alumni.common.vo.MerchantMemberVo> getMerchantMembers(Long merchantId);

    /**
     * 添加商户成员
     *
     * @param addDto 添加成员信息
     * @return 是否成功
     */
    boolean addMerchantMember(AddMerchantMemberDto addDto);

    /**
     * 更新商户成员角色
     *
     * @param updateDto 更新角色信息
     * @return 是否成功
     */
    boolean updateMerchantMemberRole(UpdateMerchantMemberRoleDto updateDto);

    /**
     * 删除商户成员
     *
     * @param deleteDto 删除成员信息
     * @return 是否成功
     */
    boolean deleteMerchantMember(DeleteMerchantMemberDto deleteDto);
}
