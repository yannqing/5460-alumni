package com.cmswe.alumni.service.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.Activity;
import org.apache.ibatis.annotations.Param;

/**
 * @author CMSWE
 * @description 针对表【activity（活动表）】的数据库操作Mapper
 * @createDate 2025-12-29
 * @Entity com.cmswe.alumni.common.entity.Activity
 */
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 活动详情访问时浏览量 +1（与逻辑删除一致，仅更新未删除记录）
     */
    int incrementViewCount(@Param("activityId") Long activityId);
}
