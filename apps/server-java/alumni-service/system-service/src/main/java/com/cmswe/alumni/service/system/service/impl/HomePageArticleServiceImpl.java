package com.cmswe.alumni.service.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.system.HomePageArticleApplyService;
import com.cmswe.alumni.api.system.HomePageArticleService;
import com.cmswe.alumni.api.user.FileService;
import com.cmswe.alumni.common.dto.CreateHomePageArticleDto;
import com.cmswe.alumni.common.dto.QueryHomePageArticleListDto;
import com.cmswe.alumni.common.dto.QueryMyHomePageArticleListDto;
import com.cmswe.alumni.common.dto.UpdateHomePageArticleDto;
import com.cmswe.alumni.common.entity.*;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.FilesVo;
import com.cmswe.alumni.common.vo.HomePageArticleDetailVo;
import com.cmswe.alumni.common.vo.HomePageArticleVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.service.system.mapper.HomePageArticleMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页公众号文章 Service 实现类
 */
@Slf4j
@Service
public class HomePageArticleServiceImpl extends ServiceImpl<HomePageArticleMapper, HomePageArticle> implements HomePageArticleService {

    @Resource
    private HomePageArticleMapper homePageArticleMapper;

    @Resource
    private FileService fileService;

    @Lazy
    @Resource
    private HomePageArticleApplyService homePageArticleApplyService;

    @Override
    public PageVo<HomePageArticleVo> getArticlePage(QueryHomePageArticleListDto queryDto) {
        // 1.参数校验
        if (queryDto == null) {
            throw new BusinessException("参数为空");
        }

        // 2.获取参数
        Long current = queryDto.getCurrent();
        Long size = queryDto.getSize();

        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        // 3.构造查询条件
        LambdaQueryWrapper<HomePageArticle> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询启用状态的文章
        queryWrapper.eq(HomePageArticle::getArticleStatus, 1);

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(HomePageArticle::getCreateTime);

        // 4.执行分页查询 - 注意：这里需要查询所有文章来组装父子关系，不能直接分页
        // 先查询所有启用的文章
        List<HomePageArticle> allArticles = this.list(queryWrapper);

        // 5.转换为VO并查询封面图信息
        List<HomePageArticleVo> allVoList = allArticles.stream()
                .map(article -> {
                    HomePageArticleVo vo = HomePageArticleVo.objToVo(article);

                    // 查询并设置封面图信息
                    if (article.getCoverImg() != null) {
                        try {
                            Files file = fileService.getById(article.getCoverImg());
                            if (file != null) {
                                vo.setCoverImg(FilesVo.objToVo(file));
                            }
                        } catch (Exception e) {
                            log.warn("查询封面图失败 - ArticleId: {}, CoverImgId: {}, Error: {}",
                                    article.getHomeArticleId(), article.getCoverImg(), e.getMessage());
                            // 查询失败时不设置封面图，vo.coverImg 保持为 null
                        }
                    }

                    // 直接从文章表中获取发布者头像
                    // publisherAvatar 字段已在 objToVo 中通过 BeanUtils.copyProperties 自动复制

                    return vo;
                })
                .toList();

        // 6. 组装父子关系
        // 6.1 将文章分为父文章和子文章
        List<HomePageArticleVo> parentArticles = new ArrayList<>();
        Map<Long, List<HomePageArticleVo>> childrenMap = new HashMap<>();

        for (HomePageArticleVo vo : allVoList) {
            if (vo.getPid() == null || vo.getPid() == 0) {
                // 父文章（根节点）
                parentArticles.add(vo);
            } else {
                // 子文章，按 pid 分组
                childrenMap.computeIfAbsent(vo.getPid(), k -> new ArrayList<>()).add(vo);
            }
        }

        // 6.2 将子文章设置到对应父文章的 children 字段
        for (HomePageArticleVo parent : parentArticles) {
            List<HomePageArticleVo> children = childrenMap.get(parent.getHomeArticleId());
            if (children != null && !children.isEmpty()) {
                parent.setChildren(children);
            }
        }

        // 7. 对父文章进行分页
        long total = parentArticles.size();
        int start = (int) ((current - 1) * size);
        int end = (int) Math.min(start + size, total);

        List<HomePageArticleVo> pagedParentArticles;
        if (start >= total) {
            pagedParentArticles = new ArrayList<>();
        } else {
            pagedParentArticles = parentArticles.subList(start, end);
        }

        log.info("分页查询首页文章列表 - Current: {}, Size: {}, Total: {}, ParentCount: {}",
                current, size, total, parentArticles.size());

        Page<HomePageArticleVo> resultPage = new Page<HomePageArticleVo>(current, size, total)
                .setRecords(pagedParentArticles);
        return PageVo.of(resultPage);
    }

    @Override
    public HomePageArticleDetailVo getArticleDetailById(Long homeArticleId) {
        // 1.校验id
        if (homeArticleId == null) {
            throw new BusinessException("参数不能为空，请重试");
        }

        // 2.查询数据库
        HomePageArticle article = homePageArticleMapper.selectById(homeArticleId);

        // 3.返回校验值
        if (article == null) {
            throw new BusinessException("数据不存在，请重试");
        }

        // 4.转 vo
        HomePageArticleDetailVo detailVo = HomePageArticleDetailVo.objToVo(article);

        log.info("根据id查询首页文章详情 id:{}", homeArticleId);

        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(CreateHomePageArticleDto createDto) {
        // 1.参数校验
        if (createDto == null) {
            throw new BusinessException("参数为空");
        }

        // 2.转换为实体对象
        HomePageArticle article = new HomePageArticle();
        BeanUtils.copyProperties(createDto, article);

        // 3.设置默认值，状态强制为 -1（待审核）
        article.setArticleStatus(-1);

        // 4.保存到数据库
        boolean saveResult = this.save(article);
        if (!saveResult) {
            throw new BusinessException("新增文章失败");
        }

        log.info("新增首页文章成功 - ArticleId: {}, Title: {}", article.getHomeArticleId(), article.getArticleTitle());

        // 5.创建审核申请记录
        HomePageArticleApply apply = new HomePageArticleApply();
        apply.setHomeArticleId(article.getHomeArticleId());
        apply.setApplyStatus(0); // 0-审核中
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());

        boolean applyResult = homePageArticleApplyService.save(apply);
        if (!applyResult) {
            throw new BusinessException("创建审核记录失败");
        }

        log.info("创建审核记录成功 - ArticleId: {}, ApplyId: {}", article.getHomeArticleId(), apply.getHomeArticleApplyId());

        return article.getHomeArticleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateArticle(UpdateHomePageArticleDto updateDto) {
        // 1.参数校验
        if (updateDto == null || updateDto.getHomeArticleId() == null) {
            throw new BusinessException("参数为空");
        }

        // 2.查询文章是否存在
        HomePageArticle existingArticle = homePageArticleMapper.selectById(updateDto.getHomeArticleId());
        if (existingArticle == null) {
            throw new BusinessException("文章不存在");
        }

        // 3.转换为实体对象
        HomePageArticle article = new HomePageArticle();
        BeanUtils.copyProperties(updateDto, article);

        // 4.修改文章后重新提交审核，状态设置为 -1（待审核）
        article.setArticleStatus(-1);

        // 5.更新数据库
        boolean updateResult = this.updateById(article);
        if (!updateResult) {
            throw new BusinessException("更新文章失败");
        }

        log.info("更新首页文章成功 - ArticleId: {}, Title: {}", article.getHomeArticleId(), article.getArticleTitle());

        // 6.创建审核申请记录
        HomePageArticleApply apply = new HomePageArticleApply();
        apply.setHomeArticleId(article.getHomeArticleId());
        apply.setApplyStatus(0); // 0-审核中
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());

        boolean applyResult = homePageArticleApplyService.save(apply);
        if (!applyResult) {
            throw new BusinessException("创建审核记录失败");
        }

        log.info("创建审核记录成功 - ArticleId: {}, ApplyId: {}", article.getHomeArticleId(), apply.getHomeArticleApplyId());

        return true;
    }

    @Override
    public PageVo<HomePageArticleVo> getMyArticlePage(QueryMyHomePageArticleListDto queryDto) {
        // 1.参数校验
        if (queryDto == null) {
            throw new BusinessException("参数为空");
        }

        // 2.获取参数
        Long current = queryDto.getCurrent();
        Long size = queryDto.getSize();
        Long publishWxId = queryDto.getPublishWxId();
        Integer articleStatus = queryDto.getArticleStatus();

        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        // 3.构造查询条件
        LambdaQueryWrapper<HomePageArticle> queryWrapper = new LambdaQueryWrapper<>();

        // 根据发布者id查询
        if (publishWxId != null) {
            queryWrapper.eq(HomePageArticle::getPublishWxId, publishWxId);
        }

        // 根据文章状态查询（可选）
        if (articleStatus != null) {
            queryWrapper.eq(HomePageArticle::getArticleStatus, articleStatus);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(HomePageArticle::getCreateTime);

        // 4.执行分页查询
        Page<HomePageArticle> articlePage = this.page(new Page<>(current, size), queryWrapper);

        // 5.转换为VO并查询封面图信息
        List<HomePageArticleVo> list = articlePage.getRecords().stream()
                .map(article -> {
                    HomePageArticleVo vo = HomePageArticleVo.objToVo(article);

                    // 查询并设置封面图信息
                    if (article.getCoverImg() != null) {
                        try {
                            Files file = fileService.getById(article.getCoverImg());
                            if (file != null) {
                                vo.setCoverImg(FilesVo.objToVo(file));
                            }
                        } catch (Exception e) {
                            log.warn("查询封面图失败 - ArticleId: {}, CoverImgId: {}, Error: {}",
                                    article.getHomeArticleId(), article.getCoverImg(), e.getMessage());
                            // 查询失败时不设置封面图，vo.coverImg 保持为 null
                        }
                    }

                    // 直接从文章表中获取发布者头像
                    // publisherAvatar 字段已在 objToVo 中通过 BeanUtils.copyProperties 自动复制

                    return vo;
                })
                .toList();

        log.info("分页查询本人首页文章列表 - PublishWxId: {}, Current: {}, Size: {}, Total: {}",
                publishWxId, current, size, articlePage.getTotal());

        Page<HomePageArticleVo> resultPage = new Page<HomePageArticleVo>(current, size, articlePage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteArticle(Long homeArticleId) {
        // 1.参数校验
        if (homeArticleId == null) {
            throw new BusinessException("文章ID不能为空");
        }

        // 2.查询文章是否存在
        HomePageArticle existingArticle = homePageArticleMapper.selectById(homeArticleId);
        if (existingArticle == null) {
            throw new BusinessException("文章不存在");
        }

        // 3.逻辑删除文章
        boolean deleteResult = this.removeById(homeArticleId);
        if (!deleteResult) {
            throw new BusinessException("删除文章失败");
        }

        log.info("删除首页文章成功 - ArticleId: {}, Title: {}", homeArticleId, existingArticle.getArticleTitle());

        return true;
    }
}
