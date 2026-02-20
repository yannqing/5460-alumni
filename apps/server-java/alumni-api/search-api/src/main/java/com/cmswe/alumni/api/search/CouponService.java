package com.cmswe.alumni.api.search;

import com.cmswe.alumni.common.dto.ClaimCouponDto;
import com.cmswe.alumni.common.dto.CreateCouponDto;
import com.cmswe.alumni.common.dto.QueryUserCouponDto;
import com.cmswe.alumni.common.dto.VerifyCouponDto;
import com.cmswe.alumni.common.dto.QueryMerchantCouponDto;
import com.cmswe.alumni.common.dto.UpdateCouponDto;
import com.cmswe.alumni.common.vo.CouponVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.UserCouponVo;

/**
 * 优惠券服务接口
 *
 * @author CNI Alumni System
 */
public interface CouponService {

    /**
     * 用户领取优惠券
     *
     * @param wxId           用户微信ID
     * @param claimCouponDto 领取请求
     * @return 用户优惠券信息
     */
    UserCouponVo claimCoupon(Long wxId, ClaimCouponDto claimCouponDto);

    /**
     * 商户管理员创建优惠券
     *
     * @param wxId            创建人用户ID
     * @param createCouponDto 创建优惠券请求
     * @return 优惠券信息
     */
    CouponVo createCoupon(Long wxId, CreateCouponDto createCouponDto);

    /**
     * 分页查询用户优惠券列表
     *
     * @param wxId     用户微信ID
     * @param queryDto 查询参数
     * @return 分页结果
     */
    PageVo<UserCouponVo> queryUserCoupons(Long wxId, QueryUserCouponDto queryDto);

    /**
     * 获取优惠券详情
     *
     * @param couponId 优惠券ID
     * @return 优惠券详情
     */
    CouponVo getCouponDetail(Long couponId);

    /**
     * 获取用户优惠券详情（包含核销码生成/获取）
     *
     * @param userCouponId 用户优惠券ID
     * @return 用户优惠券详情
     */
    UserCouponVo getUserCouponDetail(Long userCouponId);

    /**
     * 刷新优惠券核销码
     *
     * @param userCouponId 用户优惠券ID
     * @return 用户优惠券详情
     */
    UserCouponVo refreshVerificationCode(Long userCouponId);

    /**
     * 核销优惠券
     *
     * @param verifierId 核销人ID
     * @param verifyDto  核销信息
     * @return 是否核销成功
     */
    boolean verifyCoupon(Long verifierId, VerifyCouponDto verifyDto);

    /**
     * 分页查询商户优惠券列表（管理员）
     *
     * @param queryDto 查询参数
     * @return 分页结果
     */
    PageVo<CouponVo> queryMerchantCoupons(QueryMerchantCouponDto queryDto);

    /**
     * 更新优惠券（仅未发布的优惠券可以编辑）
     *
     * @param updateDto 更新请求
     * @return 是否成功
     */
    boolean updateCoupon(UpdateCouponDto updateDto);

    /**
     * 删除优惠券（仅未发布和已结束的优惠券可以删除）
     *
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    boolean deleteCoupon(Long couponId);
}
