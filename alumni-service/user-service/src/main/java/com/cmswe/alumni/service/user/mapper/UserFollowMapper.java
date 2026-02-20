package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【user_follow(用户关注关系表)】的数据库操作Mapper
 * @Entity com.cmswe.alumni.common.entity.UserFollow
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

}
