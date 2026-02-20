package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户优惠券 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-17
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 查询用户的优惠券列表（按状态）
     *
     * @param userId 用户ID
     * @param status 状态（可选）
     * @param limit  查询数量
     * @return 用户优惠券列表
     */
    List<UserCoupon> selectByUserIdAndStatus(@Param("userId") Long userId,
                                             @Param("status") Integer status,
                                             @Param("limit") Integer limit);

    /**
     * 查询用户已领取的某张优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券
     */
    UserCoupon selectByUserIdAndCouponId(@Param("userId") Long userId,
                                         @Param("couponId") Long couponId);

    /**
     * 统计用户已领取的某张优惠券数量
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 数量
     */
    Long countByUserIdAndCouponId(@Param("userId") Long userId,
                                  @Param("couponId") Long couponId);

    /**
     * 查询即将过期的用户优惠券（用于提醒）
     *
     * @param hours        小时数
     * @param currentTime  当前时间
     * @param expireReminded 是否已提醒
     * @param limit        查询数量
     * @return 用户优惠券列表
     */
    List<UserCoupon> selectExpiringSoon(@Param("hours") Integer hours,
                                       @Param("currentTime") LocalDateTime currentTime,
                                       @Param("expireReminded") Integer expireReminded,
                                       @Param("limit") Integer limit);

    /**
     * 标记优惠券已提醒
     *
     * @param userCouponId 用户优惠券ID
     * @return 更新行数
     */
    int markAsReminded(@Param("userCouponId") Long userCouponId);

    /**
     * 批量更新过期优惠券状态
     *
     * @param currentTime 当前时间
     * @return 更新行数
     */
    int batchUpdateExpired(@Param("currentTime") LocalDateTime currentTime);
}
