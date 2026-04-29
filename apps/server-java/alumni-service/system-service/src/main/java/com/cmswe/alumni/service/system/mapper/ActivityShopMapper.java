package com.cmswe.alumni.service.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.ActivityShop;
import com.cmswe.alumni.common.vo.ActivityListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动-门店关联 Mapper
 */
@Mapper
public interface ActivityShopMapper extends BaseMapper<ActivityShop> {

    /**
     * 查询商户下所有审核通过且启用的门店ID
     */
    @Select("SELECT shop_id FROM shop WHERE merchant_id = #{merchantId} AND is_delete = 0 AND review_status = 1 AND status = 1")
    List<Long> selectShopIdsByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 查询商户关联的活动列表（通过 activity_shop 关联）
     * 按创建时间倒序，限制 10 条
     */
    @Select("""
            SELECT DISTINCT
                CAST(a.activity_id AS CHAR) AS activityId,
                a.activity_title AS activityTitle,
                a.activity_type AS activityType,
                a.create_time AS createTime
            FROM activity a
            INNER JOIN activity_shop aso ON aso.activity_id = a.activity_id
            INNER JOIN shop s ON s.shop_id = aso.shop_id
            WHERE s.merchant_id = #{merchantId}
              AND a.is_delete = 0
              AND a.status IN (1, 3)
            ORDER BY a.create_time DESC
            LIMIT 10
            """)
    List<ActivityListVo> selectActivitiesByMerchantId(@Param("merchantId") Long merchantId);
}
