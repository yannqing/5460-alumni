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
 * 校友搜索文档
 * 索引名称: alumni_index_v1
 *
 * @author CNI Alumni System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "alumni_index_v1")
@Setting(
    shards = 5,
    replicas = 1,
    refreshInterval = "1s"
)
public class AlumniDocument {

    /**
     * 校友ID（主键）
     */
    @Id
    @Field(type = FieldType.Keyword)
    private Long alumniId;

    /**
     * 用户ID
     */
    @Field(type = FieldType.Keyword)
    private Long userId;

    /**
     * 微信用户ID
     */
    @Field(type = FieldType.Keyword)
    private String wxId;

    /**
     * 真实姓名（IK分词）
     */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
        otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String realName;

    /**
     * 昵称（IK分词）
     */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
        otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String nickname;

    /**
     * 手机号
     */
    @Field(type = FieldType.Keyword)
    private String phone;

    /**
     * 邮箱
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String email;

    /**
     * 头像
     */
    @Field(type = FieldType.Keyword, index = false)
    private String avatar;

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
     * 毕业年份
     */
    @Field(type = FieldType.Integer)
    private Integer graduationYear;

    /**
     * 专业
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String major;

    /**
     * 地理位置
     */
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
     * 区县
     */
    @Field(type = FieldType.Keyword)
    private String district;

    /**
     * 行业
     */
    @Field(type = FieldType.Keyword)
    private String industry;

    /**
     * 职位
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String position;

    /**
     * 公司名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String company;

    /**
     * 是否可搜索（隐私设置）
     */
    @Field(type = FieldType.Boolean)
    private Boolean searchable;

    /**
     * 隐私级别
     */
    @Field(type = FieldType.Keyword)
    private String privacyLevel;

    /**
     * 是否已认证
     */
    @Field(type = FieldType.Boolean)
    private Boolean certified;

    /**
     * 认证状态
     */
    @Field(type = FieldType.Keyword)
    private String certificationStatus;

    /**
     * 标签
     */
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /**
     * 个性签名
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String signature;

    /**
     * 最后登录时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime lastLoginTime;

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

    /**
     * 搜索建议字段（用于自动补全）
     * 注意：当前实现基于 Redis 热搜，不使用 ES completion suggester，因此不索引此字段
     */
    @org.springframework.data.annotation.Transient
    private Object suggest;
}
