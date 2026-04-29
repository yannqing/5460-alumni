package com.cmswe.alumni.service.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.ActivityShop;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动-门店关联 Mapper
 */
@Mapper
public interface ActivityShopMapper extends BaseMapper<ActivityShop> {
}
