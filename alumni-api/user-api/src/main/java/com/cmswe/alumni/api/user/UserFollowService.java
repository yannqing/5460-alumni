package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.*;
import com.cmswe.alumni.common.entity.UserFollow;
import com.cmswe.alumni.common.vo.FollowerItemVo;
import com.cmswe.alumni.common.vo.FollowingItemVo;
import com.cmswe.alumni.common.vo.FriendItemVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 用户关注服务接口
 */
public interface UserFollowService extends IService<UserFollow> {

    /**
     * 关注目标（用户、校友会、母校、商户等）
     * @param wxId 用户 id
     * @param followDto 关注信息
     * @return 是否成功
     */
    boolean follow(Long wxId, FollowDto followDto);

    /**
     * 取消关注
     * @param wxId 用户 id
     * @param unFollowRequest 取消关注请求
     * @return 是否成功
     */
    boolean unfollow(Long wxId, UnFollowRequest unFollowRequest);

    /**
     * 更新关注状态（如设置特别关注、免打扰等）
     * @param wxId 用户 id
     * @param followDto 关注信息
     * @return 是否成功
     */
    boolean updateFollowStatus(Long wxId, FollowDto followDto);

    /**
     * 检查是否已关注
     * @param wxId 用户 id
     * @param targetType 目标类型
     * @param targetId 目标 ID
     * @return 是否已关注
     */
    boolean isFollowing(Long wxId, Integer targetType, Long targetId);

    /**
     * 分页查询我关注的列表
     * @param wxId 用户 id
     * @param queryDto 查询条件
     * @return 分页结果（根据 targetType 返回不同类型的详细信息）
     */
    PageVo<FollowingItemVo<?>> getFollowingList(Long wxId, QueryFollowingListDto queryDto);

    /**
     * 分页查询我的粉丝列表
     * @param wxId 用户 id
     * @param queryDto 查询条件
     * @return 分页结果
     */
    PageVo<FollowerItemVo> getFollowerList(Long wxId, QueryFollowerListDto queryDto);

    /**
     * 分页查询好友列表（互相关注）
     * @param wxId 用户 id
     * @param queryDto 查询条件
     * @return 分页结果
     */
    PageVo<FriendItemVo> getFriendList(Long wxId, QueryFriendListDto queryDto);
}
