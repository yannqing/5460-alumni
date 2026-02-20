package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.UserBlacklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【user_blacklist(用户黑名单表)】的数据库操作Mapper
 * @Entity com.cmswe.alumni.common.entity.UserBlacklist
 */
@Mapper
public interface UserBlacklistMapper extends BaseMapper<UserBlacklist> {

}
