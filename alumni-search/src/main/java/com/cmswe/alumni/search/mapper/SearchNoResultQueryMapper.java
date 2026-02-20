package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.SearchNoResultQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 搜索无结果查询 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Mapper
public interface SearchNoResultQueryMapper extends BaseMapper<SearchNoResultQuery> {

    /**
     * 查询高频无结果查询（按查询次数倒序）
     *
     * @param searchType 搜索类型（NULL表示查询所有类型）
     * @param status     处理状态（NULL表示查询所有状态）
     * @param limit      查询数量
     * @return 无结果查询列表
     */
    List<SearchNoResultQuery> selectTopFrequent(@Param("searchType") String searchType,
                                                @Param("status") String status,
                                                @Param("limit") Integer limit);

    /**
     * 增加查询次数（如果记录不存在则插入）
     *
     * @param keyword    关键词
     * @param searchType 搜索类型
     * @return 影响的记录数
     */
    int incrementQueryCount(@Param("keyword") String keyword,
                           @Param("searchType") String searchType);

    /**
     * 批量更新处理状态
     *
     * @param ids     记录ID列表
     * @param status  新状态
     * @param handler 处理人
     * @return 更新的记录数
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids,
                         @Param("status") String status,
                         @Param("handler") String handler);
}
