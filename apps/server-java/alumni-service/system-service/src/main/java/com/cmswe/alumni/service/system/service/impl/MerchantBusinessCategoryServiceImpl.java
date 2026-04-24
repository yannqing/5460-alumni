package com.cmswe.alumni.service.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.system.MerchantBusinessCategoryService;
import com.cmswe.alumni.common.entity.MerchantBusinessCategory;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.MerchantBusinessCategoryVo;
import com.cmswe.alumni.service.system.mapper.MerchantBusinessCategoryMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商户经营类目及范围 Service 实现类
 */
@Service
public class MerchantBusinessCategoryServiceImpl extends ServiceImpl<MerchantBusinessCategoryMapper, MerchantBusinessCategory> implements MerchantBusinessCategoryService {

    @Override
    public List<MerchantBusinessCategoryVo> listAllAsTree() {
        // 1. 查询所有启用的类目
        List<MerchantBusinessCategory> allCategories = this.list(new LambdaQueryWrapper<MerchantBusinessCategory>()
                .eq(MerchantBusinessCategory::getStatus, 1)
                .orderByAsc(MerchantBusinessCategory::getSortOrder));

        if (allCategories == null || allCategories.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 转换为 VO
        List<MerchantBusinessCategoryVo> allVoList = allCategories.stream().map(category -> {
            MerchantBusinessCategoryVo vo = new MerchantBusinessCategoryVo();
            BeanUtils.copyProperties(category, vo);
            // 手动转换 ID 为 String 以防止 JS 精度丢失
            vo.setId(String.valueOf(category.getId()));
            vo.setParentId(String.valueOf(category.getParentId()));
            return vo;
        }).collect(Collectors.toList());

        // 3. 构建树形结构
        Map<String, List<MerchantBusinessCategoryVo>> parentMap = allVoList.stream()
                .collect(Collectors.groupingBy(MerchantBusinessCategoryVo::getParentId));

        allVoList.forEach(vo -> vo.setChildren(parentMap.get(vo.getId())));

        // 4. 返回一级类目 (parent_id = 0)
        return allVoList.stream()
                .filter(vo -> "0".equals(vo.getParentId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MerchantBusinessCategoryVo> listServicesByParentId(Long parentId) {
        if (parentId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "一级类目ID不能为空");
        }

        // 先校验一级类目本身有效，避免传任意ID导致语义不清
        MerchantBusinessCategory parent = this.getById(parentId);
        if (parent == null || !Integer.valueOf(1).equals(parent.getStatus()) || !Integer.valueOf(1).equals(parent.getLevel())) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "一级类目不存在或不可用");
        }

        List<MerchantBusinessCategory> serviceList = this.list(new LambdaQueryWrapper<MerchantBusinessCategory>()
                .eq(MerchantBusinessCategory::getParentId, parentId)
                .eq(MerchantBusinessCategory::getLevel, 2)
                .eq(MerchantBusinessCategory::getStatus, 1)
                .orderByAsc(MerchantBusinessCategory::getSortOrder)
                .orderByAsc(MerchantBusinessCategory::getId));

        if (serviceList == null || serviceList.isEmpty()) {
            return new ArrayList<>();
        }

        return serviceList.stream().map(category -> {
            MerchantBusinessCategoryVo vo = new MerchantBusinessCategoryVo();
            BeanUtils.copyProperties(category, vo);
            vo.setId(String.valueOf(category.getId()));
            vo.setParentId(String.valueOf(category.getParentId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
