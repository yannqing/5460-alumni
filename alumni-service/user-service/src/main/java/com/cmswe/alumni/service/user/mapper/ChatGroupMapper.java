package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.ChatGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天群组 Mapper
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Mapper
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    /**
     * 查询用户创建的群组列表
     *
     * @param groupOwnerId 群主ID
     * @return 群组列表
     */
    List<ChatGroup> selectByGroupOwnerId(@Param("groupOwnerId") Long groupOwnerId);

    /**
     * 查询用户加入的所有群组（通过群成员表关联）
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<ChatGroup> selectByUserId(@Param("userId") Long userId);

    /**
     * 更新群成员数量
     *
     * @param groupId 群组ID
     * @param delta   增量（正数为增加，负数为减少）
     * @return 影响行数
     */
    int updateMemberCount(@Param("groupId") Long groupId, @Param("delta") Integer delta);

    /**
     * 查询指定类型的群组
     *
     * @param groupType 群类型
     * @return 群组列表
     */
    List<ChatGroup> selectByGroupType(@Param("groupType") String groupType);
}
