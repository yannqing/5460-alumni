package com.cmswe.alumni.search.util;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.json.JsonData;
import com.cmswe.alumni.common.dto.search.SearchFilter;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch 查询构建器工具类
 *
 * @author CNI Alumni System
 */
public class SearchQueryBuilder {

    /**
     * 构建校友搜索查询
     *
     * @param keyword 搜索关键词
     * @param filter 过滤条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param highlight 是否高亮
     * @return NativeQuery
     */
    public static NativeQuery buildAlumniQuery(
            String keyword,
            SearchFilter filter,
            Integer pageNum,
            Integer pageSize,
            Boolean highlight) {

        NativeQueryBuilder queryBuilder = NativeQuery.builder();

        // 1. 构建复合查询
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 1.1 多字段搜索（姓名、昵称、专业、学校等）
        if (keyword != null && !keyword.trim().isEmpty()) {
            boolQueryBuilder.must(multiMatchQuery(keyword));
        }

        // 1.2 只搜索可见用户（隐私过滤）
        boolQueryBuilder.filter(f -> f.term(t -> t.field("searchable").value(true)));

        // 1.3 应用过滤条件
        applyFilters(boolQueryBuilder, filter);

        // 2. Function Score 相关性排序
        Query query = Query.of(q -> q.functionScore(fs -> {
            fs.query(Query.of(qq -> qq.bool(boolQueryBuilder.build())));

            // 2.1 认证用户加分
            fs.functions(fn -> fn
                    .filter(f -> f.term(t -> t.field("certified").value(true)))
                    .weight(2.0));

            // 2.2 最近活跃用户加分
            fs.functions(fn -> fn
                    .filter(f -> f.range(r -> r
                            .field("lastLoginTime")
                            .gte(JsonData.of("now-7d"))))
                    .weight(1.5));

            // 2.3 地理位置衰减（如果有位置过滤）
            // TODO: ES 8.x Java Client 的 DecayFunction API 需要不同的实现方式
            // 暂时注释，可通过 geo_distance 过滤器替代
            /*
            if (filter != null && filter.getLatitude() != null && filter.getLongitude() != null) {
                fs.functions(fn -> fn
                        .filter(f -> f.matchAll(m -> m))
                        .exp(exp -> exp
                                .field("location")
                                .origin(filter.getLatitude() + "," + filter.getLongitude())
                                .scale("50km")
                                .decay(0.5)));
            }
            */

            fs.scoreMode(FunctionScoreMode.Sum);
            fs.boostMode(FunctionBoostMode.Multiply);

            return fs;
        }));

        queryBuilder.withQuery(query);

        // 3. 分页
        queryBuilder.withPageable(org.springframework.data.domain.PageRequest.of(
                pageNum - 1, pageSize));

        // 4. 高亮
        if (highlight != null && highlight) {
            queryBuilder.withHighlightQuery(buildHighlight());
        }

        return queryBuilder.build();
    }

    /**
     * 构建多字段匹配查询
     */
    private static Query multiMatchQuery(String keyword) {
        return Query.of(q -> q.multiMatch(m -> m
                .query(keyword)
                .fields("realName^3", "nickname^2", "schoolName^1.5",
                        "major", "company", "position", "signature")
                .type(TextQueryType.BestFields)
                .fuzziness("AUTO")
                .prefixLength(1)));
    }

    /**
     * 应用过滤条件
     */
    private static void applyFilters(BoolQuery.Builder boolQuery, SearchFilter filter) {
        if (filter == null) {
            return;
        }

        // 学校过滤
        if (filter.getSchoolId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("schoolId").value(filter.getSchoolId())));
        }

        // 省份过滤
        if (filter.getProvince() != null && !filter.getProvince().isEmpty()) {
            boolQuery.filter(f -> f.term(t -> t.field("province").value(filter.getProvince())));
        }

        // 城市过滤
        if (filter.getCity() != null && !filter.getCity().isEmpty()) {
            boolQuery.filter(f -> f.term(t -> t.field("city").value(filter.getCity())));
        }

        // 毕业年份范围
        if (filter.getGraduationYearStart() != null || filter.getGraduationYearEnd() != null) {
            boolQuery.filter(f -> f.range(r -> {
                if (filter.getGraduationYearStart() != null) {
                    r.gte(JsonData.of(filter.getGraduationYearStart()));
                }
                if (filter.getGraduationYearEnd() != null) {
                    r.lte(JsonData.of(filter.getGraduationYearEnd()));
                }
                return r.field("graduationYear");
            }));
        }

        // 行业过滤
        if (filter.getIndustries() != null && !filter.getIndustries().isEmpty()) {
            boolQuery.filter(f -> f.terms(t -> t
                    .field("industry")
                    .terms(ts -> ts.value(filter.getIndustries().stream()
                            .map(FieldValue::of)
                            .toList()))));
        }

        // 标签过滤
        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            boolQuery.filter(f -> f.terms(t -> t
                    .field("tags")
                    .terms(ts -> ts.value(filter.getTags().stream()
                            .map(FieldValue::of)
                            .toList()))));
        }

        // 只显示已认证
        if (filter.getOnlyCertified() != null && filter.getOnlyCertified()) {
            boolQuery.filter(f -> f.term(t -> t.field("certified").value(true)));
        }

        // 地理位置过滤
        if (filter.getLatitude() != null && filter.getLongitude() != null) {
            Integer radius = filter.getRadius() != null ? filter.getRadius() : 50;
            boolQuery.filter(f -> f.geoDistance(g -> g
                    .field("location")
                    .location(l -> l.latlon(ll -> ll
                            .lat(filter.getLatitude())
                            .lon(filter.getLongitude())))
                    .distance(radius + "km")));
        }

        // 时间范围过滤
        if (filter.getStartDate() != null || filter.getEndDate() != null) {
            boolQuery.filter(f -> f.range(r -> {
                if (filter.getStartDate() != null) {
                    r.gte(JsonData.of(filter.getStartDate().toString()));
                }
                if (filter.getEndDate() != null) {
                    r.lte(JsonData.of(filter.getEndDate().toString()));
                }
                return r.field("createTime");
            }));
        }
    }

    /**
     * 构建高亮查询
     */
    private static HighlightQuery buildHighlight() {
        HighlightFieldParameters params = HighlightFieldParameters.builder()
                .withPreTags("<em class='highlight'>")
                .withPostTags("</em>")
                .withFragmentSize(150)
                .withNumberOfFragments(3)
                .build();

        Highlight highlight = new Highlight(List.of(
                new HighlightField("realName", params),
                new HighlightField("nickname", params),
                new HighlightField("major", params),
                new HighlightField("company", params),
                new HighlightField("position", params),
                new HighlightField("signature", params)
        ));

        return new HighlightQuery(highlight, null);
    }

    /**
     * 构建地理位置排序查询
     */
    public static NativeQuery buildGeoQuery(
            Double latitude,
            Double longitude,
            Integer radius,
            Integer pageNum,
            Integer pageSize) {

        Query query = Query.of(q -> q.bool(b -> b
                .filter(f -> f.term(t -> t.field("searchable").value(true)))
                .filter(f -> f.geoDistance(g -> g
                        .field("location")
                        .location(l -> l.latlon(ll -> ll
                                .lat(latitude)
                                .lon(longitude)))
                        .distance(radius + "km")))));

        return NativeQuery.builder()
                .withQuery(query)
                .withPageable(org.springframework.data.domain.PageRequest.of(
                        pageNum - 1, pageSize))
                .build();
    }

    /**
     * 构建缓存 Key
     */
    public static String buildCacheKey(String prefix, Object... params) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object param : params) {
            sb.append(":").append(param != null ? param.toString() : "null");
        }
        return sb.toString();
    }
}
