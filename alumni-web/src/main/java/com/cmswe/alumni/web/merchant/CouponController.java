package com.cmswe.alumni.web.merchant;

import com.cmswe.alumni.api.search.CouponService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ClaimCouponDto;
import com.cmswe.alumni.common.dto.CreateCouponDto;
import com.cmswe.alumni.common.dto.QueryUserCouponDto;
import com.cmswe.alumni.common.dto.VerifyCouponDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.CouponVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.UserCouponVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 优惠券管理 Controller
 *
 * @author CNI Alumni System
 */
@Tag(name = "优惠券管理", description = "优惠券相关接口")
@Slf4j
@RestController
@RequestMapping("/coupon")
public class CouponController {

        @Resource
        private CouponService couponService;

        /**
         * 用户领取优惠券
         *
         * @param securityUser   当前登录用户
         * @param claimCouponDto 领取请求
         * @return 用户优惠券信息
         */
        @PostMapping("/claim")
        @Operation(summary = "领取优惠券")
        public BaseResponse<UserCouponVo> claimCoupon(
                        @AuthenticationPrincipal SecurityUser securityUser,
                        @Valid @RequestBody ClaimCouponDto claimCouponDto) {
                Long wxId = securityUser.getWxUser().getWxId();
                log.info("用户领取优惠券 - 用户ID: {}, 优惠券ID: {}", wxId, claimCouponDto.getCouponId());

                UserCouponVo userCouponVo = couponService.claimCoupon(wxId, claimCouponDto);

                log.info("用户领取优惠券成功 - 用户ID: {}, 优惠券ID: {}, 用户优惠券ID: {}",
                                wxId, claimCouponDto.getCouponId(), userCouponVo.getUserCouponId());

                return ResultUtils.success(Code.SUCCESS, userCouponVo, "领取成功");
        }

        /**
         * 商户管理员创建优惠券
         *
         * @param securityUser    当前登录用户
         * @param createCouponDto 创建优惠券请求
         * @return 优惠券信息
         */
        @PostMapping("/create")
        @Operation(summary = "创建优惠券")
        public BaseResponse<CouponVo> createCoupon(
                        @AuthenticationPrincipal SecurityUser securityUser,
                        @Valid @RequestBody CreateCouponDto createCouponDto) {
                Long wxId = securityUser.getWxUser().getWxId();
                log.info("商户管理员创建优惠券 - 用户ID: {}, 商户ID: {}, 优惠券名称: {}",
                                wxId, createCouponDto.getMerchantId(), createCouponDto.getCouponName());

                CouponVo couponVo = couponService.createCoupon(wxId, createCouponDto);

                log.info("创建优惠券成功 - 用户ID: {}, 商户ID: {}, 优惠券ID: {}, 优惠券名称: {}",
                                wxId, createCouponDto.getMerchantId(), couponVo.getCouponId(),
                                couponVo.getCouponName());

                return ResultUtils.success(Code.SUCCESS, couponVo, "创建成功");
        }

        /**
         * 获取用户个人优惠券列表
         *
         * @param securityUser 当前登录用户
         * @param queryDto     查询参数
         * @return 优惠券列表
         */
        @GetMapping("/my-coupons")
        @Operation(summary = "获取用户个人优惠券列表")
        public BaseResponse<PageVo<UserCouponVo>> listMyCoupons(
                        @AuthenticationPrincipal SecurityUser securityUser,
                        @Valid QueryUserCouponDto queryDto) {
                Long wxId = securityUser.getWxUser().getWxId();
                log.info("获取用户个人优惠券列表 - 用户ID: {}, 查询参数: {}", wxId, queryDto);

                PageVo<UserCouponVo> pageVo = couponService.queryUserCoupons(wxId, queryDto);

                return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
        }

        /**
         * 获取优惠券详情
         *
         * @param couponId 优惠券ID
         * @return 优惠券详情
         */
        @Deprecated
        @GetMapping("/{couponId}")
        @Operation(summary = "获取优惠券详情")
        public BaseResponse<CouponVo> getCouponDetail(@PathVariable Long couponId) {
                log.info("查询优惠券详情 - 优惠券ID: {}", couponId);
                CouponVo couponVo = couponService.getCouponDetail(couponId);
                return ResultUtils.success(Code.SUCCESS, couponVo, "查询成功");
        }

        /**
         * 获取用户券详情（含核销码生成）
         *
         * @param userCouponId 用户优惠券ID
         * @return 用户优惠券详情
         */
        @GetMapping("/user-coupon/{userCouponId}")
        @Operation(summary = "获取用户券详情（含核销码生成）")
        public BaseResponse<UserCouponVo> getUserCouponDetail(@PathVariable Long userCouponId) {
                log.info("查询用户券详情 - 用户券ID: {}", userCouponId);
                UserCouponVo vo = couponService.getUserCouponDetail(userCouponId);
                return ResultUtils.success(Code.SUCCESS, vo, "查询成功");
        }

        /**
         * 刷新优惠券核销码
         *
         * @param userCouponId 用户优惠券ID
         * @return 用户优惠券详情
         */
        @PostMapping("/user-coupon/refresh-code/{userCouponId}")
        @Operation(summary = "刷新优惠券核销码")
        public BaseResponse<UserCouponVo> refreshVerificationCode(@PathVariable Long userCouponId) {
                log.info("刷新优惠券核销码 - 用户券ID: {}", userCouponId);
                UserCouponVo vo = couponService.refreshVerificationCode(userCouponId);
                return ResultUtils.success(Code.SUCCESS, vo, "刷新成功");
        }

        /**
         * 商户核销优惠券
         *
         * @param securityUser 当前核销人（商户管理员/店员）
         * @param verifyDto    核销信息
         * @return 是否成功
         */
        @PostMapping("/verify")
        @Operation(summary = "商户核销优惠券")
        public BaseResponse<Boolean> verifyCoupon(
                        @AuthenticationPrincipal SecurityUser securityUser,
                        @Valid @RequestBody VerifyCouponDto verifyDto) {
                Long verifierId = securityUser.getWxUser().getWxId();
                log.info("商户核销优惠券 - 核销人ID: {}, 核销码: {}, 店铺ID: {}",
                                verifierId, verifyDto.getVerificationCode(), verifyDto.getShopId());

                boolean result = couponService.verifyCoupon(verifierId, verifyDto);

                if (result) {
                        log.info("优惠券核销成功 - 核销码: {}", verifyDto.getVerificationCode());
                        return ResultUtils.success(Code.SUCCESS, true, "核销成功");
                } else {
                        return ResultUtils.failure(Code.FAILURE, false, "核销失败");
                }
        }
}
