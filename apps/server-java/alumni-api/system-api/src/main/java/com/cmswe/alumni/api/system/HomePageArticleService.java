package com.cmswe.alumni.api.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.CreateHomePageArticleDto;
import com.cmswe.alumni.common.dto.QueryHomePageArticleListDto;
import com.cmswe.alumni.common.dto.QueryMyHomePageArticleListDto;
import com.cmswe.alumni.common.dto.UpdateHomePageArticleDto;
import com.cmswe.alumni.common.entity.HomePageArticle;
import com.cmswe.alumni.common.vo.HomePageArticleDetailVo;
import com.cmswe.alumni.common.vo.HomePageArticleVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 首页公众号文章 API Service 接口
 */
public interface HomePageArticleService extends IService<HomePageArticle> {

    /**
     * 分页查询文章列表
     * @param queryDto 查询参数
     * @return 分页结果
     */
    PageVo<HomePageArticleVo> getArticlePage(QueryHomePageArticleListDto queryDto);

    /**
     * 根据ID查询文章详情
     * @param homeArticleId 文章ID
     * @return 文章详情
     */
    HomePageArticleDetailVo getArticleDetailById(Long homeArticleId);

    /**
     * 新增首页文章
     * @param createDto 新增文章参数
     * @param actualPublisherWxId 实际发布用户ID（从token解析获取）
     * @return 文章ID
     */
    Long createArticle(CreateHomePageArticleDto createDto, Long actualPublisherWxId);

    /**
     * 更新首页文章
     * @param updateDto 更新文章参数
     * @return 是否更新成功
     */
    Boolean updateArticle(UpdateHomePageArticleDto updateDto);

    /**
     * 分页查询本人有权限管理的文章列表
     * @param queryDto 查询参数
     * @param currentUserWxId 当前用户ID
     * @return 分页结果
     */
    PageVo<HomePageArticleVo> getMyArticlePage(QueryMyHomePageArticleListDto queryDto, Long currentUserWxId);

    /**
     * 删除首页文章
     * @param homeArticleId 文章ID
     * @return 是否删除成功
     */
    Boolean deleteArticle(Long homeArticleId);

    /**
     * 检查并扣减首页文章发布配额
     * @param publishType 发布者类型（ASSOCIATION-校友会，LOCAL_PLATFORM-校促会）
     * @param publishWxId 发布者ID
     */
    void checkAndDeductQuota(String publishType, Long publishWxId);
}
