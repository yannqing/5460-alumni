package com.cmswe.alumni.service.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.common.entity.SysTag;
import com.cmswe.alumni.common.entity.SysTagRelation;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.service.system.mapper.SysTagRelationMapper;
import com.cmswe.alumni.service.system.service.SysTagRelationService;
import com.cmswe.alumni.service.system.service.SysTagService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签关联关系表 Service 实现类
 * @author system
 */
@Slf4j
@Service
public class SysTagRelationServiceImpl extends ServiceImpl<SysTagRelationMapper, SysTagRelation> implements SysTagRelationService {

    @Resource
    private SysTagService sysTagService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTagToTarget(Long tagId, Long targetId, Integer targetType, String createBy) {
        if (tagId == null || targetId == null || targetType == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 检查标签是否存在
        SysTag tag = sysTagService.getById(tagId);
        if (tag == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "标签不存在");
        }

        // 检查是否已经关联
        LambdaQueryWrapper<SysTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTagRelation::getTagId, tagId)
                .eq(SysTagRelation::getTargetId, targetId)
                .eq(SysTagRelation::getTargetType, targetType);

        SysTagRelation existingRelation = this.getOne(queryWrapper);
        if (existingRelation != null) {
            log.info("标签关联已存在，无需重复添加");
            return true;
        }

        // 创建新的关联关系
        SysTagRelation relation = new SysTagRelation();
        relation.setTagId(tagId);
        relation.setTargetId(targetId);
        relation.setTargetType(targetType);
        relation.setCreateBy(createBy);

        return this.save(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddTagsToTarget(List<Long> tagIds, Long targetId, Integer targetType, String createBy) {
        if (tagIds == null || tagIds.isEmpty() || targetId == null || targetType == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        for (Long tagId : tagIds) {
            addTagToTarget(tagId, targetId, targetType, createBy);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeTagFromTarget(Long tagId, Long targetId, Integer targetType) {
        if (tagId == null || targetId == null || targetType == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LambdaQueryWrapper<SysTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTagRelation::getTagId, tagId)
                .eq(SysTagRelation::getTargetId, targetId)
                .eq(SysTagRelation::getTargetType, targetType);

        return this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRemoveTagsFromTarget(List<Long> tagIds, Long targetId, Integer targetType) {
        if (tagIds == null || tagIds.isEmpty() || targetId == null || targetType == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LambdaQueryWrapper<SysTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTagRelation::getTargetId, targetId)
                .eq(SysTagRelation::getTargetType, targetType)
                .in(SysTagRelation::getTagId, tagIds);

        return this.remove(queryWrapper);
    }

    @Override
    public List<SysTag> getTagsByTarget(Long targetId, Integer targetType) {
        if (targetId == null || targetType == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 查询该目标对象的所有标签关联
        LambdaQueryWrapper<SysTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTagRelation::getTargetId, targetId)
                .eq(SysTagRelation::getTargetType, targetType);

        List<SysTagRelation> relations = this.list(queryWrapper);
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有标签ID
        List<Long> tagIds = relations.stream()
                .map(SysTagRelation::getTagId)
                .collect(Collectors.toList());

        // 查询标签详情
        return sysTagService.listByIds(tagIds);
    }

    @Override
    public List<Long> getTargetIdsByTag(Long tagId, Integer targetType) {
        if (tagId == null || targetType == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LambdaQueryWrapper<SysTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTagRelation::getTagId, tagId)
                .eq(SysTagRelation::getTargetType, targetType);

        List<SysTagRelation> relations = this.list(queryWrapper);
        return relations.stream()
                .map(SysTagRelation::getTargetId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getTargetIdsByTags(List<Long> tagIds, Integer targetType) {
        if (tagIds == null || tagIds.isEmpty() || targetType == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 查询拥有这些标签的所有目标对象
        LambdaQueryWrapper<SysTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTagRelation::getTargetType, targetType)
                .in(SysTagRelation::getTagId, tagIds);

        List<SysTagRelation> relations = this.list(queryWrapper);

        // 按目标ID分组，计算每个目标拥有的标签数量
        Map<Long, Long> targetTagCountMap = relations.stream()
                .collect(Collectors.groupingBy(
                        SysTagRelation::getTargetId,
                        Collectors.counting()
                ));

        // 只返回拥有所有指定标签的目标ID（交集）
        return targetTagCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() == tagIds.size())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearTargetTags(Long targetId, Integer targetType) {
        if (targetId == null || targetType == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LambdaQueryWrapper<SysTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTagRelation::getTargetId, targetId)
                .eq(SysTagRelation::getTargetType, targetType);

        return this.remove(queryWrapper);
    }

    @Override
    public Map<Long, List<SysTag>> batchGetTagsByTargets(List<Long> targetIds, Integer targetType) {
        if (targetIds == null || targetIds.isEmpty() || targetType == null) {
            log.warn("批量查询标签参数为空");
            return new java.util.HashMap<>();
        }

        // 1. 批量查询所有目标对象的标签关联关系
        LambdaQueryWrapper<SysTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysTagRelation::getTargetId, targetIds)
                .eq(SysTagRelation::getTargetType, targetType);

        List<SysTagRelation> relations = this.list(queryWrapper);
        if (relations.isEmpty()) {
            log.debug("批量查询标签：未找到任何关联关系，targetType={}", targetType);
            return new java.util.HashMap<>();
        }

        // 2. 提取所有标签ID并批量查询标签详情
        List<Long> tagIds = relations.stream()
                .map(SysTagRelation::getTagId)
                .distinct()
                .collect(Collectors.toList());

        List<SysTag> allTags = sysTagService.listByIds(tagIds);

        // 3. 构建标签ID到标签对象的映射
        Map<Long, SysTag> tagMap = allTags.stream()
                .collect(Collectors.toMap(SysTag::getTagId, tag -> tag, (v1, v2) -> v1));

        // 4. 按目标ID分组，组装结果
        Map<Long, List<SysTag>> resultMap = relations.stream()
                .filter(relation -> tagMap.containsKey(relation.getTagId()))
                .collect(Collectors.groupingBy(
                        SysTagRelation::getTargetId,
                        Collectors.mapping(
                                relation -> tagMap.get(relation.getTagId()),
                                Collectors.toList()
                        )
                ));

        log.debug("批量查询标签完成：查询了{}个目标对象，找到{}个有标签的对象",
                targetIds.size(), resultMap.size());

        return resultMap;
    }
}
