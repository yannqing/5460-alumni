package com.cmswe.alumni.service.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.common.entity.SysTag;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.service.system.mapper.SysTagMapper;
import com.cmswe.alumni.service.system.service.SysTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统标签定义表 Service 实现类
 * @author system
 */
@Slf4j
@Service
public class SysTagServiceImpl extends ServiceImpl<SysTagMapper, SysTag> implements SysTagService {

    @Override
    public List<SysTag> getTagsByCategory(Integer category) {
        if (category == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LambdaQueryWrapper<SysTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTag::getCategory, category)
                .orderByAsc(SysTag::getSortOrder)
                .orderByAsc(SysTag::getCreateTime);

        return this.list(queryWrapper);
    }

    @Override
    public List<SysTag> getTagsByParentId(Long parentId) {
        if (parentId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LambdaQueryWrapper<SysTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTag::getParentId, parentId)
                .orderByAsc(SysTag::getSortOrder)
                .orderByAsc(SysTag::getCreateTime);

        return this.list(queryWrapper);
    }

    @Override
    public SysTag getTagByCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        LambdaQueryWrapper<SysTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTag::getCode, code);

        return this.getOne(queryWrapper);
    }

    @Override
    public boolean createTag(SysTag sysTag) {
        if (sysTag == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        // 检查标签代码是否已存在
        SysTag existingTag = getTagByCode(sysTag.getCode());
        if (existingTag != null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "标签代码已存在");
        }

        return this.save(sysTag);
    }

    @Override
    public boolean updateTag(SysTag sysTag) {
        if (sysTag == null || sysTag.getTagId() == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        SysTag existing = this.getById(sysTag.getTagId());
        if (existing == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "标签不存在");
        }

        // 如果修改了标签代码，检查新代码是否已存在
        if (sysTag.getCode() != null && !sysTag.getCode().equals(existing.getCode())) {
            SysTag existingByCode = getTagByCode(sysTag.getCode());
            if (existingByCode != null && !existingByCode.getTagId().equals(sysTag.getTagId())) {
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "标签代码已存在");
            }
        }

        return this.updateById(sysTag);
    }

    @Override
    public boolean deleteTag(Long tagId) {
        if (tagId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        SysTag existing = this.getById(tagId);
        if (existing == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "标签不存在");
        }

        // 检查是否有子标签
        LambdaQueryWrapper<SysTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTag::getParentId, tagId);
        long childCount = this.count(queryWrapper);

        if (childCount > 0) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "存在子标签，无法删除");
        }

        return this.removeById(tagId);
    }
}
