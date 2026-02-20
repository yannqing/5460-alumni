package com.cmswe.alumni.api.search;

import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.vo.search.SearchResultItem;
import org.springframework.data.domain.Page;

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
    Page<SearchResultItem> searchAlumni(String keyword, SearchFilter filter,
                                        Integer pageNum, Integer pageSize, Boolean highlight);

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
    Page<SearchResultItem> searchNearbyAlumni(Double latitude, Double longitude,
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
