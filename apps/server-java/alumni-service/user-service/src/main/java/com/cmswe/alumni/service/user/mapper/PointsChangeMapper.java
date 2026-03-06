package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.PointsChange;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分变化表 Mapper
 */
@Mapper
public interface PointsChangeMapper extends BaseMapper<PointsChange> {
}
