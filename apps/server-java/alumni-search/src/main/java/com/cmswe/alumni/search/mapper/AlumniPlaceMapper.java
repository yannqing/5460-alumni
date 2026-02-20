package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.AlumniPlace;
import com.cmswe.alumni.common.vo.NearbyPlaceVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 校友企业/场所 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-26
 */
@Mapper
public interface AlumniPlaceMapper extends BaseMapper<AlumniPlace> {

    /**
     * 根据地理位置分页查询附近企业/场所（带距离计算和条件筛选）
     * 注意：只查询正常运营（status=1）且审核通过（review_status=1）的企业/场所
     *
     * @param latitude      纬度
     * @param longitude     经度
     * @param radius        半径（公里）
     * @param placeType     场所类型（可选）：1-企业 2-场所
     * @param placeName     场所/企业名称（可选）
     * @param isRecommended 是否推荐（可选）
     * @param offset        分页偏移量
     * @param pageSize      每页数量
     * @return 企业/场所列表（包含距离）
     */
    List<NearbyPlaceVo> selectNearbyWithPage(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") Integer radius,
            @Param("placeType") Integer placeType,
            @Param("placeName") String placeName,
            @Param("isRecommended") Integer isRecommended,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 统计附近企业/场所总数（用于分页）
     * 注意：只统计正常运营（status=1）且审核通过（review_status=1）的企业/场所
     *
     * @param latitude      纬度
     * @param longitude     经度
     * @param radius        半径（公里）
     * @param placeType     场所类型（可选）：1-企业 2-场所
     * @param placeName     场所/企业名称（可选）
     * @param isRecommended 是否推荐（可选）
     * @return 总数
     */
    Long countNearbyPlaces(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") Integer radius,
            @Param("placeType") Integer placeType,
            @Param("placeName") String placeName,
            @Param("isRecommended") Integer isRecommended
    );

    /**
     * 增加场所/企业浏览次数
     *
     * @param placeId 场所/企业ID
     * @return 更新行数
     */
    int incrementViewCount(@Param("placeId") Long placeId);

    /**
     * 增加场所/企业点击次数
     *
     * @param placeId 场所/企业ID
     * @return 更新行数
     */
    int incrementClickCount(@Param("placeId") Long placeId);
}
