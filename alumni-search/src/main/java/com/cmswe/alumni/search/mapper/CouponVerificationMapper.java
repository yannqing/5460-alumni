package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.CouponVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券核销记录 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-17
 */
@Mapper
public interface CouponVerificationMapper extends BaseMapper<CouponVerification> {

    /**
     * 根据店铺ID查询核销记录
     *
     * @param shopId    店铺ID
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param limit     查询数量
     * @return 核销记录列表
     */
    List<CouponVerification> selectByShopId(@Param("shopId") Long shopId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           @Param("limit") Integer limit);

    /**
     * 根据商户ID查询核销记录
     *
     * @param merchantId 商户ID
     * @param startTime  开始时间（可选）
     * @param endTime    结束时间（可选）
     * @param limit      查询数量
     * @return 核销记录列表
     */
    List<CouponVerification> selectByMerchantId(@Param("merchantId") Long merchantId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("limit") Integer limit);

    /**
     * 根据用户ID查询核销记录
     *
     * @param userId    用户ID
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param limit     查询数量
     * @return 核销记录列表
     */
    List<CouponVerification> selectByUserId(@Param("userId") Long userId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           @Param("limit") Integer limit);

    /**
     * 统计店铺核销数量
     *
     * @param shopId    店铺ID
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 核销数量
     */
    Long countByShopId(@Param("shopId") Long shopId,
                      @Param("startTime") LocalDateTime startTime,
                      @Param("endTime") LocalDateTime endTime);

    /**
     * 根据核销码查询核销记录
     *
     * @param verificationCode 核销码
     * @return 核销记录
     */
    CouponVerification selectByVerificationCode(@Param("verificationCode") String verificationCode);
}
