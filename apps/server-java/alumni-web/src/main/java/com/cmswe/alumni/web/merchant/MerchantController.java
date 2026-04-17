package com.cmswe.alumni.web.merchant;

import com.cmswe.alumni.api.search.ShopService;
import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.dto.ApplyMerchantDto;
import com.cmswe.alumni.common.dto.QueryMerchantListDto;
import com.cmswe.alumni.common.dto.AddMerchantMemberDto;
import com.cmswe.alumni.common.dto.UpdateMerchantMemberRoleDto;
import com.cmswe.alumni.common.dto.DeleteMerchantMemberDto;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.MerchantDetailVo;
import com.cmswe.alumni.common.vo.MerchantListVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.ShopDetailVo;
import com.cmswe.alumni.common.vo.MerchantMemberVo;

import java.util.List;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



/**
 * 商户管理 Controller
 *
 * @author CNI Alumni System
 */
@Tag(name = "商户管理", description = "商户相关接口")
@Slf4j
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Resource
    private MerchantService merchantService;

    @Resource
    private WxUserMapper wxUserMapper;

    @Resource
    private ShopService shopService;

    /**
     * 分页查询商户列表
     *
     * @param queryMerchantListDto 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询商户列表")
    public BaseResponse<PageVo<MerchantListVo>> selectPage(@RequestBody QueryMerchantListDto queryMerchantListDto) {
        log.info("分页查询商户列表，查询条件：{}", queryMerchantListDto);
        PageVo<MerchantListVo> pageVo = merchantService.selectByPage(queryMerchantListDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "分页查询成功");
    }

    /**
     * 根据商户ID查询当前用户的审核失败商户申请
     *
     * @param securityUser 当前登录用户
     * @param merchantId 商户ID
     * @return 商户详情
     */
    @GetMapping("/pending/{merchantId}")
    @Operation(summary = "查询当前用户「待审核」或「审核失败」的商户申请（回填用）")
    public BaseResponse<MerchantDetailVo> getPendingMerchant(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long merchantId) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("查询待审核/审核失败商户申请 - 用户ID: {}, 商户ID: {}", wxId, merchantId);

        MerchantDetailVo merchantDetail = merchantService.getPendingMerchantByIdAndUserId(merchantId, wxId);
        return ResultUtils.success(Code.SUCCESS, merchantDetail, "查询成功");
    }

    /**
     * 修改「待审核」状态下的商户入驻申请（与 POST /apply 字段一致）
     */
    @PutMapping("/pending-application/{merchantId}")
    @Operation(summary = "更新待审核的商户入驻申请")
    public BaseResponse<Boolean> updatePendingMerchantApplication(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long merchantId,
            @Valid @RequestBody ApplyMerchantDto applyDto) {
        Long wxId = securityUser.getWxUser().getWxId();

        WxUser wxUser = wxUserMapper.selectById(wxId);
        if (wxUser == null) {
            log.error("用户不存在 - 用户ID: {}", wxId);
            throw new BusinessException("用户不存在");
        }
        if (wxUser.getCertificationFlag() == null || wxUser.getCertificationFlag() == 0) {
            throw new BusinessException("只有认证校友才能申请商户入驻");
        }

        boolean result = merchantService.updatePendingMerchantApplication(wxId, merchantId, applyDto);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, true, "保存成功，请等待审核");
        }
        return ResultUtils.failure(Code.FAILURE, false, "更新失败");
    }

    /**
     * 用户提交商户入驻申请
     *
     * @param securityUser 当前登录用户
     * @param applyDto 申请信息
     * @return 提交结果
     */
    @PostMapping("/apply")
    @Operation(summary = "提交商户入驻申请")
    public BaseResponse<Boolean> applyMerchant(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ApplyMerchantDto applyDto) {
        Long wxId = securityUser.getWxUser().getWxId();

        // 从数据库查询实时的用户信息
        WxUser wxUser = wxUserMapper.selectById(wxId);
        if (wxUser == null) {
            log.error("用户不存在 - 用户ID: {}", wxId);
            throw new BusinessException("用户不存在");
        }

        Integer certificationFlag = wxUser.getCertificationFlag();

        log.info("用户提交商户入驻申请 - 用户ID: {}, 商户名称: {}, 认证标识: {}", wxId, applyDto.getMerchantName(), certificationFlag);

        // 校验用户是否已认证（校友）
        if (certificationFlag == null || certificationFlag == 0) {
            log.warn("未认证用户尝试提交商户入驻申请 - 用户ID: {}, certificationFlag: {}", wxId, certificationFlag);
            throw new BusinessException("只有认证校友才能申请商户入驻");
        }

        boolean result = merchantService.applyMerchant(wxId, applyDto);

        if (result) {
            log.info("商户入驻申请提交成功 - 用户ID: {}, 商户名称: {}", wxId, applyDto.getMerchantName());
            return ResultUtils.success(Code.SUCCESS, true, "申请提交成功，预计1-2个工作日内完成审核");
        } else {
            log.error("商户入驻申请提交失败 - 用户ID: {}, 商户名称: {}", wxId, applyDto.getMerchantName());
            return ResultUtils.failure(Code.FAILURE, false, "申请提交失败");
        }
    }

    /**
     * 根据商户ID查询商户详情
     *
     * @param merchantId 商户ID
     * @return 商户详情
     */
    @GetMapping("/{merchantId}")
    @Operation(summary = "根据商户ID查询商户详情")
    public BaseResponse<MerchantDetailVo> getMerchantById(@PathVariable Long merchantId) {
        log.info("查询商户详情 - 商户ID: {}", merchantId);
        MerchantDetailVo merchantDetail = merchantService.getMerchantDetailById(merchantId);
        return ResultUtils.success(Code.SUCCESS, merchantDetail, "查询成功");
    }

    /**
     * 根据店铺ID查询门店详情
     *
     * @param shopId 店铺ID
     * @return 门店详情
     */
    @GetMapping("/shop/{shopId}")
    @Operation(summary = "根据店铺ID查询门店详情")
    public BaseResponse<ShopDetailVo> getShopById(@PathVariable Long shopId) {
        log.info("查询门店详情 - 店铺ID: {}", shopId);
        ShopDetailVo shopDetail = shopService.getShopDetail(shopId);
        return ResultUtils.success(Code.SUCCESS, shopDetail, "查询成功");
    }

    /**
     * 商户主账号查询待审核等门店详情（用于编辑；权限与更新店铺一致）
     */
    @GetMapping("/shop/{shopId}/for-edit")
    @Operation(summary = "申请人/商户主账号查询门店编辑用详情（含待审核）")
    public BaseResponse<ShopDetailVo> getShopForApplicantEdit(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long shopId) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("门店编辑详情 - 用户ID: {}, 店铺ID: {}", wxId, shopId);
        ShopDetailVo shopDetail = shopService.getShopDetailForApplicantEdit(wxId, shopId);
        return ResultUtils.success(Code.SUCCESS, shopDetail, "查询成功");
    }

    /**
     * 查询商户成员列表
     *
     * @param merchantId 商户ID
     * @return 商户成员列表
     */
    @GetMapping("/{merchantId}/members")
    @Operation(summary = "查询商户成员列表")
    public BaseResponse<List<MerchantMemberVo>> getMerchantMembers(@PathVariable Long merchantId) {
        log.info("查询商户成员列表 - 商户ID: {}", merchantId);
        List<MerchantMemberVo> members = merchantService.getMerchantMembers(merchantId);
        return ResultUtils.success(Code.SUCCESS, members, "查询成功");
    }

    /**
     * 添加商户成员
     *
     * @param addDto 添加成员信息
     * @return 操作结果
     */
    @PostMapping("/member/add")
    @Operation(summary = "添加商户成员")
    public BaseResponse<Boolean> addMerchantMember(@Valid @RequestBody AddMerchantMemberDto addDto) {
        log.info("添加商户成员 - 商户ID: {}, 用户ID: {}", addDto.getMerchantId(), addDto.getWxId());
        boolean result = merchantService.addMerchantMember(addDto);
        return ResultUtils.success(Code.SUCCESS, result, "添加成功");
    }

    /**
     * 更新商户成员角色
     *
     * @param updateDto 更新角色信息
     * @return 操作结果
     */
    @PostMapping("/member/update-role")
    @Operation(summary = "更新商户成员角色")
    public BaseResponse<Boolean> updateMerchantMemberRole(@Valid @RequestBody UpdateMerchantMemberRoleDto updateDto) {
        log.info("更新商户成员角色 - 商户ID: {}, 用户ID: {}, 新角色ID: {}",
                updateDto.getMerchantId(), updateDto.getWxId(), updateDto.getRoleOrId());
        boolean result = merchantService.updateMerchantMemberRole(updateDto);
        return ResultUtils.success(Code.SUCCESS, result, "更新成功");
    }

    /**
     * 删除商户成员
     *
     * @param deleteDto 删除成员信息
     * @return 操作结果
     */
    @PostMapping("/member/delete")
    @Operation(summary = "删除商户成员")
    public BaseResponse<Boolean> deleteMerchantMember(@Valid @RequestBody DeleteMerchantMemberDto deleteDto) {
        log.info("删除商户成员 - 商户ID: {}, 用户ID: {}", deleteDto.getMerchantId(), deleteDto.getWxId());
        boolean result = merchantService.deleteMerchantMember(deleteDto);
        return ResultUtils.success(Code.SUCCESS, result, "删除成功");
    }
}
