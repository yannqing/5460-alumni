package com.cmswe.alumni.service.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.system.HomePageArticleApplyService;
import com.cmswe.alumni.api.system.HomePageArticleService;
import com.cmswe.alumni.api.user.FileService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.user.RoleUserService;
import com.cmswe.alumni.common.dto.CreateChildArticleDto;
import com.cmswe.alumni.common.dto.CreateHomePageArticleDto;
import com.cmswe.alumni.common.dto.QueryHomePageArticleListDto;
import com.cmswe.alumni.common.dto.QueryMyHomePageArticleListDto;
import com.cmswe.alumni.common.dto.QueryOrganizationArticleListDto;
import com.cmswe.alumni.common.dto.UpdateChildArticleDto;
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
import java.util.Objects;
import java.util.Set;
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

    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Resource
    private LocalPlatformService localPlatformService;

    @Resource
    private RoleService roleService;

    @Resource
    private RoleUserService roleUserService;

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

        // 强制只查询展示在首页的文章
        queryWrapper.eq(HomePageArticle::getShowOnHomepage, 1);

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

        // 5.查询子文章列表（如果是父文章）
        if (article.getPid() == 0L) {
            List<HomePageArticle> childArticles = this.list(
                    new LambdaQueryWrapper<HomePageArticle>()
                            .eq(HomePageArticle::getPid, homeArticleId)
                            .orderByAsc(HomePageArticle::getCreateTime)
            );

            if (!childArticles.isEmpty()) {
                List<HomePageArticleVo> childrenVoList = childArticles.stream()
                        .map(childArticle -> {
                            HomePageArticleVo childVo = HomePageArticleVo.objToVo(childArticle);

                            // 查询并设置封面图信息
                            if (childArticle.getCoverImg() != null) {
                                try {
                                    Files file = fileService.getById(childArticle.getCoverImg());
                                    if (file != null) {
                                        childVo.setCoverImg(FilesVo.objToVo(file));
                                    }
                                } catch (Exception e) {
                                    log.warn("查询子文章封面图失败 - ArticleId: {}, CoverImgId: {}, Error: {}",
                                            childArticle.getHomeArticleId(), childArticle.getCoverImg(), e.getMessage());
                                }
                            }

                            return childVo;
                        })
                        .toList();

                detailVo.setChildren(childrenVoList);
                log.info("查询到子文章 - ParentArticleId: {}, ChildCount: {}", homeArticleId, childrenVoList.size());
            }
        }

        log.info("根据id查询首页文章详情 id:{}", homeArticleId);

        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(CreateHomePageArticleDto createDto, Long actualPublisherWxId) {
        // 1.参数校验
        if (createDto == null) {
            throw new BusinessException("参数为空");
        }

        // 2.转换为实体对象（父文章）
        HomePageArticle parentArticle = new HomePageArticle();
        BeanUtils.copyProperties(createDto, parentArticle, "childArticles");

        // 3.设置父文章的默认值
        parentArticle.setPid(0L); // 父文章的 pid 为 0
        parentArticle.setArticleStatus(0); // 0-禁用（待审核通过后启用）
        parentArticle.setApplyStatus(0);   // 0-待审核
        parentArticle.setActualPublisherWxId(actualPublisherWxId); // 设置实际发布用户ID

        // 设置是否展示在首页，如果前端未传递则默认为0（不展示）
        if (parentArticle.getShowOnHomepage() == null) {
            parentArticle.setShowOnHomepage(0);
        }

        // 3.5 检查首页发布配额（如果选择展示在首页）
        // 注意：这里只检查配额是否足够，不扣除。配额将在审核通过时扣除
        if (parentArticle.getShowOnHomepage() != null && parentArticle.getShowOnHomepage() == 1) {
            checkQuotaAvailable(parentArticle.getPublishType(), parentArticle.getPublishWxId());
        }

        // 4.保存父文章到数据库
        boolean saveResult = this.save(parentArticle);
        if (!saveResult) {
            throw new BusinessException("新增父文章失败");
        }

        Long parentArticleId = parentArticle.getHomeArticleId();
        log.info("新增首页文章成功（待审核）- ArticleId: {}, Title: {}, ActualPublisher: {}",
                parentArticleId, parentArticle.getArticleTitle(), actualPublisherWxId);

        // 5.批量保存子文章（如果有）
        if (createDto.getChildArticles() != null && !createDto.getChildArticles().isEmpty()) {
            List<HomePageArticle> childArticles = new ArrayList<>();

            for (var childDto : createDto.getChildArticles()) {
                HomePageArticle childArticle = new HomePageArticle();
                BeanUtils.copyProperties(childDto, childArticle);

                // 子文章继承父文章的关键信息
                childArticle.setPid(parentArticleId); // 设置父文章ID
                childArticle.setPublishWxId(parentArticle.getPublishWxId());
                childArticle.setPublishUsername(parentArticle.getPublishUsername());
                childArticle.setPublishType(parentArticle.getPublishType());
                childArticle.setPublisherAvatar(parentArticle.getPublisherAvatar());
                childArticle.setActualPublisherWxId(actualPublisherWxId);
                childArticle.setShowOnHomepage(parentArticle.getShowOnHomepage()); // 继承父文章的首页展示设置

                // 子文章的审核状态与父文章一致
                childArticle.setArticleStatus(0); // 0-禁用（待审核通过后启用）
                childArticle.setApplyStatus(0);   // 0-待审核

                childArticles.add(childArticle);
            }

            // 批量保存子文章
            boolean batchSaveResult = this.saveBatch(childArticles);
            if (!batchSaveResult) {
                throw new BusinessException("批量保存子文章失败");
            }

            log.info("批量保存子文章成功 - ParentArticleId: {}, 子文章数量: {}",
                    parentArticleId, childArticles.size());
        }

        return parentArticleId;
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

        // 3.转换为实体对象（父文章）
        HomePageArticle article = new HomePageArticle();
        BeanUtils.copyProperties(updateDto, article, "childArticles");

        // 保留原有的实际发布者ID（不允许修改）
        article.setActualPublisherWxId(existingArticle.getActualPublisherWxId());

        // 4.修改文章后重新提交审核
        article.setArticleStatus(0);  // 0-禁用（待审核通过后启用）
        article.setApplyStatus(0);    // 0-待审核
        // 清空之前的审核信息
        article.setReviewerWxId(null);
        article.setReviewerName(null);
        article.setReviewOpinion(null);
        article.setReviewedTime(null);

        // 4.5 处理首页展示配额变更（如果从不展示改为展示）
        // 注意：这里只检查配额是否足够，不扣除。配额将在审核通过时扣除
        boolean needCheckQuota = false;
        if (updateDto.getShowOnHomepage() != null && updateDto.getShowOnHomepage() == 1) {
            // 如果原来不展示，现在要展示，需要检查配额
            if (existingArticle.getShowOnHomepage() == null || existingArticle.getShowOnHomepage() == 0) {
                needCheckQuota = true;
            }
        }

        if (needCheckQuota) {
            checkQuotaAvailable(article.getPublishType(), article.getPublishWxId());
        }

        // 5.更新父文章到数据库
        boolean updateResult = this.updateById(article);
        if (!updateResult) {
            throw new BusinessException("更新父文章失败");
        }

        log.info("更新首页文章成功（重新提交审核）- ArticleId: {}, Title: {}", article.getHomeArticleId(), article.getArticleTitle());

        // 6.处理子文章（如果有）
        if (updateDto.getChildArticles() != null) {
            Long parentArticleId = updateDto.getHomeArticleId();

            // 6.1 查询现有的所有子文章
            List<HomePageArticle> existingChildren = this.list(
                    new LambdaQueryWrapper<HomePageArticle>()
                            .eq(HomePageArticle::getPid, parentArticleId)
            );

            // 6.2 收集要保留/更新的子文章ID
            Set<Long> childIdsInDto = updateDto.getChildArticles().stream()
                    .map(UpdateChildArticleDto::getHomeArticleId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 6.3 删除不在更新列表中的子文章（逻辑删除）
            List<Long> childIdsToDelete = existingChildren.stream()
                    .map(HomePageArticle::getHomeArticleId)
                    .filter(id -> !childIdsInDto.contains(id))
                    .collect(Collectors.toList());

            if (!childIdsToDelete.isEmpty()) {
                boolean deleteResult = this.removeByIds(childIdsToDelete);
                if (!deleteResult) {
                    throw new BusinessException("删除旧子文章失败");
                }
                log.info("删除不在更新列表中的子文章 - ParentArticleId: {}, 删除数量: {}", parentArticleId, childIdsToDelete.size());
            }

            // 6.4 处理每个子文章（更新或新增）
            List<HomePageArticle> childrenToUpdate = new ArrayList<>();
            List<HomePageArticle> childrenToInsert = new ArrayList<>();

            for (var childDto : updateDto.getChildArticles()) {
                HomePageArticle childArticle = new HomePageArticle();
                BeanUtils.copyProperties(childDto, childArticle);

                // 子文章继承父文章的关键信息
                childArticle.setPid(parentArticleId);
                childArticle.setPublishWxId(article.getPublishWxId());
                childArticle.setPublishUsername(article.getPublishUsername());
                childArticle.setPublishType(article.getPublishType());
                childArticle.setPublisherAvatar(article.getPublisherAvatar());
                childArticle.setActualPublisherWxId(article.getActualPublisherWxId());
                childArticle.setShowOnHomepage(article.getShowOnHomepage());

                // 子文章的审核状态与父文章一致（重新审核）
                childArticle.setArticleStatus(0);
                childArticle.setApplyStatus(0);
                // 清空审核信息
                childArticle.setReviewerWxId(null);
                childArticle.setReviewerName(null);
                childArticle.setReviewOpinion(null);
                childArticle.setReviewedTime(null);

                if (childDto.getHomeArticleId() != null) {
                    // 有ID，更新现有子文章
                    childrenToUpdate.add(childArticle);
                } else {
                    // 无ID，新增子文章
                    childrenToInsert.add(childArticle);
                }
            }

            // 6.5 批量更新子文章
            if (!childrenToUpdate.isEmpty()) {
                boolean batchUpdateResult = this.updateBatchById(childrenToUpdate);
                if (!batchUpdateResult) {
                    throw new BusinessException("批量更新子文章失败");
                }
                log.info("批量更新子文章成功 - ParentArticleId: {}, 更新数量: {}", parentArticleId, childrenToUpdate.size());
            }

            // 6.6 批量新增子文章
            if (!childrenToInsert.isEmpty()) {
                boolean batchInsertResult = this.saveBatch(childrenToInsert);
                if (!batchInsertResult) {
                    throw new BusinessException("批量新增子文章失败");
                }
                log.info("批量新增子文章成功 - ParentArticleId: {}, 新增数量: {}", parentArticleId, childrenToInsert.size());
            }
        }

        return true;
    }

    @Override
    public PageVo<HomePageArticleVo> getMyArticlePage(QueryMyHomePageArticleListDto queryDto, Long currentUserWxId) {
        // 1.参数校验
        if (queryDto == null) {
            throw new BusinessException("参数为空");
        }
        if (currentUserWxId == null) {
            throw new BusinessException("当前用户ID不能为空");
        }

        // 2.获取参数
        Long current = queryDto.getCurrent();
        Long size = queryDto.getSize();
        String articleTitle = queryDto.getArticleTitle();
        Integer articleStatus = queryDto.getArticleStatus();
        List<Integer> applyStatusList = queryDto.getApplyStatusList();

        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        // 3.查询用户的角色
        List<Role> userRoles = roleService.getRolesByUserId(currentUserWxId);

        // 4.判断是否是系统管理员
        boolean isSystemAdmin = userRoles.stream()
                .anyMatch(role -> "SYSTEM_SUPER_ADMIN".equals(role.getRoleCode()));

        // 5.构造查询条件
        LambdaQueryWrapper<HomePageArticle> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询父文章（pid=0）
        queryWrapper.eq(HomePageArticle::getPid, 0L);

        if (isSystemAdmin) {
            // 系统管理员：查询所有文章（不添加发布者限制）
            log.info("系统管理员查询所有文章 - UserId: {}", currentUserWxId);
        } else {
            // 非系统管理员：查询有权限管理的组织的文章
            List<Long> authorizedOrganizationIds = getAuthorizedOrganizationIds(currentUserWxId);

            if (authorizedOrganizationIds.isEmpty()) {
                // 没有管理任何组织，返回空列表
                log.info("用户无管理权限的组织 - UserId: {}", currentUserWxId);
                Page<HomePageArticleVo> emptyPage = new Page<>(current, size, 0);
                return PageVo.of(emptyPage);
            }

            // 限制查询有权限的组织的文章
            queryWrapper.in(HomePageArticle::getPublishWxId, authorizedOrganizationIds);
            log.info("查询有权限管理的组织的文章 - UserId: {}, OrganizationIds: {}",
                    currentUserWxId, authorizedOrganizationIds);
        }

        // 根据文章标题模糊搜索（可选）
        if (articleTitle != null && !articleTitle.trim().isEmpty()) {
            queryWrapper.like(HomePageArticle::getArticleTitle, articleTitle.trim());
        }

        // 根据文章状态查询（可选）
        if (articleStatus != null) {
            queryWrapper.eq(HomePageArticle::getArticleStatus, articleStatus);
        }

        // 根据审核状态查询（可选，支持多选）
        if (applyStatusList != null && !applyStatusList.isEmpty()) {
            queryWrapper.in(HomePageArticle::getApplyStatus, applyStatusList);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(HomePageArticle::getCreateTime);

        // 6.执行分页查询
        Page<HomePageArticle> articlePage = this.page(new Page<>(current, size), queryWrapper);

        // 7.转换为VO并查询封面图信息
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

                    return vo;
                })
                .toList();

        log.info("分页查询有权限管理的文章列表 - UserId: {}, Current: {}, Size: {}, Total: {}",
                currentUserWxId, current, size, articlePage.getTotal());

        Page<HomePageArticleVo> resultPage = new Page<HomePageArticleVo>(current, size, articlePage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    /**
     * 获取用户有权限管理的组织ID列表
     *
     * @param wxId 用户ID
     * @return 组织ID列表（包括校友会和校促会）
     */
    private List<Long> getAuthorizedOrganizationIds(Long wxId) {
        List<Long> organizationIds = new ArrayList<>();

        // 查询用户管理的所有组织（从 role_user 表）
        List<RoleUser> roleUsers = roleUserService.list(
                new LambdaQueryWrapper<RoleUser>()
                        .eq(RoleUser::getWxId, wxId)
                        .isNotNull(RoleUser::getOrganizeId)
        );

        // 按组织类型分组
        Map<Integer, List<Long>> organizationsByType = roleUsers.stream()
                .collect(Collectors.groupingBy(
                        RoleUser::getType,
                        Collectors.mapping(RoleUser::getOrganizeId, Collectors.toList())
                ));

        // 处理校友会（type=2）
        List<Long> associationIds = organizationsByType.getOrDefault(2, new ArrayList<>());
        organizationIds.addAll(associationIds);

        // 处理校促会（type=1）
        List<Long> platformIds = organizationsByType.getOrDefault(1, new ArrayList<>());
        organizationIds.addAll(platformIds);

        // 如果用户管理校促会，还需要包括该校促会下所有校友会的ID
        if (!platformIds.isEmpty()) {
            List<AlumniAssociation> associationsUnderPlatforms = alumniAssociationService.list(
                    new LambdaQueryWrapper<AlumniAssociation>()
                            .in(AlumniAssociation::getPlatformId, platformIds)
                            .eq(AlumniAssociation::getStatus, 1) // 只查询启用的校友会
            );

            List<Long> associationIdsUnderPlatforms = associationsUnderPlatforms.stream()
                    .map(AlumniAssociation::getAlumniAssociationId)
                    .collect(Collectors.toList());

            organizationIds.addAll(associationIdsUnderPlatforms);

            log.info("用户管理的校促会及其下校友会 - UserId: {}, PlatformIds: {}, AssociationIds: {}",
                    wxId, platformIds, associationIdsUnderPlatforms);
        }

        // 去重
        return organizationIds.stream().distinct().collect(Collectors.toList());
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

    /**
     * 检查首页文章发布配额是否可用（不扣除）
     *
     * @param publishType 发布者类型（ASSOCIATION-校友会，LOCAL_PLATFORM-校促会）
     * @param publishWxId 发布者ID
     */
    private void checkQuotaAvailable(String publishType, Long publishWxId) {
        if (publishType == null || publishWxId == null) {
            throw new BusinessException("发布者类型和ID不能为空");
        }

        // 统一转换为大写，支持大小写不敏感
        String normalizedPublishType = publishType.toUpperCase();

        if ("ASSOCIATION".equals(normalizedPublishType)) {
            // 校友会
            AlumniAssociation association = alumniAssociationService.getById(publishWxId);
            if (association == null) {
                throw new BusinessException("校友会不存在");
            }

            Integer quota = association.getMonthlyHomepageArticleQuota();
            if (quota == null || quota <= 0) {
                throw new BusinessException("首页文章发布次数不足，请联系管理员增加配额");
            }

            log.info("校友会首页文章配额检查通过 - AssociationId: {}, 当前配额: {}",
                    publishWxId, quota);

        } else if ("LOCAL_PLATFORM".equals(normalizedPublishType)) {
            // 校促会
            LocalPlatform platform = localPlatformService.getById(publishWxId);
            if (platform == null) {
                throw new BusinessException("校促会不存在");
            }

            Integer quota = platform.getMonthlyHomepageArticleQuota();
            if (quota == null || quota <= 0) {
                throw new BusinessException("首页文章发布次数不足，请联系管理员增加配额");
            }

            log.info("校促会首页文章配额检查通过 - PlatformId: {}, 当前配额: {}",
                    publishWxId, quota);

        } else {
            // 不支持的发布者类型，抛出异常
            throw new BusinessException("不支持的发布者类型: " + publishType + "，仅支持 ASSOCIATION（校友会）或 LOCAL_PLATFORM（校促会）");
        }
    }

    /**
     * 检查并扣减首页文章发布配额
     *
     * @param publishType 发布者类型（ASSOCIATION-校友会，LOCAL_PLATFORM-校促会）
     * @param publishWxId 发布者ID
     */
    public void checkAndDeductQuota(String publishType, Long publishWxId) {
        if (publishType == null || publishWxId == null) {
            throw new BusinessException("发布者类型和ID不能为空");
        }

        // 统一转换为大写，支持大小写不敏感
        String normalizedPublishType = publishType.toUpperCase();

        if ("ASSOCIATION".equals(normalizedPublishType)) {
            // 校友会
            AlumniAssociation association = alumniAssociationService.getById(publishWxId);
            if (association == null) {
                throw new BusinessException("校友会不存在");
            }

            Integer quota = association.getMonthlyHomepageArticleQuota();
            if (quota == null || quota <= 0) {
                throw new BusinessException("首页文章发布次数不足，请联系管理员增加配额");
            }

            // 扣减配额
            association.setMonthlyHomepageArticleQuota(quota - 1);
            boolean updateResult = alumniAssociationService.updateById(association);
            if (!updateResult) {
                throw new BusinessException("扣减配额失败");
            }

            log.info("校友会首页文章配额扣减成功 - AssociationId: {}, 剩余配额: {}",
                    publishWxId, quota - 1);

        } else if ("LOCAL_PLATFORM".equals(normalizedPublishType)) {
            // 校促会
            LocalPlatform platform = localPlatformService.getById(publishWxId);
            if (platform == null) {
                throw new BusinessException("校促会不存在");
            }

            Integer quota = platform.getMonthlyHomepageArticleQuota();
            if (quota == null || quota <= 0) {
                throw new BusinessException("首页文章发布次数不足，请联系管理员增加配额");
            }

            // 扣减配额
            platform.setMonthlyHomepageArticleQuota(quota - 1);
            boolean updateResult = localPlatformService.updateById(platform);
            if (!updateResult) {
                throw new BusinessException("扣减配额失败");
            }

            log.info("校促会首页文章配额扣减成功 - PlatformId: {}, 剩余配额: {}",
                    publishWxId, quota - 1);

        } else {
            // 不支持的发布者类型，抛出异常
            throw new BusinessException("不支持的发布者类型: " + publishType + "，仅支持 ASSOCIATION（校友会）或 LOCAL_PLATFORM（校促会）");
        }
    }

    @Override
    public PageVo<HomePageArticleVo> getAssociationArticlePage(QueryOrganizationArticleListDto queryDto) {
        // 1.参数校验
        if (queryDto == null || queryDto.getOrganizationId() == null) {
            throw new BusinessException("参数不能为空");
        }

        // 2.获取参数
        Long current = queryDto.getCurrent();
        Long size = queryDto.getSize();
        Long organizationId = queryDto.getOrganizationId();
        String articleTitle = queryDto.getArticleTitle();

        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        // 3.构造查询条件
        LambdaQueryWrapper<HomePageArticle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(HomePageArticle::getPublishType, "ASSOCIATION") // 发布者类型：校友会
                .eq(HomePageArticle::getPublishWxId, organizationId) // 发布者ID
                .eq(HomePageArticle::getArticleStatus, 1) // 状态：1-启用
                .eq(HomePageArticle::getApplyStatus, 1) // 审核状态：1-审核通过
                .eq(HomePageArticle::getPid, 0L); // 只查询父文章

        // 根据文章标题模糊搜索（可选）
        if (articleTitle != null && !articleTitle.trim().isEmpty()) {
            queryWrapper.like(HomePageArticle::getArticleTitle, articleTitle.trim());
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
                        }
                    }

                    return vo;
                })
                .toList();

        log.info("分页查询校友会文章列表 - OrganizationId: {}, Current: {}, Size: {}, Total: {}",
                organizationId, current, size, articlePage.getTotal());

        Page<HomePageArticleVo> resultPage = new Page<HomePageArticleVo>(current, size, articlePage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public PageVo<HomePageArticleVo> getPlatformArticlePage(QueryOrganizationArticleListDto queryDto) {
        // 1.参数校验
        if (queryDto == null || queryDto.getOrganizationId() == null) {
            throw new BusinessException("参数不能为空");
        }

        // 2.获取参数
        Long current = queryDto.getCurrent();
        Long size = queryDto.getSize();
        Long organizationId = queryDto.getOrganizationId();
        String articleTitle = queryDto.getArticleTitle();

        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        // 3.构造查询条件
        LambdaQueryWrapper<HomePageArticle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(HomePageArticle::getPublishType, "LOCAL_PLATFORM") // 发布者类型：校促会
                .eq(HomePageArticle::getPublishWxId, organizationId) // 发布者ID
                .eq(HomePageArticle::getArticleStatus, 1) // 状态：1-启用
                .eq(HomePageArticle::getApplyStatus, 1) // 审核状态：1-审核通过
                .eq(HomePageArticle::getPid, 0L); // 只查询父文章

        // 根据文章标题模糊搜索（可选）
        if (articleTitle != null && !articleTitle.trim().isEmpty()) {
            queryWrapper.like(HomePageArticle::getArticleTitle, articleTitle.trim());
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
                        }
                    }

                    return vo;
                })
                .toList();

        log.info("分页查询校促会文章列表 - OrganizationId: {}, Current: {}, Size: {}, Total: {}",
                organizationId, current, size, articlePage.getTotal());

        Page<HomePageArticleVo> resultPage = new Page<HomePageArticleVo>(current, size, articlePage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public HomePageArticleDetailVo getPublishedArticleDetailById(Long homeArticleId) {
        // 1.校验id
        if (homeArticleId == null) {
            throw new BusinessException("参数不能为空，请重试");
        }

        // 2.查询数据库
        HomePageArticle article = homePageArticleMapper.selectById(homeArticleId);

        // 3.返回校验值
        if (article == null) {
            throw new BusinessException("文章不存在，请重试");
        }

        // 4.校验文章状态（必须是已发布且审核通过的）
        if (article.getArticleStatus() == null || article.getArticleStatus() != 1) {
            throw new BusinessException("文章未发布或已禁用");
        }
        if (article.getApplyStatus() == null || article.getApplyStatus() != 1) {
            throw new BusinessException("文章未通过审核");
        }

        // 5.转 vo
        HomePageArticleDetailVo detailVo = HomePageArticleDetailVo.objToVo(article);

        // 6.查询子文章列表（如果是父文章）
        if (article.getPid() == 0L) {
            List<HomePageArticle> childArticles = this.list(
                    new LambdaQueryWrapper<HomePageArticle>()
                            .eq(HomePageArticle::getPid, homeArticleId)
                            .eq(HomePageArticle::getArticleStatus, 1) // 子文章也必须是已发布的
                            .eq(HomePageArticle::getApplyStatus, 1) // 子文章也必须是审核通过的
                            .orderByAsc(HomePageArticle::getCreateTime)
            );

            if (!childArticles.isEmpty()) {
                List<HomePageArticleVo> childrenVoList = childArticles.stream()
                        .map(childArticle -> {
                            HomePageArticleVo childVo = HomePageArticleVo.objToVo(childArticle);

                            // 查询并设置封面图信息
                            if (childArticle.getCoverImg() != null) {
                                try {
                                    Files file = fileService.getById(childArticle.getCoverImg());
                                    if (file != null) {
                                        childVo.setCoverImg(FilesVo.objToVo(file));
                                    }
                                } catch (Exception e) {
                                    log.warn("查询子文章封面图失败 - ArticleId: {}, CoverImgId: {}, Error: {}",
                                            childArticle.getHomeArticleId(), childArticle.getCoverImg(), e.getMessage());
                                }
                            }

                            return childVo;
                        })
                        .toList();

                detailVo.setChildren(childrenVoList);
                log.info("查询到已发布子文章 - ParentArticleId: {}, ChildCount: {}", homeArticleId, childrenVoList.size());
            }
        }

        log.info("根据id查询已发布文章详情 id:{}", homeArticleId);

        return detailVo;
    }
}
