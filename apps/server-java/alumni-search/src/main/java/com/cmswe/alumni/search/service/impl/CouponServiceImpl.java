package com.cmswe.alumni.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.search.CouponService;
import com.cmswe.alumni.common.dto.ClaimCouponDto;
import com.cmswe.alumni.common.dto.CreateCouponDto;
import com.cmswe.alumni.common.dto.QueryUserCouponDto;
import com.cmswe.alumni.common.dto.VerifyCouponDto;
import com.cmswe.alumni.common.dto.QueryMerchantCouponDto;
import com.cmswe.alumni.common.dto.UpdateCouponDto;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cmswe.alumni.common.entity.Coupon;
import com.cmswe.alumni.common.entity.CouponVerification;
import com.cmswe.alumni.common.entity.UserCoupon;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.CouponVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.UserCouponVo;
import com.cmswe.alumni.search.mapper.CouponMapper;
import com.cmswe.alumni.search.mapper.CouponVerificationMapper;
import com.cmswe.alumni.search.mapper.ShopMapper;
import com.cmswe.alumni.search.mapper.UserCouponMapper;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 优惠券服务实现
 *
 * @author CNI Alumni System
 */
@Slf4j
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private UserCouponMapper userCouponMapper;

    @Resource
    private CouponVerificationMapper couponVerificationMapper;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private WxUserMapper wxUserMapper;

    @Resource
    private com.cmswe.alumni.common.utils.WechatMiniUtil wechatMiniUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCouponVo claimCoupon(Long wxId, ClaimCouponDto claimCouponDto) {
        // 1. 参数校验
        Optional.ofNullable(wxId)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));
        Optional.ofNullable(claimCouponDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));
        Optional.ofNullable(claimCouponDto.getCouponId())
                .orElseThrow(() -> new BusinessException("优惠券ID不能为空"));

        Long couponId = claimCouponDto.getCouponId();

        // 2. 查询优惠券信息
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }

        // 3. 校验优惠券状态
        if (coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券未发布或已下架");
        }

        // 4. 校验优惠券有效期
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidStartTime() != null && now.isBefore(coupon.getValidStartTime())) {
            throw new BusinessException("优惠券领取时间未开始");
        }
        if (coupon.getValidEndTime() != null && now.isAfter(coupon.getValidEndTime())) {
            throw new BusinessException("优惠券已过期");
        }

        // 5. 校验库存
        if (coupon.getTotalQuantity() != null && coupon.getTotalQuantity() != -1) {
            if (coupon.getRemainQuantity() == null || coupon.getRemainQuantity() <= 0) {
                throw new BusinessException("优惠券已被领完");
            }
        }

        // 6. 校验是否仅限校友
        if (coupon.getIsAlumniOnly() != null && coupon.getIsAlumniOnly() == 1) {
            WxUser wxUser = wxUserMapper.selectById(wxId);
            if (wxUser == null) {
                throw new BusinessException("用户不存在");
            }
            if (wxUser.getIsAlumni() == null || wxUser.getIsAlumni() != 1) {
                throw new BusinessException("该优惠券仅限校友领取");
            }
        }

        // 7. 校验领取次数限制
        if (coupon.getPerUserLimit() != null && coupon.getPerUserLimit() > 0) {
            Long userClaimedCount = userCouponMapper.countByUserIdAndCouponId(wxId, couponId);
            if (userClaimedCount != null && userClaimedCount >= coupon.getPerUserLimit()) {
                throw new BusinessException("您已达到该优惠券的领取上限");
            }
        }

        // 8. 扣减库存（原子操作，防止超卖）
        int stockUpdated = couponMapper.decrementStock(couponId);
        if (stockUpdated == 0) {
            throw new BusinessException("优惠券库存不足");
        }

        // 9. 增加已领取数量
        couponMapper.incrementReceivedCount(couponId);

        // 10. 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setCouponId(couponId);
        userCoupon.setUserId(wxId);
        userCoupon.setMerchantId(coupon.getMerchantId());
        userCoupon.setShopId(coupon.getShopId());
        userCoupon.setCouponName(coupon.getCouponName());
        userCoupon.setCouponType(coupon.getCouponType());
        userCoupon.setDiscountValue(coupon.getDiscountValue());
        userCoupon.setMinSpend(coupon.getMinSpend());
        userCoupon.setReceiveTime(now);
        userCoupon.setReceiveChannel(claimCouponDto.getReceiveChannel());
        userCoupon.setReceiveSource(claimCouponDto.getReceiveSource());
        userCoupon.setStatus(1); // 1-未使用
        userCoupon.setValidStartTime(coupon.getValidStartTime());
        userCoupon.setValidEndTime(coupon.getValidEndTime());
        userCoupon.setExpireReminded(0);
        userCoupon.setCreateTime(now);
        userCoupon.setUpdateTime(now);

        // 11. 保存用户优惠券记录
        userCouponMapper.insert(userCoupon);

        // 12. 同步更新门店统计数据
        if (coupon.getShopId() != null) {
            shopMapper.updateStatistics(coupon.getShopId());
        }

        // 13. 转换为VO返回
        UserCouponVo userCouponVo = new UserCouponVo();
        BeanUtils.copyProperties(userCoupon, userCouponVo);

        // 加载完整的优惠券信息
        CouponVo couponVo = CouponVo.objToVo(coupon);
        userCouponVo.setCoupon(couponVo);

        log.info("用户领取优惠券成功 - 用户ID: {}, 优惠券ID: {}, 用户优惠券ID: {}",
                wxId, couponId, userCoupon.getUserCouponId());

        return userCouponVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponVo createCoupon(Long wxId, CreateCouponDto createCouponDto) {
        // 1. 参数校验
        Optional.ofNullable(wxId)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));
        Optional.ofNullable(createCouponDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        // 2. 校验必填字段
        if (createCouponDto.getMerchantId() == null) {
            throw new BusinessException("商户ID不能为空");
        }
        if (createCouponDto.getCouponName() == null || createCouponDto.getCouponName().isBlank()) {
            throw new BusinessException("优惠券名称不能为空");
        }
        if (createCouponDto.getCouponType() == null) {
            throw new BusinessException("优惠券类型不能为空");
        }
        if (createCouponDto.getDiscountValue() == null) {
            throw new BusinessException("优惠值不能为空");
        }
        if (createCouponDto.getTotalQuantity() == null) {
            throw new BusinessException("发行总量不能为空");
        }
        if (createCouponDto.getValidStartTime() == null) {
            throw new BusinessException("有效期开始时间不能为空");
        }
        if (createCouponDto.getValidEndTime() == null) {
            throw new BusinessException("有效期结束时间不能为空");
        }
        if (createCouponDto.getPublishType() == null) {
            throw new BusinessException("发布方式不能为空");
        }

        // 3. 校验有效期
        if (createCouponDto.getValidStartTime().isAfter(createCouponDto.getValidEndTime())) {
            throw new BusinessException("有效期开始时间不能晚于结束时间");
        }

        // 4. 校验定时发布时间
        if (createCouponDto.getPublishType() == 2) {
            if (createCouponDto.getPublishTime() == null) {
                throw new BusinessException("定时发布时必须填写发布时间");
            }
            if (createCouponDto.getPublishTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException("发布时间不能早于当前时间");
            }
        }

        // 5. 创建优惠券实体
        Coupon coupon = new Coupon();
        LocalDateTime now = LocalDateTime.now();

        // 基本信息
        coupon.setMerchantId(createCouponDto.getMerchantId());
        coupon.setShopId(createCouponDto.getShopId());
        coupon.setCouponCode(generateCouponCode()); // 生成唯一优惠券编码
        coupon.setCouponName(createCouponDto.getCouponName());
        coupon.setCouponType(createCouponDto.getCouponType());
        coupon.setCouponDesc(createCouponDto.getCouponDesc());
        coupon.setCouponImage(createCouponDto.getCouponImage());

        // 折扣信息
        coupon.setDiscountType(createCouponDto.getDiscountType());
        coupon.setDiscountValue(createCouponDto.getDiscountValue());
        coupon.setMinSpend(createCouponDto.getMinSpend());
        coupon.setMaxDiscount(createCouponDto.getMaxDiscount());

        // 发行信息
        coupon.setTotalQuantity(createCouponDto.getTotalQuantity());
        coupon.setRemainQuantity(createCouponDto.getTotalQuantity()); // 初始剩余数量 = 发行总量
        coupon.setPerUserLimit(createCouponDto.getPerUserLimit() != null ? createCouponDto.getPerUserLimit() : 1);
        coupon.setIsAlumniOnly(createCouponDto.getIsAlumniOnly() != null ? createCouponDto.getIsAlumniOnly() : 0);

        // 有效期信息
        coupon.setValidStartTime(createCouponDto.getValidStartTime());
        coupon.setValidEndTime(createCouponDto.getValidEndTime());
        coupon.setUseTimeLimit(createCouponDto.getUseTimeLimit());

        // 发布信息
        coupon.setPublishType(createCouponDto.getPublishType());
        coupon.setPublishTime(createCouponDto.getPublishTime());

        // 状态：立即发布=1，定时发布=0
        if (createCouponDto.getPublishType() == 1) {
            coupon.setStatus(1); // 1-已发布
        } else {
            coupon.setStatus(0); // 0-未发布（等待定时发布）
        }

        // 统计信息初始化
        coupon.setReceivedCount(0L);
        coupon.setUsedCount(0L);
        coupon.setViewCount(0L);

        // 创建人和时间
        coupon.setCreatedBy(wxId);
        coupon.setCreateTime(now);
        coupon.setUpdateTime(now);

        // 6. 保存优惠券
        boolean saveResult = this.save(coupon);
        if (!saveResult) {
            throw new BusinessException("创建优惠券失败");
        }

        // 7. 转换为VO返回
        CouponVo couponVo = new CouponVo();
        BeanUtils.copyProperties(coupon, couponVo);

        log.info("创建优惠券成功 - 创建人ID: {}, 商户ID: {}, 优惠券ID: {}, 优惠券名称: {}",
                wxId, createCouponDto.getMerchantId(), coupon.getCouponId(), coupon.getCouponName());

        return couponVo;
    }

    @Override
    public PageVo<UserCouponVo> queryUserCoupons(Long wxId, QueryUserCouponDto queryDto) {
        // 1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 2. 构造查询条件
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, wxId);

        if (queryDto.getStatus() != null) {
            queryWrapper.eq(UserCoupon::getStatus, queryDto.getStatus());
        }

        if (queryDto.getCouponType() != null) {
            queryWrapper.eq(UserCoupon::getCouponType, queryDto.getCouponType());
        }

        // 默认按领取时间倒序
        queryWrapper.orderByDesc(UserCoupon::getReceiveTime);

        // 3. 执行物理分页查询
        Page<UserCoupon> page = new Page<>(queryDto.getCurrent(), queryDto.getPageSize());
        Page<UserCoupon> userCouponPage = userCouponMapper.selectPage(page, queryWrapper);

        // 4. 提取所有优惠券ID，批量查询优惠券信息
        List<Long> couponIds = userCouponPage.getRecords().stream()
                .map(UserCoupon::getCouponId)
                .distinct()
                .toList();

        Map<Long, Coupon> couponMap = new java.util.HashMap<>();
        if (!couponIds.isEmpty()) {
            List<Coupon> coupons = couponMapper.selectBatchIds(couponIds);
            couponMap = coupons.stream()
                    .collect(java.util.stream.Collectors.toMap(Coupon::getCouponId, coupon -> coupon));
        }

        // 5. 转换并返回
        Map<Long, Coupon> finalCouponMap = couponMap;
        List<UserCouponVo> records = userCouponPage.getRecords().stream()
                .map(entity -> {
                    UserCouponVo vo = new UserCouponVo();
                    BeanUtils.copyProperties(entity, vo);

                    // 加载完整的优惠券信息
                    Coupon coupon = finalCouponMap.get(entity.getCouponId());
                    if (coupon != null) {
                        vo.setCoupon(CouponVo.objToVo(coupon));
                    }

                    return vo;
                })
                .toList();

        return new PageVo<>(records, userCouponPage.getTotal(), userCouponPage.getCurrent(), userCouponPage.getSize());
    }

    @Override
    public CouponVo getCouponDetail(Long couponId) {
        if (couponId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 1. 查询优惠券
        Coupon coupon = this.getById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }

        // 2. 增加浏览次数
        couponMapper.incrementViewCount(couponId);

        // 3. 转换为VO返回
        CouponVo couponVo = new CouponVo();
        BeanUtils.copyProperties(coupon, couponVo);

        return couponVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCouponVo getUserCouponDetail(Long userCouponId) {
        if (userCouponId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 1. 查询用户优惠券
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new BusinessException("用户优惠券记录不存在");
        }

        // 2. 检查核销码是否需要生成或刷新
        boolean needGenerate = userCoupon.getVerificationCode() == null ||
                userCoupon.getVerificationExpireTime() == null ||
                userCoupon.getVerificationExpireTime().isBefore(LocalDateTime.now());

        if (needGenerate && userCoupon.getStatus() == 1) {
            refreshCode(userCoupon);
            userCouponMapper.updateById(userCoupon);
        }

        // 3. 转换为VO
        UserCouponVo vo = new UserCouponVo();
        BeanUtils.copyProperties(userCoupon, vo);

        // 加载完整的优惠券信息
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon != null) {
            vo.setCoupon(CouponVo.objToVo(coupon));
        }

        // 生成二维码
        if (vo.getVerificationCode() != null) {
            vo.setBase64CodeImg(generateQrCode(vo.getVerificationCode()));
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCouponVo refreshVerificationCode(Long userCouponId) {
        if (userCouponId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new BusinessException("用户优惠券记录不存在");
        }

        if (userCoupon.getStatus() != 1) {
            throw new BusinessException("优惠券状态异常，无法刷新核销码");
        }

        refreshCode(userCoupon);
        userCouponMapper.updateById(userCoupon);

        UserCouponVo vo = new UserCouponVo();
        BeanUtils.copyProperties(userCoupon, vo);

        // 加载完整的优惠券信息
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon != null) {
            vo.setCoupon(CouponVo.objToVo(coupon));
        }

        // 生成二维码
        if (vo.getVerificationCode() != null) {
            vo.setBase64CodeImg(generateQrCode(vo.getVerificationCode()));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyCoupon(Long verifierId, VerifyCouponDto verifyDto) {
        // 1. 参数校验
        Optional.ofNullable(verifyDto).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));
        String code = verifyDto.getVerificationCode();

        // 2. 根据核销码查询
        UserCoupon userCoupon = userCouponMapper.selectOne(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getVerificationCode, code)
                        .last("LIMIT 1"));

        if (userCoupon == null) {
            throw new BusinessException("核销码无效");
        }

        // 校验适用门店
        if (userCoupon.getShopId() != null && !userCoupon.getShopId().equals(verifyDto.getShopId())) {
            throw new BusinessException("该优惠券不适用于当前门店");
        }

        // 3. 校验状态与有效期
        if (userCoupon.getStatus() != 1) {
            throw new BusinessException("该优惠券已被使用或已失效");
        }

        LocalDateTime now = LocalDateTime.now();
        if (userCoupon.getVerificationExpireTime() == null || userCoupon.getVerificationExpireTime().isBefore(now)) {
            throw new BusinessException("核销码已过期，请刷新后重试");
        }

        // 校验优惠券本身有效期
        if (userCoupon.getValidEndTime().isBefore(now)) {
            userCoupon.setStatus(3); // 标记为过期
            userCouponMapper.updateById(userCoupon);
            throw new BusinessException("该优惠券已超过有效期");
        }

        // 4. 记录核销日志（先创建核销记录，获取ID）
        CouponVerification verification = new CouponVerification();
        verification.setUserCouponId(userCoupon.getUserCouponId());
        verification.setCouponId(userCoupon.getCouponId());
        verification.setUserId(userCoupon.getUserId());
        verification.setMerchantId(userCoupon.getMerchantId());
        verification.setShopId(verifyDto.getShopId());
        verification.setVerificationCode(code);
        verification.setVerificationTime(now);
        verification.setVerifierId(verifierId);

        // 订单金额可选，如果未提供则设置为null
        verification.setOrderAmount(verifyDto.getOrderAmount());
        verification.setDiscountAmount(userCoupon.getDiscountValue());

        // 计算实际金额：只有当订单金额不为null时才计算
        if (verifyDto.getOrderAmount() != null && userCoupon.getDiscountValue() != null) {
            verification.setActualAmount(verifyDto.getOrderAmount().subtract(userCoupon.getDiscountValue()));
        } else {
            verification.setActualAmount(null);
        }

        verification.setVerificationMethod(verifyDto.getVerificationMethod());
        verification.setDeviceInfo(verifyDto.getDeviceInfo());
        verification.setCreateTime(now);

        couponVerificationMapper.insert(verification);

        // 5. 更新用户优惠券状态（包括核销记录ID）
        userCoupon.setStatus(2); // 已使用
        userCoupon.setUseTime(now);
        userCoupon.setVerificationId(verification.getVerificationId()); // 设置核销记录ID
        userCouponMapper.updateById(userCoupon);

        // 6. 更新统计数据
        // 门店统计
        shopMapper.updateStatistics(verifyDto.getShopId());
        // 优惠券模板统计
        couponMapper.incrementUsedCount(userCoupon.getCouponId());

        return true;
    }

    /**
     * 刷新核销码（5分钟有效）
     */
    private void refreshCode(UserCoupon userCoupon) {
        // 规则：优惠券编码后 4 位 + 随机 6 位数字
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        String pin = String.format("%06d", new Random().nextInt(1000000));
        String baseCode = (coupon != null && coupon.getCouponCode() != null)
                ? coupon.getCouponCode().substring(Math.max(0, coupon.getCouponCode().length() - 4))
                : "0000";

        userCoupon.setVerificationCode(baseCode + pin);
        userCoupon.setVerificationExpireTime(LocalDateTime.now().plusMinutes(5));
    }

    /**
     * 生成唯一优惠券编码
     *
     * @return 优惠券编码
     */
    private String generateCouponCode() {
        // 格式：CPN + 时间戳 + 随机UUID前8位
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "CPN" + timestamp + uuid;
    }

    /**
     * 生成二维码Base64（调用微信小程序码接口）
     */
    private String generateQrCode(String code) {
        // 使用小程序码接口，scene为核销码，page为页面路径
        // 注意：小程序端需要在 onLoad(options) 中处理 options.scene
        // 如果是从已有二维码扫码进入，取 options.scene；如果是参数进入，取 options.code
        // 这里 scene 直接存核销码，前端解析后作为 code 使用
        return wechatMiniUtil.createWxaCodeUnlimit(code, "pages/audit/merchant/coupon/verify/verify", 300);
    }

    @Override
    public PageVo<CouponVo> queryMerchantCoupons(QueryMerchantCouponDto queryDto) {
        // 1. 参数校验
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));
        Optional.ofNullable(queryDto.getMerchantId())
                .orElseThrow(() -> new BusinessException("商户ID不能为空"));

        // 2. 获取查询参数
        Long merchantId = queryDto.getMerchantId();
        String couponName = queryDto.getCouponName();
        Integer couponType = queryDto.getCouponType();
        Integer status = queryDto.getStatus();
        Integer reviewStatus = queryDto.getReviewStatus();
        Long shopId = queryDto.getShopId();
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();

        log.info("查询商户优惠券列表 - 商户ID: {}, 当前页: {}, 每页大小: {}", merchantId, current, pageSize);

        // 3. 构建查询条件
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Coupon::getMerchantId, merchantId)
                .like(StringUtils.isNotBlank(couponName), Coupon::getCouponName, couponName)
                .eq(couponType != null, Coupon::getCouponType, couponType)
                .eq(status != null, Coupon::getStatus, status)
                .eq(reviewStatus != null, Coupon::getReviewStatus, reviewStatus)
                .eq(shopId != null, Coupon::getShopId, shopId)
                .orderByDesc(Coupon::getCreateTime);

        // 4. 分页查询
        Page<Coupon> couponPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 5. 转换为 VO
        List<CouponVo> couponVos = couponPage.getRecords().stream()
                .map(CouponVo::objToVo)
                .toList();

        log.info("查询商户优惠券列表成功 - 商户ID: {}, 总记录数: {}", merchantId, couponPage.getTotal());

        // 6. 构建分页结果
        Page<CouponVo> voPage = new Page<>(current, pageSize, couponPage.getTotal());
        voPage.setRecords(couponVos);

        return PageVo.of(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCoupon(UpdateCouponDto updateDto) {
        // 1. 参数校验
        Optional.ofNullable(updateDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));
        Optional.ofNullable(updateDto.getCouponId())
                .orElseThrow(() -> new BusinessException("优惠券ID不能为空"));

        Long couponId = updateDto.getCouponId();

        log.info("更新优惠券 - 优惠券ID: {}", couponId);

        // 2. 查询优惠券
        Coupon coupon = this.getById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "优惠券不存在");
        }

        // 3. 校验优惠券状态（只能编辑未发布的优惠券）
        if (coupon.getStatus() == null || coupon.getStatus() != 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "只能编辑未发布的优惠券");
        }

        // 4. 更新优惠券信息
        BeanUtils.copyProperties(updateDto, coupon);
        coupon.setCouponId(couponId); // 确保ID不被覆盖
        coupon.setUpdateTime(LocalDateTime.now());

        // 如果修改了发行总量，同步更新剩余数量
        if (updateDto.getTotalQuantity() != null) {
            // 剩余数量 = 新的总量 - 已领取数量
            long receivedCount = coupon.getReceivedCount() != null ? coupon.getReceivedCount() : 0;
            if (updateDto.getTotalQuantity() == -1) {
                coupon.setRemainQuantity(-1); // 不限量
            } else {
                int remainQuantity = (int) (updateDto.getTotalQuantity() - receivedCount);
                coupon.setRemainQuantity(Math.max(0, remainQuantity));
            }
        }

        boolean updateResult = this.updateById(coupon);
        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新优惠券失败");
        }

        log.info("更新优惠券成功 - 优惠券ID: {}", couponId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCoupon(Long couponId) {
        // 1. 参数校验
        if (couponId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "优惠券ID不能为空");
        }

        log.info("删除优惠券 - 优惠券ID: {}", couponId);

        // 2. 查询优惠券
        Coupon coupon = this.getById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "优惠券不存在");
        }

        // 3. 校验优惠券状态（只能删除未发布、已结束、已下架的优惠券）
        if (coupon.getStatus() == null || (coupon.getStatus() != 0 && coupon.getStatus() != 2 && coupon.getStatus() != 3)) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "只能删除未发布、已结束或已下架的优惠券");
        }

        // 4. 检查是否有已领取但未使用的优惠券
        long unusedCount = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getCouponId, couponId)
                        .eq(UserCoupon::getStatus, 1) // 1-未使用
        );

        if (unusedCount > 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR,
                    "该优惠券还有 " + unusedCount + " 张未使用，无法删除");
        }

        // 5. 逻辑删除优惠券
        boolean deleteResult = this.removeById(couponId);
        if (!deleteResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "删除优惠券失败");
        }

        log.info("删除优惠券成功 - 优惠券ID: {}", couponId);

        return true;
    }
}
