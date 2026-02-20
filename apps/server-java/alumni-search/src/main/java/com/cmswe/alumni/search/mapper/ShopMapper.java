package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-17
 */
@Mapper
public interface ShopMapper extends BaseMapper<Shop> {

    /**
     * 根据商户ID查询店铺列表
     *
     * @param merchantId 商户ID
     * @return 店铺列表
     */
    List<Shop> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 根据地理位置查询附近店铺（简单版，实际使用ES）
     * 注意：此方法仅用于小范围查询，实际生产环境应使用ES geo_point查询
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param radius    半径（公里）
     * @param limit     查询数量
     * @return 店铺列表
     */
    List<Shop> selectNearby(@Param("latitude") BigDecimal latitude,
                           @Param("longitude") BigDecimal longitude,
                           @Param("radius") Integer radius,
                           @Param("limit") Integer limit);

    /**
     * 根据城市和区县查询店铺列表
     *
     * @param city     城市
     * @param district 区县（可选）
     * @param status   状态（可选）
     * @param limit    查询数量
     * @return 店铺列表
     */
    List<Shop> selectByCityAndDistrict(@Param("city") String city,
                                       @Param("district") String district,
                                       @Param("status") Integer status,
                                       @Param("limit") Integer limit);

    /**
     * 查询推荐店铺列表
     *
     * @param city  城市（可选）
     * @param limit 查询数量
     * @return 店铺列表
     */
    List<Shop> selectRecommended(@Param("city") String city,
                                 @Param("limit") Integer limit);

    /**
     * 更新店铺统计数据
     *
     * @param shopId 店铺ID
     * @return 更新行数
     */
    int updateStatistics(@Param("shopId") Long shopId);

    /**
     * 增加店铺浏览次数
     *
     * @param shopId 店铺ID
     * @return 更新行数
     */
    int incrementViewCount(@Param("shopId") Long shopId);

    /**
     * 增加店铺点击次数
     *
     * @param shopId 店铺ID
     * @return 更新行数
     */
    int incrementClickCount(@Param("shopId") Long shopId);

    /**
     * 根据地理位置分页查询附近店铺（带距离计算和条件筛选）
     * 注意：只查询营业中（status=1）且有有效优惠券的店铺
     *
     * @param latitude      纬度
     * @param longitude     经度
     * @param radius        半径（公里）
     * @param shopName      店铺名称（可选）
     * @param isRecommended 是否推荐（可选）
     * @param offset        分页偏移量
     * @param pageSize      每页数量
     * @return 店铺列表（包含距离）
     */
    List<com.cmswe.alumni.common.vo.NearbyShopVo> selectNearbyWithPage(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") Integer radius,
            @Param("shopName") String shopName,
            @Param("isRecommended") Integer isRecommended,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 统计附近店铺总数（用于分页）
     * 注意：只统计营业中（status=1）且有有效优惠券的店铺
     *
     * @param latitude      纬度
     * @param longitude     经度
     * @param radius        半径（公里）
     * @param shopName      店铺名称（可选）
     * @param isRecommended 是否推荐（可选）
     * @return 总数
     */
    Long countNearbyShops(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") Integer radius,
            @Param("shopName") String shopName,
            @Param("isRecommended") Integer isRecommended
    );

    /**
     * 查询店铺的有效优惠券列表
     *
     * @param shopId 店铺ID
     * @return 优惠券列表
     */
    List<com.cmswe.alumni.common.vo.ShopCouponVo> selectCouponsByShopId(@Param("shopId") Long shopId);

    /**
     * 根据店铺ID查询商铺详情
     *
     * @param shopId 店铺ID
     * @return 商铺详情
     */
    com.cmswe.alumni.common.vo.ShopDetailVo selectShopDetailById(@Param("shopId") Long shopId);
}
