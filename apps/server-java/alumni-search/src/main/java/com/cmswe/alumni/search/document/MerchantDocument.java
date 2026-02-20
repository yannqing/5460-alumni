package com.cmswe.alumni.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商户搜索文档
 * 索引名称: merchant_index_v1
 *
 * @author CNI Alumni System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "merchant_index_v1")
@Setting(
    shards = 3,
    replicas = 1,
    refreshInterval = "1s"
)
public class MerchantDocument {

    /**
     * 商户ID（主键）
     */
    @Id
    @Field(type = FieldType.Keyword)
    private Long merchantId;

    /**
     * 商户名称
     */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
        otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String merchantName;

    /**
     * 商户类型
     */
    @Field(type = FieldType.Keyword)
    private String merchantType;

    /**
     * 行业
     */
    @Field(type = FieldType.Keyword)
    private String industry;

    /**
     * 店主姓名
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String ownerName;

    /**
     * 用户ID（店主）
     */
    @Field(type = FieldType.Keyword)
    private Long userId;

    /**
     * 营业执照号
     */
    @Field(type = FieldType.Keyword)
    private String businessLicense;

    /**
     * 地理位置
     */
    @Field(type = FieldType.Object)
    @GeoPointField
    private GeoPoint location;

    /**
     * 详细地址
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String address;

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
     * 区县
     */
    @Field(type = FieldType.Keyword)
    private String district;

    /**
     * 联系电话
     */
    @Field(type = FieldType.Keyword)
    private String contactPhone;

    /**
     * 邮箱
     */
    @Field(type = FieldType.Keyword)
    private String email;

    /**
     * 会员等级
     */
    @Field(type = FieldType.Keyword)
    private String memberTier;

    /**
     * 评分
     */
    @Field(type = FieldType.Float)
    private Float rating;

    /**
     * 审核状态
     */
    @Field(type = FieldType.Keyword)
    private String reviewStatus;

    /**
     * 商户简介
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String description;

    /**
     * 标签
     */
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /**
     * 商户 Logo
     */
    @Field(type = FieldType.Keyword, index = false)
    private String logo;

    /**
     * 轮播图
     */
    @Field(type = FieldType.Keyword, index = false)
    private List<String> banners;

    /**
     * 营业状态
     */
    @Field(type = FieldType.Keyword)
    private String status;

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
