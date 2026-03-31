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
     *
     * 修复：增强 nickname 的模糊匹配能力
     * - 对于短关键词（<= 3 个字符），同时使用 wildcard 查询 nickname.keyword
     * - 保留原有的 multi_match 查询以支持分词匹配
     */
    private static Query multiMatchQuery(String keyword) {
        // 原有的 multi_match 查询
        Query baseQuery = Query.of(q -> q.multiMatch(m -> m
                .query(keyword)
                .fields("realName^3", "nickname^2", "schoolName^1.5",
                        "major", "company", "position", "signature")
                .type(TextQueryType.BestFields)
                .fuzziness("0")  // 禁用模糊匹配，提升搜索精准度，避免误召回（2026-03-31）
                .operator(Operator.And)  // 要求所有分词都必须匹配（AND逻辑）（2026-03-31）
                .prefixLength(1)));

        // 如果是短关键词，添加 wildcard 查询增强 nickname 匹配
        if (keyword != null && keyword.length() <= 10) {
            return Query.of(q -> q.bool(b -> b
                    .should(baseQuery)
                    .should(Query.of(wq -> wq.wildcard(w -> w
                            .field("nickname.keyword")
                            .value("*" + keyword + "*")
                            .boost(1.5f)))) // 给 wildcard 查询更高的权重
                    .minimumShouldMatch("1")));
        }

        return baseQuery;
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

    /**
     * 构建校友列表查询（兼容 MySQL 的 QueryAlumniListDto）
     *
     * @param dto 查询条件
     * @param followedUserIds "我的关注"筛选的用户ID列表，为null表示不筛选
     * @return NativeQuery
     */
    public static NativeQuery buildAlumniListQuery(
            com.cmswe.alumni.common.dto.QueryAlumniListDto dto,
            List<Long> followedUserIds) {

        NativeQueryBuilder queryBuilder = NativeQuery.builder();
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 1. 构建 OR 条件：nickname、name、phone（模糊匹配）
        String nickname = dto.getNickname();
        String name = dto.getName();
        String phone = dto.getPhone();

        boolean hasOrCondition = (nickname != null && !nickname.trim().isEmpty())
                || (name != null && !name.trim().isEmpty())
                || (phone != null && !phone.trim().isEmpty());

        if (hasOrCondition) {
            boolQueryBuilder.must(Query.of(q -> q.bool(b -> {
                if (nickname != null && !nickname.trim().isEmpty()) {
                    b.should(Query.of(sq -> sq.match(m -> m
                            .field("nickname")
                            .query(nickname)
                            .fuzziness("AUTO"))));
                }
                if (name != null && !name.trim().isEmpty()) {
                    b.should(Query.of(sq -> sq.match(m -> m
                            .field("realName")
                            .query(name)
                            .fuzziness("AUTO"))));
                }
                if (phone != null && !phone.trim().isEmpty()) {
                    b.should(Query.of(sq -> sq.wildcard(w -> w
                            .field("phone")
                            .value("*" + phone + "*"))));
                }
                b.minimumShouldMatch("1");
                return b;
            })));
        }

        // 2. AND 条件：其他字段精确匹配或模糊匹配
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            boolQueryBuilder.filter(f -> f.wildcard(w -> w
                    .field("email")
                    .value("*" + dto.getEmail() + "*")));
        }

        if (dto.getCurContinent() != null && !dto.getCurContinent().trim().isEmpty()) {
            boolQueryBuilder.filter(f -> f.term(t -> t
                    .field("curContinent")
                    .value(dto.getCurContinent())));
        }

        if (dto.getCurCountry() != null && !dto.getCurCountry().trim().isEmpty()) {
            boolQueryBuilder.filter(f -> f.term(t -> t
                    .field("curCountry")
                    .value(dto.getCurCountry())));
        }

        if (dto.getCurProvince() != null && !dto.getCurProvince().trim().isEmpty()) {
            boolQueryBuilder.filter(f -> f.term(t -> t
                    .field("province")
                    .value(dto.getCurProvince())));
        }

        if (dto.getCurCity() != null && !dto.getCurCity().trim().isEmpty()) {
            boolQueryBuilder.filter(f -> f.term(t -> t
                    .field("city")
                    .value(dto.getCurCity())));
        }

        if (dto.getGender() != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t
                    .field("gender")
                    .value(dto.getGender())));
        }

        if (dto.getConstellation() != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t
                    .field("constellation")
                    .value(dto.getConstellation())));
        }

        if (dto.getBirthDate() != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t
                    .field("birthDate")
                    .value(dto.getBirthDate().toString())));
        }

        if (dto.getSignature() != null && !dto.getSignature().trim().isEmpty()) {
            boolQueryBuilder.filter(f -> f.match(m -> m
                    .field("signature")
                    .query(dto.getSignature())));
        }

        if (dto.getIdentifyCode() != null && !dto.getIdentifyCode().trim().isEmpty()) {
            boolQueryBuilder.filter(f -> f.wildcard(w -> w
                    .field("identifyCode")
                    .value("*" + dto.getIdentifyCode() + "*")));
        }

        // 3. "我的关注"筛选
        if (followedUserIds != null && !followedUserIds.isEmpty()) {
            boolQueryBuilder.filter(f -> f.terms(t -> t
                    .field("wxId")
                    .terms(ts -> ts.value(followedUserIds.stream()
                            .map(id -> FieldValue.of(id.toString()))
                            .toList()))));
        }

        // 4. 只搜索可见用户（隐私过滤）
        boolQueryBuilder.filter(f -> f.term(t -> t.field("searchable").value(true)));

        // 5. 强制筛选：realName 和 nickname 不能同时为空
        boolQueryBuilder.filter(Query.of(q -> q.bool(b -> b
                .should(Query.of(sq -> sq.exists(e -> e.field("realName"))))
                .should(Query.of(sq -> sq.exists(e -> e.field("nickname"))))
                .minimumShouldMatch("1"))));

        queryBuilder.withQuery(Query.of(q -> q.bool(boolQueryBuilder.build())));

        // 6. 分页
        int current = dto.getCurrent() > 0 ? dto.getCurrent() : 1;
        int pageSize = dto.getPageSize() > 0 ? dto.getPageSize() : 10;
        queryBuilder.withPageable(org.springframework.data.domain.PageRequest.of(
                current - 1, pageSize));

        // 7. 排序：先按指定字段排序，再按wxId排序（确保排序稳定）
        String sortField = dto.getSortField() != null ? dto.getSortField() : "createTime";
        String sortOrder = dto.getSortOrder();
        boolean isAsc = "ascend".equals(sortOrder);

        if ("createdTime".equals(sortField) || "createTime".equals(sortField)) {
            queryBuilder.withSort(org.springframework.data.domain.Sort.by(
                    isAsc ? org.springframework.data.domain.Sort.Direction.ASC
                          : org.springframework.data.domain.Sort.Direction.DESC,
                    "createTime"
            ));
        }

        // 辅助排序：按 wxId 降序（保证排序稳定性）
        queryBuilder.withSort(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "wxId"));

        return queryBuilder.build();
    }
}
