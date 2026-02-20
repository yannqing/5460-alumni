package com.cmswe.alumni.service.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.SysTag;

import java.util.List;

/**
 * 系统标签定义表 Service 接口
 * @author system
 */
public interface SysTagService extends IService<SysTag> {

    /**
     * 根据标签分类获取标签列表
     * @param category 标签分类: 1-通用, 2-用户画像, 3-商户类型, 4-行业领域
     * @return 标签列表
     */
    List<SysTag> getTagsByCategory(Integer category);

    /**
     * 根据父标签ID获取子标签列表
     * @param parentId 父标签ID
     * @return 子标签列表
     */
    List<SysTag> getTagsByParentId(Long parentId);

    /**
     * 根据标签代码获取标签
     * @param code 标签代码
     * @return 标签信息
     */
    SysTag getTagByCode(String code);

    /**
     * 创建标签
     * @param sysTag 标签信息
     * @return 是否创建成功
     */
    boolean createTag(SysTag sysTag);

    /**
     * 更新标签
     * @param sysTag 标签信息
     * @return 是否更新成功
     */
    boolean updateTag(SysTag sysTag);

    /**
     * 删除标签（逻辑删除）
     * @param tagId 标签ID
     * @return 是否删除成功
     */
    boolean deleteTag(Long tagId);
}
