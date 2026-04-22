package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.api.user.UserFavoriteService;
import com.cmswe.alumni.common.entity.Merchant;
import com.cmswe.alumni.common.entity.UserFavorite;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.SnowflakeIdGenerator;
import com.cmswe.alumni.common.vo.MerchantFavoriteItemVo;
import com.cmswe.alumni.common.vo.MerchantFavoriteToggleVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.service.user.mapper.UserFavoriteMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户收藏服务实现
 */
@Slf4j
@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    private static final int TARGET_TYPE_MERCHANT = 1;
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Resource
    private MerchantService merchantService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantFavoriteToggleVo toggleMerchantFavorite(Long wxId, Long merchantId) {
        if (wxId == null || merchantId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID和商户ID不能为空");
        }

        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户不存在");
        }

        LambdaQueryWrapper<UserFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFavorite::getWxId, wxId)
                .eq(UserFavorite::getTargetType, TARGET_TYPE_MERCHANT)
                .eq(UserFavorite::getTargetId, merchantId)
                .last("LIMIT 1");

        UserFavorite existing = this.getOne(queryWrapper, false);
        LocalDateTime now = LocalDateTime.now();

        boolean nowFavorited;
        Long favoriteId;
        if (existing == null) {
            UserFavorite favorite = new UserFavorite();
            favoriteId = snowflakeIdGenerator.nextId();
            favorite.setFavoriteId(favoriteId);
            favorite.setWxId(wxId);
            favorite.setTargetType(TARGET_TYPE_MERCHANT);
            favorite.setTargetId(merchantId);
            favorite.setIsDeleted(NOT_DELETED);
            favorite.setCreatedTime(now);
            favorite.setUpdatedTime(now);
            this.save(favorite);
            nowFavorited = true;
        } else {
            favoriteId = existing.getFavoriteId();
            Integer currentDeleted = existing.getIsDeleted() == null ? NOT_DELETED : existing.getIsDeleted();
            existing.setIsDeleted(currentDeleted == NOT_DELETED ? DELETED : NOT_DELETED);
            existing.setUpdatedTime(now);
            this.updateById(existing);
            nowFavorited = existing.getIsDeleted() == NOT_DELETED;
        }

        MerchantFavoriteToggleVo vo = new MerchantFavoriteToggleVo();
        vo.setFavoriteId(String.valueOf(favoriteId));
        vo.setWxId(String.valueOf(wxId));
        vo.setMerchantId(String.valueOf(merchantId));
        vo.setTargetType(TARGET_TYPE_MERCHANT);
        vo.setFavorited(nowFavorited);
        vo.setAction(nowFavorited ? "favorite" : "unfavorite");
        log.info("切换商户收藏成功 - wxId: {}, merchantId: {}, favorited: {}", wxId, merchantId, nowFavorited);
        return vo;
    }

    @Override
    public PageVo<MerchantFavoriteItemVo> listMerchantFavorites(Long wxId, Long current, Long pageSize) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }
        long safeCurrent = (current == null || current < 1) ? 1L : current;
        long safePageSize = (pageSize == null || pageSize < 1) ? 10L : Math.min(pageSize, 50L);

        Page<UserFavorite> page = this.page(
                new Page<>(safeCurrent, safePageSize),
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getWxId, wxId)
                        .eq(UserFavorite::getTargetType, TARGET_TYPE_MERCHANT)
                        .eq(UserFavorite::getIsDeleted, NOT_DELETED)
                        .orderByDesc(UserFavorite::getUpdatedTime)
                        .orderByDesc(UserFavorite::getCreatedTime)
        );
        List<UserFavorite> favorites = page.getRecords();
        if (favorites == null || favorites.isEmpty()) {
            return new PageVo<>(new ArrayList<>(), page.getTotal(), page.getCurrent(), page.getSize());
        }

        Set<Long> merchantIds = favorites.stream()
                .map(UserFavorite::getTargetId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return new PageVo<>(new ArrayList<>(), page.getTotal(), page.getCurrent(), page.getSize());
        }

        Map<Long, Merchant> merchantMap = merchantService.listByIds(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getMerchantId, Function.identity(), (a, b) -> a));

        List<MerchantFavoriteItemVo> result = new ArrayList<>();
        for (UserFavorite favorite : favorites) {
            Merchant merchant = merchantMap.get(favorite.getTargetId());
            if (merchant == null) {
                continue;
            }
            MerchantFavoriteItemVo item = new MerchantFavoriteItemVo();
            item.setFavoriteId(String.valueOf(favorite.getFavoriteId()));
            item.setTargetType(TARGET_TYPE_MERCHANT);
            item.setMerchantId(String.valueOf(merchant.getMerchantId()));
            item.setMerchantName(merchant.getMerchantName());
            item.setLogo(merchant.getLogo());
            item.setMerchantType(merchant.getMerchantType());
            item.setContactPhone(merchant.getContactPhone());
            item.setBusinessCategory(merchant.getBusinessCategory());
            item.setStatus(merchant.getStatus());
            item.setFavoritedTime(favorite.getUpdatedTime() != null ? favorite.getUpdatedTime() : favorite.getCreatedTime());
            result.add(item);
        }
        return new PageVo<>(result, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
