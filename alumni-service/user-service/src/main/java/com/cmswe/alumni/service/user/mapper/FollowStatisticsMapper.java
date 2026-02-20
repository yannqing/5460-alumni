package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.FollowStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【follow_statistics(关注统计表（支持实时和历史分析）)】的数据库操作Mapper
 * @Entity com.cmswe.alumni.common.entity.FollowStatistics
 */
@Mapper
public interface FollowStatisticsMapper extends BaseMapper<FollowStatistics> {

}
