package com.cmswe.alumni.search.service.impl;

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
import com.cmswe.alumni.common.vo.NearbyShopVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.ShopApprovalVo;
import com.cmswe.alumni.common.vo.ShopCouponVo;
import com.cmswe.alumni.common.vo.ShopDetailVo;
import com.cmswe.alumni.common.vo.ShopListVo;
import com.cmswe.alumni.search.mapper.ShopMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        shop.setReviewStatus(0); // 默认待审核
        shop.setIsRecommended(0); // 默认不推荐
        shop.setViewCount(0L);
        shop.setClickCount(0L);
        shop.setCouponReceivedCount(0L);
        shop.setCouponVerifiedCount(0L);
        shop.setIsDelete(0);

        // 4. 保存
        return this.save(shop);
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
        return this.removeById(shopId);
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
                List<ShopCouponVo> coupons = this.baseMapper.selectCouponsByShopId(shopId);
                shop.setCoupons(coupons);
                log.debug("店铺 {} 加载了 {} 个优惠券", shopId, coupons.size());
            } catch (Exception e) {
                log.error("加载店铺 {} 的优惠券失败", shop.getShopId(), e);
                shop.setCoupons(List.of());
            }
        });

        // 5. 查询总数
        Long total = this.baseMapper.countNearbyShops(
                queryDto.getLatitude(),
                queryDto.getLongitude(),
                radius,
                shopName,
                isRecommended);

        log.info("查询附近{}km商铺，位置：[{}, {}]，找到{}个结果（仅营业中且有优惠券的店铺）",
                radius, queryDto.getLatitude(), queryDto.getLongitude(), total);

        // 6. 构建分页结果
        Page<NearbyShopVo> page = new Page<>(current, pageSize, total);
        page.setRecords(shopList);

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
                        merchantListVo.setAlumniAssociationId(String.valueOf(merchant.getAlumniAssociationId()));
                    }
                    shopDetail.setMerchant(merchantListVo);
                }
            }
        } catch (Exception e) {
            log.error("加载店铺 {} 的商户信息失败", shopId, e);
        }

        // 6. 查询优惠券列表
        try {
            List<ShopCouponVo> coupons = this.baseMapper.selectCouponsByShopId(shopId);
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
        }

        return result;
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

        // 3. 构建查询条件
        Page<Shop> shopPage = this.lambdaQuery()
                .like(queryDto.getShopName() != null && !queryDto.getShopName().trim().isEmpty(),
                      Shop::getShopName, queryDto.getShopName())
                .eq(queryDto.getReviewStatus() != null, Shop::getReviewStatus, queryDto.getReviewStatus())
                .eq(queryDto.getMerchantId() != null, Shop::getMerchantId, queryDto.getMerchantId())
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
    public List<ShopListVo> getMyAvailableShops(Long wxId) {
        // 1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }

        log.info("开始查询用户可用门店列表 - 用户ID: {}", wxId);

        // 2. 定义角色代码常量
        final String SHOP_ADMIN_ROLE_CODE = "ORGANIZE_SHOP_ADMIN"; // 门店管理员
        final String MERCHANT_ADMIN_ROLE_CODE = "ORGANIZE_MERCHANT_ADMIN"; // 商户管理员

        List<ShopListVo> shopVoList = new java.util.ArrayList<>();

        // 3. 查询门店管理员角色的门店
        try {
            // 3.1 查询门店管理员角色
            com.cmswe.alumni.common.entity.Role shopAdminRole = roleService.lambdaQuery()
                    .eq(com.cmswe.alumni.common.entity.Role::getRoleCode, SHOP_ADMIN_ROLE_CODE)
                    .eq(com.cmswe.alumni.common.entity.Role::getStatus, 1)
                    .one();

            if (shopAdminRole != null) {
                log.info("找到门店管理员角色 - 角色ID: {}, 角色代码: {}",
                        shopAdminRole.getRoleId(), shopAdminRole.getRoleCode());

                // 3.2 查询该用户在门店管理员角色下的关联关系
                List<com.cmswe.alumni.common.entity.RoleUser> shopAdminRoleUsers = roleUserService.lambdaQuery()
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getWxId, wxId)
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getRoleId, shopAdminRole.getRoleId())
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getType, 3) // 3-商户
                        .list();

                if (!shopAdminRoleUsers.isEmpty()) {
                    // 3.3 提取门店ID列表（organizeId就是shopId）
                    List<Long> shopIds = shopAdminRoleUsers.stream()
                            .map(com.cmswe.alumni.common.entity.RoleUser::getOrganizeId)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    log.info("用户作为门店管理员管理的门店数量: {}", shopIds.size());

                    if (!shopIds.isEmpty()) {
                        // 3.4 查询这些门店的详细信息
                        List<Shop> shops = this.lambdaQuery()
                                .in(Shop::getShopId, shopIds)
                                .eq(Shop::getReviewStatus, 1) // 审核通过
                                .eq(Shop::getStatus, 1) // 营业中
                                .list();

                        // 3.5 转换为VO
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

        // 4. 查询商户管理员角色的门店
        try {
            // 4.1 查询商户管理员角色
            com.cmswe.alumni.common.entity.Role merchantAdminRole = roleService.lambdaQuery()
                    .eq(com.cmswe.alumni.common.entity.Role::getRoleCode, MERCHANT_ADMIN_ROLE_CODE)
                    .eq(com.cmswe.alumni.common.entity.Role::getStatus, 1)
                    .one();

            if (merchantAdminRole != null) {
                log.info("找到商户管理员角色 - 角色ID: {}, 角色代码: {}",
                        merchantAdminRole.getRoleId(), merchantAdminRole.getRoleCode());

                // 4.2 查询该用户在商户管理员角色下的关联关系
                List<com.cmswe.alumni.common.entity.RoleUser> merchantAdminRoleUsers = roleUserService.lambdaQuery()
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getWxId, wxId)
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getRoleId, merchantAdminRole.getRoleId())
                        .eq(com.cmswe.alumni.common.entity.RoleUser::getType, 3) // 3-商户
                        .list();

                if (!merchantAdminRoleUsers.isEmpty()) {
                    // 4.3 提取商户ID列表（organizeId就是merchantId）
                    List<Long> merchantIds = merchantAdminRoleUsers.stream()
                            .map(com.cmswe.alumni.common.entity.RoleUser::getOrganizeId)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    log.info("用户作为商户管理员管理的商户数量: {}", merchantIds.size());

                    if (!merchantIds.isEmpty()) {
                        // 4.4 查询这些商户下所有审核通过且启用的门店
                        List<Shop> merchantShops = this.lambdaQuery()
                                .in(Shop::getMerchantId, merchantIds)
                                .eq(Shop::getReviewStatus, 1) // 审核通过
                                .eq(Shop::getStatus, 1) // 营业中
                                .list();

                        // 4.5 转换为VO，并去重（避免与门店管理员角色的门店重复）
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
