package com.cmswe.alumni.web.merchant;

import com.cmswe.alumni.api.search.CouponService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.QueryMerchantCouponDto;
import com.cmswe.alumni.common.dto.UpdateCouponDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.CouponVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 优惠券管理 Controller（管理员）
 *
 * @author CNI Alumni System
 */
@Tag(name = "优惠券管理", description = "优惠券管理相关接口（管理员）")
@Slf4j
@RestController
@RequestMapping("/coupon/management")
public class CouponManagementController {

    @Resource
    private CouponService couponService;

    /**
     * 根据商户ID分页查询优惠券列表
     *
     * @param queryDto 查询条件
     * @return 优惠券列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询商户优惠券列表")
    public BaseResponse<PageVo<CouponVo>> queryMerchantCoupons(
            @Valid @RequestBody QueryMerchantCouponDto queryDto) {
        log.info("查询商户优惠券列表 - 商户ID: {}, 当前页: {}, 每页大小: {}",
                queryDto.getMerchantId(), queryDto.getCurrent(), queryDto.getPageSize());

        PageVo<CouponVo> pageVo = couponService.queryMerchantCoupons(queryDto);

        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    /**
     * 编辑优惠券（仅未发布的优惠券可编辑）
     *
     * @param updateDto 更新请求
     * @return 操作结果
     */
    @PostMapping("/update")
    @Operation(summary = "编辑优惠券")
    public BaseResponse<Boolean> updateCoupon(@Valid @RequestBody UpdateCouponDto updateDto) {
        log.info("编辑优惠券 - 优惠券ID: {}, 优惠券名称: {}",
                updateDto.getCouponId(), updateDto.getCouponName());

        boolean result = couponService.updateCoupon(updateDto);

        if (result) {
            log.info("编辑优惠券成功 - 优惠券ID: {}", updateDto.getCouponId());
            return ResultUtils.success(Code.SUCCESS, true, "编辑成功");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "编辑失败");
        }
    }

    /**
     * 删除优惠券（仅未发布、已结束、已下架的优惠券可删除）
     *
     * @param couponId 优惠券ID
     * @return 操作结果
     */
    @DeleteMapping("/{couponId}")
    @Operation(summary = "删除优惠券")
    public BaseResponse<Boolean> deleteCoupon(@PathVariable Long couponId) {
        log.info("删除优惠券 - 优惠券ID: {}", couponId);

        boolean result = couponService.deleteCoupon(couponId);

        if (result) {
            log.info("删除优惠券成功 - 优惠券ID: {}", couponId);
            return ResultUtils.success(Code.SUCCESS, true, "删除成功");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "删除失败");
        }
    }

    /**
     * 查询优惠券详情
     *
     * @param couponId 优惠券ID
     * @return 优惠券详情
     */
    @GetMapping("/{couponId}")
    @Operation(summary = "查询优惠券详情")
    public BaseResponse<CouponVo> getCouponDetail(@PathVariable Long couponId) {
        log.info("查询优惠券详情 - 优惠券ID: {}", couponId);

        CouponVo couponVo = couponService.getCouponDetail(couponId);

        return ResultUtils.success(Code.SUCCESS, couponVo, "查询成功");
    }
}
