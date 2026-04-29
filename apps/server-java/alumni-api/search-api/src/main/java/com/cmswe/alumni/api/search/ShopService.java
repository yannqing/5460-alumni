package com.cmswe.alumni.api.search;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.ApproveShopDto;
import com.cmswe.alumni.common.dto.CreateShopDto;
import com.cmswe.alumni.common.dto.QueryNearbyShopDto;
import com.cmswe.alumni.common.dto.QueryShopApprovalDto;
import com.cmswe.alumni.common.dto.UpdateShopDto;
import com.cmswe.alumni.common.entity.Shop;
import com.cmswe.alumni.common.vo.NearbyMerchantVo;
import com.cmswe.alumni.common.vo.NearbyShopVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.ShopApprovalVo;
import com.cmswe.alumni.common.vo.ShopDetailVo;
import com.cmswe.alumni.common.vo.ShopListVo;

import java.util.List;

/**
 * 店铺服务接口
 *
 * @author CNI Alumni System
 * @since 2025-12-22
 */
public interface ShopService extends IService<Shop> {

    /**
     * 创建店铺
     *
     * @param wxId          操作人微信ID
     * @param createShopDto 创建店铺请求参数
     * @return 是否成功
     */
    boolean createShop(Long wxId, CreateShopDto createShopDto);

    /**
     * 更新店铺信息
     *
     * @param wxId          操作人微信ID
     * @param updateShopDto 更新店铺请求参数
     * @return 是否成功
     */
    boolean updateShop(Long wxId, UpdateShopDto updateShopDto);

    /**
     * 删除店铺
     *
     * @param wxId   操作人微信ID
     * @param shopId 店铺ID
     * @return 是否成功
     */
    boolean deleteShop(Long wxId, Long shopId);

    /**
     * 撤销本人「待审核」的门店申请（审核状态置为 3-已撤销；仅创建人 createdBy 与「我的申请」详情权限一致）
     */
    boolean cancelPendingShopApplication(Long wxId, Long shopId);

    /**
     * 根据地理位置查询附近店铺（分页）
     *
     * @param queryDto 查询参数（包含经纬度、半径、分页等）
     * @return 附近店铺分页列表
     */
    PageVo<NearbyShopVo> getNearbyShops(QueryNearbyShopDto queryDto);

    /**
     * 按商户维度分页查询附近商户（用于发现页附近优惠）
     *
     * @param queryDto 查询参数（包含经纬度、半径、分页等）
     * @return 附近商户分页列表
     */
    PageVo<NearbyMerchantVo> getNearbyMerchants(QueryNearbyShopDto queryDto);

    /**
     * 分页查询附近有活动的商户
     */
    PageVo<NearbyMerchantVo> getNearbyActivities(QueryNearbyShopDto queryDto);

    /**
     * 根据店铺ID查询商铺详情
     *
     * @param shopId 店铺ID
     * @return 商铺详情
     */
    ShopDetailVo getShopDetail(Long shopId);

    /**
     * 商户主账号查询门店详情（含待审核），与 {@link #updateShop} 权限一致，用于编辑页
     *
     * @param wxId   当前用户
     * @param shopId 店铺ID
     * @return 详情；审核通过时等价于 {@link #getShopDetail(Long)}（不含强制公开字段裁剪）
     */
    ShopDetailVo getShopDetailForApplicantEdit(Long wxId, Long shopId);

    /**
     * 管理员根据商户ID查询店铺列表（分页）
     *
     * @param merchantId 商户ID
     * @param current    当前页
     * @param size       每页大小
     * @return 店铺列表分页数据
     */
    PageVo<ShopListVo> getShopsByMerchantId(Long merchantId, Long current, Long size);

    /**
     * 管理员审批店铺申请
     *
     * @param reviewerId  审核人微信ID
     * @param approveDto  审批信息
     * @return 是否成功
     */
    boolean approveShop(Long reviewerId, ApproveShopDto approveDto);

    /**
     * 分页查询店铺审批记录
     *
     * @param queryDto 查询条件
     * @return 审批记录列表
     */
    PageVo<ShopApprovalVo> selectApprovalRecordsByPage(QueryShopApprovalDto queryDto);

    /**
     * 根据店铺 ID 查询单条申请详情（与审批列表字段一致，含商户名称）
     *
     * @param shopId 店铺 ID
     * @return 申请详情；店铺不存在时由实现抛出业务异常
     */
    ShopApprovalVo getShopApprovalDetailByShopId(Long shopId);

    /**
     * 获取本人可用的门店列表
     * 1. 如果是门店管理员(ORGANIZE_SHOP_ADMIN)，返回其管理的门店
     * 2. 如果是商户管理员(ORGANIZE_MERCHANT_ADMIN)，返回该商户下所有审核通过且启用的门店
     *
     * @param wxId 当前用户微信ID
     * @return 可用门店列表
     */
    List<ShopListVo> getMyAvailableShops(Long wxId);
}
