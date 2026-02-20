package com.cmswe.alumni.web;

import com.cmswe.alumni.api.search.ShopService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ApproveShopDto;
import com.cmswe.alumni.common.dto.CreateShopDto;
import com.cmswe.alumni.common.dto.QueryShopApprovalDto;
import com.cmswe.alumni.common.dto.UpdateShopDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.ShopApprovalVo;
import com.cmswe.alumni.common.vo.ShopListVo;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺管理 Controller
 *
 * @author CNI Alumni System
 */
@Tag(name = "店铺管理", description = "店铺相关接口")
@Slf4j
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private ShopService shopService;

    /**
     * 创建店铺
     *
     * @param securityUser  当前登录用户
     * @param createShopDto 创建店铺请求参数
     * @return 是否成功
     */
    @PostMapping("/create")
    @Operation(summary = "创建店铺")
    public BaseResponse<Boolean> createShop(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody CreateShopDto createShopDto) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("创建店铺 - 用户ID: {}, 商户ID: {}, 店铺名称: {}",
                wxId, createShopDto.getMerchantId(), createShopDto.getShopName());

        boolean result = shopService.createShop(wxId, createShopDto);

        if (result) {
            log.info("创建店铺成功 - 用户ID: {}, 商户ID: {}, 店铺名称: {}",
                    wxId, createShopDto.getMerchantId(), createShopDto.getShopName());
            return ResultUtils.success(Code.SUCCESS, true, "创建成功");
        } else {
            log.error("创建店铺失败 - 用户ID: {}, 商户ID: {}, 店铺名称: {}",
                    wxId, createShopDto.getMerchantId(), createShopDto.getShopName());
            return ResultUtils.failure(Code.FAILURE, false, "创建失败");
        }
    }

    /**
     * 更新店铺信息
     *
     * @param securityUser  当前登录用户
     * @param updateShopDto 更新店铺请求参数
     * @return 是否成功
     */
    @PostMapping("/update")
    @Operation(summary = "更新店铺信息")
    public BaseResponse<Boolean> updateShop(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody UpdateShopDto updateShopDto) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("更新店铺 - 用户ID: {}, 店铺ID: {}", wxId, updateShopDto.getShopId());

        boolean result = shopService.updateShop(wxId, updateShopDto);

        if (result) {
            log.info("更新店铺成功 - 用户ID: {}, 店铺ID: {}", wxId, updateShopDto.getShopId());
            return ResultUtils.success(Code.SUCCESS, true, "更新成功");
        } else {
            log.error("更新店铺失败 - 用户ID: {}, 店铺ID: {}", wxId, updateShopDto.getShopId());
            return ResultUtils.failure(Code.FAILURE, false, "更新失败");
        }
    }

    /**
     * 删除店铺
     *
     * @param securityUser 当前登录用户
     * @param shopId       店铺ID
     * @return 是否成功
     */
    @DeleteMapping("/delete/{shopId}")
    @Operation(summary = "删除店铺")
    public BaseResponse<Boolean> deleteShop(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long shopId) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("删除店铺 - 用户ID: {}, 店铺ID: {}", wxId, shopId);

        boolean result = shopService.deleteShop(wxId, shopId);

        if (result) {
            log.info("删除店铺成功 - 用户ID: {}, 店铺ID: {}", wxId, shopId);
            return ResultUtils.success(Code.SUCCESS, true, "删除成功");
        } else {
            log.error("删除店铺失败 - 用户ID: {}, 店铺ID: {}", wxId, shopId);
            return ResultUtils.failure(Code.FAILURE, false, "删除失败");
        }
    }

    /**
     * 管理员根据商户ID查询店铺列表（仅审核通过的店铺）
     *
     * @param securityUser 当前登录用户
     * @param merchantId   商户ID
     * @param current      当前页（默认1）
     * @param size         每页大小（默认10）
     * @return 店铺列表分页数据（仅包含审核通过的店铺）
     */
    @GetMapping("/list/{merchantId}")
    @Operation(summary = "管理员根据商户ID查询店铺列表（仅审核通过）")
    public BaseResponse<PageVo<ShopListVo>> getShopsByMerchantId(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("管理员查询店铺列表 - 用户ID: {}, 商户ID: {}, 当前页: {}, 每页大小: {}",
                wxId, merchantId, current, size);

        PageVo<ShopListVo> pageVo = shopService.getShopsByMerchantId(merchantId, current, size);

        log.info("查询店铺列表成功 - 用户ID: {}, 商户ID: {}, 总记录数: {}",
                wxId, merchantId, pageVo.getTotal());

        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    /**
     * 管理员审批店铺申请
     *
     * @param securityUser 当前登录的管理员
     * @param approveDto   审批信息
     * @return 审批结果
     */
    @PostMapping("/approve")
    @Operation(summary = "管理员审批店铺申请")
    public BaseResponse<Boolean> approveShop(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ApproveShopDto approveDto) {
        Long reviewerId = securityUser.getWxUser().getWxId();

        log.info("管理员审批店铺申请 - 审核人ID: {}, 店铺ID: {}, 审核状态: {}",
                reviewerId, approveDto.getShopId(), approveDto.getReviewStatus());

        boolean result = shopService.approveShop(reviewerId, approveDto);

        if (result) {
            String message = approveDto.getReviewStatus() == 1 ? "审核通过" : "审核驳回";
            log.info("店铺审批成功 - 审核人ID: {}, 店铺ID: {}, 结果: {}",
                    reviewerId, approveDto.getShopId(), message);
            return ResultUtils.success(Code.SUCCESS, true, message + "成功");
        } else {
            log.error("店铺审批失败 - 审核人ID: {}, 店铺ID: {}",
                    reviewerId, approveDto.getShopId());
            return ResultUtils.failure(Code.FAILURE, false, "审批失败");
        }
    }

    /**
     * 分页查询店铺审批记录
     *
     * @param queryDto 查询条件
     * @return 审批记录列表
     */
    @GetMapping("/approval/records")
    @Operation(summary = "分页查询店铺审批记录")
    public BaseResponse<PageVo<ShopApprovalVo>> listApprovalRecords(@Valid QueryShopApprovalDto queryDto) {
        log.info("管理员查询店铺审批记录 - 条件: {}", queryDto);
        PageVo<ShopApprovalVo> result = shopService.selectApprovalRecordsByPage(queryDto);
        return ResultUtils.success(result);
    }

    /**
     * 获取本人可用的门店列表
     * 1. 如果是门店管理员(ORGANIZE_SHOP_ADMIN)，返回其管理的门店
     * 2. 如果是商户管理员(ORGANIZE_MERCHANT_ADMIN)，返回该商户下所有审核通过且启用的门店
     *
     * @param securityUser 当前登录用户
     * @return 可用门店列表
     */
    @GetMapping("/my/available")
    @Operation(summary = "获取本人可用的门店列表")
    public BaseResponse<List<ShopListVo>> getMyAvailableShops(
            @AuthenticationPrincipal SecurityUser securityUser) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("查询用户可用门店列表 - 用户ID: {}", wxId);

        List<ShopListVo> shops = shopService.getMyAvailableShops(wxId);

        log.info("查询用户可用门店列表成功 - 用户ID: {}, 门店数量: {}", wxId, shops.size());
        return ResultUtils.success(Code.SUCCESS, shops, "查询成功");
    }
}
