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

    /**
     * 报名计数 +1（同时校验未超员）。
     * <p>当 max_participants 为 NULL 表示不限名额；否则要求 current_participants &lt; max_participants。
     * 通过 affected rows 判断是否成功，避免并发场景下超员。</p>
     */
    int tryIncrementParticipants(@Param("activityId") Long activityId);

    /**
     * 报名计数 -1（最低不低于 0），用于审核拒绝/取消已通过的报名。
     */
    int decrementParticipants(@Param("activityId") Long activityId);
}
