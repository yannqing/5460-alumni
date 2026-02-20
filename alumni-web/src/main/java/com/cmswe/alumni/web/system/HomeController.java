package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.system.BannerService;
import com.cmswe.alumni.api.system.HomePageArticleService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.QueryHomePageArticleListDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.BannerVo;
import com.cmswe.alumni.common.vo.HomePageArticleVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页数据 Controller
 * 用于获取首页相关的所有数据（轮播图、文章等）
 */
@Slf4j
@Tag(name = "首页数据")
@RestController
@RequestMapping("/home")
public class HomeController {

    @Resource
    private BannerService bannerService;

    @Resource
    private HomePageArticleService homePageArticleService;

    /**
     * 获取首页轮播图列表
     * @return 轮播图列表
     */
    @GetMapping("/banners")
    @Operation(summary = "获取首页轮播图列表")
    public BaseResponse<List<BannerVo>> getHomeBanners() {
        List<BannerVo> banners = bannerService.getHomePageBanners();
        return ResultUtils.success(Code.SUCCESS, banners, "获取成功");
    }

    /**
     * 记录轮播图点击
     * @param bannerId 轮播图ID
     * @return 成功响应
     */
    @PostMapping("/banners/{bannerId}/click")
    @Operation(summary = "记录轮播图点击")
    public BaseResponse<Void> clickBanner(@PathVariable Long bannerId) {
        bannerService.incrementClickCount(bannerId);
        return ResultUtils.success(Code.SUCCESS, null, "记录成功");
    }

    /**
     * 获取首页文章列表
     * @param queryDto 查询参数
     * @return 文章列表
     */
    @PostMapping("/articles")
    @Operation(summary = "获取首页文章列表")
    public BaseResponse<PageVo<HomePageArticleVo>> getHomeArticles(@RequestBody QueryHomePageArticleListDto queryDto) {
        PageVo<HomePageArticleVo> articlePage = homePageArticleService.getArticlePage(queryDto);
        return ResultUtils.success(Code.SUCCESS, articlePage, "获取成功");
    }
}
