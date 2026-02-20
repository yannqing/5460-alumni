package com.cmswe.alumni.api.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.PublishTopicDto;
import com.cmswe.alumni.common.dto.QueryShopActivityDto;
import com.cmswe.alumni.common.dto.UpdateActivityDto;
import com.cmswe.alumni.common.entity.Activity;
import com.cmswe.alumni.common.vo.ActivityDetailVo;
import com.cmswe.alumni.common.vo.ActivityListVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 活动服务接口
 *
 * @author CNI Alumni System
 * @since 2025-01-22
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 商家发布话题
     *
     * @param wxId            操作人微信ID
     * @param publishTopicDto 发布话题请求参数
     * @return 是否成功
     */
    boolean publishTopic(Long wxId, PublishTopicDto publishTopicDto);

    /**
     * 根据ID查询活动详情（普通用户使用，仅返回审核通过且非草稿的活动）
     *
     * @param activityId 活动ID
     * @return 活动详情
     */
    ActivityDetailVo getActivityDetail(Long activityId);

    /**
     * 商家根据门店ID查询活动列表（包含所有审核状态和所有活动状态）
     *
     * @param wxId     操作人微信ID
     * @param queryDto 查询参数
     * @return 活动列表分页数据
     */
    PageVo<ActivityListVo> getShopActivities(Long wxId, QueryShopActivityDto queryDto);

    /**
     * 商家编辑活动
     *
     * @param wxId              操作人微信ID
     * @param updateActivityDto 编辑活动请求参数
     * @return 是否成功
     */
    boolean updateActivity(Long wxId, UpdateActivityDto updateActivityDto);

    /**
     * 商家删除活动
     *
     * @param wxId       操作人微信ID
     * @param activityId 活动ID
     * @return 是否成功
     */
    boolean deleteActivity(Long wxId, Long activityId);
}
