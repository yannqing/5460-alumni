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
 * 首页公众号文章审核 Service 实现类
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

        Long applyId = approveDto.getHomeArticleApplyId();
        Integer applyStatus = approveDto.getApplyStatus();

        // 校验审核状态
        if (applyStatus == null || (applyStatus != 1 && applyStatus != 2)) {
            throw new BusinessException("审核状态不正确，必须为1（通过）或2（拒绝）");
        }

        // 2. 查询审核记录
        HomePageArticleApply apply = this.getById(applyId);
        if (apply == null) {
            throw new BusinessException("审核记录不存在");
        }

        // 3. 检查审核状态
        if (apply.getApplyStatus() != 0) {
            throw new BusinessException("该申请已被审核，无法重复审核");
        }

        // 4. 更新审核记录
        apply.setApplyStatus(applyStatus);
        apply.setAppliedWxId(approverWxId);
        apply.setAppliedName(approverName);
        apply.setAppliedDescription(approveDto.getAppliedDescription());
        apply.setCompletedTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(apply);

        // 5. 如果审核通过，更新文章状态为启用
        if (result && applyStatus == 1) {
            HomePageArticle article = homePageArticleService.getById(apply.getHomeArticleId());
            if (article != null) {
                article.setArticleStatus(1); // 1-启用
                homePageArticleService.updateById(article);
                log.info("审核通过，文章已启用 - ArticleId: {}", article.getHomeArticleId());
            }
        }

        if (result) {
            log.info("审核文章成功 - ApplyId: {}, Status: {}, Approver: {}",
                    applyId, applyStatus, approverName);
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

        // 3. 构造查询条件
        LambdaQueryWrapper<HomePageArticleApply> queryWrapper = new LambdaQueryWrapper<>();

        // 如果指定了状态，则按状态筛选；否则查询所有状态
        if (applyStatus != null) {
            queryWrapper.eq(HomePageArticleApply::getApplyStatus, applyStatus);
        }

        // 根据状态排序：待审核按创建时间降序，已审核按完成时间降序
        if (applyStatus != null && applyStatus == 0) {
            queryWrapper.orderByDesc(HomePageArticleApply::getCreateTime);
        } else if (applyStatus != null && (applyStatus == 1 || applyStatus == 2)) {
            queryWrapper.orderByDesc(HomePageArticleApply::getCompletedTime);
        } else {
            // 查询所有状态时，按创建时间降序
            queryWrapper.orderByDesc(HomePageArticleApply::getCreateTime);
        }

        // 4. 执行分页查询
        Page<HomePageArticleApply> applyPage = this.page(new Page<>(current, size), queryWrapper);
        List<HomePageArticleApplyVo> list = applyPage.getRecords().stream()
                .map(apply -> {
                    HomePageArticleApplyVo vo = HomePageArticleApplyVo.objToVo(apply);
                    // 填充文章信息
                    if (apply.getHomeArticleId() != null) {
                        HomePageArticle article = homePageArticleService.getById(apply.getHomeArticleId());
                        if (article != null) {
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
                        }
                    }
                    // 填充审批人信息
                    if (apply.getAppliedWxId() != null) {
                        try {
                            WxUserInfo user = wxUserInfoService.getById(apply.getAppliedWxId());
                            if (user != null) {
                                vo.setAppliedUserInfo(WxUserInfoVo.objToVo(user));
                            }
                        } catch (Exception e) {
                            log.warn("查询审批人信息失败 - AppliedWxId: {}, Error: {}",
                                    apply.getAppliedWxId(), e.getMessage());
                        }
                    }
                    return vo;
                })
                .toList();

        String statusDesc = applyStatus == null ? "全部"
                : (applyStatus == 0 ? "待审核" : (applyStatus == 1 ? "审核通过" : "审核拒绝"));
        log.info("分页查询审核记录列表 - Current: {}, Size: {}, Status: {}, Total: {}",
                current, size, statusDesc, applyPage.getTotal());

        Page<HomePageArticleApplyVo> resultPage = new Page<HomePageArticleApplyVo>(current, size, applyPage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }
}
