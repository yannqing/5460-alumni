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
     * 根据商户ID和用户ID查询「待审核」或「审核失败」的本人商户申请（用于编辑页回填）
     *
     * @param merchantId 商户ID
     * @param wxId       用户ID
     * @return 商户详情
     */
    MerchantDetailVo getPendingMerchantByIdAndUserId(Long merchantId, Long wxId);

    /**
     * 更新本人「待审核」状态下的商户入驻申请（字段与 {@link #applyMerchant} 一致）
     *
     * @param wxId       用户ID
     * @param merchantId 商户申请ID
     * @param applyDto   申请信息
     * @return 是否成功
     */
    boolean updatePendingMerchantApplication(Long wxId, Long merchantId, ApplyMerchantDto applyDto);

    /**
     * 撤销本人「待审核」的商户入驻申请（审核状态置为 3-已撤销）
     */
    boolean cancelPendingMerchantApplication(Long wxId, Long merchantId);

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
     * 管理员查看单条商户入驻申请详情（含营业执照等申请资料）
     *
     * @param merchantId 商户ID
     * @return 审批记录详情
     */
    MerchantApprovalVo getApprovalRecordByMerchantId(Long merchantId);

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
     * 商户管理员更新商户基本信息（部分字段更新，未传的字段不变）
     *
     * @param wxId 当前用户微信ID
     * @param dto  更新内容（须含 merchantId）
     * @return 更新后的商户详情
     */
    MerchantDetailVo updateMerchantInfo(Long wxId, UpdateMerchantDto dto);

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
