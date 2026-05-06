package com.cmswe.alumni.service.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.system.ActivityService;
import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.common.dto.PublishMerchantActivityDto;
import com.cmswe.alumni.common.dto.PublishTopicDto;
import com.cmswe.alumni.common.dto.QueryMerchantActivityDto;
import com.cmswe.alumni.common.dto.QueryPublicActivityDto;
import com.cmswe.alumni.common.dto.QueryShopActivityDto;
import com.cmswe.alumni.common.dto.UpdateActivityDto;
import com.cmswe.alumni.common.entity.Activity;
import com.cmswe.alumni.common.entity.ActivityShop;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.ActivityPublishedStatusUtil;
import com.cmswe.alumni.common.vo.ActivityDetailVo;
import com.cmswe.alumni.common.vo.ActivityListVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.service.system.mapper.ActivityMapper;
import com.cmswe.alumni.service.system.mapper.ActivityShopMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 活动服务实现
 *
 * @author CNI Alumni System
 * @since 2025-01-22
 */
@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @jakarta.annotation.Resource
    private ActivityShopMapper activityShopMapper;

    @jakarta.annotation.Resource
    private MerchantService merchantService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishTopic(Long wxId, PublishTopicDto publishTopicDto) {
        // 1. 参数校验
        if (wxId == null || publishTopicDto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 校验报名时间
        if (publishTopicDto.getIsSignup() == 1) {
            if (publishTopicDto.getRegistrationStartTime() == null ||
                publishTopicDto.getRegistrationEndTime() == null) {
                throw new BusinessException("需要报名时，报名开始时间和截止时间不能为空");
            }
            if (publishTopicDto.getRegistrationStartTime().isAfter(publishTopicDto.getRegistrationEndTime())) {
                throw new BusinessException("报名开始时间不能晚于截止时间");
            }
            if (publishTopicDto.getRegistrationEndTime().isAfter(publishTopicDto.getStartTime())) {
                throw new BusinessException("报名截止时间不能晚于活动开始时间");
            }
        }

        // 3. 校验活动时间
        if (publishTopicDto.getStartTime().isAfter(publishTopicDto.getEndTime())) {
            throw new BusinessException("活动开始时间不能晚于结束时间");
        }

        // 4. 创建活动实体
        Activity activity = new Activity();
        BeanUtils.copyProperties(publishTopicDto, activity);

        LocalDateTime now = LocalDateTime.now();

        // 固定字段
        activity.setOrganizerType(5); // 主办方类型固定为5（门店）
        activity.setActivityCategory("话题"); // 活动分类固定为"话题"
        
        int status = ActivityPublishedStatusUtil.compute(now, publishTopicDto.getIsSignup(),
                publishTopicDto.getStartTime(), publishTopicDto.getEndTime(),
                publishTopicDto.getRegistrationStartTime(), publishTopicDto.getRegistrationEndTime());
        activity.setStatus(status);
        
        activity.setReviewStatus(1); // 审核状态固定为1（审核通过）
        activity.setIsRecommended(0); // 是否推荐固定为0
        activity.setIsPublic(1); // 默认公开

        // 初始化字段
        activity.setCurrentParticipants(0);
        activity.setViewCount(0L);
        activity.setCreatedBy(wxId);
        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        activity.setIsDelete(0);

        // 5. 保存
        boolean result = this.save(activity);

        if (result) {
            log.info("商家发布话题成功 - 用户ID: {}, 主办方ID: {}, 活动标题: {}",
                    wxId, publishTopicDto.getOrganizerId(), publishTopicDto.getActivityTitle());
        } else {
            log.error("商家发布话题失败 - 用户ID: {}, 主办方ID: {}, 活动标题: {}",
                    wxId, publishTopicDto.getOrganizerId(), publishTopicDto.getActivityTitle());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishMerchantActivity(Long wxId, PublishMerchantActivityDto dto) {
        // 1. 参数校验
        if (wxId == null || dto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 校验活动类型
        if (dto.getActivityType() != 1 && dto.getActivityType() != 2) {
            throw new BusinessException("活动类型只能为1（优惠活动）或2（话题活动）");
        }

        // 2. 校验报名时间（话题活动且需要报名时）
        if (dto.getActivityType() == 2 && dto.getIsSignup() == 1) {
            if (dto.getRegistrationStartTime() == null || dto.getRegistrationEndTime() == null) {
                throw new BusinessException("需要报名时，报名开始时间和截止时间不能为空");
            }
            if (dto.getRegistrationStartTime().isAfter(dto.getRegistrationEndTime())) {
                throw new BusinessException("报名开始时间不能晚于截止时间");
            }
            if (dto.getRegistrationEndTime().isAfter(dto.getStartTime())) {
                throw new BusinessException("报名截止时间不能晚于活动开始时间");
            }
        }

        // 优惠活动强制不需要报名
        if (dto.getActivityType() == 1) {
            dto.setIsSignup(0);
        }

        // 3. 校验活动时间
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new BusinessException("活动开始时间不能晚于结束时间");
        }

        // 4. 创建活动实体
        Activity activity = new Activity();
        BeanUtils.copyProperties(dto, activity);

        LocalDateTime now = LocalDateTime.now();

        // 固定字段
        activity.setOrganizerType(3); // 主办方类型为3（商户）
        activity.setOrganizerId(dto.getMerchantId());

        // 设置主办方名称和头像（从商户信息获取）
        try {
            var merchant = merchantService.getById(dto.getMerchantId());
            if (merchant != null) {
                activity.setOrganizerName(merchant.getMerchantName());
                activity.setOrganizerAvatar(merchant.getLogo());
            }
        } catch (Exception e) {
            log.warn("获取商户信息失败，merchantId={}", dto.getMerchantId(), e);
        }

        activity.setActivityCategory(dto.getActivityType() == 1 ? "优惠活动" : "话题");
        activity.setActivityType(dto.getActivityType());

        int status = ActivityPublishedStatusUtil.compute(now, dto.getIsSignup(),
                dto.getStartTime(), dto.getEndTime(),
                dto.getRegistrationStartTime(), dto.getRegistrationEndTime());
        activity.setStatus(status);
        activity.setReviewStatus(1); // 审核通过
        activity.setIsRecommended(0);
        activity.setIsPublic(1);
        activity.setCurrentParticipants(0);
        activity.setViewCount(0L);
        activity.setCreatedBy(wxId);
        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        activity.setIsDelete(0);

        // 5. 保存活动
        boolean result = this.save(activity);

        // 6. 保存活动-门店关联
        if (result) {
            List<Long> shopIds = dto.getShopIds();
            // 如果未指定门店，则关联该商户所有审核通过且启用的门店
            if (shopIds == null || shopIds.isEmpty()) {
                shopIds = activityShopMapper.selectShopIdsByMerchantId(dto.getMerchantId());
            }
            if (shopIds != null) {
                for (Long shopId : shopIds) {
                    ActivityShop as = new ActivityShop();
                    as.setActivityId(activity.getActivityId());
                    as.setShopId(shopId);
                    as.setCreateTime(now);
                    activityShopMapper.insert(as);
                }
            }
        }

        if (result) {
            log.info("商户发布活动成功 - 用户ID: {}, 商户ID: {}, 活动类型: {}, 标题: {}",
                    wxId, dto.getMerchantId(), dto.getActivityType(), dto.getActivityTitle());
        }

        return result;
    }

    @Override
    public PageVo<ActivityListVo> getMerchantActivities(Long wxId, QueryMerchantActivityDto queryDto) {
        // 1. 参数校验
        if (wxId == null || queryDto == null || queryDto.getMerchantId() == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 构建查询条件：商户维度的活动（organizer_type=3）
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getOrganizerType, 3)
               .eq(Activity::getOrganizerId, queryDto.getMerchantId());

        // 可选筛选条件
        if (queryDto.getActivityType() != null) {
            wrapper.eq(Activity::getActivityType, queryDto.getActivityType());
        }
        if (queryDto.getReviewStatus() != null) {
            wrapper.eq(Activity::getReviewStatus, queryDto.getReviewStatus());
        }
        if (queryDto.getStatus() != null) {
            wrapper.eq(Activity::getStatus, queryDto.getStatus());
        }

        wrapper.orderByDesc(Activity::getCreateTime);

        // 3. 分页查询
        Page<Activity> page = new Page<>(queryDto.getCurrent(), queryDto.getPageSize());
        Page<Activity> activityPage = this.page(page, wrapper);

        // 4. 转换为 VO
        List<ActivityListVo> voList = activityPage.getRecords().stream()
                .map(ActivityListVo::objToVo)
                .collect(Collectors.toList());

        log.info("商户查询活动列表 - 用户ID: {}, 商户ID: {}, 共{}条记录",
                wxId, queryDto.getMerchantId(), activityPage.getTotal());

        Page<ActivityListVo> voPage = new Page<>(queryDto.getCurrent(), queryDto.getPageSize(), activityPage.getTotal());
        voPage.setRecords(voList);
        return PageVo.of(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityDetailVo getActivityDetail(Long activityId) {
        // 1. 参数校验
        Optional.ofNullable(activityId)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        // 2. 查询活动
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在或已被删除");
        }

        // 3. 校验审核状态（只返回审核通过的活动）
        if (activity.getReviewStatus() == null || activity.getReviewStatus() != 1) {
            throw new BusinessException("活动不存在或未通过审核");
        }

        // 4. 校验状态（不能是草稿）
        if (activity.getStatus() != null && activity.getStatus() == 0) {
            throw new BusinessException("活动暂未发布");
        }

        // 5. 浏览量 +1（原子更新），再取最新数据用于返回
        this.baseMapper.incrementViewCount(activityId);
        Activity latest = this.getById(activityId);
        if (latest == null) {
            throw new BusinessException("活动不存在或已被删除");
        }

        ActivityDetailVo activityDetail = ActivityDetailVo.objToVo(latest);

        // 兜底：如果主办方名称为空且是商户活动，从商户表查询
        if (activityDetail.getOrganizerName() == null && latest.getOrganizerType() != null && latest.getOrganizerType() == 3) {
            try {
                var merchant = merchantService.getById(latest.getOrganizerId());
                if (merchant != null) {
                    activityDetail.setOrganizerName(merchant.getMerchantName());
                    activityDetail.setOrganizerAvatar(merchant.getLogo());
                }
            } catch (Exception e) {
                log.warn("获取商户主办方信息失败，merchantId={}", latest.getOrganizerId(), e);
            }
        }

        log.info("查询活动详情 - 活动ID: {}, 标题: {}", activityId, latest.getActivityTitle());

        return activityDetail;
    }

    @Override
    public PageVo<ActivityListVo> getShopActivities(Long wxId, QueryShopActivityDto queryDto) {
        // 1. 参数校验
        if (wxId == null || queryDto == null || queryDto.getShopId() == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 构建查询条件
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getOrganizerType, 5) // 主办方类型为门店
                .eq(Activity::getOrganizerId, queryDto.getShopId());

        // 可选查询条件
        if (StringUtils.hasText(queryDto.getActivityTitle())) {
            wrapper.like(Activity::getActivityTitle, queryDto.getActivityTitle());
        }
        if (queryDto.getReviewStatus() != null) {
            wrapper.eq(Activity::getReviewStatus, queryDto.getReviewStatus());
        }
        if (queryDto.getStatus() != null) {
            wrapper.eq(Activity::getStatus, queryDto.getStatus());
        }

        // 排序：按创建时间倒序
        wrapper.orderByDesc(Activity::getCreateTime);

        // 3. 分页查询
        Page<Activity> page = new Page<>(queryDto.getCurrent(), queryDto.getPageSize());
        Page<Activity> activityPage = this.page(page, wrapper);

        // 4. 转换为 VO
        List<ActivityListVo> activityListVos = activityPage.getRecords().stream()
                .map(ActivityListVo::objToVo)
                .collect(Collectors.toList());

        log.info("商家查询门店活动列表 - 用户ID: {}, 门店ID: {}, 共{}条记录",
                wxId, queryDto.getShopId(), activityPage.getTotal());

        // 5. 构建分页结果
        Page<ActivityListVo> voPage = new Page<>(queryDto.getCurrent(), queryDto.getPageSize(), activityPage.getTotal());
        voPage.setRecords(activityListVos);

        return PageVo.of(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateActivity(Long wxId, UpdateActivityDto updateActivityDto) {
        // 1. 参数校验
        if (wxId == null || updateActivityDto == null || updateActivityDto.getActivityId() == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 查询活动是否存在
        Activity existingActivity = this.getById(updateActivityDto.getActivityId());
        if (existingActivity == null) {
            throw new BusinessException("活动不存在或已被删除");
        }

        // 3. 权限校验：只能编辑自己创建的活动
        if (!existingActivity.getCreatedBy().equals(wxId)) {
            throw new BusinessException("无权编辑此活动");
        }

        // 4. 校验报名时间
        if (updateActivityDto.getIsSignup() == 1) {
            if (updateActivityDto.getRegistrationStartTime() == null ||
                updateActivityDto.getRegistrationEndTime() == null) {
                throw new BusinessException("需要报名时，报名开始时间和截止时间不能为空");
            }
            if (updateActivityDto.getRegistrationStartTime().isAfter(updateActivityDto.getRegistrationEndTime())) {
                throw new BusinessException("报名开始时间不能晚于截止时间");
            }
            if (updateActivityDto.getRegistrationEndTime().isAfter(updateActivityDto.getStartTime())) {
                throw new BusinessException("报名截止时间不能晚于活动开始时间");
            }
        }

        // 5. 校验活动时间
        if (updateActivityDto.getStartTime().isAfter(updateActivityDto.getEndTime())) {
            throw new BusinessException("活动开始时间不能晚于结束时间");
        }

        // 6. 更新活动实体
        Activity activity = new Activity();
        BeanUtils.copyProperties(updateActivityDto, activity);
        activity.setUpdateTime(LocalDateTime.now());

        // 7. 保存
        boolean result = this.updateById(activity);

        if (result) {
            log.info("商家编辑活动成功 - 用户ID: {}, 活动ID: {}, 活动标题: {}",
                    wxId, updateActivityDto.getActivityId(), updateActivityDto.getActivityTitle());
        } else {
            log.error("商家编辑活动失败 - 用户ID: {}, 活动ID: {}, 活动标题: {}",
                    wxId, updateActivityDto.getActivityId(), updateActivityDto.getActivityTitle());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteActivity(Long wxId, Long activityId) {
        // 1. 参数校验
        if (wxId == null || activityId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 查询活动是否存在
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在或已被删除");
        }

        // 3. 权限校验：只能删除自己创建的活动
        if (!activity.getCreatedBy().equals(wxId)) {
            throw new BusinessException("无权删除此活动");
        }

        // 4. 逻辑删除
        boolean result = this.removeById(activityId);

        if (result) {
            log.info("商家删除活动成功 - 用户ID: {}, 活动ID: {}, 活动标题: {}",
                    wxId, activityId, activity.getActivityTitle());
        } else {
            log.error("商家删除活动失败 - 用户ID: {}, 活动ID: {}",
                    wxId, activityId);
        }

        return result;
    }

    /**
     * 查询所有公开活动列表（is_public=1，status=1/2/3/4/6）
     *
     * @param queryDto 查询参数
     * @return 活动列表分页数据
     */
    @Override
    public PageVo<ActivityListVo> getPublicActivities(QueryPublicActivityDto queryDto) {
        // 1. 获取分页参数
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();

        // 2. 构建查询条件
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Activity::getIsPublic, 1) // 只查询公开的活动
                .in(Activity::getStatus, 1, 2, 3, 4, 6) // 含 6-未开始
                .like(StringUtils.hasText(queryDto.getActivityCategory()),
                      Activity::getActivityCategory, queryDto.getActivityCategory())
                .eq(queryDto.getOrganizerType() != null,
                    Activity::getOrganizerType, queryDto.getOrganizerType())
                .eq(queryDto.getOrganizerId() != null,
                    Activity::getOrganizerId, queryDto.getOrganizerId())
                .orderByDesc(Activity::getCreateTime); // 倒序排列（最新的在上面）

        // 3. 执行分页查询
        Page<Activity> activityPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 4. 转换为VO
        List<ActivityListVo> voList = activityPage.getRecords().stream()
                .map(this::convertToListVo)
                .collect(Collectors.toList());

        // 5. 构建分页结果
        Page<ActivityListVo> resultPage = new Page<>(current, pageSize, activityPage.getTotal());
        resultPage.setRecords(voList);

        log.info("查询公开活动列表成功 - 共{}条记录", activityPage.getTotal());
        return PageVo.of(resultPage);
    }

    /**
     * 查询首页展示的活动列表（is_public=1，status=1/2/3/4/6，show_on_homepage=1）
     *
     * @param queryDto 查询参数
     * @return 活动列表分页数据
     */
    @Override
    public PageVo<ActivityListVo> getHomepageActivities(QueryPublicActivityDto queryDto) {
        // 1. 获取分页参数
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();

        // 2. 构建查询条件
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Activity::getIsPublic, 1) // 只查询公开的活动
                .eq(Activity::getShowOnHomepage, 1) // 只查询展示在首页的活动
                .in(Activity::getStatus, 1, 2, 3, 4, 6) // 含 6-未开始
                .like(StringUtils.hasText(queryDto.getActivityCategory()),
                      Activity::getActivityCategory, queryDto.getActivityCategory())
                .eq(queryDto.getOrganizerType() != null,
                    Activity::getOrganizerType, queryDto.getOrganizerType())
                .eq(queryDto.getOrganizerId() != null,
                    Activity::getOrganizerId, queryDto.getOrganizerId())
                .orderByDesc(Activity::getCreateTime); // 倒序排列（最新的在上面）

        // 3. 执行分页查询
        Page<Activity> activityPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 4. 转换为VO
        List<ActivityListVo> voList = activityPage.getRecords().stream()
                .map(this::convertToListVo)
                .collect(Collectors.toList());

        // 5. 构建分页结果
        Page<ActivityListVo> resultPage = new Page<>(current, pageSize, activityPage.getTotal());
        resultPage.setRecords(voList);

        log.info("查询首页活动列表成功 - 共{}条记录", activityPage.getTotal());
        return PageVo.of(resultPage);
    }

    /**
     * 定时按当前时间校准活动状态（略过草稿、已取消）：
     * 不需要报名：未到活动开始为 6；已开始且未结束为 3；已过结束为 4。
     * 需要报名：未到报名开始为 6；报名期内为 1；报名结束至活动开始前为 2；活动期内为 3；已过结束为 4。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("开始执行活动状态定时更新任务, 当前时间: {}", now);

        int countEnded = this.baseMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .set(Activity::getStatus, 4)
                .set(Activity::getUpdateTime, now)
                .ne(Activity::getStatus, 4)
                .ne(Activity::getStatus, 0)
                .ne(Activity::getStatus, 5)
                .isNotNull(Activity::getEndTime)
                .le(Activity::getEndTime, now));

        int countNoSignupNotStarted = this.baseMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .set(Activity::getStatus, 6)
                .set(Activity::getUpdateTime, now)
                .eq(Activity::getIsSignup, 0)
                .ne(Activity::getStatus, 0)
                .ne(Activity::getStatus, 5)
                .isNotNull(Activity::getStartTime)
                .isNotNull(Activity::getEndTime)
                .gt(Activity::getEndTime, now)
                .gt(Activity::getStartTime, now));

        int countNoSignupOngoing = this.baseMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .set(Activity::getStatus, 3)
                .set(Activity::getUpdateTime, now)
                .eq(Activity::getIsSignup, 0)
                .ne(Activity::getStatus, 0)
                .ne(Activity::getStatus, 5)
                .isNotNull(Activity::getStartTime)
                .isNotNull(Activity::getEndTime)
                .gt(Activity::getEndTime, now)
                .le(Activity::getStartTime, now));

        int countSignupNotStarted = this.baseMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .set(Activity::getStatus, 6)
                .set(Activity::getUpdateTime, now)
                .eq(Activity::getIsSignup, 1)
                .ne(Activity::getStatus, 0)
                .ne(Activity::getStatus, 5)
                .isNotNull(Activity::getRegistrationStartTime)
                .isNotNull(Activity::getRegistrationEndTime)
                .isNotNull(Activity::getStartTime)
                .isNotNull(Activity::getEndTime)
                .gt(Activity::getEndTime, now)
                .gt(Activity::getRegistrationStartTime, now));

        int countSignupRegistering = this.baseMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .set(Activity::getStatus, 1)
                .set(Activity::getUpdateTime, now)
                .eq(Activity::getIsSignup, 1)
                .ne(Activity::getStatus, 0)
                .ne(Activity::getStatus, 5)
                .isNotNull(Activity::getRegistrationStartTime)
                .isNotNull(Activity::getRegistrationEndTime)
                .isNotNull(Activity::getStartTime)
                .isNotNull(Activity::getEndTime)
                .gt(Activity::getEndTime, now)
                .le(Activity::getRegistrationStartTime, now)
                .gt(Activity::getRegistrationEndTime, now));

        int countSignupRegClosed = this.baseMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .set(Activity::getStatus, 2)
                .set(Activity::getUpdateTime, now)
                .eq(Activity::getIsSignup, 1)
                .ne(Activity::getStatus, 0)
                .ne(Activity::getStatus, 5)
                .isNotNull(Activity::getRegistrationEndTime)
                .isNotNull(Activity::getStartTime)
                .isNotNull(Activity::getEndTime)
                .gt(Activity::getEndTime, now)
                .le(Activity::getRegistrationEndTime, now)
                .gt(Activity::getStartTime, now));

        int countSignupOngoing = this.baseMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .set(Activity::getStatus, 3)
                .set(Activity::getUpdateTime, now)
                .eq(Activity::getIsSignup, 1)
                .ne(Activity::getStatus, 0)
                .ne(Activity::getStatus, 5)
                .isNotNull(Activity::getStartTime)
                .isNotNull(Activity::getEndTime)
                .gt(Activity::getEndTime, now)
                .le(Activity::getStartTime, now));

        int sum = countEnded + countNoSignupNotStarted + countNoSignupOngoing + countSignupNotStarted
                + countSignupRegistering + countSignupRegClosed + countSignupOngoing;
        if (sum > 0) {
            log.info("活动状态更新完成: [已结束]{} [不需报名-未开始]{} [不需报名-进行中]{} [需报名-未开始]{} [需报名-报名中]{} [需报名-报名结束]{} [需报名-进行中]{}",
                    countEnded, countNoSignupNotStarted, countNoSignupOngoing, countSignupNotStarted,
                    countSignupRegistering, countSignupRegClosed, countSignupOngoing);
        }
    }

    /**
     * 将Activity转换为ActivityListVo
     *
     * @param activity 活动实体
     * @return ActivityListVo
     */
    private ActivityListVo convertToListVo(Activity activity) {
        ActivityListVo vo = new ActivityListVo();
        BeanUtils.copyProperties(activity, vo);

        // 转换ID为String避免前端精度丢失
        vo.setActivityId(String.valueOf(activity.getActivityId()));
        if (activity.getOrganizerId() != null) {
            vo.setOrganizerId(String.valueOf(activity.getOrganizerId()));
        }
        if (activity.getViewCount() != null) {
            vo.setViewCount(String.valueOf(activity.getViewCount()));
        }

        return vo;
    }
}
