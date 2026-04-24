package com.cmswe.alumni.api.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.MerchantBusinessCategory;
import com.cmswe.alumni.common.vo.MerchantBusinessCategoryVo;

import java.util.List;

/**
 * 商户经营类目及范围 Service
 */
public interface MerchantBusinessCategoryService extends IService<MerchantBusinessCategory> {
    /**
     * 获取所有经营类目及范围（树形结构）
     * @return 树形结构列表
     */
    List<MerchantBusinessCategoryVo> listAllAsTree();

    /**
     * 按一级类目ID查询其下的二级服务列表
     *
     * @param parentId 一级类目ID
     * @return 二级服务列表
     */
    List<MerchantBusinessCategoryVo> listServicesByParentId(Long parentId);
}
