package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.WxUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author yanqing
 * @description 针对表【wx_users(微信用户表)】的数据库操作Mapper
 * @Entity com.cmswe.alumni.common.entity.WxUser
 */
@Mapper
public interface WxUserMapper extends BaseMapper<WxUser> {

    /**
     * 根据unionId查询微信用户
     * @param unionId 微信unionId
     * @return 微信用户信息
     */
    @Select("SELECT * FROM wx_users WHERE union_id = #{unionId} AND is_delete = 0")
    WxUser findByUnionId(@Param("unionId") String unionId);

    /**
     * 根据openid查询微信用户
     * @param openid 微信openid
     * @return 微信用户信息
     */
    @Select("SELECT * FROM wx_users WHERE openid = #{openid} AND is_delete = 0")
    WxUser findByOpenid(@Param("openid") String openid);
}
