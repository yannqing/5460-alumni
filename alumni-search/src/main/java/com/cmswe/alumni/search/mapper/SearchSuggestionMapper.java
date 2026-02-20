package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.SearchSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 搜索建议词 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Mapper
public interface SearchSuggestionMapper extends BaseMapper<SearchSuggestion> {

    /**
     * 根据前缀查询建议词（按权重倒序）
     *
     * @param prefix     前缀
     * @param searchType 搜索类型
     * @param limit      查询数量
     * @return 建议词列表
     */
    List<SearchSuggestion> selectByPrefix(@Param("prefix") String prefix,
                                         @Param("searchType") String searchType,
                                         @Param("limit") Integer limit);

    /**
     * 查询所有启用的建议词（按权重倒序）
     *
     * @param searchType 搜索类型
     * @param limit      查询数量
     * @return 建议词列表
     */
    List<SearchSuggestion> selectEnabled(@Param("searchType") String searchType,
                                        @Param("limit") Integer limit);

    /**
     * 批量更新权重
     *
     * @param id     记录ID
     * @param weight 新权重
     * @return 更新的记录数
     */
    int updateWeight(@Param("id") Long id,
                    @Param("weight") Integer weight);

    /**
     * 批量更新状态
     *
     * @param ids    记录ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids,
                         @Param("status") Integer status);
}
