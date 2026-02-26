package com.cmswe.alumni.service.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.system.HomePageArticleApplyService;
import com.cmswe.alumni.api.system.HomePageArticleService;
import com.cmswe.alumni.api.user.FileService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.dto.ApproveArticleDto;
import com.cmswe.alumni.common.dto.QueryArticleApplyListDto;
import com.cmswe.alumni.common.entity.Files;
import com.cmswe.alumni.common.entity.HomePageArticle;
import com.cmswe.alumni.common.entity.HomePageArticleApply;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.FilesVo;
import com.cmswe.alumni.common.vo.HomePageArticleApplyVo;
import com.cmswe.alumni.common.vo.HomePageArticleVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.WxUserInfoVo;
import com.cmswe.alumni.service.system.mapper.HomePageArticleApplyMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 首页公众号文章审核 Service 实现类（已合并审核表到文章表）
 */
@Slf4j
@Service
public class HomePageArticleApplyServiceImpl extends ServiceImpl<HomePageArticleApplyMapper, HomePageArticleApply>
        implements HomePageArticleApplyService {

    @Resource
    private HomePageArticleApplyMapper homePageArticleApplyMapper;

    @Resource
    private HomePageArticleService homePageArticleService;

    @Resource
    private FileService fileService;

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveArticle(ApproveArticleDto approveDto, Long approverWxId, String approverName) {
        // 1. 参数校验
        if (approveDto == null || approveDto.getHomeArticleApplyId() == null) {
            throw new BusinessException("参数不能为空");
        }

        // 注意：这里的 homeArticleApplyId 现在实际上是文章ID
        Long articleId = approveDto.getHomeArticleApplyId();
        Integer applyStatus = approveDto.getApplyStatus();

        // 校验审核状态
        if (applyStatus == null || (applyStatus != 1 && applyStatus != 2)) {
            throw new BusinessException("审核状态不正确，必须为1（通过）或2（拒绝）");
        }

        // 2. 查询文章
        HomePageArticle article = homePageArticleService.getById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 3. 检查审核状态
        if (article.getApplyStatus() != null && article.getApplyStatus() != 0) {
            throw new BusinessException("该文章已被审核，无法重复审核");
        }

        // 4. 更新文章的审核信息
        article.setApplyStatus(applyStatus);
        article.setReviewerWxId(approverWxId);
        article.setReviewerName(approverName);
        article.setReviewOpinion(approveDto.getAppliedDescription());
        article.setReviewedTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        // 5. 如果审核通过，同时更新文章状态为启用
        if (applyStatus == 1) {
            article.setArticleStatus(1); // 1-启用
            log.info("审核通过，文章已启用 - ArticleId: {}", articleId);
        } else {
            // 审核拒绝，设置文章状态为禁用
            article.setArticleStatus(0); // 0-禁用
            log.info("审核拒绝，文章已禁用 - ArticleId: {}", articleId);
        }

        boolean result = homePageArticleService.updateById(article);

        if (result) {
            log.info("审核文章成功 - ArticleId: {}, Status: {}, Approver: {}",
                    articleId, applyStatus, approverName);
        }

        return result;
    }

    @Override
    public PageVo<HomePageArticleApplyVo> getApplyList(QueryArticleApplyListDto queryDto) {
        // 1. 参数校验
        if (queryDto == null) {
            throw new BusinessException("参数为空");
        }

        // 2. 获取参数
        Long current = queryDto.getCurrent();
        Long size = queryDto.getSize();
        Integer applyStatus = queryDto.getApplyStatus();

        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        // 3. 构造查询条件 - 从文章表查询
        LambdaQueryWrapper<HomePageArticle> queryWrapper = new LambdaQueryWrapper<>();

        // 如果指定了状态，则按状态筛选；否则查询所有状态
        if (applyStatus != null) {
            queryWrapper.eq(HomePageArticle::getApplyStatus, applyStatus);
        } else {
            // 查询所有审核状态（包括待审核、通过、拒绝）
            queryWrapper.in(HomePageArticle::getApplyStatus, 0, 1, 2);
        }

        // 根据状态排序：待审核按创建时间降序，已审核按审核完成时间降序
        if (applyStatus != null && applyStatus == 0) {
            queryWrapper.orderByDesc(HomePageArticle::getCreateTime);
        } else if (applyStatus != null && (applyStatus == 1 || applyStatus == 2)) {
            queryWrapper.orderByDesc(HomePageArticle::getReviewedTime);
        } else {
            // 查询所有状态时，按创建时间降序
            queryWrapper.orderByDesc(HomePageArticle::getCreateTime);
        }

        // 添加主键排序，避免分页重复
        queryWrapper.orderByDesc(HomePageArticle::getHomeArticleId);

        // 4. 执行分页查询
        Page<HomePageArticle> articlePage = homePageArticleService.page(new Page<>(current, size), queryWrapper);
        List<HomePageArticleApplyVo> list = articlePage.getRecords().stream()
                .map(article -> {
                    // 将文章转换为审核VO
                    HomePageArticleApplyVo vo = new HomePageArticleApplyVo();
                    vo.setHomeArticleApplyId(article.getHomeArticleId()); // 使用文章ID作为审核ID
                    vo.setApplyStatus(article.getApplyStatus());
                    vo.setAppliedWxId(article.getReviewerWxId()); // Long 类型直接设置
                    vo.setAppliedName(article.getReviewerName());
                    vo.setAppliedDescription(article.getReviewOpinion());
                    vo.setCompletedTime(article.getReviewedTime());
                    vo.setCreateTime(article.getCreateTime());

                    // 填充文章信息
                    HomePageArticleVo articleVo = HomePageArticleVo.objToVo(article);
                    // 填充封面图信息
                    if (article.getCoverImg() != null) {
                        try {
                            Files file = fileService.getById(article.getCoverImg());
                            if (file != null) {
                                articleVo.setCoverImg(FilesVo.objToVo(file));
                            }
                        } catch (Exception e) {
                            log.warn("查询文章封面图失败 - ArticleId: {}, Error: {}",
                                    article.getHomeArticleId(), e.getMessage());
                        }
                    }
                    vo.setArticleInfo(articleVo);

                    // 填充审批人信息
                    if (article.getReviewerWxId() != null) {
                        try {
                            WxUserInfo user = wxUserInfoService.getById(article.getReviewerWxId());
                            if (user != null) {
                                vo.setAppliedUserInfo(WxUserInfoVo.objToVo(user));
                            }
                        } catch (Exception e) {
                            log.warn("查询审批人信息失败 - ReviewerWxId: {}, Error: {}",
                                    article.getReviewerWxId(), e.getMessage());
                        }
                    }
                    return vo;
                })
                .toList();

        String statusDesc = applyStatus == null ? "全部"
                : (applyStatus == 0 ? "待审核" : (applyStatus == 1 ? "审核通过" : "审核拒绝"));
        log.info("分页查询审核记录列表 - Current: {}, Size: {}, Status: {}, Total: {}",
                current, size, statusDesc, articlePage.getTotal());

        Page<HomePageArticleApplyVo> resultPage = new Page<HomePageArticleApplyVo>(current, size, articlePage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }
}
