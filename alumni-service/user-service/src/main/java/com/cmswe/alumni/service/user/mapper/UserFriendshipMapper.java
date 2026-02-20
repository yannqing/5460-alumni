package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.UserFriendship;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【user_friendship(用户好友关系表（双向关系）)】的数据库操作Mapper
 * @Entity com.cmswe.alumni.common.entity.UserFriendship
 */
@Mapper
public interface UserFriendshipMapper extends BaseMapper<UserFriendship> {

}
