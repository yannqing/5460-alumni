package com.cmswe.alumni.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.nio.file.Files;

/**
 * Elasticsearch 索引自动初始化器
 *
 * 功能：
 * - 应用启动时自动读取 JSON 映射文件
 * - 创建 ES 索引（如果不存在）
 * - 替代手动执行 init_indices.sh 脚本
 *
 * 配置：
 * - spring.elasticsearch.uris: ES 服务器地址
 * - spring.elasticsearch.username: ES 用户名
 * - spring.elasticsearch.password: ES 密码
 *
 * @author CNI Alumni System
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "elasticsearch.auto-init", havingValue = "true", matchIfMissing = false)
public class ElasticsearchIndexInitializer {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username:}")
    private String elasticsearchUsername;

    /**
     * 应用启动时自动初始化索引
     */
    @PostConstruct
    public void initIndices() {
        log.info("========================================");
        log.info("Elasticsearch 索引自动初始化");
        log.info("========================================");
        log.info("ES 地址: {}", elasticsearchUris);

        if (elasticsearchUsername != null && !elasticsearchUsername.isEmpty()) {
            log.info("[✓] Elasticsearch 认证已配置 (用户: {})", elasticsearchUsername);
        }

        try {
            // 1. 检查 ES 连接
            checkElasticsearchConnection();

            // 2. 创建索引
            createIndex("alumni_index_v1", "elasticsearch/alumni_index_mapping.json");
            createIndex("association_index_v1", "elasticsearch/association_index_mapping.json");
            createIndex("merchant_index_v1", "elasticsearch/merchant_index_mapping.json");

            log.info("========================================");
            log.info("索引初始化完成！");
            log.info("========================================");

        } catch (Exception e) {
            log.error("Elasticsearch 索引初始化失败", e);
            log.warn("请检查：");
            log.warn("  1. ES 是否已启动: {}", elasticsearchUris);
            log.warn("  2. JSON 映射文件是否存在");
            log.warn("  3. 或手动执行: cd alumni-search/src/main/resources/elasticsearch && ./init_indices.sh");
        }
    }

    /**
     * 检查 ES 连接
     */
    private void checkElasticsearchConnection() {
        try {
            HealthStatus status = elasticsearchClient.cluster().health().status();
            log.info("[✓] Elasticsearch 连接正常 (状态: {})", status);
        } catch (Exception e) {
            throw new RuntimeException("无法连接到 Elasticsearch: " + elasticsearchUris, e);
        }
    }

    /**
     * 创建索引
     *
     * @param indexName 索引名称
     * @param mappingFile 映射文件路径（相对于 classpath）
     */
    private void createIndex(String indexName, String mappingFile) {
        try {
            // 1. 检查索引是否已存在
            boolean exists = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();
            if (exists) {
                log.info("[SKIP] 索引已存在: {}", indexName);
                return;
            }

            // 2. 读取 JSON 映射文件
            ClassPathResource resource = new ClassPathResource(mappingFile);
            String mappingJson = new String(Files.readAllBytes(resource.getFile().toPath()));

            // 3. 创建索引
            elasticsearchClient.indices().create(CreateIndexRequest.of(builder ->
                builder.index(indexName)
                       .withJson(new StringReader(mappingJson))
            ));

            log.info("[✓] 索引创建成功: {}", indexName);

        } catch (Exception e) {
            log.error("[ERROR] 索引创建失败: {}", indexName, e);
            throw new RuntimeException("创建索引失败: " + indexName, e);
        }
    }
}
