package com.cmswe.alumni.service.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.HomePageBanner;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页轮播图 Mapper 接口
 */
@Mapper
public interface BannerMapper extends BaseMapper<HomePageBanner> {
}
