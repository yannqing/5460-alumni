package com.cmswe.alumni.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDateTime;

/**
 * 校友会搜索文档
 * 索引名称: association_index_v1
 *
 * @author CNI Alumni System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "association_index_v1")
@Setting(
    shards = 3,
    replicas = 1,
    refreshInterval = "1s"
)
public class AssociationDocument {

    /**
     * 校友会ID（主键）
     */
    @Id
    @Field(type = FieldType.Keyword)
    private Long associationId;

    /**
     * 校友会名称
     */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
        otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String associationName;

    /**
     * 学校ID
     */
    @Field(type = FieldType.Keyword)
    private Long schoolId;

    /**
     * 学校名称
     */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word"),
        otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String schoolName;

    /**
     * 平台ID（地方校处会）
     */
    @Field(type = FieldType.Keyword)
    private Long platformId;

    /**
     * 平台名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String platformName;

    /**
     * 会长姓名
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String presidentName;

    /**
     * 联系方式
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String contactInfo;

    /**
     * 地理位置
     */
    @Field(type = FieldType.Object)
    @GeoPointField
    private GeoPoint location;

    /**
     * 省份
     */
    @Field(type = FieldType.Keyword)
    private String province;

    /**
     * 城市
     */
    @Field(type = FieldType.Keyword)
    private String city;

    /**
     * 成员数量
     */
    @Field(type = FieldType.Integer)
    private Integer memberCount;

    /**
     * 状态
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 简介
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String introduction;

    /**
     * 封面图
     */
    @Field(type = FieldType.Keyword, index = false)
    private String coverImage;

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
