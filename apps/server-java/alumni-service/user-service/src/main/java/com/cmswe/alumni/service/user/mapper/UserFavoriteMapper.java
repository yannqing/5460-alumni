package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏关系 Mapper
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}
