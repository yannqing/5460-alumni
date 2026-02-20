package com.cmswe.alumni.common.dto.search;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 搜索过滤条件
 *
 * @author CNI Alumni System
 */
@Data
public class SearchFilter {

    /**
     * 地理位置过滤 - 纬度
     */
    private Double latitude;

    /**
     * 地理位置过滤 - 经度
     */
    private Double longitude;

    /**
     * 地理位置过滤 - 半径（公里）
     */
    private Integer radius = 50;

    /**
     * 时间范围 - 开始日期
     */
    private LocalDate startDate;

    /**
     * 时间范围 - 结束日期
     */
    private LocalDate endDate;

    /**
     * 学校ID过滤
     */
    private Long schoolId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 行业过滤（多选）
     */
    private List<String> industries;

    /**
     * 标签过滤（多选）
     */
    private List<String> tags;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 毕业年份范围 - 起始
     */
    private Integer graduationYearStart;

    /**
     * 毕业年份范围 - 结束
     */
    private Integer graduationYearEnd;

    /**
     * 只显示已认证用户
     */
    private Boolean onlyCertified = false;

    /**
     * 评分最小值（商户）
     */
    private Double minRating;

    /**
     * 成员数量最小值（校友会）
     */
    private Integer minMemberCount;
}
