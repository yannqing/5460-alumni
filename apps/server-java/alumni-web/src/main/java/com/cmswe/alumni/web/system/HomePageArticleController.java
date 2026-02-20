package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.system.HomePageArticleService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.CreateHomePageArticleDto;
import com.cmswe.alumni.common.dto.QueryHomePageArticleListDto;
import com.cmswe.alumni.common.dto.QueryMyHomePageArticleListDto;
import com.cmswe.alumni.common.dto.UpdateHomePageArticleDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.HomePageArticleDetailVo;
import com.cmswe.alumni.common.vo.HomePageArticleVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 首页公众号文章 Controller
 */
@Slf4j
@Tag(name = "首页文章管理")
@RestController
@RequestMapping("/home-page-article")
public class HomePageArticleController {

    @Resource
    private HomePageArticleService homePageArticleService;

    /**
     * 分页查询首页文章列表
     * @param queryDto 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询首页文章列表")
    public BaseResponse<PageVo<HomePageArticleVo>> getArticlePage(@RequestBody QueryHomePageArticleListDto queryDto) {
        PageVo<HomePageArticleVo> articlePage = homePageArticleService.getArticlePage(queryDto);
        return ResultUtils.success(Code.SUCCESS, articlePage, "分页查询成功");
    }

    /**
     * 根据ID查询文章详情
     * @param id 文章ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询文章详情")
    public BaseResponse<HomePageArticleDetailVo> getArticleById(@PathVariable Long id) {
        HomePageArticleDetailVo articleDetail = homePageArticleService.getArticleDetailById(id);
        return ResultUtils.success(Code.SUCCESS, articleDetail, "查询成功");
    }

    /**
     * 新增首页文章
     * @param createDto 新增文章参数
     * @return 文章ID
     */
    @PostMapping("/create")
    @Operation(summary = "新增首页文章")
    public BaseResponse<String> createArticle(@Valid @RequestBody CreateHomePageArticleDto createDto) {
        Long articleId = homePageArticleService.createArticle(createDto);
        return ResultUtils.success(Code.SUCCESS, String.valueOf(articleId), "新增成功");
    }

    /**
     * 更新首页文章
     * @param updateDto 更新文章参数
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @Operation(summary = "更新首页文章")
    public BaseResponse<Boolean> updateArticle(@Valid @RequestBody UpdateHomePageArticleDto updateDto) {
        Boolean result = homePageArticleService.updateArticle(updateDto);
        return ResultUtils.success(Code.SUCCESS, result, "更新成功");
    }

    /**
     * 分页查询本人创建的文章列表
     * @param queryDto 查询参数
     * @return 分页结果
     */
    @PostMapping("/my-page")
    @Operation(summary = "分页查询本人创建的文章列表")
    public BaseResponse<PageVo<HomePageArticleVo>> getMyArticlePage(@RequestBody QueryMyHomePageArticleListDto queryDto) {
        PageVo<HomePageArticleVo> articlePage = homePageArticleService.getMyArticlePage(queryDto);
        return ResultUtils.success(Code.SUCCESS, articlePage, "查询成功");
    }

    /**
     * 删除首页文章
     * @param id 文章ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除首页文章")
    public BaseResponse<Boolean> deleteArticle(@PathVariable Long id) {
        Boolean result = homePageArticleService.deleteArticle(id);
        return ResultUtils.success(Code.SUCCESS, result, "删除成功");
    }
}
