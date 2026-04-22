package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.UserFavorite;
import com.cmswe.alumni.common.vo.MerchantFavoriteItemVo;
import com.cmswe.alumni.common.vo.MerchantFavoriteToggleVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 用户收藏服务
 */
public interface UserFavoriteService extends IService<UserFavorite> {

    /**
     * 切换商户收藏状态（收藏 <-> 取消收藏）
     *
     * @param wxId 用户ID
     * @param merchantId 商户ID
     * @return 切换结果
     */
    MerchantFavoriteToggleVo toggleMerchantFavorite(Long wxId, Long merchantId);

    /**
     * 查询用户收藏的商户列表
     *
     * @param wxId 用户ID
     * @param current 当前页
     * @param pageSize 每页大小
     * @return 收藏商户列表
     */
    PageVo<MerchantFavoriteItemVo> listMerchantFavorites(Long wxId, Long current, Long pageSize);
}
