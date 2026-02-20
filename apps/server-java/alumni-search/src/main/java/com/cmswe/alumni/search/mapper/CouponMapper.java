package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-17
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 根据商户ID查询优惠券列表
     *
     * @param merchantId 商户ID
     * @param status     状态（可选）
     * @return 优惠券列表
     */
    List<Coupon> selectByMerchantId(@Param("merchantId") Long merchantId,
                                    @Param("status") Integer status);

    /**
     * 根据店铺ID查询优惠券列表
     *
     * @param shopId 店铺ID
     * @param status 状态（可选）
     * @return 优惠券列表
     */
    List<Coupon> selectByShopId(@Param("shopId") Long shopId,
                                @Param("status") Integer status);

    /**
     * 查询有效的优惠券列表（未过期且有库存）
     *
     * @param shopId       店铺ID（可选）
     * @param isAlumniOnly 是否仅校友可领（可选）
     * @param limit        查询数量
     * @return 优惠券列表
     */
    List<Coupon> selectAvailable(@Param("shopId") Long shopId,
                                 @Param("isAlumniOnly") Integer isAlumniOnly,
                                 @Param("currentTime") LocalDateTime currentTime,
                                 @Param("limit") Integer limit);

    /**
     * 扣减优惠券库存（原子操作）
     *
     * @param couponId 优惠券ID
     * @return 更新行数（0表示库存不足）
     */
    int decrementStock(@Param("couponId") Long couponId);

    /**
     * 增加优惠券已领取数量
     *
     * @param couponId 优惠券ID
     * @return 更新行数
     */
    int incrementReceivedCount(@Param("couponId") Long couponId);

    /**
     * 增加优惠券已使用数量
     *
     * @param couponId 优惠券ID
     * @return 更新行数
     */
    int incrementUsedCount(@Param("couponId") Long couponId);

    /**
     * 增加优惠券浏览次数
     *
     * @param couponId 优惠券ID
     * @return 更新行数
     */
    int incrementViewCount(@Param("couponId") Long couponId);

    /**
     * 查询即将过期的优惠券（用于定时任务）
     *
     * @param hours 小时数
     * @return 优惠券列表
     */
    List<Coupon> selectExpiringSoon(@Param("hours") Integer hours,
                                    @Param("currentTime") LocalDateTime currentTime);
}
