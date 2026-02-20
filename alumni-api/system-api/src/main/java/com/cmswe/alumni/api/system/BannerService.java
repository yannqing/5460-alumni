package com.cmswe.alumni.api.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.CreateBannerDto;
import com.cmswe.alumni.common.dto.QueryBannerListDto;
import com.cmswe.alumni.common.dto.UpdateBannerDto;
import com.cmswe.alumni.common.entity.HomePageBanner;
import com.cmswe.alumni.common.vo.BannerVo;
import com.cmswe.alumni.common.vo.PageVo;

import java.util.List;

/**
 * 首页轮播图 API Service 接口
 */
public interface BannerService extends IService<HomePageBanner> {

    /**
     * 获取首页轮播图列表（仅返回启用且在有效期内的轮播图）
     * @return 轮播图列表
     */
    List<BannerVo> getHomePageBanners();

    /**
     * 分页查询轮播图列表（后台管理）
     * @param queryDto 查询参数
     * @return 分页结果
     */
    PageVo<BannerVo> getBannerPage(QueryBannerListDto queryDto);

    /**
     * 根据ID查询轮播图详情
     * @param bannerId 轮播图ID
     * @return 轮播图详情
     */
    BannerVo getBannerById(Long bannerId);

    /**
     * 新增轮播图
     * @param createDto 新增轮播图参数
     * @return 轮播图ID
     */
    Long createBanner(CreateBannerDto createDto);

    /**
     * 更新轮播图
     * @param updateDto 更新轮播图参数
     * @return 是否更新成功
     */
    Boolean updateBanner(UpdateBannerDto updateDto);

    /**
     * 删除轮播图
     * @param bannerId 轮播图ID
     * @return 是否删除成功
     */
    Boolean deleteBanner(Long bannerId);

    /**
     * 增加轮播图点击次数
     * @param bannerId 轮播图ID
     */
    void incrementClickCount(Long bannerId);
}
