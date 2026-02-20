package com.cmswe.alumni.service.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.SysTag;
import com.cmswe.alumni.common.entity.SysTagRelation;

import java.util.List;

/**
 * 标签关联关系表 Service 接口
 * @author system
 */
public interface SysTagRelationService extends IService<SysTagRelation> {

    /**
     * 为目标对象添加标签
     * @param tagId 标签ID
     * @param targetId 目标对象ID
     * @param targetType 目标类型: 1-校友(User), 2-商户(Shop), 3-活动(Event)
     * @param createBy 操作人
     * @return 是否添加成功
     */
    boolean addTagToTarget(Long tagId, Long targetId, Integer targetType, String createBy);

    /**
     * 批量为目标对象添加标签
     * @param tagIds 标签ID列表
     * @param targetId 目标对象ID
     * @param targetType 目标类型: 1-校友(User), 2-商户(Shop), 3-活动(Event)
     * @param createBy 操作人
     * @return 是否添加成功
     */
    boolean batchAddTagsToTarget(List<Long> tagIds, Long targetId, Integer targetType, String createBy);

    /**
     * 移除目标对象的标签
     * @param tagId 标签ID
     * @param targetId 目标对象ID
     * @param targetType 目标类型
     * @return 是否移除成功
     */
    boolean removeTagFromTarget(Long tagId, Long targetId, Integer targetType);

    /**
     * 批量移除目标对象的标签
     * @param tagIds 标签ID列表
     * @param targetId 目标对象ID
     * @param targetType 目标类型
     * @return 是否移除成功
     */
    boolean batchRemoveTagsFromTarget(List<Long> tagIds, Long targetId, Integer targetType);

    /**
     * 获取目标对象的所有标签
     * @param targetId 目标对象ID
     * @param targetType 目标类型
     * @return 标签列表
     */
    List<SysTag> getTagsByTarget(Long targetId, Integer targetType);

    /**
     * 获取拥有指定标签的目标对象ID列表
     * @param tagId 标签ID
     * @param targetType 目标类型
     * @return 目标对象ID列表
     */
    List<Long> getTargetIdsByTag(Long tagId, Integer targetType);

    /**
     * 获取拥有指定标签列表的目标对象ID列表（交集）
     * @param tagIds 标签ID列表
     * @param targetType 目标类型
     * @return 目标对象ID列表
     */
    List<Long> getTargetIdsByTags(List<Long> tagIds, Integer targetType);

    /**
     * 清空目标对象的所有标签
     * @param targetId 目标对象ID
     * @param targetType 目标类型
     * @return 是否清空成功
     */
    boolean clearTargetTags(Long targetId, Integer targetType);
}
