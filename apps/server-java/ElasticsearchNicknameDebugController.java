package com.cmswe.alumni.web.debug;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.cmswe.alumni.search.document.AlumniDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Elasticsearch Nickname 字段调试控制器
 * 临时调试接口，用于检查 nickname 字段的索引情况
 *
 * 使用完毕后请删除此文件
 */
@Slf4j
@RestController
@RequestMapping("/debug/es")
public class ElasticsearchNicknameDebugController {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    /**
     * 1. 检查索引统计信息
     * GET /debug/es/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getIndexStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 查询总文档数
            NativeQuery countQuery = NativeQuery.builder()
                    .withQuery(Query.of(q -> q.matchAll(m -> m)))
                    .build();

            SearchHits<AlumniDocument> allHits = elasticsearchOperations.search(
                    countQuery, AlumniDocument.class);
            stats.put("totalDocuments", allHits.getTotalHits());

            // 查询有 nickname 的文档数
            NativeQuery nicknameExistsQuery = NativeQuery.builder()
                    .withQuery(Query.of(q -> q.exists(e -> e.field("nickname"))))
                    .build();

            SearchHits<AlumniDocument> nicknameHits = elasticsearchOperations.search(
                    nicknameExistsQuery, AlumniDocument.class);
            stats.put("documentsWithNickname", nicknameHits.getTotalHits());

            // 查询 nickname 为空或 null 的文档数
            long emptyNickname = allHits.getTotalHits() - nicknameHits.getTotalHits();
            stats.put("documentsWithoutNickname", emptyNickname);

            stats.put("status", "success");

        } catch (Exception e) {
            log.error("获取索引统计失败", e);
            stats.put("status", "error");
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * 2. 随机查看文档样本
     * GET /debug/es/samples?size=5
     */
    @GetMapping("/samples")
    public Map<String, Object> getSamples(@RequestParam(defaultValue = "5") int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(Query.of(q -> q.matchAll(m -> m)))
                    .withMaxResults(size)
                    .build();

            SearchHits<AlumniDocument> hits = elasticsearchOperations.search(
                    query, AlumniDocument.class);

            List<Map<String, Object>> samples = hits.getSearchHits().stream()
                    .map(hit -> {
                        AlumniDocument doc = hit.getContent();
                        Map<String, Object> sample = new HashMap<>();
                        sample.put("wxId", doc.getWxId());
                        sample.put("nickname", doc.getNickname());
                        sample.put("realName", doc.getRealName());
                        sample.put("hasNickname", doc.getNickname() != null && !doc.getNickname().isEmpty());
                        return sample;
                    })
                    .collect(Collectors.toList());

            result.put("samples", samples);
            result.put("count", samples.size());
            result.put("status", "success");

        } catch (Exception e) {
            log.error("获取样本失败", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 3. 测试 nickname 搜索
     * GET /debug/es/search-nickname?keyword=张三
     */
    @GetMapping("/search-nickname")
    public Map<String, Object> searchByNickname(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 方法1: 只搜索 nickname 字段
            NativeQuery nicknameQuery = NativeQuery.builder()
                    .withQuery(Query.of(q -> q.match(m -> m
                            .field("nickname")
                            .query(keyword)
                            .fuzziness("AUTO"))))
                    .withMaxResults(10)
                    .build();

            SearchHits<AlumniDocument> nicknameHits = elasticsearchOperations.search(
                    nicknameQuery, AlumniDocument.class);

            List<Map<String, Object>> nicknameResults = nicknameHits.getSearchHits().stream()
                    .map(hit -> {
                        AlumniDocument doc = hit.getContent();
                        Map<String, Object> item = new HashMap<>();
                        item.put("wxId", doc.getWxId());
                        item.put("nickname", doc.getNickname());
                        item.put("realName", doc.getRealName());
                        item.put("score", hit.getScore());
                        return item;
                    })
                    .collect(Collectors.toList());

            result.put("nicknameOnlyResults", nicknameResults);
            result.put("nicknameOnlyCount", nicknameHits.getTotalHits());

            // 方法2: multi_match 搜索（模拟接口）
            NativeQuery multiMatchQuery = NativeQuery.builder()
                    .withQuery(Query.of(q -> q.multiMatch(m -> m
                            .query(keyword)
                            .fields("realName^3", "nickname^2", "schoolName^1.5",
                                    "major", "company", "position", "signature")
                            .fuzziness("AUTO"))))
                    .withMaxResults(10)
                    .build();

            SearchHits<AlumniDocument> multiMatchHits = elasticsearchOperations.search(
                    multiMatchQuery, AlumniDocument.class);

            List<Map<String, Object>> multiMatchResults = multiMatchHits.getSearchHits().stream()
                    .map(hit -> {
                        AlumniDocument doc = hit.getContent();
                        Map<String, Object> item = new HashMap<>();
                        item.put("wxId", doc.getWxId());
                        item.put("nickname", doc.getNickname());
                        item.put("realName", doc.getRealName());
                        item.put("score", hit.getScore());
                        return item;
                    })
                    .collect(Collectors.toList());

            result.put("multiMatchResults", multiMatchResults);
            result.put("multiMatchCount", multiMatchHits.getTotalHits());

            result.put("keyword", keyword);
            result.put("status", "success");

        } catch (Exception e) {
            log.error("搜索失败: keyword={}", keyword, e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 4. 查找 nickname 为空的文档
     * GET /debug/es/empty-nickname?size=10
     */
    @GetMapping("/empty-nickname")
    public Map<String, Object> getEmptyNickname(@RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(Query.of(q -> q.bool(b -> b
                            .mustNot(mn -> mn.exists(e -> e.field("nickname"))))))
                    .withMaxResults(size)
                    .build();

            SearchHits<AlumniDocument> hits = elasticsearchOperations.search(
                    query, AlumniDocument.class);

            List<Map<String, Object>> emptyDocs = hits.getSearchHits().stream()
                    .map(hit -> {
                        AlumniDocument doc = hit.getContent();
                        Map<String, Object> item = new HashMap<>();
                        item.put("wxId", doc.getWxId());
                        item.put("nickname", doc.getNickname());
                        item.put("realName", doc.getRealName());
                        return item;
                    })
                    .collect(Collectors.toList());

            result.put("emptyNicknameDocs", emptyDocs);
            result.put("totalEmptyCount", hits.getTotalHits());
            result.put("status", "success");

        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 5. 测试模糊查询 wildcard
     * GET /debug/es/wildcard-nickname?keyword=张
     */
    @GetMapping("/wildcard-nickname")
    public Map<String, Object> wildcardSearch(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();

        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(Query.of(q -> q.wildcard(w -> w
                            .field("nickname.keyword")
                            .value("*" + keyword + "*"))))
                    .withMaxResults(10)
                    .build();

            SearchHits<AlumniDocument> hits = elasticsearchOperations.search(
                    query, AlumniDocument.class);

            List<Map<String, Object>> wildcardResults = hits.getSearchHits().stream()
                    .map(hit -> {
                        AlumniDocument doc = hit.getContent();
                        Map<String, Object> item = new HashMap<>();
                        item.put("wxId", doc.getWxId());
                        item.put("nickname", doc.getNickname());
                        item.put("realName", doc.getRealName());
                        item.put("score", hit.getScore());
                        return item;
                    })
                    .collect(Collectors.toList());

            result.put("wildcardResults", wildcardResults);
            result.put("wildcardCount", hits.getTotalHits());
            result.put("keyword", keyword);
            result.put("status", "success");

        } catch (Exception e) {
            log.error("通配符搜索失败: keyword={}", keyword, e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }
}
