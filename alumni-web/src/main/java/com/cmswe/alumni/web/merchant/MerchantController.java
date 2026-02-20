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
    @Operation(summary = "查询当前用户的审核失败商户申请")
    public BaseResponse<MerchantDetailVo> getPendingMerchant(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long merchantId) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("查询审核失败商户 - 用户ID: {}, 商户ID: {}", wxId, merchantId);

        MerchantDetailVo merchantDetail = merchantService.getPendingMerchantByIdAndUserId(merchantId, wxId);
        return ResultUtils.success(Code.SUCCESS, merchantDetail, "查询成功");
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

        Integer isAlumni = wxUser.getIsAlumni();

        log.info("用户提交商户入驻申请 - 用户ID: {}, 商户名称: {}, 是否校友: {}", wxId, applyDto.getMerchantName(), isAlumni);

        // 校验用户是否为校友
        if (isAlumni == null || isAlumni != 1) {
            log.warn("非校友用户尝试提交商户入驻申请 - 用户ID: {}, isAlumni: {}", wxId, isAlumni);
            throw new BusinessException("只有校友才能申请商户入驻");
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
