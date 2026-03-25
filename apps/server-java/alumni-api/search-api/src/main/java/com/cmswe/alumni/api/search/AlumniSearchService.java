package com.cmswe.alumni.api.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmswe.alumni.common.dto.QueryAlumniListDto;
import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.vo.UserListResponse;
import com.cmswe.alumni.common.vo.search.SearchResultItem;

/**
 * 校友搜索服务接口
 *
 * @author CNI Alumni System
 */
public interface AlumniSearchService {

    /**
     * 搜索校友
     *
     * @param keyword 搜索关键词
     * @param filter 过滤条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param highlight 是否高亮
     * @return 搜索结果
     */
    org.springframework.data.domain.Page<SearchResultItem> searchAlumni(String keyword, SearchFilter filter,
                                        Integer pageNum, Integer pageSize, Boolean highlight);

    /**
     * 查询校友列表（兼容原 MySQL 查询接口）
     * 使用 Elasticsearch 查询，与 UserService.queryAlumniList() 保持入参出参一致
     *
     * @param queryAlumniListDto 查询条件（与 MySQL 查询保持一致）
     * @param wxId 当前用户ID（用于"我的关注"筛选，可为null）
     * @return 用户列表（与 MySQL 返回格式一致，使用 MyBatis-Plus 的 Page）
     */
    Page<UserListResponse> queryAlumniList(QueryAlumniListDto queryAlumniListDto, Long wxId);

    /**
     * 按地理位置搜索附近的校友
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 半径（公里）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    org.springframework.data.domain.Page<SearchResultItem> searchNearbyAlumni(Double latitude, Double longitude,
                                              Integer radius, Integer pageNum, Integer pageSize);

    /**
     * 索引校友数据
     *
     * @param alumniId 校友ID
     */
    void indexAlumni(Long alumniId);

    /**
     * 批量索引校友数据
     *
     * @param alumniIds 校友ID列表
     */
    void batchIndexAlumni(Iterable<Long> alumniIds);

    /**
     * 删除校友索引
     *
     * @param alumniId 校友ID
     */
    void deleteAlumni(Long alumniId);

    /**
     * 全量索引重建
     */
    void rebuildIndex();
}
