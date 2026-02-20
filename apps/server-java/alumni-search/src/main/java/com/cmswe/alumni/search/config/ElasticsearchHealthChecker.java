package com.cmswe.alumni.search.config;

import com.cmswe.alumni.search.repository.AlumniDocumentRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch 健康检查器
 *
 * <p>功能：
 * <ul>
 *   <li>应用启动时检查 ES 索引状态</li>
 *   <li>如果索引为空，打印明确的提示信息</li>
 *   <li>告知管理员如何初始化数据</li>
 * </ul>
 *
 * <p>设计理念（方案C - 混合方案）：
 * <ul>
 *   <li>启动时检查并提示，但不自动执行</li>
 *   <li>给管理员明确的操作指引</li>
 *   <li>生产环境安全可控</li>
 * </ul>
 *
 * @author CNI Alumni System
 */
@Slf4j
@Component
public class ElasticsearchHealthChecker {

    @Autowired
    private AlumniDocumentRepository alumniRepository;

    @Value("${server.port:8080}")
    private Integer serverPort;

    @Value("${app.url:http://localhost}")
    private String appUrl;

    /**
     * 应用启动时执行健康检查
     */
    @PostConstruct
    public void checkHealth() {
        log.info("========================================");
        log.info("Elasticsearch 健康检查");
        log.info("========================================");

        try {
            // 检查 ES 连接和索引状态
            long count = alumniRepository.count();

            if (count == 0) {
                // 索引为空，打印警告和操作指引
                printEmptyIndexWarning();
            } else {
                // 索引有数据，打印正常信息
                log.info("[✓] ES 索引状态正常");
                log.info("[✓] 当前文档数: {}", count);
                log.info("========================================");
            }

        } catch (Exception e) {
            // ES 连接失败
            log.error("========================================");
            log.error("[✗] Elasticsearch 连接失败");
            log.error("[✗] 错误信息: {}", e.getMessage());
            log.error("========================================");
            log.warn("请检查：");
            log.warn("  1. Elasticsearch 是否已启动");
            log.warn("  2. 配置的 ES 地址是否正确");
            log.warn("  3. 网络连接是否正常");
            log.error("========================================");
        }
    }

    /**
     * 打印索引为空的警告信息
     */
    private void printEmptyIndexWarning() {
        String baseUrl = String.format("%s:%d", appUrl, serverPort);

        log.warn("========================================");
        log.warn("[!] 检测到 Elasticsearch 索引为空");
        log.warn("========================================");
        log.warn("");
        log.warn("原因分析：");
        log.warn("  1. 这是首次部署，尚未初始化数据");
        log.warn("  2. Canal 同步功能未启用（只能同步新数据）");
        log.warn("  3. 索引数据被误删除");
        log.warn("");
        log.warn("解决方案：");
        log.warn("  请调用以下接口进行全量数据初始化：");
        log.warn("");
        log.warn("  1️⃣  查看索引状态：");
        log.warn("     GET  {}/admin/search/status", baseUrl);
        log.warn("");
        log.warn("  2️⃣  重建索引（推荐）：");
        log.warn("     POST {}/admin/search/rebuild", baseUrl);
        log.warn("");
        log.warn("  3️⃣  使用 curl 命令：");
        log.warn("     curl -X POST {}/admin/search/rebuild", baseUrl);
        log.warn("");
        log.warn("  4️⃣  使用 Swagger UI：");
        log.warn("     {}/doc.html#/搜索管理", baseUrl);
        log.warn("");
        log.warn("注意：");
        log.warn("  - 重建索引会从数据库同步所有用户数据到 ES");
        log.warn("  - 重建过程可能需要几秒到几分钟（取决于数据量）");
        log.warn("  - 重建完成后，搜索功能即可正常使用");
        log.warn("");
        log.warn("========================================");
    }
}
