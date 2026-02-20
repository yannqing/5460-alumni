package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.FollowStatisticsService;
import com.cmswe.alumni.api.user.UserFollowService;
import com.cmswe.alumni.common.constant.CommonConstant;
import com.cmswe.alumni.common.dto.*;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.entity.UserFollow;
import com.cmswe.alumni.common.entity.UserFriendship;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.JwtUtils;
import com.cmswe.alumni.common.vo.*;
import com.cmswe.alumni.redis.utils.RedisCache;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationMapper;
import com.cmswe.alumni.service.association.mapper.SchoolMapper;
import com.cmswe.alumni.service.user.mapper.UserFollowMapper;
import com.cmswe.alumni.service.user.mapper.UserFriendshipMapper;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import com.cmswe.alumni.service.user.service.message.UnifiedMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户关注服务实现类
 */
@Slf4j
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements UserFollowService {

    @Resource
    private FollowStatisticsService followStatisticsService;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private WxUserInfoMapper wxUserInfoMapper;

    @Resource
    private UserFriendshipMapper userFriendshipMapper;

    @Resource
    private SchoolMapper schoolMapper;

    @Resource
    private AlumniAssociationMapper alumniAssociationMapper;

    @Resource
    private RedisCache redisCache;

    @Resource
    private UnifiedMessageService unifiedMessageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(Long wxId, FollowDto followDto) {
        // 如果关注类型是用户，检查是否尝试关注自己
        if (followDto.getTargetType() == 1 && followDto.getTargetId().equals(wxId)) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "不能关注自己");
        }

        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getWxId, wxId)
                .eq(UserFollow::getTargetType, followDto.getTargetType())
                .eq(UserFollow::getTargetId, followDto.getTargetId());

        UserFollow existingFollow = this.getOne(queryWrapper);

        if (existingFollow != null) {
            // 如果已经关注，不允许重复关注
            throw new BusinessException(ErrorType.ARGS_ERROR, "您已经关注过该目标，无需重复关注");
        }

        // 创建新的关注记录
        UserFollow userFollow = new UserFollow();
        userFollow.setWxId(wxId);
        userFollow.setTargetType(followDto.getTargetType());
        userFollow.setTargetId(followDto.getTargetId());
        userFollow.setRemark(followDto.getRemark());
        userFollow.setFollowStatus(followDto.getFollowStatus() != null ? followDto.getFollowStatus() : 1);

        boolean result = this.save(userFollow);

        if (result) {
            // 更新统计
            followStatisticsService.updateFollowerCount(followDto.getTargetType(), followDto.getTargetId(), 1);
            followStatisticsService.updateFollowingCount(wxId, 1);

            // 发送关注通知到Kafka（仅当关注目标是用户时）
            if (followDto.getTargetType() == 1) {
                sendFollowNotificationAsync(wxId, followDto.getTargetId());
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollow(Long wxId, UnFollowRequest unFollowRequest) {
        Integer targetType = unFollowRequest.getTargetType();
        Long targetId = unFollowRequest.getTargetId();

        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getWxId, wxId)
                .eq(UserFollow::getTargetType, targetType)
                .eq(UserFollow::getTargetId, targetId);

        UserFollow userFollow = this.getOne(queryWrapper);

        if (userFollow == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "未找到关注记录");
        }

        boolean result = this.removeById(userFollow.getFollowId());

        if (result) {
            // 更新统计
            followStatisticsService.updateFollowerCount(targetType, targetId, -1);
            followStatisticsService.updateFollowingCount(wxId, -1);
        }

        return result;
    }

    @Override
    public boolean updateFollowStatus(Long wxId, FollowDto followDto) {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getWxId, wxId)
                .eq(UserFollow::getTargetType, followDto.getTargetType())
                .eq(UserFollow::getTargetId, followDto.getTargetId());

        UserFollow userFollow = this.getOne(queryWrapper);

        if (userFollow == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "未找到关注记录");
        }

        userFollow.setFollowStatus(followDto.getFollowStatus());
        userFollow.setRemark(followDto.getRemark());
        userFollow.setUpdatedTime(LocalDateTime.now());

        return this.updateById(userFollow);
    }

    @Override
    public boolean isFollowing(Long wxId, Integer targetType, Long targetId) {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getWxId, wxId)
                .eq(UserFollow::getTargetType, targetType)
                .eq(UserFollow::getTargetId, targetId);

        return this.count(queryWrapper) > 0;
    }

    @Override
    public PageVo<FollowingItemVo<?>> getFollowingList(Long wxId, QueryFollowingListDto queryDto) {
        // 构建分页对象
        Page<UserFollow> page = new Page<>(queryDto.getCurrent(), queryDto.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getWxId, wxId);

        // 目标类型筛选
        if (queryDto.getTargetType() != null) {
            queryWrapper.eq(UserFollow::getTargetType, queryDto.getTargetType());
        }

        // 关注状态筛选
        if (queryDto.getFollowStatus() != null) {
            queryWrapper.eq(UserFollow::getFollowStatus, queryDto.getFollowStatus());
        }

        // 排序
        if (StringUtils.isNotBlank(queryDto.getSortField())) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(queryDto.getSortOrder());
            queryWrapper.orderBy(true, isAsc, "created_time".equals(queryDto.getSortField())
                    ? UserFollow::getCreatedTime : UserFollow::getUpdatedTime);
        } else {
            queryWrapper.orderByDesc(UserFollow::getCreatedTime);
        }

        // 执行查询
        Page<UserFollow> followPage = this.page(page, queryWrapper);

        // 转换为 VO（使用泛型设计）
        List<FollowingItemVo<?>> voList = followPage.getRecords().stream().map(follow -> {
            return buildFollowingItemVo(follow);
        }).collect(Collectors.toList());

        // 关键词筛选（在内存中过滤，因为需要关联查询）
        if (StringUtils.isNotBlank(queryDto.getKeyword())) {
            voList = voList.stream()
                    .filter(vo -> matchesKeyword(vo, queryDto.getKeyword()))
                    .collect(Collectors.toList());
        }

        return new PageVo<>(voList, followPage.getTotal(), followPage.getCurrent(), followPage.getSize());
    }

    /**
     * 构建关注列表项VO（根据目标类型填充不同的详细信息）
     *
     * @param follow 关注记录
     * @return FollowingItemVo
     */
    private FollowingItemVo<?> buildFollowingItemVo(UserFollow follow) {
        FollowingItemVo<Object> vo = new FollowingItemVo<>();
        vo.setFollowId(follow.getFollowId());
        vo.setTargetType(follow.getTargetType());
        vo.setTargetId(String.valueOf(follow.getTargetId()));
        vo.setFollowStatus(follow.getFollowStatus());
        vo.setRemark(follow.getRemark());
        vo.setCreatedTime(follow.getCreatedTime());

        // 根据目标类型查询目标信息
        switch (follow.getTargetType()) {
            case 1 -> {
                // 用户
                WxUserInfo userInfo = wxUserInfoMapper.selectOne(new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, follow.getTargetId()));
                if (userInfo != null) {
                    UserListResponse userVo = UserListResponse.ObjToVo(userInfo);
                    userVo.setWxId(String.valueOf(follow.getTargetId()));
                    // TODO: 设置是否在线状态（需要从 Redis 获取）
                    // userVo.setIsOnline(redisCache.isUserOnline(follow.getTargetId()));
                    vo.setTargetInfo(userVo);
                } else {
                    log.warn("用户信息不存在，targetId={}", follow.getTargetId());
                }
            }
            case 2 -> {
                // 校友会
                AlumniAssociation association = alumniAssociationMapper.selectById(follow.getTargetId());
                if (association != null) {
                    AlumniAssociationListVo alumniAssociationListVo = AlumniAssociationListVo.objToVo(association);
                    vo.setTargetInfo(alumniAssociationListVo);
                } else {
                    log.warn("校友会信息不存在，targetId={}", follow.getTargetId());
                }
            }
            case 3 -> {
                // 母校
                School school = schoolMapper.selectById(follow.getTargetId());
                if (school != null) {
                    SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
                    schoolListVo.setSchoolId(String.valueOf(school.getSchoolId()));
                    vo.setTargetInfo(schoolListVo);
                } else {
                    log.warn("母校信息不存在，targetId={}", follow.getTargetId());
                }
            }
            case 4 -> {
                // 商户
                // TODO: 需要注入 MerchantMapper 或通过 Feign 调用 merchant-service
                // Merchant merchant = merchantMapper.selectById(follow.getTargetId());
                // if (merchant != null) {
                //     vo.setTargetInfo(MerchantListVo.objToVo(merchant));
                // }
                log.warn("商户类型的关注暂未实现详细信息查询，targetId={}", follow.getTargetId());
            }
            default -> log.warn("未知的关注目标类型：{}", follow.getTargetType());
        }

        return vo;
    }

    /**
     * 检查 VO 是否匹配关键词
     *
     * @param vo 关注列表项VO
     * @param keyword 关键词
     * @return 是否匹配
     */
    private boolean matchesKeyword(FollowingItemVo<?> vo, String keyword) {
        if (vo.getTargetInfo() == null) {
            return false;
        }

        // 根据不同的目标类型进行关键词匹配
        Object targetInfo = vo.getTargetInfo();
        if (targetInfo instanceof UserListResponse userVo) {
            return (userVo.getNickname() != null && userVo.getNickname().contains(keyword))
                    || (userVo.getName() != null && userVo.getName().contains(keyword))
                    || (userVo.getSignature() != null && userVo.getSignature().contains(keyword));
        } else if (targetInfo instanceof AlumniAssociationListVo associationVo) {
            return associationVo.getAssociationName() != null
                    && associationVo.getAssociationName().contains(keyword);
        } else if (targetInfo instanceof SchoolListVo schoolVo) {
            return schoolVo.getSchoolName() != null
                    && schoolVo.getSchoolName().contains(keyword);
        }
        // TODO: 添加其他类型的关键词匹配（如 MerchantListVo）

        return false;
    }

    @Override
    public PageVo<FollowerItemVo> getFollowerList(Long wxId, QueryFollowerListDto queryDto) {
        // 构建分页对象
        Page<UserFollow> page = new Page<>(queryDto.getCurrent(), queryDto.getPageSize());

        // 构建查询条件：查询关注我的人（targetType=1 且 targetId=我的ID）
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getTargetType, 1)
                .eq(UserFollow::getTargetId, wxId);

        // 关注状态筛选
        if (queryDto.getFollowStatus() != null) {
            queryWrapper.eq(UserFollow::getFollowStatus, queryDto.getFollowStatus());
        }

        // 排序
        if (StringUtils.isNotBlank(queryDto.getSortField())) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(queryDto.getSortOrder());
            queryWrapper.orderBy(true, isAsc, "created_time".equals(queryDto.getSortField())
                    ? UserFollow::getCreatedTime : UserFollow::getUpdatedTime);
        } else {
            queryWrapper.orderByDesc(UserFollow::getCreatedTime);
        }

        // 执行查询
        Page<UserFollow> followPage = this.page(page, queryWrapper);

        // 转换为 VO
        List<FollowerItemVo> voList = followPage.getRecords().stream().map(follow -> {
            FollowerItemVo vo = new FollowerItemVo();
            vo.setFollowId(String.valueOf(follow.getFollowId()));
            vo.setWxId(String.valueOf(follow.getWxId()));
            vo.setFollowStatus(follow.getFollowStatus());
            vo.setRemark(follow.getRemark());
            vo.setCreatedTime(follow.getCreatedTime());

            // 查询粉丝用户信息
            WxUserInfo userInfo = wxUserInfoMapper.selectOne(
                    new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, follow.getWxId())
            );
            if (userInfo != null) {
                vo.setUserName(userInfo.getNickname());
                vo.setAvatar(userInfo.getAvatarUrl());
                vo.setDescription(userInfo.getDescription());
            } else {
                log.warn("粉丝用户信息不存在，wxId={}", follow.getWxId());
            }

            // 检查是否互相关注
            LambdaQueryWrapper<UserFollow> mutualQuery = new LambdaQueryWrapper<>();
            mutualQuery.eq(UserFollow::getWxId, wxId)
                    .eq(UserFollow::getTargetType, 1)
                    .eq(UserFollow::getTargetId, follow.getWxId());
            vo.setIsMutualFollow(this.count(mutualQuery) > 0);

            return vo;
        }).collect(Collectors.toList());

        // 关键词筛选
        if (StringUtils.isNotBlank(queryDto.getKeyword())) {
            voList = voList.stream()
                    .filter(vo -> vo.getUserName() != null && vo.getUserName().contains(queryDto.getKeyword()))
                    .collect(Collectors.toList());
        }

        return new PageVo<>(voList, followPage.getTotal(), followPage.getCurrent(), followPage.getSize());
    }

    @Override
    public PageVo<FriendItemVo> getFriendList(Long wxId, QueryFriendListDto queryDto) {
        // 构建分页对象
        Page<UserFriendship> page = new Page<>(queryDto.getCurrent(), queryDto.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<UserFriendship> queryWrapper = new LambdaQueryWrapper<>();
        // 查询我参与的好友关系（我是 A 或者我是 B）
        queryWrapper.and(wrapper -> wrapper.eq(UserFriendship::getWxIdA, wxId).or().eq(UserFriendship::getWxIdB, wxId));

        // 关系类型筛选
        if (queryDto.getRelationship() != null) {
            queryWrapper.eq(UserFriendship::getRelationship, queryDto.getRelationship());
        }

        // 状态筛选
        if (queryDto.getStatus() != null) {
            queryWrapper.eq(UserFriendship::getStatus, queryDto.getStatus());
        }

        // 只查询星标好友
        if (queryDto.getOnlyStar() != null && queryDto.getOnlyStar()) {
            queryWrapper.and(wrapper -> wrapper.eq(UserFriendship::getIsStarA, 1).or().eq(UserFriendship::getIsStarB, 1));
        }

        // 排序
        if (StringUtils.isNotBlank(queryDto.getSortField())) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(queryDto.getSortOrder());
            if ("intimacy_score".equals(queryDto.getSortField())) {
                queryWrapper.orderBy(true, isAsc, UserFriendship::getIntimacyScore);
            } else if ("last_interact".equals(queryDto.getSortField())) {
                queryWrapper.orderBy(true, isAsc, UserFriendship::getLastInteract);
            } else {
                queryWrapper.orderBy(true, isAsc, UserFriendship::getAddTime);
            }
        } else {
            queryWrapper.orderByDesc(UserFriendship::getLastInteract);
        }

        // 执行查询
        Page<UserFriendship> friendshipPage = userFriendshipMapper.selectPage(page, queryWrapper);

        // 转换为 VO
        List<FriendItemVo> voList = friendshipPage.getRecords().stream().map(friendship -> {
            FriendItemVo vo = new FriendItemVo();
            vo.setFriendshipId(String.valueOf(friendship.getFriendshipId()));
            vo.setRelationship(friendship.getRelationship());
            vo.setStatus(friendship.getStatus());
            vo.setIntimacyScore(friendship.getIntimacyScore());
            vo.setLastInteract(friendship.getLastInteract());
            vo.setAddTime(friendship.getAddTime());

            // 判断当前用户是 A 还是 B，确定好友ID
            Long friendWxId;
            if (wxId.equals(friendship.getWxIdA())) {
                friendWxId = friendship.getWxIdB();
                vo.setMyRemark(friendship.getRemarkAToB());
                vo.setFriendRemark(friendship.getRemarkBToA());
                vo.setIsStar(friendship.getIsStarA() == 1);
            } else {
                friendWxId = friendship.getWxIdA();
                vo.setMyRemark(friendship.getRemarkBToA());
                vo.setFriendRemark(friendship.getRemarkAToB());
                vo.setIsStar(friendship.getIsStarB() == 1);
            }

            vo.setFriendWxId(String.valueOf(friendWxId));

            // 查询好友用户信息
            WxUserInfo userInfo = wxUserInfoMapper.selectOne(
                    new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, friendWxId)
            );
            if (userInfo != null) {
                vo.setFriendName(userInfo.getNickname());
                vo.setFriendAvatar(userInfo.getAvatarUrl());
                vo.setFriendDescription(userInfo.getDescription());
            } else {
                log.warn("好友用户信息不存在，wxId={}", friendWxId);
            }

            // 查询好友在线状态
            vo.setIsOnline(redisCache.isUserOnline(friendWxId));

            return vo;
        }).collect(Collectors.toList());

        // 关键词筛选
        if (StringUtils.isNotBlank(queryDto.getKeyword())) {
            voList = voList.stream()
                    .filter(vo -> (vo.getFriendName() != null && vo.getFriendName().contains(queryDto.getKeyword()))
                            || (vo.getMyRemark() != null && vo.getMyRemark().contains(queryDto.getKeyword())))
                    .collect(Collectors.toList());
        }

        return new PageVo<>(voList, friendshipPage.getTotal(), friendshipPage.getCurrent(), friendshipPage.getSize());
    }

    // ==================== 消息通知相关方法 ====================

    /**
     * 异步发送关注通知到Kafka
     *
     * @param fromUserId 关注者用户ID
     * @param toUserId   被关注者用户ID
     */
    private void sendFollowNotificationAsync(Long fromUserId, Long toUserId) {
        try {
            // 获取关注者用户信息
            WxUserInfo fromUser = wxUserInfoMapper.selectOne(
                    new LambdaQueryWrapper<WxUserInfo>().eq(WxUserInfo::getWxId, fromUserId)
            );
            if (fromUser == null) {
                log.warn("[UserFollowService] 发送关注通知失败 - 关注者不存在: {}", fromUserId);
                return;
            }

            String fromUsername = fromUser.getNickname() != null ? fromUser.getNickname() : "用户" + fromUserId;

            // 发送关注通知到Kafka（异步）
            boolean success = unifiedMessageService.sendFollowNotification(fromUserId, fromUsername, toUserId);

            if (success) {
                log.info("[UserFollowService] 关注通知已发送到Kafka - From: {} ({}), To: {}",
                        fromUserId, fromUsername, toUserId);
            } else {
                log.error("[UserFollowService] 关注通知发送到Kafka失败 - From: {}, To: {}", fromUserId, toUserId);
            }

        } catch (Exception e) {
            // 发送通知失败不影响关注操作本身，只记录日志
            log.error("[UserFollowService] 发送关注通知异常 - From: {}, To: {}, Error: {}",
                    fromUserId, toUserId, e.getMessage(), e);
        }
    }
}
