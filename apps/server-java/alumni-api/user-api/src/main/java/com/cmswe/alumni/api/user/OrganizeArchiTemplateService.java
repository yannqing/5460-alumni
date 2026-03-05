package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.OrganizeArchiTemplate;
import com.cmswe.alumni.common.vo.OrganizeArchiTemplateVo;

import java.util.List;

/**
 * 组织架构模板服务接口
 */
public interface OrganizeArchiTemplateService extends IService<OrganizeArchiTemplate> {

    /**
     * 获取所有可用的组织架构模板列表
     *
     * @param organizeType 组织类型（可选，不传则返回所有类型）
     * @return 模板列表
     */
    List<OrganizeArchiTemplateVo> getAllTemplates(Integer organizeType);
}
