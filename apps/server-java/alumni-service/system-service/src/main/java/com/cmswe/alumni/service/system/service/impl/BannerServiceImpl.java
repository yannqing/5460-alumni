package com.cmswe.alumni.service.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.system.BannerService;
import com.cmswe.alumni.api.user.FileService;
import com.cmswe.alumni.common.dto.CreateBannerDto;
import com.cmswe.alumni.common.dto.QueryBannerListDto;
import com.cmswe.alumni.common.dto.UpdateBannerDto;
import com.cmswe.alumni.common.entity.Files;
import com.cmswe.alumni.common.entity.HomePageBanner;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.BannerVo;
import com.cmswe.alumni.common.vo.FilesVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.service.system.mapper.BannerMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页轮播图 Service 实现类
 */
@Slf4j
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, HomePageBanner> implements BannerService {

    @Resource
    private BannerMapper bannerMapper;

    @Resource
    private FileService fileService;

    @Override
    public List<BannerVo> getHomePageBanners() {
        // 构造查询条件：启用状态、在有效期内
        LambdaQueryWrapper<HomePageBanner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomePageBanner::getBannerStatus, 1);

        LocalDateTime now = LocalDateTime.now();
        // 开始时间为空或开始时间小于等于当前时间
        queryWrapper.and(wrapper -> wrapper.isNull(HomePageBanner::getStartTime)
                .or()
                .le(HomePageBanner::getStartTime, now));

        // 结束时间为空或结束时间大于等于当前时间
        queryWrapper.and(wrapper -> wrapper.isNull(HomePageBanner::getEndTime)
                .or()
                .ge(HomePageBanner::getEndTime, now));

        // 按排序字段升序，创建时间降序
        queryWrapper.orderByAsc(HomePageBanner::getSortOrder)
                .orderByDesc(HomePageBanner::getCreateTime);

        List<HomePageBanner> banners = this.list(queryWrapper);

        // 转换为 VO 并填充图片信息
        return banners.stream().map(banner -> {
            BannerVo vo = BannerVo.objToVo(banner);
            // 填充图片信息
            if (banner.getBannerImage() != null) {
                try {
                    Files file = fileService.getById(banner.getBannerImage());
                    if (file != null) {
                        vo.setBannerImage(FilesVo.objToVo(file));
                    }
                } catch (Exception e) {
                    log.warn("查询轮播图图片失败 - BannerId: {}, ImageId: {}, Error: {}",
                            banner.getBannerId(), banner.getBannerImage(), e.getMessage());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageVo<BannerVo> getBannerPage(QueryBannerListDto queryDto) {
        // 1.参数校验
        if (queryDto == null) {
            throw new BusinessException("参数为空");
        }

        // 2.获取参数
        Integer pageNum = queryDto.getPageNum();
        Integer pageSize = queryDto.getPageSize();

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        // 3.构造查询条件
        LambdaQueryWrapper<HomePageBanner> queryWrapper = new LambdaQueryWrapper<>();

        // 标题模糊搜索
        if (StringUtils.isNotBlank(queryDto.getBannerTitle())) {
            queryWrapper.like(HomePageBanner::getBannerTitle, queryDto.getBannerTitle());
        }

        // 状态筛选
        if (queryDto.getBannerStatus() != null) {
            queryWrapper.eq(HomePageBanner::getBannerStatus, queryDto.getBannerStatus());
        }

        // 跳转类型筛选
        if (queryDto.getBannerType() != null) {
            queryWrapper.eq(HomePageBanner::getBannerType, queryDto.getBannerType());
        }

        // 按排序字段升序，创建时间降序
        queryWrapper.orderByAsc(HomePageBanner::getSortOrder)
                .orderByDesc(HomePageBanner::getCreateTime);

        // 4.执行分页查询
        Page<HomePageBanner> page = new Page<>(pageNum, pageSize);
        Page<HomePageBanner> resultPage = this.page(page, queryWrapper);

        // 5.转换为 VO 并填充图片信息
        List<BannerVo> bannerVos = resultPage.getRecords().stream().map(banner -> {
            BannerVo vo = BannerVo.objToVo(banner);
            // 填充图片信息
            if (banner.getBannerImage() != null) {
                try {
                    Files file = fileService.getById(banner.getBannerImage());
                    if (file != null) {
                        vo.setBannerImage(FilesVo.objToVo(file));
                    }
                } catch (Exception e) {
                    log.warn("查询轮播图图片失败 - BannerId: {}, ImageId: {}, Error: {}",
                            banner.getBannerId(), banner.getBannerImage(), e.getMessage());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        // 6.封装返回结果
        PageVo<BannerVo> pageVo = new PageVo<>();
        pageVo.setRecords(bannerVos);
        pageVo.setTotal(resultPage.getTotal());
        pageVo.setCurrent(resultPage.getCurrent());
        pageVo.setSize(resultPage.getSize());

        return pageVo;
    }

    @Override
    public BannerVo getBannerById(Long bannerId) {
        if (bannerId == null) {
            throw new BusinessException("轮播图ID不能为空");
        }

        HomePageBanner banner = this.getById(bannerId);
        if (banner == null) {
            throw new BusinessException("轮播图不存在");
        }

        BannerVo vo = BannerVo.objToVo(banner);

        // 填充图片信息
        if (banner.getBannerImage() != null) {
            try {
                Files file = fileService.getById(banner.getBannerImage());
                if (file != null) {
                    vo.setBannerImage(FilesVo.objToVo(file));
                }
            } catch (Exception e) {
                log.warn("查询轮播图图片失败 - BannerId: {}, ImageId: {}, Error: {}",
                        banner.getBannerId(), banner.getBannerImage(), e.getMessage());
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBanner(CreateBannerDto createDto) {
        if (createDto == null) {
            throw new BusinessException("参数为空");
        }

        // 创建轮播图实体
        HomePageBanner banner = new HomePageBanner();
        BeanUtils.copyProperties(createDto, banner);

        // 设置默认值
        banner.setViewCount(0L);
        banner.setClickCount(0L);
        banner.setCreateTime(LocalDateTime.now());
        banner.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        boolean success = this.save(banner);
        if (!success) {
            throw new BusinessException("创建轮播图失败");
        }

        return banner.getBannerId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBanner(UpdateBannerDto updateDto) {
        if (updateDto == null || updateDto.getBannerId() == null) {
            throw new BusinessException("参数错误");
        }

        // 检查轮播图是否存在
        HomePageBanner existBanner = this.getById(updateDto.getBannerId());
        if (existBanner == null) {
            throw new BusinessException("轮播图不存在");
        }

        // 更新轮播图信息
        HomePageBanner banner = new HomePageBanner();
        BeanUtils.copyProperties(updateDto, banner);
        banner.setUpdateTime(LocalDateTime.now());

        return this.updateById(banner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBanner(Long bannerId) {
        if (bannerId == null) {
            throw new BusinessException("轮播图ID不能为空");
        }

        // 检查轮播图是否存在
        HomePageBanner existBanner = this.getById(bannerId);
        if (existBanner == null) {
            throw new BusinessException("轮播图不存在");
        }

        // 逻辑删除
        return this.removeById(bannerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementClickCount(Long bannerId) {
        if (bannerId == null) {
            return;
        }

        LambdaUpdateWrapper<HomePageBanner> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HomePageBanner::getBannerId, bannerId);
        updateWrapper.setSql("click_count = click_count + 1");

        this.update(updateWrapper);
    }
}
