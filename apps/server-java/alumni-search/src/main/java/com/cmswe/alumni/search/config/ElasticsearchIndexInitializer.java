package com.cmswe.alumni.search.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

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
 * - elasticsearch.auto-init: true/false（是否启用自动初始化）
 * - elasticsearch.host: ES 服务器地址
 *
 * @author CNI Alumni System
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "elasticsearch.auto-init", havingValue = "true", matchIfMissing = false)
public class ElasticsearchIndexInitializer {

    @Value("${elasticsearch.host:http://localhost:9200}")
    private String elasticsearchHost;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 应用启动时自动初始化索引
     */
    @PostConstruct
    public void initIndices() {
        log.info("========================================");
        log.info("Elasticsearch 索引自动初始化");
        log.info("========================================");
        log.info("ES 地址: {}", elasticsearchHost);

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
            log.warn("  1. ES 是否已启动: {}", elasticsearchHost);
            log.warn("  2. JSON 映射文件是否存在");
            log.warn("  3. 或手动执行: cd alumni-search/src/main/resources/elasticsearch && ./init_indices.sh");
        }
    }

    /**
     * 检查 ES 连接
     */
    private void checkElasticsearchConnection() {
        try {
            String healthUrl = elasticsearchHost + "/_cluster/health";
            restTemplate.getForObject(healthUrl, String.class);
            log.info("[✓] Elasticsearch 连接正常");
        } catch (Exception e) {
            throw new RuntimeException("无法连接到 Elasticsearch: " + elasticsearchHost, e);
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
            String checkUrl = elasticsearchHost + "/" + indexName;
            try {
                restTemplate.headForHeaders(checkUrl);
                log.info("[SKIP] 索引已存在: {}", indexName);
                return;
            } catch (Exception e) {
                // 索引不存在，继续创建
            }

            // 2. 读取 JSON 映射文件
            ClassPathResource resource = new ClassPathResource(mappingFile);
            String mappingJson = new String(Files.readAllBytes(resource.getFile().toPath()));

            // 3. 发送创建索引请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(mappingJson, headers);

            String createUrl = elasticsearchHost + "/" + indexName;
            restTemplate.exchange(createUrl, HttpMethod.PUT, request, String.class);

            log.info("[✓] 索引创建成功: {}", indexName);

        } catch (Exception e) {
            log.error("[ERROR] 索引创建失败: {}", indexName, e);
            throw new RuntimeException("创建索引失败: " + indexName, e);
        }
    }
}
