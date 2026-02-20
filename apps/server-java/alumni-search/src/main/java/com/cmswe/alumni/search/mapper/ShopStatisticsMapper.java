package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.ShopStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 店铺统计 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-17
 */
@Mapper
public interface ShopStatisticsMapper extends BaseMapper<ShopStatistics> {

    /**
     * 查询店铺指定日期的统计数据
     *
     * @param shopId   店铺ID
     * @param statDate 统计日期
     * @return 店铺统计
     */
    ShopStatistics selectByShopIdAndDate(@Param("shopId") Long shopId,
                                        @Param("statDate") LocalDate statDate);

    /**
     * 查询店铺指定日期范围的统计数据
     *
     * @param shopId    店铺ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 店铺统计列表
     */
    List<ShopStatistics> selectByShopIdAndDateRange(@Param("shopId") Long shopId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * 查询商户指定日期范围的统计数据
     *
     * @param merchantId 商户ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 店铺统计列表
     */
    List<ShopStatistics> selectByMerchantIdAndDateRange(@Param("merchantId") Long merchantId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}
