package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.UserFollow;
import com.cmswe.alumni.service.user.dto.FollowIdCountRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 针对表【user_follow(用户关注关系表)】的数据库操作Mapper
 * @Entity com.cmswe.alumni.common.entity.UserFollow
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 作为「用户」被关注：target_type=1，按 target_id 聚合粉丝数
     */
    @Select("SELECT target_id AS id, COUNT(*) AS cnt FROM user_follow WHERE target_type = 1 AND is_deleted = 0 GROUP BY target_id")
    List<FollowIdCountRow> selectFollowerCountGroupByUserTarget();

    /**
     * 作为关注者：按 wx_id 聚合关注数（所有目标类型）
     */
    @Select("SELECT wx_id AS id, COUNT(*) AS cnt FROM user_follow WHERE is_deleted = 0 GROUP BY wx_id")
    List<FollowIdCountRow> selectFollowingCountGroupByWxId();
}
