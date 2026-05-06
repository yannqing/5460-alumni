package com.cmswe.alumni.search.service.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.search.ShopService;
import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.common.dto.ApproveShopDto;
import com.cmswe.alumni.common.dto.CreateShopDto;
import com.cmswe.alumni.common.dto.QueryNearbyShopDto;
import com.cmswe.alumni.common.dto.QueryShopApprovalDto;
import com.cmswe.alumni.common.dto.UpdateShopDto;
import com.cmswe.alumni.common.entity.Merchant;
import com.cmswe.alumni.common.entity.Shop;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.entity.Activity;
import com.cmswe.alumni.common.vo.ActivityListVo;
import com.cmswe.alumni.common.vo.MerchantListVo;
import com.cmswe.alumni.common.vo.NearbyMerchantVo;
import com.cmswe.alumni.common.vo.NearbyShopVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.ShopApprovalVo;
import com.cmswe.alumni.common.vo.ShopCouponVo;
import com.cmswe.alumni.common.vo.ShopDetailVo;
import com.cmswe.alumni.common.vo.ShopListVo;
import com.cmswe.alumni.search.mapper.MerchantMapper;
import com.cmswe.alumni.search.mapper.ShopMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 店铺服务实现
 *
 * @author CNI Alumni System
 * @since 2025-12-22
 */
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Resource
    private MerchantMapper merchantMapper;

    @Lazy
    @Resource
    private MerchantService merchantService;

    @Resource
    private com.cmswe.alumni.api.user.RoleService roleService;

    @Resource
    private com.cmswe.alumni.api.user.RoleUserService roleUserService;

    @Lazy
    @Resource
    private com.cmswe.alumni.api.system.ActivityService activityService;

    @Resource
    private com.cmswe.alumni.api.user.UserFavoriteService userFavoriteService;

    private List<Long> parseAssociationIds(String associationIdStr) {
        if (StringUtils.isBlank(associationIdStr)) {
            return new ArrayList<>();
        }
        String trimmed = associationIdStr.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                return JSON.parseArray(trimmed, Long.class);
            } catch (Exception e) {
                log.error("解析校友会ID数组失败: {}", trimmed, e);
            }
        }
        try {
            Long id = Long.parseLong(trimmed);
            return new ArrayList<>(Collections.singletonList(id));
        } catch (NumberFormatException e) {
            log.warn("校友会ID字段格式非数字且非数组: {}", trimmed);
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createShop(Long wxId, CreateShopDto createShopDto) {
        // 1. 参数校验
        if (wxId == null || createShopDto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 校验商户是否存在且属于该用户
        Merchant merchant = merchantService.getById(createShopDto.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        if (!merchant.getUserId().equals(wxId)) {
            // 这里可以增加管理员权限校验，暂时只允许主账号创建
            throw new BusinessException("无权为此商户创建店铺");
        }
        if (merchant.getReviewStatus() != 1) {
            throw new BusinessException("商户状态异常，无法创建店铺");
        }

        // 3. 创建店铺实体
        Shop shop = new Shop();
        BeanUtils.copyProperties(createShopDto, shop);

        LocalDateTime now = LocalDateTime.now();
        shop.setCreatedBy(wxId);
        shop.setCreateTime(now);
        shop.setUpdateTime(now);
        shop.setStatus(1); // 默认营业中
        shop.setReviewStatus(1); // 直接审核通过
        shop.setReviewTime(now); // 自动通过时间
        shop.setIsRecommended(0); // 默认不推荐
        shop.setViewCount(0L);
        shop.setClickCount(0L);
        shop.setCouponReceivedCount(0L);
        shop.setCouponVerifiedCount(0L);
        shop.setIsDelete(0);

        // 4. 保存
        boolean saved = this.save(shop);
        if (saved) {
            // 5. 自动通过后，刷新商户统计信息（门店数量+1）
            refreshMerchantStatistics(shop.getMerchantId());
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateShop(Long wxId, UpdateShopDto updateShopDto) {
        // 1. 参数校验
        if (wxId == null || updateShopDto == null || updateShopDto.getShopId() == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 查询店铺并校验所属权
        Shop shop = this.getById(updateShopDto.getShopId());
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }

        Merchant merchant = merchantService.getById(shop.getMerchantId());
        if (merchant == null || !merchant.getUserId().equals(wxId)) {
            throw new BusinessException("无权修改此店铺信息");
        }

        // 3. 更新字段
        BeanUtils.copyProperties(updateShopDto, shop, "shopId", "merchantId", "createdBy", "createTime");
        shop.setUpdateTime(LocalDateTime.now());

        // 4. 保存
        return this.updateById(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteShop(Long wxId, Long shopId) {
        // 1. 参数校验
        if (wxId == null || shopId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 查询店铺并校验所属权
        Shop shop = this.getById(shopId);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }

        Merchant merchant = merchantService.getById(shop.getMerchantId());
        if (merchant == null || !merchant.getUserId().equals(wxId)) {
            throw new BusinessException("无权删除此店铺");
        }

        // 3. 逻辑删除
        Long merchantId = shop.getMerchantId();
        boolean removed = this.removeById(shopId);
        if (removed) {
            refreshMerchantStatistics(merchantId);
        }
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelPendingShopApplication(Long wxId, Long shopId) {
        if (wxId == null || shopId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }
        Shop shop = this.getById(shopId);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        if (!wxId.equals(shop.getCreatedBy())) {
            throw new BusinessException("无权撤销该门店申请");
        }
        if (shop.getReviewStatus() == null || shop.getReviewStatus() != 0) {
            throw new BusinessException("仅待审核的门店申请可撤销");
        }
        shop.setReviewStatus(3);
        shop.setReviewReason(null);
        shop.setReviewTime(null);
        shop.setUpdateTime(LocalDateTime.now());
        log.info("用户撤销待审核门店申请 - 用户ID: {}, 店铺ID: {}", wxId, shopId);
        return this.updateById(shop);
    }

    @Override
    public PageVo<NearbyShopVo> getNearbyShops(QueryNearbyShopDto queryDto) {
        // 1. 参数校验
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        if (queryDto.getLatitude() == null || queryDto.getLongitude() == null) {
            throw new BusinessException("经纬度不能为空");
        }

        // 2. 获取查询参数
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();
        int offset = (current - 1) * pageSize;
        Integer radius = queryDto.getRadius();
        String shopName = queryDto.getShopName();
        Integer isRecommended = queryDto.getIsRecommended();

        // 3. 执行分页查询（只查询营业中且有优惠券的店铺）
        List<NearbyShopVo> shopList = this.baseMapper.selectNearbyWithPage(
                queryDto.getLatitude(),
                queryDto.getLongitude(),
                radius,
                shopName,
                isRecommended,
                offset,
                pageSize);

        // 4. 为每个店铺加载优惠券列表
        shopList.forEach(shop -> {
            try {
                Long shopId = Long.parseLong(shop.getShopId());
                Long merchantId = Long.parseLong(shop.getMerchantId());
                List<ShopCouponVo> coupons = this.baseMapper.selectCouponsByShopId(shopId, merchantId);
                shop.setCoupons(coupons);
                log.debug("店铺 {} 加载了 {} 个优惠券", shopId, coupons.size());
            } catch (Exception e) {
                log.error("加载店铺 {} 的优惠券失败", shop.getShopId(), e);
                shop.setCoupons(List.of());
            }
        });

        // 5. 查询总数（MyBatis 在 COUNT 无结果行时可能映射为 null，需兜底避免 Page 拆箱 NPE）
        long total = Optional.ofNullable(
                        this.baseMapper.countNearbyShops(
                                queryDto.getLatitude(),
                                queryDto.getLongitude(),
                                radius,
                                shopName,
                                isRecommended))
                .orElse(0L);

        log.info("查询附近{}km商铺，位置：[{}, {}]，找到{}个结果（仅营业中且有优惠券的店铺）",
                radius, queryDto.getLatitude(), queryDto.getLongitude(), total);

        // 6. 构建分页结果
        Page<NearbyShopVo> page = new Page<>(current, pageSize, total);
        page.setRecords(shopList);

        return PageVo.of(page);
    }

    @Override
    public PageVo<NearbyMerchantVo> getNearbyMerchants(QueryNearbyShopDto queryDto) {
        // 1. 参数校验
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        if (queryDto.getLatitude() == null || queryDto.getLongitude() == null) {
            throw new BusinessException("经纬度不能为空");
        }

        // 2. 获取查询参数
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();
        int offset = (current - 1) * pageSize;
        Integer radius = queryDto.getRadius();
        String merchantName = queryDto.getShopName(); // 复用 shopName 字段作为商户名搜索
        Integer isRecommended = queryDto.getIsRecommended();

        // 3. 执行分页查询（按商户维度，只查询有有效专属优惠券的商户）
        List<NearbyMerchantVo> merchantList = this.baseMapper.selectNearbyMerchantsWithPage(
                queryDto.getLatitude(),
                queryDto.getLongitude(),
                radius,
                merchantName,
                isRecommended,
                offset,
                pageSize);

        // 4. 为每个商户加载优惠券列表和收藏数量
        merchantList.forEach(merchant -> {
            try {
                Long merchantId = Long.parseLong(merchant.getMerchantId());
                List<ShopCouponVo> coupons = this.baseMapper.selectCouponsByMerchantId(merchantId);
                merchant.setCoupons(coupons);
                log.debug("商户 {} 加载了 {} 个专属优惠券", merchantId, coupons.size());

                // 查询商户收藏数量
                Long favoriteCount = userFavoriteService.count(
                        new LambdaQueryWrapper<com.cmswe.alumni.common.entity.UserFavorite>()
                                .eq(com.cmswe.alumni.common.entity.UserFavorite::getTargetType, 1)
                                .eq(com.cmswe.alumni.common.entity.UserFavorite::getTargetId, merchantId)
                                .eq(com.cmswe.alumni.common.entity.UserFavorite::getIsDeleted, 0));
                merchant.setFavoriteCount(favoriteCount);
            } catch (Exception e) {
                log.error("加载商户 {} 的优惠券或收藏数失败", merchant.getMerchantId(), e);
                merchant.setCoupons(List.of());
                merchant.setFavoriteCount(0L);
            }
        });

        // 5. 查询总数（MyBatis 在 COUNT 无结果行时可能映射为 null，需兜底避免 Page 拆箱 NPE）
        long total = Optional.ofNullable(
                        this.baseMapper.countNearbyMerchants(
                                queryDto.getLatitude(),
                                queryDto.getLongitude(),
                                radius,
                                merchantName,
                                isRecommended))
                .orElse(0L);

        log.info("查询附近{}km商户，位置：[{}, {}]，找到{}个结果（有专属优惠券的商户）",
                radius, queryDto.getLatitude(), queryDto.getLongitude(), total);

        // 6. 构建分页结果
        Page<NearbyMerchantVo> page = new Page<>(current, pageSize, total);
        page.setRecords(merchantList);

        return PageVo.of(page);
    }

    @Override
    public PageVo<NearbyMerchantVo> getNearbyActivities(QueryNearbyShopDto queryDto) {
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        if (queryDto.getLatitude() == null || queryDto.getLongitude() == null) {
            throw new BusinessException("经纬度不能为空");
        }

        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();
        Integer radius = queryDto.getRadius();
        String merchantName = queryDto.getShopName();

        Long merchantTotal = Optional.ofNullable(
                this.baseMapper.countNearbyActivities(
                        queryDto.getLatitude(),
                        queryDto.getLongitude(),
                        radius,
                        merchantName))
                .orElse(0L);
        Long associationTotal = Optional.ofNullable(
                this.baseMapper.countNearbyAssociationActivities(
                        queryDto.getLatitude(),
                        queryDto.getLongitude(),
                        merchantName,
                        radius))
                .orElse(0L);
        long total = merchantTotal + associationTotal;

        int fetchMerchantSize = (int) Math.min(merchantTotal, Integer.MAX_VALUE);
        int fetchAssociationSize = (int) Math.min(associationTotal, Integer.MAX_VALUE);

        List<NearbyMerchantVo> merchantList = fetchMerchantSize > 0
                ? this.baseMapper.selectNearbyActivitiesWithPage(
                queryDto.getLatitude(),
                queryDto.getLongitude(),
                radius,
                merchantName,
                0,
                fetchMerchantSize)
                : List.of();
        List<NearbyMerchantVo> associationList = fetchAssociationSize > 0
                ? this.baseMapper.selectNearbyAssociationActivitiesWithPage(
                queryDto.getLatitude(),
                queryDto.getLongitude(),
                merchantName,
                radius,
                0,
                fetchAssociationSize)
                : List.of();

        merchantList.forEach(merchant -> {
            try {
                Long merchantId = Long.parseLong(merchant.getMerchantId());
                log.info("加载商户 {} 的活动，merchantId={}", merchant.getMerchantName(), merchantId);
                List<NearbyMerchantVo.ActivityItem> activities =
                        this.baseMapper.selectActivitiesByMerchantId(merchantId);
                log.info("商户 {} 找到 {} 个活动", merchant.getMerchantName(), activities.size());
                merchant.setActivities(activities);
                merchant.setSourceType("merchant");

                Long favoriteCount = userFavoriteService.count(
                        new LambdaQueryWrapper<com.cmswe.alumni.common.entity.UserFavorite>()
                                .eq(com.cmswe.alumni.common.entity.UserFavorite::getTargetType, 1)
                                .eq(com.cmswe.alumni.common.entity.UserFavorite::getTargetId, merchantId)
                                .eq(com.cmswe.alumni.common.entity.UserFavorite::getIsDeleted, 0));
                merchant.setFavoriteCount(favoriteCount);
            } catch (Exception e) {
                log.error("加载商户 {} 的活动或收藏数失败", merchant.getMerchantId(), e);
                merchant.setActivities(List.of());
                merchant.setFavoriteCount(0L);
            }
        });

        associationList.forEach(association -> {
            try {
                Long associationId = Long.parseLong(association.getMerchantId());
                List<NearbyMerchantVo.ActivityItem> activities =
                        this.baseMapper.selectActivitiesByAssociationId(associationId);
                association.setActivities(activities);
                association.setSourceType("association");
                association.setFavoriteCount(0L);
            } catch (Exception e) {
                log.error("加载校友会 {} 的活动失败", association.getMerchantId(), e);
                association.setActivities(List.of());
                association.setFavoriteCount(0L);
            }
        });

        List<NearbyMerchantVo> mergedList = new ArrayList<>(merchantList.size() + associationList.size());
        mergedList.addAll(merchantList);
        mergedList.addAll(associationList);
        mergedList.sort(Comparator.comparing(
                item -> Optional.ofNullable(item.getDistance()).orElse(java.math.BigDecimal.valueOf(Double.MAX_VALUE))
        ));

        int offset = Math.max((current - 1) * pageSize, 0);
        int toIndex = Math.min(offset + pageSize, mergedList.size());
        List<NearbyMerchantVo> pagedList = offset >= mergedList.size()
                ? List.of()
                : mergedList.subList(offset, toIndex);

        log.info("查询附近{}km活动，位置：[{}, {}]，找到{}个结果",
                radius, queryDto.getLatitude(), queryDto.getLongitude(), total);

        Page<NearbyMerchantVo> page = new Page<>(current, pageSize, total);
        page.setRecords(pagedList);

        return PageVo.of(page);
    }

    @Override
    public ShopDetailVo getShopDetail(Long shopId) {
        // 1. 参数校验
        Optional.ofNullable(shopId)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        // 2. 先查询店铺基本信息，验证审核状态
        Shop shop = this.getById(shopId);
        if (shop == null) {
            throw new BusinessException("商铺不存在或已被删除");
        }

        // 3. 检查审核状态（只返回审核通过的店铺）
        if (shop.getReviewStatus() == null || shop.getReviewStatus() != 1) {
            throw new BusinessException("商铺不存在或未通过审核");
        }

        // 4. 查询商铺详情
        ShopDetailVo shopDetail = this.baseMapper.selectShopDetailById(shopId);
        if (shopDetail == null) {
            throw new BusinessException("商铺不存在或已被删除");
        }

        // 5. 查询商户信息
        try {
            if (shop.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(shop.getMerchantId());
                if (merchant != null) {
                    MerchantListVo merchantListVo = MerchantListVo.objToVo(merchant);
                    // 处理 Long 类型精度丢失
                    merchantListVo.setMerchantId(String.valueOf(merchant.getMerchantId()));
                    if (merchant.getUserId() != null) {
                        merchantListVo.setUserId(String.valueOf(merchant.getUserId()));
                    }
        if (merchant.getAlumniAssociationId() != null) {
            List<Long> associationIds = parseAssociationIds(merchant.getAlumniAssociationId());
            if (!associationIds.isEmpty()) {
                merchantListVo.setAlumniAssociationId(String.valueOf(associationIds.get(0)));
            }
        }
                    shopDetail.setMerchant(merchantListVo);
                }
            }
        } catch (Exception e) {
            log.error("加载店铺 {} 的商户信息失败", shopId, e);
        }

        // 6. 查询优惠券列表
        try {
            List<ShopCouponVo> coupons = this.baseMapper.selectCouponsByShopId(shopId, shop.getMerchantId());
            shopDetail.setCoupons(coupons);
            log.debug("店铺 {} 加载了 {} 个优惠券", shopId, coupons.size());
        } catch (Exception e) {
            log.error("加载店铺 {} 的优惠券失败", shopId, e);
            shopDetail.setCoupons(List.of());
        }

        // 7. 查询活动列表（查询该门店下审核通过的活动）
        try {
            List<Activity> activities = activityService.lambdaQuery()
                    .eq(Activity::getOrganizerType, 5) // 主办方类型为门店
                    .eq(Activity::getOrganizerId, shopId)
                    .eq(Activity::getReviewStatus, 1) // 审核通过
                    .orderByDesc(Activity::getCreateTime)
                    .list();

            List<ActivityListVo> activityListVos = activities.stream()
                    .map(ActivityListVo::objToVo)
                    .collect(Collectors.toList());

            shopDetail.setActivities(activityListVos);
            log.debug("店铺 {} 加载了 {} 个活动", shopId, activityListVos.size());
        } catch (Exception e) {
            log.error("加载店铺 {} 的活动列表失败", shopId, e);
            shopDetail.setActivities(List.of());
        }

        log.info("查询商铺详情，shopId: {}", shopId);

        return shopDetail;
    }

    @Override
    public ShopDetailVo getShopDetailForApplicantEdit(Long wxId, Long shopId) {
        if (wxId == null || shopId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }
        Shop shop = this.getById(shopId);
        if (shop == null) {
            throw new BusinessException("商铺不存在");
        }
        Merchant merchant = merchantService.getById(shop.getMerchantId());
        if (merchant == null || !merchant.getUserId().equals(wxId)) {
            throw new BusinessException("无权查看或修改此店铺信息");
        }
        if (shop.getReviewStatus() != null && shop.getReviewStatus() == 1) {
            return getShopDetail(shopId);
        }
        return buildShopDetailVoFromEntity(shop);
    }

    /**
     * 未审核通过时无法走 {@link #getShopDetail(Long)}，改为从实体组装（与「我的申请」详情一致）
     */
    private ShopDetailVo buildShopDetailVoFromEntity(Shop shop) {
        ShopDetailVo vo = new ShopDetailVo();
        vo.setShopId(shop.getShopId() != null ? String.valueOf(shop.getShopId()) : null);
        vo.setShopName(shop.getShopName());
        vo.setShopType(shop.getShopType());
        vo.setProvince(shop.getProvince());
        vo.setCity(shop.getCity());
        vo.setDistrict(shop.getDistrict());
        vo.setAddress(shop.getAddress());
        vo.setLatitude(shop.getLatitude());
        vo.setLongitude(shop.getLongitude());
        vo.setPhone(shop.getPhone());
        vo.setBusinessHours(shop.getBusinessHours());
        vo.setShopImages(shop.getShopImages());
        vo.setLogo(shop.getLogo());
        vo.setDescription(shop.getDescription());
        vo.setStatus(shop.getStatus());
        vo.setReviewStatus(shop.getReviewStatus());
        vo.setReviewReason(shop.getReviewReason());
        vo.setReviewTime(shop.getReviewTime());
        vo.setIsRecommended(shop.getIsRecommended());
        vo.setCreateTime(shop.getCreateTime());
        vo.setUpdateTime(shop.getUpdateTime());
        if (shop.getMerchantId() != null) {
            Merchant m = merchantService.getById(shop.getMerchantId());
            if (m != null) {
                MerchantListVo mvo = MerchantListVo.objToVo(m);
                mvo.setMerchantId(String.valueOf(m.getMerchantId()));
                if (m.getUserId() != null) {
                    mvo.setUserId(String.valueOf(m.getUserId()));
                }
                if (m.getAlumniAssociationId() != null) {
                    List<Long> associationIds = parseAssociationIds(m.getAlumniAssociationId());
                    if (!associationIds.isEmpty()) {
                        mvo.setAlumniAssociationId(String.valueOf(associationIds.get(0)));
                    }
                }
                vo.setMerchant(mvo);
            }
        }
        return vo;
    }

    @Override
    public PageVo<ShopListVo> getShopsByMerchantId(Long merchantId, Long current, Long size) {
        // 1. 参数校验
        if (merchantId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }
        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        // 2. 校验商户是否存在
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }

        // 3. 构建分页查询（只查询审核通过的店铺）
        Page<Shop> page = new Page<>(current, size);
        Page<Shop> shopPage = this.lambdaQuery()
                .eq(Shop::getMerchantId, merchantId)
                .eq(Shop::getReviewStatus, 1)
                .orderByDesc(Shop::getCreateTime)
                .page(page);

        // 4. 转换为 VO
        List<ShopListVo> shopListVos = shopPage.getRecords().stream()
                .map(ShopListVo::objToVo)
                .collect(Collectors.toList());

        // 5. 构建分页结果
        Page<ShopListVo> voPage = new Page<>(current, size, shopPage.getTotal());
        voPage.setRecords(shopListVos);

        log.info("管理员查询商户店铺列表，merchantId: {}, 找到{}个审核通过的店铺", merchantId, shopPage.getTotal());

        return PageVo.of(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveShop(Long reviewerId, ApproveShopDto approveDto) {
        // 1. 参数校验
        if (reviewerId == null || approveDto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }
        if (approveDto.getShopId() == null) {
            throw new BusinessException("店铺ID不能为空");
        }
        if (approveDto.getReviewStatus() == null) {
            throw new BusinessException("审核状态不能为空");
        }
        if (approveDto.getReviewStatus() != 1 && approveDto.getReviewStatus() != 2) {
            throw new BusinessException("审核状态只能是1（通过）或2（驳回）");
        }
        if (approveDto.getReviewStatus() == 2 &&
            (approveDto.getReviewReason() == null || approveDto.getReviewReason().trim().isEmpty())) {
            throw new BusinessException("审核驳回时必须填写驳回原因");
        }

        // 2. 查询店铺
        Shop shop = this.getById(approveDto.getShopId());
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }

        // 3. 检查店铺当前状态
        if (shop.getReviewStatus() != null && shop.getReviewStatus() != 0) {
            throw new BusinessException("该店铺已审核，无需重复审核");
        }

        // 4. 更新审核状态
        shop.setReviewStatus(approveDto.getReviewStatus());
        shop.setReviewReason(approveDto.getReviewReason());
        shop.setReviewerId(reviewerId);
        shop.setReviewTime(LocalDateTime.now());
        shop.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(shop);

        if (result) {
            String statusMsg = approveDto.getReviewStatus() == 1 ? "审核通过" : "审核驳回";
            log.info("店铺审核成功 - 审核人ID: {}, 店铺ID: {}, 审核状态: {}",
                    reviewerId, approveDto.getShopId(), statusMsg);
            if (approveDto.getReviewStatus() == 1) {
                refreshMerchantStatistics(shop.getMerchantId());
            }
        }

        return result;
    }

    /**
     * 按库中 shop 记录重算商户冗余统计（shop_count、券相关累计等），与 {@link MerchantMapper#updateStatistics} 一致。
     */
    private void refreshMerchantStatistics(Long merchantId) {
        if (merchantId == null) {
            return;
        }
        int rows = merchantMapper.updateStatistics(merchantId);
        log.info("已刷新商户统计信息 merchantId={}, 影响行数={}", merchantId, rows);
    }

    @Override
    public PageVo<ShopApprovalVo> selectApprovalRecordsByPage(QueryShopApprovalDto queryDto) {
        // 1. 参数校验
        if (queryDto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 构建分页查询
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();
        if (current < 1) {
            current = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }

        Page<Shop> page = new Page<>(current, pageSize);

        Long alumniAssociationId = queryDto.getAlumniAssociationId();
        Long merchantId = queryDto.getMerchantId();
        List<Long> scopedMerchantIds = null;
        if (alumniAssociationId != null) {
            scopedMerchantIds = merchantService.lambdaQuery()
                    .select(Merchant::getMerchantId)
                    .apply("(alumni_association_id = CAST({0} AS CHAR) OR JSON_CONTAINS(alumni_association_id, CAST({0} AS CHAR)))", alumniAssociationId)
                    .eq(merchantId != null, Merchant::getMerchantId, merchantId)
                    .list()
                    .stream()
                    .map(Merchant::getMerchantId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            if (scopedMerchantIds.isEmpty()) {
                Page<ShopApprovalVo> emptyPage = new Page<>(current, pageSize, 0);
                emptyPage.setRecords(List.of());
                return PageVo.of(emptyPage);
            }
        }

        // 3. 构建查询条件（审核列表不展示已撤销：review_status = 3）
        Page<Shop> shopPage = this.lambdaQuery()
                .like(queryDto.getShopName() != null && !queryDto.getShopName().trim().isEmpty(),
                      Shop::getShopName, queryDto.getShopName())
                .eq(queryDto.getReviewStatus() != null, Shop::getReviewStatus, queryDto.getReviewStatus())
                .ne(Shop::getReviewStatus, 3)
                .eq(alumniAssociationId == null && merchantId != null, Shop::getMerchantId, merchantId)
                .in(alumniAssociationId != null, Shop::getMerchantId, scopedMerchantIds)
                .eq(queryDto.getShopType() != null, Shop::getShopType, queryDto.getShopType())
                .orderByDesc(Shop::getCreateTime)
                .page(page);

        // 4. 转换为 VO
        List<ShopApprovalVo> approvalVos = shopPage.getRecords().stream()
                .map(shop -> {
                    ShopApprovalVo vo = ShopApprovalVo.objToVo(shop);
                    // 加载商户名称
                    try {
                        if (shop.getMerchantId() != null) {
                            Merchant merchant = merchantService.getById(shop.getMerchantId());
                            if (merchant != null) {
                                vo.setMerchantName(merchant.getMerchantName());
                            }
                        }
                    } catch (Exception e) {
                        log.error("加载店铺 {} 的商户名称失败", shop.getShopId(), e);
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        // 5. 构建分页结果
        Page<ShopApprovalVo> voPage = new Page<>(current, pageSize, shopPage.getTotal());
        voPage.setRecords(approvalVos);

        log.info("查询店铺审批记录，查询条件: {}, 找到{}条记录", queryDto, shopPage.getTotal());

        return PageVo.of(voPage);
    }

    @Override
    public ShopApprovalVo getShopApprovalDetailByShopId(Long shopId) {
        if (shopId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "shop_id 不能为空");
        }
        Shop shop = this.getById(shopId);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        ShopApprovalVo vo = ShopApprovalVo.objToVo(shop);
        try {
            if (shop.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(shop.getMerchantId());
                if (merchant != null) {
                    vo.setMerchantName(merchant.getMerchantName());
                }
            }
        } catch (Exception e) {
            log.error("加载店铺 {} 的商户名称失败", shopId, e);
        }
        return vo;
    }

    @Override
    public List<ShopListVo> getMyAvailableShops(Long wxId) {
        // 1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }

        log.info("开始查询用户可用门店列表 - 用户ID: {}", wxId);

        // 2. 定义角色代码常量
        final String SYSTEM_SUPER_ADMIN_ROLE_CODE = "SYSTEM_SUPER_ADMIN"; // 系统超级管理员
        final String SHOP_ADMIN_ROLE_CODE = "ORGANIZE_SHOP_ADMIN"; // 门店管理员
        final String MERCHANT_ADMIN_ROLE_CODE = "ORGANIZE_MERCHANT_ADMIN"; // 商户管理员

        List<ShopListVo> shopVoList = new java.util.ArrayList<>();

        // 3. 系统超级管理员：可查看全部审核通过且营业中的门店
        try {
            List<com.cmswe.alumni.common.entity.Role> userRoles = roleService.getRolesByUserId(wxId);
            boolean isSystemSuperAdmin = userRoles != null && userRoles.stream()
                    .anyMatch(role -> SYSTEM_SUPER_ADMIN_ROLE_CODE.equals(role.getRoleCode()));
            if (isSystemSuperAdmin) {
                List<Shop> allShops = this.lambdaQuery()
                        .eq(Shop::getReviewStatus, 1) // 审核通过
                        .eq(Shop::getStatus, 1) // 营业中
                        .list();

                List<ShopListVo> allShopVos = allShops.stream()
                        .map(ShopListVo::objToVo)
                        .collect(Collectors.toList());
                log.info("系统超级管理员查询可用门店成功 - 用户ID: {}, 门店数量: {}", wxId, allShopVos.size());
                return allShopVos;
            }
        } catch (Exception e) {
            log.error("查询系统超级管理员角色失败 - 用户ID: {}", wxId, e);
        }

        // 4. 查询门店管理员角色的门店
        try {
            // 4.1 查询门店管理员角色
            com.cmswe.alumni.common.entity.Role shopAdminRole = roleService.lambdaQuery()
                    .eq(com.cmswe.alumni.common.entity.Role::getRoleCode, SHOP_ADMIN_ROLE_CODE)
                    .eq(com.cmswe.alumni.common.entity.Role::getStatus, 1)
                    .one();

            if (shopAdminRole != null) {
                log.info("找到门店管理员角色 - 角色ID: {}, 角色代码: {}",
                        shopAdminRole.getRoleId(), shopAdminRole.getRoleCode());

                // 4.2 查询该用户在门店管理员角色下的关联关系
                List<com.cmswe.alumni.common.entity.RoleUser> shopAdminRoleUsers = roleUserService.lambdaQuery()
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getWxId, wxId)
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getRoleId, shopAdminRole.getRoleId())
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getType, 4) // 4-门店
                        .list();

                if (!shopAdminRoleUsers.isEmpty()) {
                    // 4.3 提取门店ID列表（organizeId就是shopId）
                    List<Long> shopIds = shopAdminRoleUsers.stream()
                            .map(com.cmswe.alumni.common.entity.RoleUser::getOrganizeId)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    log.info("用户作为门店管理员管理的门店数量: {}", shopIds.size());

                    if (!shopIds.isEmpty()) {
                        // 4.4 查询这些门店的详细信息
                        List<Shop> shops = this.lambdaQuery()
                                .in(Shop::getShopId, shopIds)
                                .eq(Shop::getReviewStatus, 1) // 审核通过
                                .eq(Shop::getStatus, 1) // 营业中
                                .list();

                        // 4.5 转换为VO
                        List<ShopListVo> shopAdminShops = shops.stream()
                                .map(ShopListVo::objToVo)
                                .collect(Collectors.toList());

                        shopVoList.addAll(shopAdminShops);
                        log.info("添加门店管理员可用门店数量: {}", shopAdminShops.size());
                    }
                }
            } else {
                log.warn("未找到门店管理员角色配置 - 角色代码: {}", SHOP_ADMIN_ROLE_CODE);
            }
        } catch (Exception e) {
            log.error("查询门店管理员角色门店失败 - 用户ID: {}", wxId, e);
        }

        // 5. 查询商户管理员角色的门店
        try {
            // 5.1 查询商户管理员角色
            com.cmswe.alumni.common.entity.Role merchantAdminRole = roleService.lambdaQuery()
                    .eq(com.cmswe.alumni.common.entity.Role::getRoleCode, MERCHANT_ADMIN_ROLE_CODE)
                    .eq(com.cmswe.alumni.common.entity.Role::getStatus, 1)
                    .one();

            if (merchantAdminRole != null) {
                log.info("找到商户管理员角色 - 角色ID: {}, 角色代码: {}",
                        merchantAdminRole.getRoleId(), merchantAdminRole.getRoleCode());

                // 5.2 查询该用户在商户管理员角色下的关联关系
                List<com.cmswe.alumni.common.entity.RoleUser> merchantAdminRoleUsers = roleUserService.lambdaQuery()
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getWxId, wxId)
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getRoleId, merchantAdminRole.getRoleId())
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getType, 3) // 3-商户
                        .list();

                if (!merchantAdminRoleUsers.isEmpty()) {
                    // 5.3 提取商户ID列表（organizeId就是merchantId）
                    List<Long> merchantIds = merchantAdminRoleUsers.stream()
                            .map(com.cmswe.alumni.common.entity.RoleUser::getOrganizeId)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    log.info("用户作为商户管理员管理的商户数量: {}", merchantIds.size());

                    if (!merchantIds.isEmpty()) {
                        // 5.4 查询这些商户下所有审核通过且启用的门店
                        List<Shop> merchantShops = this.lambdaQuery()
                                .in(Shop::getMerchantId, merchantIds)
                                .eq(Shop::getReviewStatus, 1) // 审核通过
                                .eq(Shop::getStatus, 1) // 营业中
                                .list();

                        // 5.5 转换为VO，并去重（避免与门店管理员角色的门店重复）
                        List<Long> existingShopIds = shopVoList.stream()
                                .map(vo -> Long.valueOf(vo.getShopId()))
                                .collect(Collectors.toList());

                        List<ShopListVo> merchantAdminShops = merchantShops.stream()
                                .filter(shop -> !existingShopIds.contains(shop.getShopId()))
                                .map(ShopListVo::objToVo)
                                .collect(Collectors.toList());

                        shopVoList.addAll(merchantAdminShops);
                        log.info("添加商户管理员可用门店数量: {}", merchantAdminShops.size());
                    }
                }
            } else {
                log.warn("未找到商户管理员角色配置 - 角色代码: {}", MERCHANT_ADMIN_ROLE_CODE);
            }
        } catch (Exception e) {
            log.error("查询商户管理员角色门店失败 - 用户ID: {}", wxId, e);
        }

        log.info("查询用户可用门店列表完成 - 用户ID: {}, 总门店数: {}", wxId, shopVoList.size());

        return shopVoList;
    }
}
