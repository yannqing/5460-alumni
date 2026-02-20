package com.cmswe.alumni.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 母校搜索文档
 * 索引名称: school_index_v1
 *
 * @author CNI Alumni System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "school_index_v1")
@Setting(
    shards = 3,
    replicas = 1,
    refreshInterval = "1s"
)
public class SchoolDocument {

    /**
     * 母校ID（主键）
     */
    @Id
    @Field(type = FieldType.Keyword)
    private Long schoolId;

    /**
     * 学校logo
     */
    @Field(type = FieldType.Keyword, index = false)
    private String logo;

    /**
     * 学校名称
     */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
        otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String schoolName;

    /**
     * 学校编码
     */
    @Field(type = FieldType.Keyword)
    private String schoolCode;

    /**
     * 校友总会ID
     */
    @Field(type = FieldType.Keyword)
    private Long headquartersId;

    /**
     * 所在省
     */
    @Field(type = FieldType.Keyword)
    private String province;

    /**
     * 所在市
     */
    @Field(type = FieldType.Keyword)
    private String city;

    /**
     * 办学层次
     */
    @Field(type = FieldType.Keyword)
    private String level;

    /**
     * 合并院校（json 数组）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String mergedInstitutions;

    /**
     * 曾用名（json 数组）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String previousName;

    /**
     * 其他内容
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String otherInfo;

    /**
     * 学校描述
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String description;

    /**
     * 建校日期
     */
    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate foundingDate;

    /**
     * 学校地址
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String location;

    /**
     * 官方认证状态（0-未认证，1-已认证）
     */
    @Field(type = FieldType.Integer)
    private Integer officialCertification;

    /**
     * 状态：0-禁用 1-启用
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
}
